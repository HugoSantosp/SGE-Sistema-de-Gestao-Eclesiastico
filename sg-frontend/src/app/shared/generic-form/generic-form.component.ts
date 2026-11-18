import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ValidationService } from '../services/validation.service';
import { ToastService } from '../services/toast.service';
import { AuthService } from '../../core/auth/auth.service';

interface FieldConfig {
  name: string;
  label: string;
  type: string;
  required?: boolean;
  options?: { value: string; label: string }[];
  /** GET que devolve a lista de opções do select (ex.: '/api/membros?situacao=ATIVO') */
  optionsApi?: string;
  /** Campo da resposta usado como texto da opção (padrão: 'nome') */
  optionLabel?: string;
  /** Campos limpos quando este select fica vazio */
  clearOnEmpty?: string[];
  /** Exibe o campo apenas quando outro campo tem o valor indicado (ex.: { field: 'tipo', equals: 'OFERTA' }) */
  showWhen?: { field: string; equals: string | string[] };
  uploadHint?: string;
  validators?: any[];
  placeholder?: string;
  helpText?: string;
  mask?: string;
  minLength?: number;
  maxLength?: number;
  min?: number;
  max?: number;
  pattern?: string;
  patternMessage?: string;
}

@Component({
  selector: 'app-generic-form',
  templateUrl: './generic-form.component.html',
  styles: [':host { display: contents; }']
})
export class GenericFormComponent implements OnInit {
  title = '';
  apiUrl = '';
  fields: FieldConfig[] = [];
  isEdit = false;

  formGroup!: FormGroup;
  form: any = {};
  loading = false;
  saving = false;
  error = '';
  uploading: Record<string, boolean> = {};
  /** URL assinada (temporária) usada só na pré-visualização após upload; o valor salvo é o caminho permanente */
  previewUrls: Record<string, string> = {};
  showPassword: Record<string, boolean> = {};
  
  // Erros de validação por campo
  fieldErrors: Record<string, string> = {};

  /** Valor do select no momento da carga, para distinguir "nunca teve" de "usuário limpou" */
  private originalSelectValues: Record<string, any> = {};

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
    private fb: FormBuilder,
    private toastService: ToastService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const data = this.route.snapshot.data;
    this.title = data['title'] || 'Formulário';
    this.apiUrl = data['api'] || '';
    this.fields = data['fields'] || [];
    this.isEdit = !!data['isEdit'] || !!this.route.snapshot.params['id'];

    // Inicializa formulário reativo com validações
    this.initForm();
    this.setupConditionalFields();

    // Selects alimentados por API (ex.: líder da célula vindo do cadastro de membros)
    this.loadDynamicOptions();

    if (this.isEdit) {
      this.loading = true;
      const id = this.route.snapshot.params['id'];
      this.http.get<any>(`${this.apiUrl}/${id}`).subscribe({
        next: (d) => { 
          // Campos de senha nunca podem ser preenchidos com o hash da edição:
          // ao salvar, uma senha em branco mantém a anterior no backend.
          const isEdit = this.isEdit;
          this.fields.forEach(f => {
            if (f.type === 'password' && isEdit) {
              d[f.name] = '';
            }
            // <option> compara string: sem isso o select não fica marcado na edição
            if (f.type === 'select' && d[f.name] !== null && d[f.name] !== undefined) {
              d[f.name] = String(d[f.name]);
            }
            if (f.type === 'select') {
              this.originalSelectValues[f.name] = d[f.name] ?? null;
            }
          });
          this.formGroup.patchValue(d);
          this.form = d;
          this.loading = false; 
        },
        error: () => { 
          this.toastService.error('Erro ao carregar dados');
          this.loading = false; 
        }
      });
    }
  }

  private initForm(): void {
    const group: any = {};

    this.fields.forEach(field => {
      const validators = this.getValidatorsForField(field);
      group[field.name] = ['', validators];
    });

    this.formGroup = this.fb.group(group);
  }

  private loadDynamicOptions(): void {
    this.fields.filter(f => !!f.optionsApi).forEach(field => {
      this.http.get<any[]>(field.optionsApi!).subscribe({
        next: (data) => {
          const labelKey = field.optionLabel || 'nome';
          field.options = (data || []).map(item => ({
            value: String(item.id),
            label: String(item[labelKey] ?? item.id)
          }));
          // As opções chegam depois do patchValue da edição: reaplica o valor
          // para o select não ficar em branco quando o registro já tem líder.
          const control = this.formGroup.get(field.name);
          const atual = control?.value;
          if (control && atual !== null && atual !== undefined && atual !== '') {
            setTimeout(() => control.setValue(String(atual), { emitEvent: false }), 0);
          }
        },
        error: () => {
          this.toastService.error(`Erro ao carregar opções de ${field.label}`);
        }
      });
    });
  }

  private setupConditionalFields(): void {
    this.fields.filter(f => !!f.showWhen).forEach(field => {
      const control = this.formGroup.get(field.name);
      const watcher = this.formGroup.get(field.showWhen!.field);
      if (!control || !watcher) return;

      const aplicar = (valor: any) => {
        const esperados = Array.isArray(field.showWhen!.equals)
          ? field.showWhen!.equals
          : [field.showWhen!.equals];
        const visivel = esperados.includes(String(valor ?? ''));
        if (visivel) {
          control.enable({ emitEvent: false });
        } else {
          // Campo oculto não pode bloquear o formulário: desabilita e limpa
          control.disable({ emitEvent: false });
          control.setValue('', { emitEvent: false });
        }
      };

      aplicar(watcher.value);
      watcher.valueChanges.subscribe(aplicar);
    });
  }

  isFieldVisible(field: FieldConfig): boolean {
    if (!field.showWhen) return true;
    const atual = String(this.formGroup?.get(field.showWhen.field)?.value ?? '');
    const esperados = Array.isArray(field.showWhen.equals)
      ? field.showWhen.equals
      : [field.showWhen.equals];
    return esperados.includes(atual);
  }

  private getValidatorsForField(field: FieldConfig): any[] {
    const validators = [];

    if (field.required) {
      validators.push(Validators.required);
    }

    if (field.minLength) {
      validators.push(Validators.minLength(field.minLength));
    }

    if (field.maxLength) {
      validators.push(Validators.maxLength(field.maxLength));
    }

    if (field.min !== undefined) {
      validators.push(Validators.min(field.min));
    }

    if (field.max !== undefined) {
      validators.push(Validators.max(field.max));
    }

    if (field.pattern) {
      validators.push(Validators.pattern(field.pattern));
    }

    // Validações específicas por tipo
    switch (field.name) {
      case 'email':
        validators.push(Validators.email);
        break;
      case 'documento':
        validators.push(ValidationService.cpf());
        break;
      case 'telefone':
        validators.push(ValidationService.phone());
        break;
      case 'cep':
        validators.push(ValidationService.cep());
        break;
    }

    return validators;
  }

  get baseRoute(): string {
    const url = this.router.url;
    // Remove parâmetros de query e fragmentos
    const cleanUrl = url.split('?')[0].split('#')[0];
    const segments = cleanUrl.split('/').filter(x => x);
    
    // Se está em modo edição (editar/:id), volta para a listagem
    if (this.isEdit && segments.length >= 3) {
      // Remove os dois últimos segmentos (editar e id)
      return '/' + segments.slice(0, -2).join('/');
    }
    
    // Se está em modo novo (novo), volta para a listagem
    if (segments.includes('novo')) {
      return '/' + segments.filter(s => s !== 'novo').join('/');
    }
    
    // Caso contrário, usa os dois primeiros segmentos
    return '/' + segments.slice(0, 2).join('/');
  }

  getErrorMessage(fieldName: string): string {
    const control = this.formGroup.get(fieldName);
    if (control && control.errors && control.touched) {
      return ValidationService.getErrorMessage(fieldName, control.errors);
    }
    return '';
  }

  isFieldInvalid(fieldName: string): boolean {
    const control = this.formGroup.get(fieldName);
    return !!(control && control.invalid && control.touched);
  }

  triggerUpload(fieldName: string): void {
    const fileInputs = document.querySelectorAll<HTMLInputElement>('input[type="file"]');
    fileInputs.forEach(input => {
      if (input.getAttribute('data-field') === fieldName) {
        input.click();
      }
    });
  }

  onFileSelected(event: any, fieldName: string): void {
    const file = event.target.files?.[0];
    if (!file) return;

    // Validação de arquivo
    if (!this.validateFile(file, fieldName)) {
      return;
    }

    this.uploading[fieldName] = true;

    const formData = new FormData();
    formData.append('file', file);

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.authService.getToken()}`
    });

    this.http.post<any>('/api/upload', formData, { headers }).subscribe({
      next: (res) => {
        this.formGroup.get(fieldName)?.setValue(res.url);
        this.form[fieldName] = res.url;
        this.previewUrls[fieldName] = res.signedUrl || '';
        this.uploading[fieldName] = false;
        this.toastService.success('Imagem enviada com sucesso!');
      },
      error: (err) => {
        const msg = err?.status === 413
          ? 'Arquivo muito grande. Tamanho máximo permitido: 15 MB'
          : (err?.error?.error || 'Erro ao fazer upload da imagem');
        this.toastService.error(msg);
        this.uploading[fieldName] = false;
      }
    });
  }

  private validateFile(file: File, fieldName: string): boolean {
    // Tamanho máximo: 15MB
    const maxSize = 15 * 1024 * 1024;
    if (file.size > maxSize) {
      this.toastService.error('Arquivo muito grande. Tamanho máximo: 15MB');
      return false;
    }

    // Tipos permitidos
    const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
    if (!allowedTypes.includes(file.type)) {
      this.toastService.error('Tipo de arquivo não permitido. Use: JPG, PNG, GIF ou WebP');
      return false;
    }

    return true;
  }

  removeImage(fieldName: string): void {
    delete this.previewUrls[fieldName];
    this.formGroup.get(fieldName)?.setValue('');
    this.form[fieldName] = '';
  }

  onImageError(fieldName: string): void {
    // Não limpa o valor: a URL assinada pode ter apenas expirado e apagar o campo removeria a foto ao salvar
    delete this.previewUrls[fieldName];
  }

  save(): void {
    // Marca todos os campos como touched para mostrar erros
    ValidationService.markFormGroupTouched(this.formGroup);

    // Verifica se o formulário é válido
    if (this.formGroup.invalid) {
      this.fieldErrors = ValidationService.getFormErrors(this.formGroup);
      this.toastService.warning('Por favor, corrija os erros no formulário');
      return;
    }

    this.saving = true;
    this.error = '';
    this.fieldErrors = {};

    // Atualiza o objeto form com os valores do formulário
    this.form = { ...this.form, ...this.formGroup.value };

    // Campos condicionais ocultos não são enviados ao backend
    this.fields.forEach(f => {
      if (f.showWhen && !this.isFieldVisible(f)) {
        delete this.form[f.name];
      }
    });

    // Selects alimentados por API usam id numérico: vazio deve virar null, não ""
    this.fields.forEach(f => {
      if (f.type === 'select' && f.optionsApi) {
        if (this.form[f.name] === '') {
          this.form[f.name] = null;
        }
        const originalValue = this.originalSelectValues[f.name];
        const hadOriginalValue = originalValue !== undefined && originalValue !== null && originalValue !== '';
        const wasChanged = this.formGroup.get(f.name)?.dirty;
        if (this.form[f.name] === null && f.clearOnEmpty && (hadOriginalValue || wasChanged)) {
          f.clearOnEmpty.forEach(k => { this.form[k] = null; });
        }
      }
    });

    // Em edição, senha em branco significa "manter a atual": o campo não é enviado,
    // para o backend não re-criptografar um hash nem invalidar a senha do usuário.
    if (this.isEdit) {
      this.fields.forEach(f => {
        if (f.type === 'password' && !String(this.form[f.name] ?? '').trim()) {
          delete this.form[f.name];
        }
      });
    }

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.authService.getToken()}`,
      'Content-Type': 'application/json'
    });

    const obs = this.isEdit
      ? this.http.put(`${this.apiUrl}/${this.route.snapshot.params['id']}`, this.form, { headers })
      : this.http.post(this.apiUrl, this.form, { headers });

    obs.subscribe({
      next: () => {
        this.toastService.success(
          this.isEdit ? 'Registro atualizado com sucesso!' : 'Registro criado com sucesso!'
        );
        this.router.navigate([this.baseRoute]);
      },
      error: (err: any) => {
        this.handleSaveError(err);
      }
    });
  }

  private handleSaveError(err: any): void {
    const body = err?.error;
    let msg = 'Erro ao salvar registro';

    if (body?.fieldErrors) {
      // Erros de validação do backend
      const entries = Object.entries(body.fieldErrors) as [string, string][];
      if (entries.length > 0) {
        this.fieldErrors = Object.fromEntries(entries);
        msg = 'Corrija os erros indicados no formulário';
      }
    } else if (body?.error) {
      msg = body.error;
    }

    this.error = msg;
    this.toastService.error(msg);
    this.saving = false;
  }

  onCancel(): void {
    if (this.formGroup.dirty) {
      if (confirm('Tem certeza que deseja sair? As alterações não salvas serão perdidas.')) {
        this.router.navigate([this.baseRoute]);
      }
    } else {
      this.router.navigate([this.baseRoute]);
    }
  }

  togglePasswordVisibility(fieldName: string): void {
    this.showPassword[fieldName] = !this.showPassword[fieldName];
  }

  getFieldErrorKeys(): string[] {
    return Object.keys(this.fieldErrors);
  }
}

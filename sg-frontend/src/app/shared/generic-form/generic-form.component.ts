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
  
  // Erros de validação por campo
  fieldErrors: Record<string, string> = {};

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

    if (this.isEdit) {
      this.loading = true;
      const id = this.route.snapshot.params['id'];
      this.http.get<any>(`${this.apiUrl}/${id}`).subscribe({
        next: (d) => { 
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
        this.uploading[fieldName] = false;
        this.toastService.success('Imagem enviada com sucesso!');
      },
      error: () => {
        this.toastService.error('Erro ao fazer upload da imagem');
        this.uploading[fieldName] = false;
      }
    });
  }

  private validateFile(file: File, fieldName: string): boolean {
    // Tamanho máximo: 5MB
    const maxSize = 5 * 1024 * 1024;
    if (file.size > maxSize) {
      this.toastService.error('Arquivo muito grande. Tamanho máximo: 5MB');
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
    this.formGroup.get(fieldName)?.setValue('');
    this.form[fieldName] = '';
  }

  onImageError(fieldName: string): void {
    this.formGroup.get(fieldName)?.setValue('');
    this.form[fieldName] = '';
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

  getFieldErrorKeys(): string[] {
    return Object.keys(this.fieldErrors);
  }
}

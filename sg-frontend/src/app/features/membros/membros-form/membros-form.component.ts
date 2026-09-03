import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MembrosService } from '../membros.service';
import { StatusMembro, Ministerio } from '../../../core/models/sge.models';

@Component({
  selector: 'app-membros-form',
  templateUrl: './membros-form.component.html',
  styles: [':host { display: contents; }']
})
export class MembrosFormComponent implements OnInit {
  isEdit = false;
  loading = false;
  saving = false;
  error = '';

  statusOptions = [StatusMembro.ATIVO, StatusMembro.INATIVO];
  ministerios: Ministerio[] = [];
  form: any = { nome: '', documento: '', telefone: '', endereco: '', situacao: StatusMembro.ATIVO, ministerioId: null };

  constructor(
    private service: MembrosService,
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Load ministries for select
    this.http.get<Ministerio[]>('/api/ministerios').subscribe({
      next: (data) => { this.ministerios = data; }
    });

    const id = this.route.snapshot.params['id'];
    if (id) {
      this.isEdit = true;
      this.loading = true;
      this.service.buscarPorId(id).subscribe({
        next: (data) => {
          this.form = { ...data, situacao: data.situacao as StatusMembro };
          this.loading = false;
        },
        error: () => { this.error = 'Erro ao carregar membro'; this.loading = false; }
      });
    }
  }

  save(): void {
    this.saving = true;
    this.error = '';

    // Campos de data vazios viram null (evita 400 de parsing no backend)
    ['dataNasc', 'dataBatismo'].forEach(k => { if (this.form[k] === '') this.form[k] = null; });

    const obs = this.isEdit
      ? this.service.atualizar(this.route.snapshot.params['id'], this.form)
      : this.service.criar(this.form);

    obs.subscribe({
      next: () => this.router.navigate(['/membros']),
      error: (err: any) => {
        const body = err?.error;
        let msg = 'Erro ao salvar membro';
        if (body?.fieldErrors) {
          const entries = Object.entries(body.fieldErrors) as [string, string][];
          if (entries.length > 0) msg = entries.map(([f, m]) => `${f}: ${m}`).join('; ');
        } else if (body?.error) {
          msg = body.error;
        }
        this.error = msg;
        this.saving = false;
      }
    });
  }
}

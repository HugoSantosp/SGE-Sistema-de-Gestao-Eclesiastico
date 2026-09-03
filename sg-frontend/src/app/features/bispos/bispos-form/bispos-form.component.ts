import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BisposService } from '../bispos.service';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-bispos-form',
  templateUrl: './bispos-form.component.html',
  styles: [':host { display: contents; }']
})
export class BisposFormComponent implements OnInit {
  isEdit = false; loading = false; saving = false; error = '';
  uploadingFoto = false;
  form: any = { nome: '', email: '', documento: '', telefone: '', endereco: '', foto: '' };

  constructor(
    private service: BisposService, 
    private route: ActivatedRoute, 
    private router: Router,
    private http: HttpClient,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.params['id'];
    if (id) {
      this.isEdit = true; this.loading = true;
      this.service.buscarPorId(id).subscribe({ next: (d) => { this.form = d; this.loading = false; }, error: () => this.loading = false });
    }
  }

  onFotoSelected(event: any): void {
    const file = event.target.files?.[0];
    if (!file) return;

    this.uploadingFoto = true;
    const formData = new FormData();
    formData.append('file', file);
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${this.authService.getToken()}` });

    this.http.post<any>('/api/upload', formData, { headers }).subscribe({
      next: (res) => { this.form.foto = res.url; this.uploadingFoto = false; },
      error: () => { this.error = 'Erro ao enviar foto'; this.uploadingFoto = false; }
    });
  }

  save(): void {
    this.saving = true;
    const obs = this.isEdit ? this.service.atualizar(this.route.snapshot.params['id'], this.form) : this.service.criar(this.form);
    obs.subscribe({ next: () => this.router.navigate(['/bispos']), error: () => { this.error = 'Erro ao salvar'; this.saving = false; } });
  }
}

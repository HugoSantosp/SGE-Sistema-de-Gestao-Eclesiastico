import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { getApiUrl } from '../../../core/config/backend-config';

@Component({
  selector: 'app-mural-page',
  templateUrl: './mural-page.component.html',
  styleUrls: ['../landing-page/landing-page.component.scss']
})
export class MuralPageComponent implements OnInit {
  profissionais: any[] = [];
  loading = true;
  churchInfo: any = { nome: 'ICERT - Agência do Reino de Deus' };

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.http.get(getApiUrl('/api/public/info')).subscribe({
      next: (data: any) => { this.churchInfo = { ...this.churchInfo, ...data }; }
    });
    this.carregarProfissionais();
  }

  private carregarProfissionais(): void {
    this.http.get<any[]>(getApiUrl('/api/public/profissionais')).subscribe({
      next: (data) => { this.profissionais = data; },
      error: () => { this.loading = false; this.fotoReloading = false; },
      complete: () => { this.loading = false; this.fotoReloading = false; }
    });
  }

  // URLs assinadas expiram: se uma imagem falhar, busca os dados de novo (novas URLs), no máximo 2 vezes
  private fotoRetries = 0;
  private fotoReloading = false;

  onFotoError(): void {
    if (this.fotoReloading || this.fotoRetries >= 2) return;
    this.fotoRetries++;
    this.fotoReloading = true;
    this.carregarProfissionais();
  }
}

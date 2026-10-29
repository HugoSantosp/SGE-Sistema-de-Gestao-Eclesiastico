import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { getApiUrl } from '../../../core/config/backend-config';

@Component({
  selector: 'app-celulas-page',
  templateUrl: './celulas-page.component.html',
  styleUrls: ['../landing-page/landing-page.component.scss']
})
export class CelulasPageComponent implements OnInit {
  celulas: any[] = [];
  loading = true;
  churchInfo: any = { nome: 'ICERT - Agência do Reino de Deus' };

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.http.get(getApiUrl('/api/public/info')).subscribe({
      next: (data: any) => { this.churchInfo = { ...this.churchInfo, ...data }; }
    });
    this.carregarCelulas();
  }

  private carregarCelulas(): void {
    this.http.get<any[]>(getApiUrl('/api/public/celulas')).subscribe({
      next: (data) => { this.celulas = data; },
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
    this.carregarCelulas();
  }
}

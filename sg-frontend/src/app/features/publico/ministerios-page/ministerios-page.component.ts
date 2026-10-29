import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { getApiUrl } from '../../../core/config/backend-config';

@Component({
  selector: 'app-ministerios-page',
  templateUrl: './ministerios-page.component.html',
  styleUrls: ['../landing-page/landing-page.component.scss']
})
export class MinisteriosPageComponent implements OnInit {
  ministerios: any[] = [];
  loading = true;
  churchInfo: any = { nome: 'ICERT - Agência do Reino de Deus' };

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.http.get(getApiUrl('/api/public/info')).subscribe({
      next: (data: any) => { this.churchInfo = { ...this.churchInfo, ...data }; }
    });
    this.carregarMinisterios();
  }

  private carregarMinisterios(): void {
    this.http.get<any>(getApiUrl('/api/public/ministerios')).subscribe({
      next: (data) => { this.ministerios = data; },
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
    this.carregarMinisterios();
  }

  scrollTo(section: string): void {
    document.getElementById(section)?.scrollIntoView({ behavior: 'smooth' });
  }
}

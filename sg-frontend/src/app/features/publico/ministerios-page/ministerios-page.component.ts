import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

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
    this.http.get('/api/public/info').subscribe({
      next: (data: any) => { this.churchInfo = { ...this.churchInfo, ...data }; }
    });
    this.http.get<any[]>('/api/public/ministerios').subscribe({
      next: (data) => { this.ministerios = data; },
      complete: () => { this.loading = false; }
    });
  }

  scrollTo(section: string): void {
    document.getElementById(section)?.scrollIntoView({ behavior: 'smooth' });
  }
}

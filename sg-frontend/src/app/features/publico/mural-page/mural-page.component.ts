import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

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
    this.http.get('/api/public/info').subscribe({
      next: (data: any) => { this.churchInfo = { ...this.churchInfo, ...data }; }
    });
    this.http.get<any[]>('/api/public/profissionais').subscribe({
      next: (data) => { this.profissionais = data; },
      complete: () => { this.loading = false; }
    });
  }
}

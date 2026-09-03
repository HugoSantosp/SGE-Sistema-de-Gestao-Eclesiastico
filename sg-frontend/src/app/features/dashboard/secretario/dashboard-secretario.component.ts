import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-dashboard-secretario',
  templateUrl: './dashboard-secretario.component.html',
  styles: [':host { display: contents; }']
})
export class DashboardSecretarioComponent implements OnInit {
  stats = { tarefasPendentes: 0, tarefasHoje: 0, membrosTotal: 0, notificacoes: 0 };
  loading = true;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<any[]>('/api/tarefas').subscribe({
      next: (d) => {
        this.stats.tarefasPendentes = d.filter(t => t.statusTarefa === 'PENDENTE').length;
        const hoje = new Date().toISOString().split('T')[0];
        this.stats.tarefasHoje = d.filter(t => t.dataTarefa === hoje).length;
      }
    });
    this.http.get<any>('/api/membros/estatisticas').subscribe({
      next: (d) => { this.stats.membrosTotal = d.total; },
      complete: () => this.loading = false
    });
  }
}

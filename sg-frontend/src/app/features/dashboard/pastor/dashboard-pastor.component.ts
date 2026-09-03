import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import Chart from 'chart.js/auto';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-dashboard-pastor',
  templateUrl: './dashboard-pastor.component.html',
  styles: [':host { display: contents; }']
})
export class DashboardPastorComponent implements OnInit, OnDestroy {
  stats = { membrosAtivos: 0, membrosInativos: 0, tarefasPendentes: 0, totalTarefas: 0 };
  loading = true;
  igrejaNome = '';
  private chart: Chart | null = null;

  constructor(private http: HttpClient, private auth: AuthService) {}

  ngOnInit(): void { this.loadData(); }
  ngOnDestroy(): void { this.chart?.destroy(); }

  private loadData(): void {
    const user = this.auth.getCurrentUser();
    this.igrejaNome = user?.nome || 'Minha Igreja';

    this.http.get<any>('/api/membros/estatisticas').subscribe({
      next: (d) => { this.stats.membrosAtivos = d.ativos; this.stats.membrosInativos = d.inativos; },
      complete: () => { this.loading = false; setTimeout(() => this.renderChart(), 50); }
    });
  }

  private renderChart(): void {
    const canvas = document.getElementById('pastorChart') as HTMLCanvasElement;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;
    this.chart = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: ['Ativos', 'Inativos'],
        datasets: [{ data: [this.stats.membrosAtivos, this.stats.membrosInativos], backgroundColor: ['#f97316', 'rgba(107,114,128,0.5)'], borderWidth: 2 }]
      },
      options: { responsive: true, maintainAspectRatio: true, cutout: '70%', plugins: { legend: { display: false } } }
    });
  }
}

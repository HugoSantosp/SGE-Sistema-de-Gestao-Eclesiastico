import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import Chart from 'chart.js/auto';

@Component({
  selector: 'app-dashboard-presidente',
  templateUrl: './dashboard-presidente.component.html',
  styles: [':host { display: contents; }']
})
export class DashboardPresidenteComponent implements OnInit, OnDestroy {
  stats = { totalMembros: 0, membrosAtivos: 0, membrosInativos: 0, totalPastores: 0 };
  loading = true;
  private chart: Chart | null = null;

  constructor(private http: HttpClient) {}

  ngOnInit(): void { this.loadData(); }
  ngOnDestroy(): void { this.chart?.destroy(); }

  private loadData(): void {
    this.http.get<any>('/api/membros/estatisticas').subscribe({
      next: (d) => { this.stats.totalMembros = d.total; this.stats.membrosAtivos = d.ativos; this.stats.membrosInativos = d.inativos; },
      complete: () => { this.loading = false; setTimeout(() => this.renderChart(), 50); }
    });
  }

  private renderChart(): void {
    const canvas = document.getElementById('presidenteChart') as HTMLCanvasElement;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;
    this.chart = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: ['Ativos', 'Inativos'],
        datasets: [{
          data: [this.stats.membrosAtivos, this.stats.membrosInativos],
          backgroundColor: ['#f97316', 'rgba(107,114,128,0.5)'],
          borderWidth: 2, hoverOffset: 8
        }]
      },
      options: {
        responsive: true, maintainAspectRatio: true, cutout: '70%',
        plugins: { legend: { display: false } }
      }
    });
  }
}

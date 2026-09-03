import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import Chart from 'chart.js/auto';

interface DashboardStats {
  totalMembros: number;
  membrosAtivos: number;
  membrosInativos: number;
  totalPastores: number;
  tarefasPendentes: number;
  contasVencidas: number;
  contasAVencer: number;
}

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styles: [':host { display: contents; }']
})
export class DashboardComponent implements OnInit, OnDestroy {
  stats: DashboardStats = {
    totalMembros: 0, membrosAtivos: 0, membrosInativos: 0,
    totalPastores: 0, tarefasPendentes: 0,
    contasVencidas: 0, contasAVencer: 0
  };
  loading = true;
  private chartInstance: Chart | null = null;

  constructor(
    private http: HttpClient,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadStats();
  }

  ngOnDestroy(): void {
    this.chartInstance?.destroy();
  }

  private loadStats(): void {
    this.http.get<any>('/api/membros/estatisticas').subscribe({
      next: (data) => {
        this.stats.totalMembros = data.total;
        this.stats.membrosAtivos = data.ativos;
        this.stats.membrosInativos = data.inativos;
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      },
      complete: () => {
        this.loading = false;
        this.cdr.detectChanges();   // garante que o canvas do *ngIf já está no DOM
        this.renderChart();
      }
    });
  }

  private renderChart(): void {
    const canvas = document.getElementById('membrosChart') as HTMLCanvasElement;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    // Get computed CSS variables for theme-aware colors
    const style = getComputedStyle(document.documentElement);
    const orange = style.getPropertyValue('--orange').trim() || '#f97316';
    const textMuted = style.getPropertyValue('--text-muted').trim() || '#6b7280';

    new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: ['Membros Ativos', 'Membros Inativos'],
        datasets: [{
          data: [this.stats.membrosAtivos, this.stats.membrosInativos],
          backgroundColor: [
            orange,
            'rgba(107, 114, 128, 0.5)'
          ],
          borderColor: [
            orange,
            textMuted
          ],
          borderWidth: 2,
          hoverOffset: 8
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: true,
        cutout: '70%',
        plugins: {
          legend: {
            display: false
          },
          tooltip: {
            backgroundColor: '#1a1a1a',
            titleColor: '#e8e8e8',
            bodyColor: '#9ca3af',
            borderColor: '#262626',
            borderWidth: 1,
            padding: 12,
            cornerRadius: 8,
            callbacks: {
              label: (context) => {
                const total = (context.dataset.data as number[]).reduce((a, b) => a + b, 0);
                const value = context.parsed;
                const pct = total > 0 ? ((value / total) * 100).toFixed(1) : '0';
                return ` ${context.label}: ${value} (${pct}%)`;
              }
            }
          }
        }
      }
    });
  }
}

import { Component, OnDestroy, OnInit } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import Chart from 'chart.js/auto';
import { ToastService } from '../../../shared/services/toast.service';

interface ContaResumo {
  id: number;
  descricao: string;
  vencimento: string;
  valor: number;
  valorPago: number;
  saldoAberto: number;
  status: string;
}

@Component({
  selector: 'app-dashboard-financeiro',
  templateUrl: './dashboard-financeiro.component.html',
  styles: [':host { display: contents; }']
})
export class DashboardFinanceiroComponent implements OnInit, OnDestroy {
  inicio = '';
  fim = '';
  periodo = 'MES_ATUAL';
  data: any = null;
  loading = true;
  erro = '';

  private charts: Chart[] = [];

  constructor(private http: HttpClient, private toast: ToastService) {}

  ngOnInit(): void {
    this.aplicarPeriodo('MES_ATUAL');
  }

  ngOnDestroy(): void {
    this.destruirCharts();
  }

  aplicarPeriodo(preset: string): void {
    this.periodo = preset;
    const hoje = new Date();
    const ano = hoje.getFullYear();
    const mes = hoje.getMonth();

    const iso = (d: Date) => {
      const mm = String(d.getMonth() + 1).padStart(2, '0');
      const dd = String(d.getDate()).padStart(2, '0');
      return `${d.getFullYear()}-${mm}-${dd}`;
    };

    switch (preset) {
      case 'MES_ANTERIOR':
        this.inicio = iso(new Date(ano, mes - 1, 1));
        this.fim = iso(new Date(ano, mes, 0));
        break;
      case 'TRIMESTRE':
        this.inicio = iso(new Date(ano, mes - 2, 1));
        this.fim = iso(new Date(ano, mes + 1, 0));
        break;
      case 'ANO_ATUAL':
        this.inicio = iso(new Date(ano, 0, 1));
        this.fim = iso(new Date(ano, 11, 31));
        break;
      case 'PERSONALIZADO':
        return;
      default:
        this.inicio = iso(new Date(ano, mes, 1));
        this.fim = iso(new Date(ano, mes + 1, 0));
    }

    this.carregar();
  }

  carregar(): void {
    if (!this.inicio || !this.fim) return;

    this.loading = true;
    this.erro = '';
    this.destruirCharts();

    const params = new HttpParams().set('inicio', this.inicio).set('fim', this.fim);
    this.http.get<any>('/api/financeiro/dashboard', { params }).subscribe({
      next: (d) => {
        this.data = d;
        this.loading = false;
        setTimeout(() => this.renderizarCharts(), 50);
      },
      error: (err) => {
        this.loading = false;
        this.erro = err?.error?.message || 'Não foi possível carregar a dashboard.';
        this.toast.error(this.erro);
      }
    });
  }

  private destruirCharts(): void {
    this.charts.forEach(c => c.destroy());
    this.charts = [];
  }

  private renderizarCharts(): void {
    this.destruirCharts();
    if (!this.data) return;

    const evolucao = this.data.evolucaoMensal || [];
    this.criarChart('chartEvolucao', {
      type: 'bar',
      data: {
        labels: evolucao.map((e: any) => e.mes),
        datasets: [
          { label: 'Receitas', data: evolucao.map((e: any) => e.receitas), backgroundColor: '#22c55e' },
          { label: 'Despesas', data: evolucao.map((e: any) => e.despesas), backgroundColor: '#ef4444' }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { position: 'bottom' } },
        scales: { y: { beginAtZero: true } }
      }
    });

    this.criarChart('chartReceitas', this.chartCategoria(this.data.receitasPorCategoria, '#22c55e'));
    this.criarChart('chartDespesas', this.chartCategoria(this.data.despesasPorCategoria, '#ef4444'));
  }

  private chartCategoria(itens: any[], cor: string): any {
    const lista = itens || [];
    return {
      type: 'doughnut',
      data: {
        labels: lista.map(i => i.categoria),
        datasets: [{
          data: lista.map(i => i.valor),
          backgroundColor: [cor, '#f97316', '#3b82f6', '#a855f7', '#eab308', '#14b8a6', '#6b7280'],
          borderWidth: 2
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '60%',
        plugins: { legend: { position: 'bottom' } }
      }
    };
  }

  private criarChart(id: string, config: any): void {
    const canvas = document.getElementById(id) as HTMLCanvasElement;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;
    this.charts.push(new Chart(ctx, config));
  }

  moeda(valor: any): string {
    const numero = Number(valor ?? 0);
    return numero.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }

  percentual(valor: any): string {
    if (valor === null || valor === undefined) return '—';
    const numero = Number(valor);
    const sinal = numero > 0 ? '+' : '';
    return `${sinal}${numero.toFixed(1)}%`;
  }

  corVariacao(valor: any): string {
    if (valor === null || valor === undefined) return 'var(--text-secondary)';
    return Number(valor) >= 0 ? '#22c55e' : '#ef4444';
  }

  badgeStatus(status: string): string {
    switch (status) {
      case 'PAGA': return 'badge-success';
      case 'PARCIALMENTE_PAGA': return 'badge-info';
      case 'VENCIDA': return 'badge-danger';
      case 'CANCELADA': return 'badge-secondary';
      default: return 'badge-warning';
    }
  }

  rotuloStatus(status: string): string {
    switch (status) {
      case 'PAGA': return 'Paga';
      case 'PARCIALMENTE_PAGA': return 'Parcial';
      case 'VENCIDA': return 'Vencida';
      case 'CANCELADA': return 'Cancelada';
      case 'PENDENTE': return 'Pendente';
      default: return status || '-';
    }
  }

  get contasVencendo(): ContaResumo[] {
    return this.data?.contasVencendo || [];
  }

  get contasVencidas(): ContaResumo[] {
    return this.data?.contasVencidas || [];
  }

  get movimentacoes(): any[] {
    return this.data?.ultimasMovimentacoes || [];
  }
}

import { Component, OnInit } from '@angular/core';
import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { ToastService } from '../../../shared/services/toast.service';

type TipoRelatorio = 'receitas' | 'despesas' | 'fluxo-caixa' | 'demonstrativo';

@Component({
  selector: 'app-relatorios-financeiros',
  templateUrl: './relatorios-financeiros.component.html',
  styles: [':host { display: contents; }']
})
export class RelatoriosFinanceirosComponent implements OnInit {
  inicio = '';
  fim = '';
  tipo: TipoRelatorio = 'receitas';
  dados: any = null;
  loading = true;
  exportando = false;
  erro = '';

  readonly abas: { valor: TipoRelatorio; label: string; icone: string }[] = [
    { valor: 'receitas', label: 'Receitas', icone: 'bi-arrow-up-circle' },
    { valor: 'despesas', label: 'Despesas', icone: 'bi-arrow-down-circle' },
    { valor: 'fluxo-caixa', label: 'Fluxo de Caixa', icone: 'bi-arrow-left-right' },
    { valor: 'demonstrativo', label: 'Demonstrativo', icone: 'bi-file-earmark-text' }
  ];

  constructor(private http: HttpClient, private toast: ToastService) {}

  ngOnInit(): void {
    const hoje = new Date();
    const iso = (d: Date) => {
      const mm = String(d.getMonth() + 1).padStart(2, '0');
      const dd = String(d.getDate()).padStart(2, '0');
      return `${d.getFullYear()}-${mm}-${dd}`;
    };
    this.inicio = iso(new Date(hoje.getFullYear(), hoje.getMonth(), 1));
    this.fim = iso(new Date(hoje.getFullYear(), hoje.getMonth() + 1, 0));
    this.carregar();
  }

  selecionarAba(tipo: TipoRelatorio): void {
    if (this.tipo === tipo) return;
    this.tipo = tipo;
    this.carregar();
  }

  carregar(): void {
    if (!this.inicio || !this.fim) return;

    this.loading = true;
    this.erro = '';
    const params = new HttpParams().set('inicio', this.inicio).set('fim', this.fim);

    this.http.get<any>(`/api/financeiro/relatorios/${this.tipo}`, { params }).subscribe({
      next: (d) => {
        this.dados = d;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.dados = null;
        this.erro = err?.error?.message || 'Não foi possível carregar o relatório.';
        this.toast.error(this.erro);
      }
    });
  }

  exportar(formato: 'pdf' | 'excel'): void {
    if (!this.inicio || !this.fim) return;

    this.exportando = true;
    const params = new HttpParams().set('inicio', this.inicio).set('fim', this.fim);

    this.http.get(`/api/financeiro/relatorios/${this.tipo}/export/${formato}`, {
      params,
      observe: 'response',
      responseType: 'blob'
    }).subscribe({
      next: (resp: HttpResponse<Blob>) => {
        const blob = resp.body;
        if (!blob) {
          this.exportando = false;
          this.toast.error('Arquivo vazio recebido do servidor.');
          return;
        }

        const extensao = formato === 'pdf' ? 'pdf' : 'xlsx';
        const nome = this.nomeDoArquivo(resp) || `${this.tipo}.${extensao}`;
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = nome;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);

        this.exportando = false;
        this.toast.success('Exportação concluída.');
      },
      error: () => {
        this.exportando = false;
        this.toast.error('Erro ao exportar o relatório.');
      }
    });
  }

  private nomeDoArquivo(resp: HttpResponse<Blob>): string | null {
    const disposicao = resp.headers.get('Content-Disposition') || '';
    const match = /filename="?([^";]+)"?/i.exec(disposicao);
    return match ? match[1] : null;
  }

  moeda(valor: any): string {
    const numero = Number(valor ?? 0);
    return numero.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
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

  get itens(): any[] {
    return this.dados?.itens || this.dados?.movimentos || [];
  }
}

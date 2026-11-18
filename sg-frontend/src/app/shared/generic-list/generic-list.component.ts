import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ToastService } from '../services/toast.service';
import { FilterConfig } from '../components/search-filter/search-filter.component';

@Component({
  selector: 'app-generic-list',
  templateUrl: './generic-list.component.html',
  styles: [':host { display: contents; }']
})
export class GenericListComponent implements OnInit {
  title = '';
  apiUrl = '';
  icon = 'bi-list';
  columns: string[] = [];
  /** Rótulos amigáveis por coluna (padrão: nome da coluna capitalizado) */
  columnLabels: Record<string, string> = {};
  /** Tradução de valores por coluna: { tipo: { DIZIMO: 'Dízimo' } } */
  valueLabels: Record<string, Record<string, string>> = {};
  /** Colunas exibidas como moeda (R$) */
  currencyColumns: string[] = [];
  /** Ações extras; visibleWhen restringe o botão a um valor do registro */
  extraActions: {
    label: string;
    icon: string;
    class: string;
    api: string;
    method: string;
    confirm: string;
    visibleWhen?: { field: string; equals: any };
  }[] = [];
  /** Permissões de tela: listas somente leitura escondem Novo/Editar/Excluir */
  canCreate = true;
  canEdit = true;
  canDelete = true;
  /** Ação de alterar status (contas a pagar/receber) */
  statusAction: {
    label: string;
    icon: string;
    api: string;
    field: string;
    options: { value: string; label: string; icon?: string; hint?: string }[];
    /** Botão de texto em destaque na linha (ex.: "Pagar conta") */
    quick?: {
      label: string;
      value: string;
      icon: string;
      successMessage: string;
      visibleWhenStatusIn: string[];
    };
  } | null = null;
  showStatusDialog = false;
  statusItem: any = null;
  savingStatus = false;
  itens: any[] = [];
  loading = true;
  searchTerm = '';
  
  // Paginação
  currentPage = 1;
  itemsPerPage = 10;
  totalItems = 0;
  
  // Filtros
  filters: FilterConfig[] = [];
  activeFilters: Record<string, any> = {};
  
  // Confirmação de exclusão
  showDeleteConfirm = false;
  itemToDelete: any = null;
  deleting = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    const data = this.route.snapshot.data;
    this.title = data['title'] || 'Listagem';
    this.apiUrl = data['api'] || '';
    this.icon = data['icon'] || 'bi-list';
    this.columns = data['columns'] || [];
    this.columnLabels = data['columnLabels'] || {};
    this.valueLabels = data['valueLabels'] || {};
    this.currencyColumns = data['currencyColumns'] || [];
    this.extraActions = data['extraActions'] || [];
    this.canCreate = data['canCreate'] !== false;
    this.canEdit = data['canEdit'] !== false;
    this.canDelete = data['canDelete'] !== false;
    this.statusAction = data['statusAction'] || null;
    this.filters = data['filters'] || [];
    this.itemsPerPage = data['itemsPerPage'] || 10;
    this.load();
  }

  load(): void {
    this.loading = true;
    this.http.get<any[]>(this.apiUrl).subscribe({
      next: (d) => { 
        this.itens = d; 
        this.totalItems = d.length;
        this.loading = false; 
      },
      error: () => {
        this.loading = false;
        this.toastService.error('Erro ao carregar dados');
      }
    });
  }

  get filtered(): any[] {
    let result = [...this.itens];
    
    // Aplica busca
    if (this.searchTerm) {
      const s = this.searchTerm.toLowerCase();
      result = result.filter(i => 
        this.columns.some(c => String(i[c] || '').toLowerCase().includes(s))
      );
    }
    
    // Aplica filtros
    Object.entries(this.activeFilters).forEach(([key, value]) => {
      if (value !== '' && value !== null && value !== undefined && key !== 'search') {
        result = result.filter(i => {
          const itemValue = i[key];
          if (Array.isArray(value)) {
            return value.includes(itemValue);
          }
          return String(itemValue || '').toLowerCase().includes(String(value).toLowerCase());
        });
      }
    });
    
    return result;
  }

  get paginatedItems(): any[] {
    const start = (this.currentPage - 1) * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    return this.filtered.slice(start, end);
  }

  get baseRoute(): string {
    const urlTree = this.router.parseUrl(this.router.url);
    const segments = urlTree.root.children['primary']?.segments.map(s => s.path) || [];
    const last = segments[segments.length - 1];
    if (last === 'novo' || last === 'editar') {
      return '/' + segments.slice(0, -1).join('/');
    }
    return '/' + segments.join('/');
  }

  editar(id: number): void {
    this.router.navigate([this.baseRoute, 'editar', id]);
  }

  novo(): void {
    this.router.navigate([this.baseRoute, 'novo']);
  }

  // Abre modal de confirmação antes de deletar
  confirmDelete(item: any): void {
    this.itemToDelete = item;
    this.showDeleteConfirm = true;
  }

  // Cancela exclusão
  cancelDelete(): void {
    this.showDeleteConfirm = false;
    this.itemToDelete = null;
  }

  // Executa exclusão após confirmação
  deletar(): void {
    if (!this.itemToDelete) return;
    
    this.deleting = true;
    this.http.delete(`${this.apiUrl}/${this.itemToDelete.id}`).subscribe({
      next: () => {
        this.toastService.success('Registro excluído com sucesso!');
        this.showDeleteConfirm = false;
        this.itemToDelete = null;
        this.deleting = false;
        this.load();
      },
      error: () => {
        this.toastService.error('Erro ao excluir registro');
        this.deleting = false;
      }
    });
  }

  executarAcaoExtra(acao: any, item: any): void {
    if (acao.confirm) {
      if (confirm(acao.confirm)) {
        this.executeAction(acao, item);
      }
    } else {
      this.executeAction(acao, item);
    }
  }

  abrirStatus(item: any): void {
    this.statusItem = item;
    this.showStatusDialog = true;
  }

  cancelarStatus(): void {
    this.showStatusDialog = false;
    this.statusItem = null;
  }

  confirmarStatus(novoStatus: string): void {
    if (!this.statusAction || !this.statusItem) return;

    this.savingStatus = true;
    const url = this.statusAction.api.replace('{id}', this.statusItem.id);
    const body: any = {};
    body[this.statusAction.field] = novoStatus;

    this.http.patch(url, body).subscribe({
      next: () => {
        this.toastService.success('Status atualizado com sucesso!');
        this.savingStatus = false;
        this.showStatusDialog = false;
        this.statusItem = null;
        this.load();
      },
      error: (err: any) => {
        this.savingStatus = false;
        this.toastService.error(err?.error?.message || 'Erro ao alterar o status');
      }
    });
  }

  acaoVisivel(acao: any, item: any): boolean {
    if (!acao.visibleWhen) return true;
    return String(item[acao.visibleWhen.field]) === String(acao.visibleWhen.equals);
  }

  /** Mostra o botão "Pagar conta"/"Receber conta" apenas quando faz sentido */
  podeAcaoRapida(item: any): boolean {
    const quick = this.statusAction?.quick;
    if (!quick) return false;
    const atual = String(item[this.statusAction!.field] || '');
    return quick.visibleWhenStatusIn.includes(atual);
  }

  executarAcaoRapida(item: any): void {
    const quick = this.statusAction?.quick;
    if (!quick || !this.statusAction) return;

    const url = this.statusAction.api.replace('{id}', item.id);
    const body: any = {};
    body[this.statusAction.field] = quick.value;

    this.http.patch(url, body).subscribe({
      next: () => {
        this.toastService.success(quick.successMessage);
        this.load();
      },
      error: (err: any) => {
        this.toastService.error(err?.error?.message || 'Não foi possível concluir a ação');
      }
    });
  }

  private executeAction(acao: any, item: any): void {
    const url = acao.api.replace('{id}', item.id);
    const method = acao.method?.toLowerCase() || 'post';

    const request: any = method === 'delete'
      ? this.http.delete(url)
      : method === 'put'
        ? this.http.put(url, {})
        : method === 'patch'
          ? this.http.patch(url, {})
          : this.http.post(url, {});

    request.subscribe({
      next: () => {
        this.toastService.success('Ação executada com sucesso!');
        this.load();
      },
      error: () => {
        this.toastService.error('Erro ao executar ação');
      }
    });
  }

  // Eventos de paginação
  onPageChange(page: number): void {
    this.currentPage = page;
  }

  // Eventos de busca/filtro
  onSearchChange(term: string): void {
    this.searchTerm = term;
    this.currentPage = 1; // Volta para primeira página
  }

  onFilterChange(filters: Record<string, any>): void {
    this.activeFilters = filters;
    this.currentPage = 1; // Volta para primeira página
  }

  onClearFilters(): void {
    this.searchTerm = '';
    this.activeFilters = {};
    this.currentPage = 1;
  }

  columnLabel(col: string): string {
    return this.columnLabels[col] || (col.charAt(0).toUpperCase() + col.slice(1));
  }

  formatValue(obj: any, col: string): string {
    const val = obj[col];
    if (val === null || val === undefined) return '-';
    if (typeof val === 'boolean') return val ? 'Sim' : 'Não';
    if (col === 'senhaTemporaria') return val ? '🔴 Temporária' : '✅ Própria';

    const mapa = this.valueLabels[col];
    if (mapa && mapa[String(val)] !== undefined) return mapa[String(val)];

    if (this.currencyColumns.includes(col)) {
      const numero = Number(val);
      return isNaN(numero) ? String(val) : numero.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    }

    return String(val);
  }
}

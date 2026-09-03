import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';

/**
 * Componente de paginação reutilizável.
 * 
 * Uso:
 * <app-pagination
 *   [totalItems]="totalItems"
 *   [itemsPerPage]="itemsPerPage"
 *   [currentPage]="currentPage"
 *   (pageChange)="onPageChange($event)">
 * </app-pagination>
 */
@Component({
  selector: 'app-pagination',
  template: `
    <div class="pagination-wrapper" *ngIf="totalItems > itemsPerPage">
      <div class="pagination-info">
        Exibindo {{ startItem }}-{{ endItem }} de {{ totalItems }} registros
      </div>
      
      <nav class="pagination-nav">
        <ul class="pagination">
          <!-- Botão Anterior -->
          <li class="page-item" [class.disabled]="currentPage === 1">
            <button class="page-link" (click)="goToPage(currentPage - 1)" [disabled]="currentPage === 1">
              <i class="bi bi-chevron-left"></i>
            </button>
          </li>

          <!-- Primeira página -->
          <li class="page-item" *ngIf="showFirstPage">
            <button class="page-link" (click)="goToPage(1)">1</button>
          </li>

          <!-- Reticências iniciais -->
          <li class="page-item disabled" *ngIf="showFirstEllipsis">
            <span class="page-link">...</span>
          </li>

          <!-- Páginas do meio -->
          <li class="page-item" *ngFor="let page of visiblePages"
              [class.active]="page === currentPage">
            <button class="page-link" (click)="goToPage(page)">{{ page }}</button>
          </li>

          <!-- Reticências finais -->
          <li class="page-item disabled" *ngIf="showLastEllipsis">
            <span class="page-link">...</span>
          </li>

          <!-- Última página -->
          <li class="page-item" *ngIf="showLastPage">
            <button class="page-link" (click)="goToPage(totalPages)">{{ totalPages }}</button>
          </li>

          <!-- Botão Próximo -->
          <li class="page-item" [class.disabled]="currentPage === totalPages">
            <button class="page-link" (click)="goToPage(currentPage + 1)" [disabled]="currentPage === totalPages">
              <i class="bi bi-chevron-right"></i>
            </button>
          </li>
        </ul>
      </nav>
    </div>
  `,
  styles: [`
    .pagination-wrapper {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 16px 0;
      gap: 16px;
    }

    .pagination-info {
      font-size: 0.8125rem;
      color: var(--text-secondary, #4a5568);
    }

    .pagination {
      margin: 0;
      gap: 4px;
    }

    .page-item {
      margin: 0;
    }

    .page-link {
      display: flex;
      align-items: center;
      justify-content: center;
      min-width: 36px;
      height: 36px;
      padding: 0 8px;
      border: 1px solid var(--border-color, #e2e8f0);
      border-radius: 8px;
      background: white;
      color: var(--text-primary, #1a1a2e);
      font-size: 0.875rem;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.2s;
    }

    .page-link:hover:not(.disabled) {
      background: var(--bg-secondary, #f8f9fa);
      border-color: var(--orange, #f97316);
      color: var(--orange, #f97316);
    }

    .page-item.active .page-link {
      background: var(--orange, #f97316);
      border-color: var(--orange, #f97316);
      color: white;
    }

    .page-item.disabled .page-link {
      background: var(--bg-secondary, #f8f9fa);
      color: var(--text-muted, #a0aec0);
      cursor: not-allowed;
    }

    .page-link i {
      font-size: 0.75rem;
    }

    @media (max-width: 576px) {
      .pagination-wrapper {
        flex-direction: column;
        align-items: center;
      }

      .pagination-info {
        order: 1;
      }
    }
  `]
})
export class PaginationComponent implements OnChanges {
  @Input() totalItems = 0;
  @Input() itemsPerPage = 10;
  @Input() currentPage = 1;
  @Input() maxVisiblePages = 5;

  @Output() pageChange = new EventEmitter<number>();

  totalPages = 1;
  startItem = 0;
  endItem = 0;
  visiblePages: number[] = [];

  showFirstPage = false;
  showLastPage = false;
  showFirstEllipsis = false;
  showLastEllipsis = false;

  ngOnChanges(changes: SimpleChanges): void {
    this.calculatePagination();
  }

  calculatePagination(): void {
    this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage) || 1;
    
    // Garante que currentPage está dentro dos limites
    if (this.currentPage < 1) this.currentPage = 1;
    if (this.currentPage > this.totalPages) this.currentPage = this.totalPages;

    // Calcula itens exibidos
    this.startItem = (this.currentPage - 1) * this.itemsPerPage + 1;
    this.endItem = Math.min(this.currentPage * this.itemsPerPage, this.totalItems);

    // Calcula páginas visíveis
    this.calculateVisiblePages();
  }

  calculateVisiblePages(): void {
    const pages: number[] = [];
    let startPage: number;
    let endPage: number;

    if (this.totalPages <= this.maxVisiblePages) {
      // Mostra todas as páginas
      startPage = 1;
      endPage = this.totalPages;
    } else {
      // Calcula páginas ao redor da atual
      const halfVisible = Math.floor(this.maxVisiblePages / 2);
      startPage = Math.max(1, this.currentPage - halfVisible);
      endPage = Math.min(this.totalPages, startPage + this.maxVisiblePages - 1);

      // Ajusta se estiver no início ou fim
      if (endPage - startPage + 1 < this.maxVisiblePages) {
        startPage = Math.max(1, endPage - this.maxVisiblePages + 1);
      }
    }

    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }

    this.visiblePages = pages;
    this.showFirstPage = startPage > 1;
    this.showLastPage = endPage < this.totalPages;
    this.showFirstEllipsis = startPage > 2;
    this.showLastEllipsis = endPage < this.totalPages - 1;
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages && page !== this.currentPage) {
      this.currentPage = page;
      this.pageChange.emit(page);
      this.calculatePagination();
    }
  }
}

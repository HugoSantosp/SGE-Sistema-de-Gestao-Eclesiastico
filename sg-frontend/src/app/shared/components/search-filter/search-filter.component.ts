import { Component, EventEmitter, Input, Output } from '@angular/core';

/**
 * Interface para configuração de filtro
 */
export interface FilterConfig {
  key: string;
  label: string;
  type: 'text' | 'select' | 'date' | 'dateRange';
  options?: { value: string; label: string }[];
  placeholder?: string;
}

/**
 * Componente de busca e filtros reutilizável.
 * 
 * Uso:
 * <app-search-filter
 *   [filters]="filterConfigs"
 *   [searchPlaceholder]="'Buscar membros...'"
 *   (searchChange)="onSearch($event)"
 *   (filterChange)="onFilterChange($event)"
 *   (clearFilters)="onClearFilters()">
 * </app-search-filter>
 */
@Component({
  selector: 'app-search-filter',
  template: `
    <div class="search-filter-container">
      <!-- Barra de busca principal -->
      <div class="search-box">
        <i class="bi bi-search search-icon"></i>
        <input type="text" 
               class="search-input" 
               [placeholder]="searchPlaceholder"
               [(ngModel)]="searchTerm"
               (input)="onSearchChange()"
               (keyup.enter)="onSearch()">
        <button class="search-clear" *ngIf="searchTerm" (click)="clearSearch()">
          <i class="bi bi-x"></i>
        </button>
      </div>

      <!-- Filtros adicionais -->
      <div class="filters-row" *ngIf="filters.length > 0">
        <div class="filter-item" *ngFor="let filter of filters">
          <!-- Filtro de texto -->
          <input *ngIf="filter.type === 'text'"
                 type="text"
                 class="filter-input"
                 [placeholder]="filter.placeholder || filter.label"
                 [(ngModel)]="filterValues[filter.key]"
                 (input)="onFilterChange()">

          <!-- Filtro select -->
          <select *ngIf="filter.type === 'select'"
                  class="filter-select"
                  [(ngModel)]="filterValues[filter.key]"
                  (change)="onFilterChange()">
            <option value="">{{ filter.label }}</option>
            <option *ngFor="let option of filter.options" [value]="option.value">
              {{ option.label }}
            </option>
          </select>

          <!-- Filtro de data -->
          <input *ngIf="filter.type === 'date'"
                 type="date"
                 class="filter-input"
                 [(ngModel)]="filterValues[filter.key]"
                 (change)="onFilterChange()">

          <!-- Filtro de período -->
          <div *ngIf="filter.type === 'dateRange'" class="date-range">
            <input type="date"
                   class="filter-input"
                   [(ngModel)]="filterValues[filter.key + '_start']"
                   (change)="onFilterChange()">
            <span class="date-separator">até</span>
            <input type="date"
                   class="filter-input"
                   [(ngModel)]="filterValues[filter.key + '_end']"
                   (change)="onFilterChange()">
          </div>
        </div>

        <!-- Botão limpar filtros -->
        <button class="btn btn-sm btn-outline-secondary" 
                (click)="clearAllFilters()"
                *ngIf="hasActiveFilters">
          <i class="bi bi-x-circle me-1"></i>
          Limpar
        </button>
      </div>

      <!-- Tags de filtros ativos -->
      <div class="active-filters" *ngIf="activeFilterTags.length > 0">
        <span class="filter-tag" *ngFor="let tag of activeFilterTags">
          {{ tag.label }}: {{ tag.value }}
          <button class="tag-remove" (click)="removeFilter(tag.key)">
            <i class="bi bi-x"></i>
          </button>
        </span>
      </div>
    </div>
  `,
  styles: [`
    .search-filter-container {
      display: flex;
      flex-direction: column;
      gap: 12px;
      margin-bottom: 20px;
    }

    .search-box {
      position: relative;
      display: flex;
      align-items: center;
    }

    .search-icon {
      position: absolute;
      left: 14px;
      color: var(--text-muted, #a0aec0);
      font-size: 1rem;
    }

    .search-input {
      width: 100%;
      padding: 12px 40px 12px 42px;
      border: 1px solid var(--border-color, #e2e8f0);
      border-radius: 12px;
      background: white;
      font-size: 0.9rem;
      color: var(--text-primary, #1a1a2e);
      transition: all 0.2s;
    }

    .search-input:focus {
      outline: none;
      border-color: var(--orange, #f97316);
      box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.1);
    }

    .search-input::placeholder {
      color: var(--text-muted, #a0aec0);
    }

    .search-clear {
      position: absolute;
      right: 12px;
      background: none;
      border: none;
      color: var(--text-muted, #a0aec0);
      cursor: pointer;
      padding: 4px;
      font-size: 1rem;
      opacity: 0.6;
      transition: opacity 0.2s;
    }

    .search-clear:hover {
      opacity: 1;
    }

    .filters-row {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
      align-items: center;
    }

    .filter-item {
      flex: 1;
      min-width: 150px;
    }

    .filter-input,
    .filter-select {
      width: 100%;
      padding: 10px 14px;
      border: 1px solid var(--border-color, #e2e8f0);
      border-radius: 10px;
      background: white;
      font-size: 0.85rem;
      color: var(--text-primary, #1a1a2e);
      transition: all 0.2s;
    }

    .filter-input:focus,
    .filter-select:focus {
      outline: none;
      border-color: var(--orange, #f97316);
      box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.1);
    }

    .date-range {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .date-range .filter-input {
      flex: 1;
    }

    .date-separator {
      color: var(--text-muted, #a0aec0);
      font-size: 0.85rem;
    }

    .active-filters {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }

    .filter-tag {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 6px 12px;
      background: rgba(249, 115, 22, 0.1);
      color: var(--orange, #f97316);
      border-radius: 20px;
      font-size: 0.8rem;
      font-weight: 500;
    }

    .tag-remove {
      background: none;
      border: none;
      color: inherit;
      cursor: pointer;
      padding: 0;
      font-size: 0.9rem;
      opacity: 0.7;
      transition: opacity 0.2s;
    }

    .tag-remove:hover {
      opacity: 1;
    }

    @media (max-width: 768px) {
      .filters-row {
        flex-direction: column;
      }

      .filter-item {
        width: 100%;
      }
    }
  `]
})
export class SearchFilterComponent {
  @Input() filters: FilterConfig[] = [];
  @Input() searchPlaceholder = 'Buscar...';
  @Input() debounceTime = 300;

  @Output() searchChange = new EventEmitter<string>();
  @Output() filterChange = new EventEmitter<Record<string, any>>();
  @Output() onClearFilters = new EventEmitter<void>();

  searchTerm = '';
  filterValues: Record<string, any> = {};
  private searchTimeout: any;

  get hasActiveFilters(): boolean {
    return this.searchTerm !== '' || 
           Object.values(this.filterValues).some(v => v !== '' && v !== null && v !== undefined);
  }

  get activeFilterTags(): Array<{ key: string; label: string; value: string }> {
    const tags: Array<{ key: string; label: string; value: string }> = [];

    if (this.searchTerm) {
      tags.push({ key: 'search', label: 'Busca', value: this.searchTerm });
    }

    Object.entries(this.filterValues).forEach(([key, value]) => {
      if (value !== '' && value !== null && value !== undefined) {
        const filter = this.filters.find(f => f.key === key || key.startsWith(f.key + '_'));
        if (filter) {
          let label = filter.label;
          let displayValue = value;

          if (filter.type === 'select' && filter.options) {
            const option = filter.options.find(o => o.value === value);
            if (option) displayValue = option.label;
          }

          if (key.includes('_start')) {
            label += ' (de)';
          } else if (key.includes('_end')) {
            label += ' (até)';
          }

          tags.push({ key, label, value: displayValue });
        }
      }
    });

    return tags;
  }

  onSearchChange(): void {
    clearTimeout(this.searchTimeout);
    this.searchTimeout = setTimeout(() => {
      this.searchChange.emit(this.searchTerm);
    }, this.debounceTime);
  }

  onSearch(): void {
    clearTimeout(this.searchTimeout);
    this.searchChange.emit(this.searchTerm);
  }

  onFilterChange(): void {
    this.filterChange.emit({ ...this.filterValues, search: this.searchTerm });
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.searchChange.emit('');
  }

  removeFilter(key: string): void {
    if (key === 'search') {
      this.clearSearch();
    } else {
      this.filterValues[key] = '';
      this.onFilterChange();
    }
  }

  clearAllFilters(): void {
    this.searchTerm = '';
    this.filterValues = {};
    this.onClearFilters.emit();
    this.searchChange.emit('');
    this.filterChange.emit({});
  }
}

import { Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { getApiUrl } from '../../../core/config/backend-config';

@Component({
  selector: 'app-landing-page',
  templateUrl: './landing-page.component.html',
  styleUrls: ['./landing-page.component.scss']
})
export class LandingPageComponent implements OnInit, OnDestroy {
  churchInfo: any = {
    nome: 'ICERT - Agência do Reino de Deus',
    endereco: 'São João de Meriti, RJ',
    telefone: '',
    horarios: [
      { dia: 'Domingo', horario: '18:00' },
      { dia: 'Quarta-feira', horario: '19:30' }
    ]
  };
  pastores: any[] = [];
  eventos: any[] = [];
  loading = true;

  // Calendar state
  currentMonth: number;
  currentYear: number;
  calendarDays: (number | null)[] = [];
  monthNames = ['Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
                'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'];
  dayNames = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];
  selectedDay: number | null = null;
  selectedDayEvents: any[] = [];
  /** Eventos agrupados por dia (YYYY-MM-DD), recalculados uma vez por carga. */
  private eventsByDay: Record<string, any[]> = {};
  /** Evita exibir "nenhum evento" antes de os dados do mês chegarem. */
  eventsLoaded = false;
  eventsLoadError = false;
  showDayModal = false;
  private bodyOverflowBeforeModal: string | null = null;

  constructor(private http: HttpClient) {
    const now = new Date();
    this.currentMonth = now.getMonth() + 1;
    this.currentYear = now.getFullYear();
  }

  ngOnInit(): void {
    this.http.get<any[]>(getApiUrl('/api/public/info')).subscribe({
      next: (data: any) => { this.churchInfo = { ...this.churchInfo, ...data }; }
    });

    this.carregarPastores();

    this.loadEvents();
  }

  private carregarPastores(): void {
    this.http.get<any[]>(getApiUrl('/api/public/pastores')).subscribe({
      next: (data) => { this.pastores = data; this.fotoReloading = false; },
      error: () => { this.fotoReloading = false; }
    });
  }

  // URLs assinadas expiram: se uma imagem falhar, busca os dados de novo (novas URLs), no máximo 2 vezes
  private fotoRetries = 0;
  private fotoReloading = false;

  onFotoError(): void {
    if (this.fotoReloading || this.fotoRetries >= 2) return;
    this.fotoRetries++;
    this.fotoReloading = true;
    this.carregarPastores();
  }

  private loadEvents(): void {
    this.loading = true;
    this.eventsLoaded = false;
    this.eventsLoadError = false;
    this.http.get<any[]>(
      getApiUrl(`/api/public/eventos?mes=${this.currentMonth}&ano=${this.currentYear}`)
    ).subscribe({
      next: (data) => {
        this.eventos = data || [];
        this.eventsLoaded = true;
        this.buildCalendar();
      },
      error: () => {
        this.eventos = [];
        this.eventsLoaded = true;
        this.eventsLoadError = true;
        this.buildCalendar();
        this.loading = false;
      },
      complete: () => { this.loading = false; }
    });
  }

  buildCalendar(): void {
    const daysInMonth = new Date(this.currentYear, this.currentMonth, 0).getDate();
    const firstDayOfWeek = new Date(this.currentYear, this.currentMonth - 1, 1).getDay();

    this.calendarDays = [];
    for (let i = 0; i < firstDayOfWeek; i++) {
      this.calendarDays.push(null);
    }
    for (let day = 1; day <= daysInMonth; day++) {
      this.calendarDays.push(day);
    }

    this.buildEventsIndex();

    // Mantém o modal coerente se os dados do mês chegarem depois do clique.
    if (this.selectedDay !== null && this.showDayModal) {
      this.selectedDayEvents = this.getEventsForDay(this.selectedDay);
    }
  }

  /** Agrupa os eventos por dia uma única vez por carga, em vez de filtrar no template. */
  private buildEventsIndex(): void {
    const index: Record<string, any[]> = {};
    for (const evento of this.eventos) {
      if (!evento?.data) continue;
      (index[evento.data] ||= []).push(evento);
    }
    this.eventsByDay = index;
  }

  getEventsForDay(day: number | null): any[] {
    if (!day) return [];
    const dayStr = `${this.currentYear}-${String(this.currentMonth).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
    return this.eventsByDay[dayStr] || [];
  }

  hasEvent(day: number | null): boolean {
    return this.getEventsForDay(day).length > 0;
  }

  /** Clique em qualquer ponto da célula (não só no número/bolinha) abre o modal. */
  onDayClick(day: number | null): void {
    if (day === null) return;
    this.openDayEvents(day);
  }

  openDayEvents(day: number): void {
    this.selectedDay = day;
    this.selectedDayEvents = this.getEventsForDay(day);
    if (!this.showDayModal) {
      this.bodyOverflowBeforeModal = document.body.style.overflow;
    }
    this.showDayModal = true;
    document.body.style.overflow = 'hidden';
  }

  closeDayEvents(): void {
    this.showDayModal = false;
    this.selectedDay = null;
    this.selectedDayEvents = [];
    this.restoreBodyOverflow();
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.showDayModal) {
      this.closeDayEvents();
    }
  }

  previousMonth(): void {
    if (this.currentMonth === 1) {
      this.currentMonth = 12;
      this.currentYear--;
    } else {
      this.currentMonth--;
    }
    this.selectedDay = null;
    this.selectedDayEvents = [];
    this.showDayModal = false;
    this.restoreBodyOverflow();
    this.loadEvents();
  }

  nextMonth(): void {
    if (this.currentMonth === 12) {
      this.currentMonth = 1;
      this.currentYear++;
    } else {
      this.currentMonth++;
    }
    this.selectedDay = null;
    this.selectedDayEvents = [];
    this.showDayModal = false;
    this.restoreBodyOverflow();
    this.loadEvents();
  }

  isToday(day: number | null): boolean {
    if (!day) return false;
    const today = new Date();
    return day === today.getDate() &&
           this.currentMonth === today.getMonth() + 1 &&
           this.currentYear === today.getFullYear();
  }

  /** Data por extenso do dia selecionado, usada no cabeçalho do modal. */
  getSelectedDateLabel(): string {
    if (this.selectedDay === null) return '';
    const dias = ['domingo', 'segunda-feira', 'terça-feira', 'quarta-feira',
                  'quinta-feira', 'sexta-feira', 'sábado'];
    const d = new Date(this.currentYear, this.currentMonth - 1, this.selectedDay);
    return `${dias[d.getDay()]}, ${this.selectedDay} de ${this.monthNames[this.currentMonth - 1]} de ${this.currentYear}`;
  }

  ngOnDestroy(): void {
    this.restoreBodyOverflow();
  }

  private restoreBodyOverflow(): void {
    if (this.bodyOverflowBeforeModal !== null) {
      document.body.style.overflow = this.bodyOverflowBeforeModal;
      this.bodyOverflowBeforeModal = null;
    }
  }

  scrollTo(section: string): void {
    document.getElementById(section)?.scrollIntoView({ behavior: 'smooth' });
  }
}

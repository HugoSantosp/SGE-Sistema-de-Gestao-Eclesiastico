import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-landing-page',
  templateUrl: './landing-page.component.html',
  styleUrls: ['./landing-page.component.scss']
})
export class LandingPageComponent implements OnInit {
  churchInfo: any = {
    nome: 'ICERT - Agência do Reino de Deus',
    endereco: 'São João de Meriti, RJ',
    telefone: '',
    horarios: [
      { dia: 'Domingo', horario: '09:00' },
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

  constructor(private http: HttpClient) {
    const now = new Date();
    this.currentMonth = now.getMonth() + 1;
    this.currentYear = now.getFullYear();
  }

  ngOnInit(): void {
    this.http.get('/api/public/info').subscribe({
      next: (data: any) => { this.churchInfo = { ...this.churchInfo, ...data }; }
    });

    this.http.get<any[]>('/api/public/pastores').subscribe({
      next: (data) => { this.pastores = data; }
    });

    this.loadEvents();
  }

  private loadEvents(): void {
    this.loading = true;
    this.http.get<any[]>(`/api/public/eventos?mes=${this.currentMonth}&ano=${this.currentYear}`).subscribe({
      next: (data) => {
        this.eventos = data;
        this.buildCalendar();
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

    if (this.selectedDay !== null) {
      this.showDayEvents(this.selectedDay);
    }
  }

  getEventsForDay(day: number | null): any[] {
    if (!day) return [];
    const dayStr = `${this.currentYear}-${String(this.currentMonth).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
    return this.eventos.filter(e => e.data === dayStr);
  }

  hasEvent(day: number | null): boolean {
    return this.getEventsForDay(day).length > 0;
  }

  showDayEvents(day: number): void {
    this.selectedDay = day;
    this.selectedDayEvents = this.getEventsForDay(day);
  }

  hideDayEvents(): void {
    this.selectedDay = null;
    this.selectedDayEvents = [];
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
    this.loadEvents();
  }

  isToday(day: number | null): boolean {
    if (!day) return false;
    const today = new Date();
    return day === today.getDate() &&
           this.currentMonth === today.getMonth() + 1 &&
           this.currentYear === today.getFullYear();
  }

  getMonthName(dateStr: string): string {
    const months = ['Jan','Fev','Mar','Abr','Mai','Jun','Jul','Ago','Set','Out','Nov','Dez'];
    const d = new Date(dateStr + 'T12:00:00');
    return months[d.getMonth()];
  }

  getDay(dateStr: string): string {
    const d = new Date(dateStr + 'T12:00:00');
    return String(d.getDate()).padStart(2, '0');
  }

  scrollTo(section: string): void {
    document.getElementById(section)?.scrollIntoView({ behavior: 'smooth' });
  }
}

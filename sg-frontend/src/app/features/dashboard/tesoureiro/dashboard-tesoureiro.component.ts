import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-dashboard-tesoureiro',
  templateUrl: './dashboard-tesoureiro.component.html',
  styles: [':host { display: contents; }']
})
export class DashboardTesoureiroComponent implements OnInit {
  stats = { contasPagar: 0, contasReceber: 0, totalPagar: 0, totalReceber: 0 };
  loading = true;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<any[]>('/api/financeiro/contas-pagar').subscribe({
      next: (d) => {
        this.stats.contasPagar = d.length;
        this.stats.totalPagar = d.reduce((s, c) => s + (c.valor || 0), 0);
      },
      complete: () => this.loading = false
    });
    this.http.get<any[]>('/api/financeiro/contas-receber').subscribe({
      next: (d) => {
        this.stats.contasReceber = d.length;
        this.stats.totalReceber = d.reduce((s, c) => s + (c.valor || 0), 0);
      }
    });
  }
}

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MmApiService } from '../../../core/services/mm-api.service';
import { EscalaDetalhada } from '../../../core/models/mm.models';

@Component({
  selector: 'app-escala-publica-resultado',
  templateUrl: './resultado.component.html',
  styles: [':host { display: contents; }']
})
export class EscalaPublicaResultadoComponent implements OnInit {
  token = '';
  escala: EscalaDetalhada | null = null;
  loading = true;
  erro = '';
  instrumentosAbertos: { [key: string]: boolean } = {};

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private api: MmApiService
  ) {}

  ngOnInit(): void {
    this.token = this.route.snapshot.params['token'];
    if (!this.token) {
      this.erro = 'Link inválido';
      this.loading = false;
      return;
    }

    this.api.buscarResultadoPublico(this.token).subscribe({
      next: (data) => {
        this.escala = data;
        this.loading = false;
      },
      error: () => {
        this.erro = 'Resultado não encontrado.';
        this.loading = false;
      }
    });
  }

  getInstrumentosUsados(): string[] {
    if (!this.escala) return [];
    const insts = new Set<string>();
    this.escala.datas.forEach(d => d.designacoes.forEach(des => insts.add(des.instrumento)));
    return Array.from(insts).sort();
  }

  getIntegrantesPorInstrumento(instrumento: string): { nome: string; data: string }[] {
    if (!this.escala) return [];
    const resultado: { nome: string; data: string }[] = [];
    this.escala.datas.forEach(d => {
      d.designacoes
        .filter(des => des.instrumento === instrumento)
        .forEach(des => {
          resultado.push({ nome: des.nomeIntegrante, data: this.formatDate(d.data) });
        });
    });
    return resultado;
  }

  toggleInstrumento(inst: string): void {
    this.instrumentosAbertos[inst] = !this.instrumentosAbertos[inst];
  }

  getInstrumentIcon(instrumento: string): string {
    const icons: { [key: string]: string } = {
      'Ministro': 'bi-mic',
      'Guitarra': 'bi-music-note',
      'Violão': 'bi-music-note',
      'Baixo': 'bi-music-note',
      'Bateria': 'bi-music-note',
      'Teclado': 'bi-music-note-beamed',
      'Sax': 'bi-music-note-beamed',
      'Backing Vocal': 'bi-people'
    };
    return icons[instrumento] || 'bi-person-music';
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    const [y, m, d] = dateStr.split('-');
    const meses = ['Jan','Fev','Mar','Abr','Mai','Jun','Jul','Ago','Set','Out','Nov','Dez'];
    return `${d} ${meses[parseInt(m)-1]} ${y}`;
  }

  voltarHome(): void {
    this.router.navigate(['/']);
  }
}

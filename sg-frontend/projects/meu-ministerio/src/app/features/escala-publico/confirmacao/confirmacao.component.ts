import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MmApiService } from '../../../core/services/mm-api.service';
import { EscalaDetalhada } from '../../../core/models/mm.models';

@Component({
  selector: 'app-escala-publica-confirmacao',
  templateUrl: './confirmacao.component.html',
  styles: [
    `:host { display: contents; }`,
    `.success-check { font-size: 4rem; color: #22c55e; animation: scaleIn 0.4s ease-out; }`,
    `@keyframes scaleIn { from { transform: scale(0); opacity: 0; } to { transform: scale(1); opacity: 1; } }`
  ]
})
export class EscalaPublicaConfirmacaoComponent implements OnInit {
  token = '';
  escala: EscalaDetalhada | null = null;
  loading = true;
  erro = '';

  // Form
  nome = '';
  email = '';
  celular = '';
  dataSelecionadas: number[] = [];
  enviando = false;
  sucesso = false;
  mensagemErro = '';

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

    this.api.buscarEscalaPublica(this.token).subscribe({
      next: (data) => {
        this.escala = data;
        this.loading = false;
      },
      error: () => {
        this.erro = 'Escala não encontrada ou já fechada.';
        this.loading = false;
      }
    });
  }

  toggleData(dataId: number): void {
    const idx = this.dataSelecionadas.indexOf(dataId);
    if (idx >= 0) {
      this.dataSelecionadas.splice(idx, 1);
    } else {
      this.dataSelecionadas.push(dataId);
    }
  }

  confirmar(): void {
    if (!this.nome.trim()) {
      this.mensagemErro = 'Informe seu nome';
      return;
    }
    if (this.dataSelecionadas.length === 0) {
      this.mensagemErro = 'Selecione pelo menos uma data';
      return;
    }

    this.enviando = true;
    this.mensagemErro = '';

    this.api.confirmarDisponibilidadePublica(this.token, {
      nome: this.nome.trim(),
      email: this.email.trim() || undefined,
      celular: this.celular.trim() || undefined,
      dataIds: this.dataSelecionadas
    }).subscribe({
      next: () => {
        this.sucesso = true;
        this.enviando = false;
      },
      error: (err) => {
        this.enviando = false;
        this.mensagemErro = err.error?.message || 'Erro ao confirmar. Tente novamente.';
      }
    });
  }

  voltarHome(): void {
    this.router.navigate(['/']);
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    const [y, m, d] = dateStr.split('-');
    const meses = ['Jan','Fev','Mar','Abr','Mai','Jun','Jul','Ago','Set','Out','Nov','Dez'];
    return `${d} ${meses[parseInt(m)-1]} ${y}`;
  }
}

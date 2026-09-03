import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EscalaService, EscalaDetalhada, DataDetalhada, ConfirmacaoResponse, DesignacaoRequest } from '../escala.service';

@Component({
  selector: 'app-escala-montar',
  templateUrl: './escala-montar.component.html',
  styles: [`:host { display: contents; }`]
})
export class EscalaMontarComponent implements OnInit {
  escala: EscalaDetalhada | null = null;
  escalaId: number | null = null;
  dataId: number | null = null;
  dataAtual: DataDetalhada | null = null;
  loading = true;
  instrumentos: string[] = [];

  // Designações em edição: { [dataId: number]: DesignacaoRequest[] }
  designacoes: { [dataId: number]: DesignacaoRequest[] } = {};
  salvando = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private service: EscalaService
  ) {}

  ngOnInit(): void {
    this.escalaId = Number(this.route.snapshot.params['id']);
    this.dataId = this.route.snapshot.params['dataId']
      ? Number(this.route.snapshot.params['dataId'])
      : null;

    // Carrega instrumentos
    this.service.listarInstrumentos().subscribe({
      next: (data) => { this.instrumentos = data; },
      error: () => { this.instrumentos = ['Ministro','Guitarra','Violão','Baixo','Bateria','Teclado','Sax','Backing Vocal']; }
    });

    // Carrega escala
    this.service.buscarPorId(this.escalaId).subscribe({
      next: (data) => {
        this.escala = data;
        this.loading = false;

        // Inicializa designações para cada data
        data.datas.forEach(d => {
          this.designacoes[d.id] = d.designacoes.map(des => ({
            confirmacaoId: des.confirmacaoId,
            instrumento: des.instrumento,
            ordem: des.ordem
          }));
        });

        // Se tem dataId, encontra a data atual
        if (this.dataId) {
          this.dataAtual = data.datas.find(d => d.id === this.dataId) || null;
        }
      },
      error: () => { this.loading = false; alert('Erro ao carregar escala'); }
    });
  }

  confirmadosDaData(data: DataDetalhada): ConfirmacaoResponse[] {
    if (!this.escala) return [];
    return this.escala.confirmacoes.filter(c => c.dataIds.includes(data.id));
  }

  adicionarDesignacao(dataId: number): void {
    if (!this.designacoes[dataId]) {
      this.designacoes[dataId] = [];
    }
    // Pega a primeira confirmação disponível
    const data = this.escala?.datas.find(d => d.id === dataId);
    if (!data) return;
    const confirmados = this.confirmadosDaData(data);
    if (confirmados.length === 0) {
      alert('Não há confirmações para esta data. Compartilhe o link público primeiro.');
      return;
    }

    this.designacoes[dataId].push({
      confirmacaoId: confirmados[0].id,
      instrumento: this.instrumentos[0] || 'Ministro',
      ordem: this.designacoes[dataId].length
    });
  }

  removerDesignacao(dataId: number, index: number): void {
    this.designacoes[dataId].splice(index, 1);
  }

  salvarDesignacoes(dataId: number): void {
    if (!this.escalaId) return;
    this.salvando = true;

    this.service.salvarDesignacoes(this.escalaId, dataId, this.designacoes[dataId] || []).subscribe({
      next: () => {
        this.salvando = false;
        // Recarrega
        this.service.buscarPorId(this.escalaId!).subscribe(data => {
          this.escala = data;
          // Atualiza designações na memória
          data.datas.forEach(d => {
            this.designacoes[d.id] = d.designacoes.map(des => ({
              confirmacaoId: des.confirmacaoId,
              instrumento: des.instrumento,
              ordem: des.ordem
            }));
          });
        });
      },
      error: () => { this.salvando = false; alert('Erro ao salvar designações'); }
    });
  }

  salvarTodasDesignacoes(): void {
    if (!this.escalaId || !this.escala) return;
    this.salvando = true;
    let concluidas = 0;
    const total = this.escala.datas.length;

    this.escala.datas.forEach(d => {
      this.service.salvarDesignacoes(this.escalaId!, d.id, this.designacoes[d.id] || []).subscribe({
        next: () => {
          concluidas++;
          if (concluidas >= total) {
            this.salvando = false;
            alert('Todas as designações foram salvas!');
            this.service.buscarPorId(this.escalaId!).subscribe(data => {
              this.escala = data;
            });
          }
        },
        error: () => {
          concluidas++;
          if (concluidas >= total) {
            this.salvando = false;
            alert('Algumas designações podem não ter sido salvas.');
          }
        }
      });
    });
  }

  voltar(): void {
    this.router.navigate(['/escalas']);
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    const [y, m, d] = dateStr.split('-');
    return `${d}/${m}/${y}`;
  }
}

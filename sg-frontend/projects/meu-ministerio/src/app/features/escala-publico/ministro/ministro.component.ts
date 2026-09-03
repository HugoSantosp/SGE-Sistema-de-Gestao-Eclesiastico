import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MmApiService } from '../../../core/services/mm-api.service';
import { EscalaDetalhada, MusicaDTO } from '../../../core/models/mm.models';

@Component({
  selector: 'app-escala-publica-ministro',
  templateUrl: './ministro.component.html',
  styles: [':host { display: contents; }']
})
export class EscalaPublicaMinistroComponent implements OnInit {
  token = '';
  escala: EscalaDetalhada | null = null;
  loading = true;
  erro = '';
  editandoDataId: number | null = null;

  // Músicas em edição: { [dataId: number]: MusicaDTO[] }
  musicasEditando: { [dataId: number]: MusicaDTO[] } = {};
  salvando = false;
  sucesso = false;

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

    this.api.buscarRepertorioMinistro(this.token).subscribe({
      next: (data) => {
        this.escala = data;
        this.loading = false;

        // Inicializa músicas em edição
        data.datas.forEach(d => {
          this.musicasEditando[d.id] = d.musicas.length > 0
            ? d.musicas.map(m => ({ ...m }))
            : [{ nome: '', artista: '', link: '', ordem: 0 }];
        });
      },
      error: () => {
        this.erro = 'Repertório não encontrado.';
        this.loading = false;
      }
    });
  }

  editarData(dataId: number): void {
    this.editandoDataId = dataId;
  }

  cancelarEdicao(): void {
    this.editandoDataId = null;
    this.sucesso = false;
  }

  adicionarMusica(dataId: number): void {
    if (!this.musicasEditando[dataId]) {
      this.musicasEditando[dataId] = [];
    }
    this.musicasEditando[dataId].push({ nome: '', artista: '', link: '', ordem: this.musicasEditando[dataId].length });
  }

  removerMusica(dataId: number, index: number): void {
    this.musicasEditando[dataId].splice(index, 1);
  }

  salvarRepertorio(dataId: number): void {
    const musicas = (this.musicasEditando[dataId] || []).filter(m => m.nome.trim());
    if (musicas.length === 0) {
      alert('Adicione pelo menos uma música');
      return;
    }

    this.salvando = true;
    this.sucesso = false;

    // Ordena as músicas pela ordem
    musicas.forEach((m, i) => m.ordem = i);

    this.api.salvarMusicasPublico(this.token, dataId, musicas).subscribe({
      next: () => {
        this.salvando = false;
        this.sucesso = true;
        setTimeout(() => {
          this.sucesso = false;
          this.editandoDataId = null;
        }, 2000);
      },
      error: () => {
        this.salvando = false;
        alert('Erro ao salvar repertório. Verifique se você tem permissão.');
      }
    });
  }

  // Verifica se a data é futura ou hoje
  isDataFutura(dateStr: string): boolean {
    if (!dateStr) return false;
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    const data = new Date(dateStr + 'T00:00:00');
    return data >= hoje;
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

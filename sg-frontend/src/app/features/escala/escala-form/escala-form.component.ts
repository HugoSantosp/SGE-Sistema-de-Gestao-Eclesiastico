import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EscalaService, EscalaDetalhada, DataDetalhada } from '../escala.service';
import { ministerioUrl } from '../../../core/config/app-config';

@Component({
  selector: 'app-escala-form',
  templateUrl: './escala-form.component.html',
  styles: [`:host { display: contents; }`]
})
export class EscalaFormComponent implements OnInit {
  escalaId: number | null = null;
  escala: EscalaDetalhada | null = null;
  loading = true;

  // Formulário de edição da escala
  editTitulo = '';

  // Formulário de nova data
  novaData = { nomeEvento: '', data: '', horario: '', local: '' };
  salvandoData = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private service: EscalaService
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.params['id'];
    if (idParam) {
      this.escalaId = Number(idParam);
      this.service.buscarPorId(this.escalaId).subscribe({
        next: (data) => {
          this.escala = data;
          this.editTitulo = data.titulo;
          this.loading = false;
        },
        error: () => { this.loading = false; alert('Erro ao carregar escala'); }
      });
    } else {
      this.loading = false;
    }
  }

  voltar(): void {
    this.router.navigate(['/escalas']);
  }

  salvarTitulo(): void {
    if (!this.editTitulo.trim()) return;

    if (this.escalaId) {
      // Edição - não temos endpoint PUT ainda
      alert('Edição de título em desenvolvimento');
    } else {
      // Criação
      this.service.criar(this.editTitulo).subscribe({
        next: (res) => this.router.navigate(['/escalas', res.id]),
        error: () => alert('Erro ao criar escala')
      });
    }
  }

  adicionarData(): void {
    if (!this.escalaId) return;
    if (!this.novaData.nomeEvento || !this.novaData.data || !this.novaData.horario || !this.novaData.local) {
      alert('Preencha todos os campos da data');
      return;
    }

    this.salvandoData = true;
    this.service.adicionarData(this.escalaId, this.novaData).subscribe({
      next: () => {
        this.novaData = { nomeEvento: '', data: '', horario: '', local: '' };
        this.salvandoData = false;
        // Recarrega
        this.service.buscarPorId(this.escalaId!).subscribe(data => {
          this.escala = data;
        });
      },
      error: () => { this.salvandoData = false; alert('Erro ao adicionar data'); }
    });
  }

  removerData(dataId: number): void {
    if (!this.escalaId) return;
    if (!confirm('Remover esta data da escala?')) return;

    this.service.removerData(this.escalaId, dataId).subscribe({
      next: () => {
        this.service.buscarPorId(this.escalaId!).subscribe(data => {
          this.escala = data;
        });
      },
      error: () => alert('Erro ao remover data')
    });
  }

  copiarLinkPublico(): void {
    if (!this.escala?.publicToken) return;
    const link = ministerioUrl(`/escala/${this.escala.publicToken}`);
    navigator.clipboard.writeText(link).then(() => {
      alert('Link público copiado!');
    }).catch(() => {
      prompt('Link público da escala:', link);
    });
  }

  montarEscala(escalaId: number, dataId: number): void {
    this.router.navigate(['/escalas', escalaId, 'montar', dataId]);
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    const [y, m, d] = dateStr.split('-');
    return `${d}/${m}/${y}`;
  }
}

import { Component } from '@angular/core';
import { RelatoriosService } from './relatorios.service';

interface RelatorioOpcao {
  id: string;
  titulo: string;
  descricao: string;
  endpoint: string;
  formato: 'PDF' | 'Excel';
  arquivo: string;
}

@Component({
  selector: 'app-relatorios',
  templateUrl: './relatorios.component.html',
  styles: [':host { display: contents; }']
})
export class RelatoriosComponent {
  baixando: string | null = null;
  erro = '';

  relatorios: RelatorioOpcao[] = [
    {
      id: 'membros-pdf',
      titulo: 'Membros',
      descricao: 'Lista completa com cargo, data de batismo, contato, endereço e situação.',
      endpoint: '/api/reports/members/pdf',
      formato: 'PDF',
      arquivo: 'relatorio_membros.pdf'
    },
    {
      id: 'membros-excel',
      titulo: 'Membros',
      descricao: 'Mesmos dados em planilha, para filtrar e ordenar no Excel.',
      endpoint: '/api/reports/members/excel',
      formato: 'Excel',
      arquivo: 'relatorio_membros.xlsx'
    }
  ];

  constructor(private service: RelatoriosService) {}

  baixar(relatorio: RelatorioOpcao): void {
    this.baixando = relatorio.id;
    this.erro = '';

    this.service.baixar(relatorio.endpoint).subscribe({
      next: (blob) => {
        this.salvarArquivo(blob, relatorio.arquivo);
        this.baixando = null;
      },
      error: () => {
        this.erro = 'Não foi possível gerar o relatório. Tente novamente.';
        this.baixando = null;
      }
    });
  }

  /** Cria um link temporário e dispara o download no navegador. */
  private salvarArquivo(blob: Blob, nome: string): void {
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = nome;
    link.click();
    URL.revokeObjectURL(url);
  }
}

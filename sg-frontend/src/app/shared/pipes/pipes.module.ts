import { NgModule } from '@angular/core';
import { UploadUrlPipe } from './upload-url.pipe';

/**
 * Pipes compartilhados, sem dependências do restante do app.
 * Importado pelo SharedModule (painel admin) e pelo PublicoModule (site),
 * para o UploadUrlPipe não precisar ser declarado em dois módulos.
 */
@NgModule({
  declarations: [UploadUrlPipe],
  exports: [UploadUrlPipe]
})
export class PipesModule { }

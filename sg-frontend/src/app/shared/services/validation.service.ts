import { Injectable } from '@angular/core';
import { AbstractControl, ValidationErrors, ValidatorFn, FormGroup } from '@angular/forms';

/**
 * Service centralizado de validação para todo o frontend do SGE.
 * 
 * Fornece validadores reutilizáveis e mensagens de erro em português.
 */
@Injectable({ providedIn: 'root' })
export class ValidationService {

  /**
   * Mensagens de erro em português para cada tipo de validação
   */
  static readonly ERROR_MESSAGES: Record<string, string> = {
    required: 'Campo obrigatório',
    email: 'Email inválido',
    minlength: 'Mínimo de {min} caracteres',
    maxlength: 'Máximo de {max} caracteres',
    min: 'Valor mínimo é {min}',
    max: 'Valor máximo é {max}',
    pattern: 'Formato inválido',
    cpf: 'CPF inválido',
    cnpj: 'CNPJ inválido',
    phone: 'Telefone inválido',
    cep: 'CEP inválido',
    passwordMatch: 'As senhas não conferem',
    noWhitespace: 'Não pode conter espaços',
    alphanumeric: 'Apenas letras e números',
    futureDate: 'Data não pode ser no futuro',
    pastDate: 'Data não pode ser no passado',
    ageRange: 'Idade deve ser entre {min} e {max} anos'
  };

  /**
   * Obtém mensagem de erro para um campo específico
   */
  static getErrorMessage(fieldName: string, errors: ValidationErrors | null): string {
    if (!errors) return '';

    const firstError = Object.keys(errors)[0];
    const errorValue = errors[firstError];

    let message = ValidationService.ERROR_MESSAGES[firstError] || `Erro de validação em ${fieldName}`;

    // Substitui parâmetros na mensagem
    if (typeof errorValue === 'object' && errorValue !== null) {
      Object.keys(errorValue).forEach(key => {
        message = message.replace(`{${key}}`, errorValue[key]);
      });
    }

    return message;
  }

  /**
   * Validador de CPF
   */
  static cpf(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;

      const cpf = control.value.replace(/\D/g, '');
      
      if (cpf.length !== 11) return { cpf: true };
      if (/^(\d)\1{10}$/.test(cpf)) return { cpf: true };

      let sum = 0;
      for (let i = 0; i < 9; i++) {
        sum += parseInt(cpf.charAt(i)) * (10 - i);
      }
      
      let remainder = 11 - (sum % 11);
      if (remainder === 10 || remainder === 11) remainder = 0;
      if (remainder !== parseInt(cpf.charAt(9))) return { cpf: true };

      sum = 0;
      for (let i = 0; i < 10; i++) {
        sum += parseInt(cpf.charAt(i)) * (11 - i);
      }
      
      remainder = 11 - (sum % 11);
      if (remainder === 10 || remainder === 11) remainder = 0;
      if (remainder !== parseInt(cpf.charAt(10))) return { cpf: true };

      return null;
    };
  }

  /**
   * Validador de CNPJ
   */
  static cnpj(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;

      const cnpj = control.value.replace(/\D/g, '');
      
      if (cnpj.length !== 14) return { cnpj: true };
      if (/^(\d)\1{13}$/.test(cnpj)) return { cnpj: true };

      const weights1 = [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2];
      const weights2 = [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2];

      let sum = 0;
      for (let i = 0; i < 12; i++) {
        sum += parseInt(cnpj.charAt(i)) * weights1[i];
      }
      
      let remainder = sum % 11;
      const digit1 = remainder < 2 ? 0 : 11 - remainder;
      if (parseInt(cnpj.charAt(12)) !== digit1) return { cnpj: true };

      sum = 0;
      for (let i = 0; i < 13; i++) {
        sum += parseInt(cnpj.charAt(i)) * weights2[i];
      }
      
      remainder = sum % 11;
      const digit2 = remainder < 2 ? 0 : 11 - remainder;
      if (parseInt(cnpj.charAt(13)) !== digit2) return { cnpj: true };

      return null;
    };
  }

  /**
   * Validador de telefone brasileiro
   */
  static phone(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;

      const phone = control.value.replace(/\D/g, '');
      
      // Formatos aceitos: 10 dígitos (fixo) ou 11 dígitos (celular)
      if (phone.length < 10 || phone.length > 11) return { phone: true };
      
      // Verifica se começa com DDD válido
      const ddd = parseInt(phone.substring(0, 2));
      if (ddd < 11 || ddd > 99) return { phone: true };

      return null;
    };
  }

  /**
   * Validador de CEP brasileiro
   */
  static cep(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;

      const cep = control.value.replace(/\D/g, '');
      
      if (cep.length !== 8) return { cep: true };

      return null;
    };
  }

  /**
   * Validador de senha forte
   */
  static strongPassword(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;

      const password = control.value;
      const hasUpperCase = /[A-Z]/.test(password);
      const hasLowerCase = /[a-z]/.test(password);
      const hasNumeric = /[0-9]/.test(password);
      const hasSpecial = /[!@#$%^&*(),.?":{}|<>]/.test(password);

      const valid = hasUpperCase && hasLowerCase && hasNumeric && hasSpecial;
      
      if (!valid) {
        return { 
          strongPassword: {
            hasUpperCase,
            hasLowerCase,
            hasNumeric,
            hasSpecial
          }
        };
      }

      return null;
    };
  }

  /**
   * Validador de data não futura
   */
  static notFutureDate(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;

      const date = new Date(control.value);
      const today = new Date();
      today.setHours(23, 59, 59, 999);

      if (date > today) {
        return { futureDate: true };
      }

      return null;
    };
  }

  /**
   * Validador de data não passada
   */
  static notPastDate(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;

      const date = new Date(control.value);
      const today = new Date();
      today.setHours(0, 0, 0, 0);

      if (date < today) {
        return { pastDate: true };
      }

      return null;
    };
  }

  /**
   * Validador de idade mínima e máxima
   */
  static ageRange(minAge: number, maxAge: number): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;

      const birthDate = new Date(control.value);
      const today = new Date();
      
      let age = today.getFullYear() - birthDate.getFullYear();
      const monthDiff = today.getMonth() - birthDate.getMonth();
      
      if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
        age--;
      }

      if (age < minAge || age > maxAge) {
        return { ageRange: { min: minAge, max: maxAge } };
      }

      return null;
    };
  }

  /**
   * Validador de confirmação de senha
   */
  static passwordMatch(passwordField: string): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;

      const form = control.parent as FormGroup;
      if (!form) return null;

      const password = form.get(passwordField)?.value;
      if (password !== control.value) {
        return { passwordMatch: true };
      }

      return null;
    };
  }

  /**
   * Valida se o formulário deve ser submetido
   */
  static isFormValid(form: FormGroup): boolean {
    return form.valid && !form.pristine;
  }

  /**
   * Marca todos os campos como touched para mostrar erros
   */
  static markFormGroupTouched(form: FormGroup): void {
    Object.keys(form.controls).forEach(key => {
      const control = form.get(key);
      control?.markAsTouched();
      control?.markAsDirty();
    });
  }

  /**
   * Obtém todos os erros de um formulário formatados
   */
  static getFormErrors(form: FormGroup): Record<string, string> {
    const errors: Record<string, string> = {};

    Object.keys(form.controls).forEach(key => {
      const control = form.get(key);
      if (control && control.errors && control.touched) {
        errors[key] = ValidationService.getErrorMessage(key, control.errors);
      }
    });

    return errors;
  }
}

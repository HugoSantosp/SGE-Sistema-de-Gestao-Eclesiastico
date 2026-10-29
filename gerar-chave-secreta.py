#!/usr/bin/env python3
"""
Script para gerar chave secreta para Signed URLs
Uso: python gerar-chave-secreta.py
"""

import secrets
import os
from datetime import datetime

def main():
    # Gerar 64 bytes de aleatoriedade (512 bits)
    secret_key = secrets.token_hex(64)
    
    print("")
    print("🔐 Chave Secreta Gerada com Sucesso!")
    print("━" * 70)
    print("")
    print(secret_key)
    print("")
    print("━" * 70)
    print("")
    print("📋 Instruções:")
    print("1. Copie a chave acima (Ctrl+C)")
    print("2. Acesse: https://seu-dockploy.com")
    print("3. Settings → Environment Variables")
    print("4. Nome: APP_SIGNED_URL_SECRET")
    print("5. Valor: Cole a chave")
    print("6. Deploy (Redeploy)")
    print("")
    print("⚠️  IMPORTANTE:")
    print("   - NÃO compartilhe esta chave")
    print("   - NÃO coloque no GIT")
    print("   - Guarde em local seguro (Bitwarden, 1Password, etc)")
    print("")
    print(f"Gerado em: {datetime.now().strftime('%d/%m/%Y às %H:%M:%S')}")
    print("")
    
    # Tentar copiar para clipboard (Windows)
    try:
        if os.name == 'nt':  # Windows
            os.system(f'echo {secret_key} | clip')
            print("✅ Chave copiada para clipboard!")
    except:
        pass

if __name__ == "__main__":
    main()

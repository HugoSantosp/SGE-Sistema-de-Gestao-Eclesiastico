package com.sg.shared.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Serviço de email para envio de tokens de redefinição de senha.
 * 
 * Não há serviço de SMTP: o link é apenas registrado no log do servidor.
 * Em produção (Render), o link aparece nos logs do serviço.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${app.base-url:http://localhost:4200}")
    private String baseUrl;

    /**
     * Envia um email com o link de redefinição de senha.
     * Em desenvolvimento, apenas registra no log.
     */
    public void enviarLinkRedefinirSenha(String destinatario, String token) {
        String link = baseUrl + "/redefinir-senha?token=" + token;

        String assunto = "SGE - Redefinição de Senha";
        String corpo = """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="font-family: Arial, sans-serif; background: #f4f4f4; padding: 40px;">
                    <div style="max-width: 480px; margin: 0 auto; background: #fff; border-radius: 12px; padding: 32px; box-shadow: 0 4px 12px rgba(0,0,0,0.1);">
                        <div style="text-align: center; margin-bottom: 24px;">
                            <img src="%s/assets/logo-icert.png" alt="ICERT" style="height: 60px;">
                            <h2 style="color: #1a1a1a; margin-top: 12px;">Redefinição de Senha</h2>
                        </div>
                        <p style="color: #555; font-size: 15px; line-height: 1.6;">
                            Recebemos uma solicitação de redefinição de senha para sua conta no <strong>SGE - ICERT</strong>.
                        </p>
                        <p style="color: #555; font-size: 15px; line-height: 1.6;">
                            Clique no botão abaixo para criar uma nova senha. Este link expira em <strong>30 minutos</strong>.
                        </p>
                        <div style="text-align: center; margin: 32px 0;">
                            <a href="%s"
                               style="display: inline-block; background: #f97316; color: #fff; text-decoration: none; padding: 14px 40px; border-radius: 8px; font-size: 16px; font-weight: 600;">
                                Redefinir Senha
                            </a>
                        </div>
                        <p style="color: #999; font-size: 13px; line-height: 1.5;">
                            Se você não solicitou esta redefinição, ignore este email.
                            <br>Nenhuma alteração foi feita na sua conta.
                        </p>
                        <hr style="border: none; border-top: 1px solid #eee; margin: 24px 0;">
                        <p style="color: #bbb; font-size: 12px; text-align: center;">
                            SGE - Sistema de Gerenciamento Eclesiástico • ICERT
                        </p>
                    </div>
                </body>
                </html>
                """.formatted(baseUrl, link);

        log.info("============================================");
        log.info("📧 EMAIL PARA: {}", destinatario);
        log.info("📧 ASSUNTO: {}", assunto);
        log.info("📧 LINK: {}", link);
        log.info("============================================");

        // TODO: Em produção, usar JavaMailSender:
        // SimpleMailMessage message = new SimpleMailMessage();
        // message.setTo(destinatario);
        // message.setSubject(assunto);
        // message.setText(corpo);
        // mailSender.send(message);
    }
}

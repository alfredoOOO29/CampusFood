package com.campusfood.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String remitente;

    public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarCodigoVerificacion(String destinatario, String nombre, String codigo) {
        log.info("Iniciando proceso de envío de código de verificación a: {}", destinatario);

        // Si no hay remitente configurado o no hay mailSender, usamos simulación en consola para desarrollo
        if (mailSender == null || remitente == null || remitente.trim().isEmpty()) {
            mostrarCodigoEnConsola(destinatario, codigo, "Credenciales SMTP no configuradas (Modo Desarrollo/Simulación)");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject("Código de Verificación - CampusFood");

            String cuerpoHtml = construirPlantillaHtml(nombre, codigo);
            helper.setText(cuerpoHtml, true);

            mailSender.send(message);
            log.info("Correo con código enviado exitosamente a {}", destinatario);
        } catch (MessagingException | RuntimeException e) {
            log.error("No se pudo enviar el correo a {} vía SMTP. Razón: {}", destinatario, e.getMessage());
            // Fallback a consola para no interrumpir el registro del usuario en pruebas locales
            mostrarCodigoEnConsola(destinatario, codigo, "Fallo de conexión SMTP - Mostrando en consola de respaldo");
        }
    }

    private void mostrarCodigoEnConsola(String destinatario, String codigo, String motivo) {
        System.out.println("\n==================================================================");
        System.out.println("📬 CAMPUSFOOD - CÓDIGO DE VERIFICACIÓN (" + motivo + ")");
        System.out.println("Para: " + destinatario);
        System.out.println("Código: [" + codigo + "]");
        System.out.println("Vigencia: 15 minutos");
        System.out.println("==================================================================\n");
    }

    private String construirPlantillaHtml(String nombre, String codigo) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, Helvetica, Arial, sans-serif; background-color: #f8fafc; margin: 0; padding: 24px;'>" +
                "  <table align='center' border='0' cellpadding='0' cellspacing='0' width='100%' style='max-width: 520px; background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.06); border: 1px solid #e2e8f0;'>" +
                "    <tr>" +
                "      <td style='background: linear-gradient(135deg, #2563eb, #1d4ed8); padding: 32px 24px; text-align: center; color: #ffffff;'>" +
                "        <h1 style='margin: 0; font-size: 26px; font-weight: 800; letter-spacing: -0.5px;'>CampusFood 🎓🍔</h1>" +
                "        <p style='margin: 6px 0 0 0; font-size: 14px; opacity: 0.9;'>Plataforma Universitaria de Alimentos</p>" +
                "      </td>" +
                "    </tr>" +
                "    <tr>" +
                "      <td style='padding: 32px 28px; color: #334155;'>" +
                "        <h2 style='margin-top: 0; font-size: 20px; color: #0f172a;'>¡Hola, " + (nombre != null ? nombre : "Estudiante") + "!</h2>" +
                "        <p style='font-size: 15px; line-height: 1.6; color: #475569;'>Gracias por registrarte en CampusFood. Para activar tu cuenta y empezar a comprar o vender en el campus, ingresa el siguiente código de validación:</p>" +
                "        <div style='background-color: #f1f5f9; border: 2px dashed #94a3b8; border-radius: 12px; padding: 18px; text-align: center; margin: 28px 0;'>" +
                "          <span style='display: block; font-size: 12px; text-transform: uppercase; letter-spacing: 1.5px; color: #64748b; font-weight: 600; margin-bottom: 6px;'>Tu Código de Verificación</span>" +
                "          <span style='font-size: 34px; font-weight: 800; letter-spacing: 8px; color: #1e3a8a; font-family: monospace;'>" + codigo + "</span>" +
                "        </div>" +
                "        <p style='font-size: 13px; color: #64748b; line-height: 1.5;'>⏱️ Este código es válido durante los próximos <strong>15 minutos</strong>. Si tú no realizaste este registro, puedes desestimar este mensaje de forma segura.</p>" +
                "      </td>" +
                "    </tr>" +
                "    <tr>" +
                "      <td style='background-color: #f8fafc; padding: 18px 28px; text-align: center; border-top: 1px solid #e2e8f0; font-size: 12px; color: #94a3b8;'>" +
                "        CampusFood &copy; 2026 - Validación Segura Institucional UTP" +
                "      </td>" +
                "    </tr>" +
                "  </table>" +
                "</body>" +
                "</html>";
    }
}

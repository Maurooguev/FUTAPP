package com.futapp.service;

import com.futapp.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class PasswordResetMailService {
    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String publicUrl;
    private final String from;

    public PasswordResetMailService(ObjectProvider<JavaMailSender> mailSenders,
                                    @Value("${futapp.password-reset.mail-enabled:false}") boolean enabled,
                                    @Value("${futapp.public-url:http://localhost:5173}") String publicUrl,
                                    @Value("${futapp.mail.from:no-reply@futapp.local}") String from) {
        this.mailSender = mailSenders.getIfAvailable();
        this.enabled = enabled;
        this.publicUrl = publicUrl;
        this.from = from;
    }

    public void send(Usuario user, String rawToken) {
        if (!enabled) return;
        if (mailSender == null) throw new IllegalStateException("El servicio de correo no está configurado");
        String resetUrl = UriComponentsBuilder.fromUriString(publicUrl)
                .path("/reset-password")
                .queryParam("token", rawToken)
                .build()
                .encode()
                .toUriString();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(user.getEmail());
        message.setSubject("Restablecé tu contraseña de FUTAPP");
        message.setText("Hola " + user.getNombre() + ",\n\n"
                + "Recibimos una solicitud para restablecer tu contraseña. "
                + "Usá este enlace dentro de los próximos 30 minutos:\n\n"
                + resetUrl + "\n\n"
                + "Si no fuiste vos, ignorá este mensaje. Tu contraseña no cambiará.");
        mailSender.send(message);
    }
}

package com.futapp.service;

import com.futapp.dto.AuditoriaAdministrativaDTO;
import com.futapp.dto.PaginaDTO;
import com.futapp.model.AuditoriaAdministrativa;
import com.futapp.model.Usuario;
import com.futapp.repository.AuditoriaAdministrativaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditoriaAdministrativaService {
    private final AuditoriaAdministrativaRepository auditorias;

    public AuditoriaAdministrativaService(AuditoriaAdministrativaRepository auditorias) {
        this.auditorias = auditorias;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(Usuario usuario, String metodo, String ruta, int estadoHttp, String requestId) {
        AuditoriaAdministrativa auditoria = new AuditoriaAdministrativa();
        auditoria.setIdUsuario(usuario == null ? null : usuario.getIdUsuario());
        auditoria.setUsuario(usuario == null ? null : usuario.getNombre());
        auditoria.setMetodo(metodo);
        auditoria.setRuta(ruta.length() > 500 ? ruta.substring(0, 500) : ruta);
        auditoria.setEstadoHttp(estadoHttp);
        auditoria.setResultado(estadoHttp < 400 ? "EXITOSA" : "RECHAZADA");
        auditoria.setRequestId(requestId);
        auditoria.setFecha(LocalDateTime.now());
        auditorias.save(auditoria);
    }

    @Transactional(readOnly = true)
    public PaginaDTO<AuditoriaAdministrativaDTO> listar(int pagina, int cantidad) {
        int limite = Math.min(Math.max(cantidad, 1), 100);
        return PaginaDTO.desde(auditorias.findAllByOrderByFechaDesc(
                PageRequest.of(Math.max(pagina, 0), limite)).map(this::dto));
    }

    private AuditoriaAdministrativaDTO dto(AuditoriaAdministrativa item) {
        return new AuditoriaAdministrativaDTO(item.getIdAuditoria(), item.getIdUsuario(),
                item.getUsuario() == null ? "Sistema o usuario no autenticado" : item.getUsuario(), item.getMetodo(), item.getRuta(),
                item.getEstadoHttp(), item.getResultado(), item.getRequestId(), item.getFecha());
    }
}

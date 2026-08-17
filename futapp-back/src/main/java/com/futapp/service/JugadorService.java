package com.futapp.service;

import com.futapp.dto.JugadorRequestDTO;
import com.futapp.dto.JugadorResponseDTO;
import com.futapp.exception.ResourceNotFoundException;
import com.futapp.model.Equipo;
import com.futapp.model.Jugador;
import com.futapp.repository.EquipoRepository;
import com.futapp.repository.JugadorRepository;
import com.futapp.repository.GolRepository;
import com.futapp.repository.TarjetaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JugadorService {

    private final JugadorRepository jugadorRepository;
    private final EquipoRepository equipoRepository;
    private final GolRepository golRepository;
    private final TarjetaRepository tarjetaRepository;

    public JugadorService(JugadorRepository jugadorRepository, EquipoRepository equipoRepository,
                          GolRepository golRepository, TarjetaRepository tarjetaRepository) {
        this.jugadorRepository = jugadorRepository;
        this.equipoRepository = equipoRepository;
        this.golRepository = golRepository;
        this.tarjetaRepository = tarjetaRepository;
    }

    public List<JugadorResponseDTO> listarJugadores() {
        return jugadorRepository.findAll()
                .stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public JugadorResponseDTO obtenerJugadorPorId(Long id) {
        Jugador jugador = jugadorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado con id: " + id));

        return convertirAResponseDTO(jugador);
    }

    public List<JugadorResponseDTO> listarJugadoresPorEquipo(Long idEquipo) {
        return jugadorRepository.findByEquipoIdEquipo(idEquipo)
                .stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public JugadorResponseDTO crearJugador(JugadorRequestDTO requestDTO) {
        Equipo equipo = equipoRepository.findById(requestDTO.getIdEquipo())
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado con id: " + requestDTO.getIdEquipo()));

        validarDatosUnicos(requestDTO, null);

        Jugador jugador = new Jugador();
        jugador.setNombre(requestDTO.getNombre());
        jugador.setApellido(requestDTO.getApellido());
        jugador.setDni(normalizarOpcional(requestDTO.getDni()));
        jugador.setFechaNacimiento(requestDTO.getFechaNacimiento());
        jugador.setNumeroCamiseta(requestDTO.getNumeroCamiseta());
        jugador.setPosicion(requestDTO.getPosicion());
        jugador.setFoto(requestDTO.getFoto());
        jugador.setEstado(requestDTO.getEstado());
        jugador.setEquipo(equipo);

        Jugador jugadorGuardado = jugadorRepository.save(jugador);
        return convertirAResponseDTO(jugadorGuardado);
    }

    public JugadorResponseDTO actualizarJugador(Long id, JugadorRequestDTO requestDTO) {
        Jugador jugador = jugadorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado con id: " + id));

        Equipo equipo = equipoRepository.findById(requestDTO.getIdEquipo())
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado con id: " + requestDTO.getIdEquipo()));

        validarDatosUnicos(requestDTO, id);

        jugador.setNombre(requestDTO.getNombre());
        jugador.setApellido(requestDTO.getApellido());
        jugador.setDni(normalizarOpcional(requestDTO.getDni()));
        jugador.setFechaNacimiento(requestDTO.getFechaNacimiento());
        jugador.setNumeroCamiseta(requestDTO.getNumeroCamiseta());
        jugador.setPosicion(requestDTO.getPosicion());
        jugador.setFoto(requestDTO.getFoto());
        jugador.setEstado(requestDTO.getEstado());
        jugador.setEquipo(equipo);

        Jugador jugadorActualizado = jugadorRepository.save(jugador);
        return convertirAResponseDTO(jugadorActualizado);
    }

    public void eliminarJugador(Long id) {
        if (!jugadorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Jugador no encontrado con id: " + id);
        }

        if (golRepository.existsByJugadorIdJugador(id) || tarjetaRepository.existsByJugadorIdJugador(id)) {
            throw new IllegalArgumentException("No se puede eliminar el jugador porque tiene goles o tarjetas asociados");
        }

        jugadorRepository.deleteById(id);
    }

    private JugadorResponseDTO convertirAResponseDTO(Jugador jugador) {
        return new JugadorResponseDTO(
                jugador.getIdJugador(),
                jugador.getNombre(),
                jugador.getApellido(),
                jugador.getDni(),
                jugador.getFechaNacimiento(),
                jugador.getNumeroCamiseta(),
                jugador.getPosicion(),
                jugador.getFoto(),
                jugador.getEstado(),
                jugador.getEquipo().getIdEquipo(),
                jugador.getEquipo().getNombre()
        );
    }

    private String normalizarOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private void validarDatosUnicos(JugadorRequestDTO requestDTO, Long idJugador) {
        boolean camisetaRepetida = idJugador == null
                ? jugadorRepository.existsByEquipoIdEquipoAndNumeroCamiseta(requestDTO.getIdEquipo(), requestDTO.getNumeroCamiseta())
                : jugadorRepository.existsByEquipoIdEquipoAndNumeroCamisetaAndIdJugadorNot(requestDTO.getIdEquipo(), requestDTO.getNumeroCamiseta(), idJugador);
        if (camisetaRepetida) {
            throw new IllegalArgumentException("Ya existe un jugador con ese número de camiseta en el equipo");
        }

        String dni = normalizarOpcional(requestDTO.getDni());
        if (dni != null) {
            boolean dniRepetido = idJugador == null
                    ? jugadorRepository.existsByDni(dni)
                    : jugadorRepository.existsByDniAndIdJugadorNot(dni, idJugador);
            if (dniRepetido) {
                throw new IllegalArgumentException("Ya existe un jugador con ese DNI");
            }
        }
    }
}

package com.futapp.controller;
import com.futapp.dto.*; import com.futapp.service.ReclamoService; import org.springframework.web.bind.annotation.*; import java.util.List;
@RestController @RequestMapping("/reclamos")
public class ReclamoController {
 private final ReclamoService service; public ReclamoController(ReclamoService s){service=s;}
 @PostMapping("/equipos/{id}") public ReclamoResponseDTO equipo(@PathVariable Long id,@RequestBody ReclamoRequestDTO r){return service.reclamarEquipo(id,r);}
 @PostMapping("/jugadores/{id}") public ReclamoResponseDTO jugador(@PathVariable Long id,@RequestBody ReclamoRequestDTO r){return service.reclamarJugador(id,r);}
 @PostMapping("/equipos/{id}/fusionar/{codigo}") public ReclamoResponseDTO fusion(@PathVariable Long id,@PathVariable String codigo,@RequestBody ReclamoRequestDTO r){return service.solicitarFusion(id,codigo,r);}
 @GetMapping("/pendientes") public List<ReclamoResponseDTO> pendientes(){return service.pendientes();}
 @GetMapping("/revisables") public List<ReclamoResponseDTO> revisables(){return service.revisables();}
 @GetMapping("/pendientes/count") public java.util.Map<String,Long> cantidadPendientes(){return java.util.Map.of("cantidad",service.cantidadPendientes());}
 @GetMapping("/mios") public List<ReclamoResponseDTO> mios(){return service.mios();}
 @PutMapping("/{id}/aprobar") public ReclamoResponseDTO aprobar(@PathVariable Long id,@RequestBody(required=false) java.util.Map<String,String> body){return service.resolver(id,true,body==null?null:body.get("motivo"));}
 @PutMapping("/{id}/rechazar") public ReclamoResponseDTO rechazar(@PathVariable Long id,@RequestBody(required=false) java.util.Map<String,String> body){return service.resolver(id,false,body==null?null:body.get("motivo"));}
}

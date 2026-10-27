package com.sg.usuario;

import com.sg.usuario.dto.UsuarioRequestDTO;
import com.sg.usuario.dto.UsuarioResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuários", description = "CRUD de usuários do sistema")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Listar usuários")
    public ResponseEntity<List<UsuarioResponseDTO>> listar() {
        return ResponseEntity.ok(usuarioService.listarTodosDTO());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorIdDTO(id));
    }

    @PostMapping
    @Operation(summary = "Criar novo usuário")
    public ResponseEntity<UsuarioResponseDTO> criar(@RequestBody @Valid UsuarioRequestDTO dto) {
        Usuario usuario = usuarioService.dtoToEntity(dto);
        Usuario saved = usuarioService.salvar(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.entityToDTO(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuário")
    public ResponseEntity<UsuarioResponseDTO> atualizar(@PathVariable Long id, @RequestBody @Valid UsuarioRequestDTO dto) {
        Usuario usuario = usuarioService.dtoToEntity(dto);
        usuario.setId(id);
        Usuario updated = usuarioService.salvar(usuario);
        return ResponseEntity.ok(usuarioService.entityToDTO(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar usuário")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/resetar-senha")
    @Operation(summary = "Resetar senha para temporária",
               description = "Redefine a senha do usuário para '12345678' e marca como senha temporária (obriga troca no próximo login)")
    public ResponseEntity<Map<String, String>> resetarSenha(@PathVariable Long id) {
        usuarioService.resetarSenha(id);
        return ResponseEntity.ok(Map.of("message", "Senha resetada para temporária (12345678). Usuário deverá trocar no próximo acesso."));
    }
}

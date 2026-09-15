package br.com.campusgigs.controller;

import br.com.campusgigs.dto.PaginaResponse;
import br.com.campusgigs.dto.UsuarioResponse;
import br.com.campusgigs.service.UsuarioService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Area restrita ao papel ADMIN.
 *
 * A trava aparece em dois lugares de proposito: na cadeia de filtros
 * (/admin/** exige ROLE_ADMIN) e no @PreAuthorize do metodo - defesa em profundidade.
 * Um USER autenticado aqui recebe 403, e nao 401: ele e conhecido, so nao e autorizado.
 */
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UsuarioService usuarioService;

    public AdminController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/usuarios")
    public ResponseEntity<PaginaResponse<UsuarioResponse>> listarUsuarios(
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(PaginaResponse.de(
                usuarioService.listarTodos(pageable),
                UsuarioResponse::de));
    }
}

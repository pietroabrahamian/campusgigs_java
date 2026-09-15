package br.com.campusgigs.controller;

import br.com.campusgigs.domain.Usuario;
import br.com.campusgigs.dto.AtualizarCepRequest;
import br.com.campusgigs.dto.PaginaResponse;
import br.com.campusgigs.dto.ServicoResponse;
import br.com.campusgigs.dto.UsuarioResponse;
import br.com.campusgigs.security.UsuarioAutenticado;
import br.com.campusgigs.service.ServicoService;
import br.com.campusgigs.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final ServicoService servicoService;

    public UsuarioController(UsuarioService usuarioService, ServicoService servicoService) {
        this.usuarioService = usuarioService;
        this.servicoService = servicoService;
    }

    /** GET /usuarios/me - dados do dono do token. */
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> meuPerfil(@AuthenticationPrincipal UsuarioAutenticado autenticado) {
        Usuario usuario = usuarioService.buscarPorId(autenticado.getId());
        return ResponseEntity.ok(UsuarioResponse.de(usuario));
    }

    /**
     * PUT /usuarios/me/cep - atualiza o CEP e busca cidade/UF no servico externo.
     * Se o CEP nao existir, a resposta e 400 e nada e gravado.
     */
    @PutMapping("/me/cep")
    public ResponseEntity<UsuarioResponse> atualizarCep(@AuthenticationPrincipal UsuarioAutenticado autenticado,
                                                        @RequestBody @Valid AtualizarCepRequest request) {
        Usuario usuario = usuarioService.atualizarCep(autenticado.getId(), request.cep());
        return ResponseEntity.ok(UsuarioResponse.de(usuario));
    }

    /** GET /usuarios/me/servicos - os freelas publicados pelo usuario autenticado. */
    @GetMapping("/me/servicos")
    public ResponseEntity<PaginaResponse<ServicoResponse>> meusServicos(
            @AuthenticationPrincipal UsuarioAutenticado autenticado,
            @PageableDefault(size = 20, sort = "criadoEm", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(PaginaResponse.de(
                servicoService.listarDoPrestador(autenticado.getId(), pageable),
                ServicoResponse::de));
    }
}

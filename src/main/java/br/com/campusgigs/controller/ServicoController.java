package br.com.campusgigs.controller;

import br.com.campusgigs.domain.Servico;
import br.com.campusgigs.domain.SituacaoServico;
import br.com.campusgigs.dto.PaginaResponse;
import br.com.campusgigs.dto.ServicoRequest;
import br.com.campusgigs.dto.ServicoResponse;
import br.com.campusgigs.security.UsuarioAutenticado;
import br.com.campusgigs.service.ServicoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Freelas publicados.
 *
 * GET e publico (vitrine). Tudo que altera dados exige token, e a checagem de
 * dono/ADMIN acontece no ServicoService, que e quem conhece a regra.
 */
@RestController
@RequestMapping("/servicos")
public class ServicoController {

    private final ServicoService servicoService;

    public ServicoController(ServicoService servicoService) {
        this.servicoService = servicoService;
    }

    /** POST /servicos - qualquer usuario autenticado pode publicar. */
    @PostMapping
    public ResponseEntity<ServicoResponse> publicar(@AuthenticationPrincipal UsuarioAutenticado autenticado,
                                                    @RequestBody @Valid ServicoRequest request,
                                                    UriComponentsBuilder uriBuilder) {
        Servico servico = servicoService.publicar(autenticado.getId(), request);
        URI uri = uriBuilder.path("/servicos/{id}").buildAndExpand(servico.getId()).toUri();
        return ResponseEntity.created(uri).body(ServicoResponse.de(servico));
    }

    /** GET /servicos?situacao=ATIVO&categoria=Design&page=0&size=20 */
    @GetMapping
    public ResponseEntity<PaginaResponse<ServicoResponse>> listar(
            @RequestParam(required = false) SituacaoServico situacao,
            @RequestParam(required = false) String categoria,
            @PageableDefault(size = 20, sort = "criadoEm", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(PaginaResponse.de(
                servicoService.listar(situacao, categoria, pageable),
                ServicoResponse::de));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicoResponse> detalhar(@PathVariable Long id) {
        return ResponseEntity.ok(ServicoResponse.de(servicoService.buscarPorId(id)));
    }

    /** PUT /servicos/{id} - somente o dono do servico. */
    @PutMapping("/{id}")
    public ResponseEntity<ServicoResponse> atualizar(@PathVariable Long id,
                                                     @AuthenticationPrincipal UsuarioAutenticado autenticado,
                                                     @RequestBody @Valid ServicoRequest request) {
        return ResponseEntity.ok(ServicoResponse.de(
                servicoService.atualizar(id, autenticado.getId(), request)));
    }

    /** PATCH /servicos/{id}/pausar - somente o dono. */
    @PatchMapping("/{id}/pausar")
    public ResponseEntity<ServicoResponse> pausar(@PathVariable Long id,
                                                  @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return ResponseEntity.ok(ServicoResponse.de(servicoService.pausar(id, autenticado.getId())));
    }

    /** PATCH /servicos/{id}/reativar - somente o dono. */
    @PatchMapping("/{id}/reativar")
    public ResponseEntity<ServicoResponse> reativar(@PathVariable Long id,
                                                    @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return ResponseEntity.ok(ServicoResponse.de(servicoService.reativar(id, autenticado.getId())));
    }

    /** PATCH /servicos/{id}/encerrar - o dono ou um ADMIN. */
    @PatchMapping("/{id}/encerrar")
    public ResponseEntity<ServicoResponse> encerrar(@PathVariable Long id,
                                                    @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return ResponseEntity.ok(ServicoResponse.de(servicoService.encerrar(id, autenticado.getId())));
    }
}

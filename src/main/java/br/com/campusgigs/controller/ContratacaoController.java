package br.com.campusgigs.controller;

import br.com.campusgigs.domain.Contratacao;
import br.com.campusgigs.dto.ContratacaoRequest;
import br.com.campusgigs.dto.ContratacaoResponse;
import br.com.campusgigs.dto.PaginaResponse;
import br.com.campusgigs.security.UsuarioAutenticado;
import br.com.campusgigs.service.ContratacaoService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/** Contratacoes. Todos os endpoints exigem token. */
@RestController
@RequestMapping("/contratacoes")
public class ContratacaoController {

    private final ContratacaoService contratacaoService;

    public ContratacaoController(ContratacaoService contratacaoService) {
        this.contratacaoService = contratacaoService;
    }

    /** POST /contratacoes - contrata um servico ATIVO de outro aluno. */
    @PostMapping
    public ResponseEntity<ContratacaoResponse> contratar(@AuthenticationPrincipal UsuarioAutenticado autenticado,
                                                         @RequestBody @Valid ContratacaoRequest request,
                                                         UriComponentsBuilder uriBuilder) {
        Contratacao contratacao = contratacaoService.contratar(request.servicoId(), autenticado.getId());
        URI uri = uriBuilder.path("/contratacoes/{id}").buildAndExpand(contratacao.getId()).toUri();
        return ResponseEntity.created(uri).body(ContratacaoResponse.de(contratacao));
    }

    /** GET /contratacoes/minhas - como contratante ou como prestador. */
    @GetMapping("/minhas")
    public ResponseEntity<PaginaResponse<ContratacaoResponse>> minhas(
            @AuthenticationPrincipal UsuarioAutenticado autenticado,
            @PageableDefault(size = 20, sort = "criadoEm", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(PaginaResponse.de(
                contratacaoService.listarDoUsuario(autenticado.getId(), pageable),
                ContratacaoResponse::de));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContratacaoResponse> detalhar(@PathVariable Long id,
                                                        @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return ResponseEntity.ok(ContratacaoResponse.de(
                contratacaoService.buscarVisivelPara(id, autenticado.getId())));
    }

    /** PATCH /contratacoes/{id}/aceitar - somente o prestador do servico. */
    @PatchMapping("/{id}/aceitar")
    public ResponseEntity<ContratacaoResponse> aceitar(@PathVariable Long id,
                                                       @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return ResponseEntity.ok(ContratacaoResponse.de(
                contratacaoService.aceitar(id, autenticado.getId())));
    }

    /** PATCH /contratacoes/{id}/concluir - somente o prestador do servico. */
    @PatchMapping("/{id}/concluir")
    public ResponseEntity<ContratacaoResponse> concluir(@PathVariable Long id,
                                                        @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return ResponseEntity.ok(ContratacaoResponse.de(
                contratacaoService.concluir(id, autenticado.getId())));
    }

    /** PATCH /contratacoes/{id}/cancelar - contratante, prestador ou ADMIN. */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ContratacaoResponse> cancelar(@PathVariable Long id,
                                                        @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return ResponseEntity.ok(ContratacaoResponse.de(
                contratacaoService.cancelar(id, autenticado.getId())));
    }
}

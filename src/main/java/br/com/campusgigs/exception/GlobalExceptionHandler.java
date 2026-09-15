package br.com.campusgigs.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Ponto unico de tratamento de erros da API.
 *
 * Toda violacao - validacao, autorizacao ou regra de negocio - sai daqui no
 * formato RFC 7807 (ProblemDetail), sem stack trace e sem detalhe interno.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------------------------------------------------------------- 400

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail tratarValidacao(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> campos = new TreeMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.put(erro.getField(), erro.getDefaultMessage()));
        ex.getBindingResult().getGlobalErrors()
                .forEach(erro -> campos.put(erro.getObjectName(), erro.getDefaultMessage()));

        ProblemDetail problema = montar(HttpStatus.BAD_REQUEST, "Dados invalidos",
                "Um ou mais campos nao passaram na validacao.", req);
        problema.setProperty("campos", campos);
        return problema;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ProblemDetail tratarCorpoInvalido(Exception ex, HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST, "Requisicao malformada",
                "Nao foi possivel interpretar os dados enviados. Confira o corpo e os parametros da requisicao.", req);
    }

    @ExceptionHandler(CepNaoEncontradoException.class)
    public ProblemDetail tratarCep(CepNaoEncontradoException ex, HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST, "CEP invalido", ex.getMessage(), req);
    }

    // ---------------------------------------------------------------- 401

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail tratarCredenciaisInvalidas(BadCredentialsException ex, HttpServletRequest req) {
        return montar(HttpStatus.UNAUTHORIZED, "Credenciais invalidas",
                "E-mail ou senha incorretos.", req);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail tratarAutenticacao(AuthenticationException ex, HttpServletRequest req) {
        return montar(HttpStatus.UNAUTHORIZED, "Nao autenticado",
                "Autentique-se em POST /auth/login e envie o token no header Authorization.", req);
    }

    // ---------------------------------------------------------------- 403

    @ExceptionHandler(OperacaoNaoPermitidaException.class)
    public ProblemDetail tratarOperacaoNaoPermitida(OperacaoNaoPermitidaException ex, HttpServletRequest req) {
        return montar(HttpStatus.FORBIDDEN, "Acesso negado", ex.getMessage(), req);
    }

    /** Disparada pelo @PreAuthorize quando o papel do usuario nao cobre a operacao. */
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail tratarAcessoNegado(AccessDeniedException ex, HttpServletRequest req) {
        return montar(HttpStatus.FORBIDDEN, "Acesso negado",
                "Seu papel nao permite executar esta operacao.", req);
    }

    // ---------------------------------------------------------------- 404

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail tratarNaoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest req) {
        return montar(HttpStatus.NOT_FOUND, "Recurso nao encontrado", ex.getMessage(), req);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ProblemDetail tratarRotaInexistente(NoHandlerFoundException ex, HttpServletRequest req) {
        return montar(HttpStatus.NOT_FOUND, "Rota nao encontrada",
                "Nao existe endpoint para este caminho.", req);
    }

    // ---------------------------------------------------------------- 409

    @ExceptionHandler(RegraDeNegocioException.class)
    public ProblemDetail tratarRegraDeNegocio(RegraDeNegocioException ex, HttpServletRequest req) {
        return montar(HttpStatus.CONFLICT, "Regra de negocio violada", ex.getMessage(), req);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail tratarIntegridade(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("Violacao de integridade em {}: {}", req.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return montar(HttpStatus.CONFLICT, "Conflito de dados",
                "A operacao conflita com um registro existente.", req);
    }

    // ---------------------------------------------------------------- 503

    @ExceptionHandler(ServicoExternoIndisponivelException.class)
    public ProblemDetail tratarServicoExterno(ServicoExternoIndisponivelException ex, HttpServletRequest req) {
        log.error("Falha ao consultar servico externo: {}", ex.getMessage());
        return montar(HttpStatus.SERVICE_UNAVAILABLE, "Servico externo indisponivel", ex.getMessage(), req);
    }

    // ---------------------------------------------------------------- 500

    @ExceptionHandler(Exception.class)
    public ProblemDetail tratarErroInesperado(Exception ex, HttpServletRequest req) {
        // O detalhe fica no log do servidor; o cliente recebe apenas a mensagem generica.
        log.error("Erro inesperado em {} {}", req.getMethod(), req.getRequestURI(), ex);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado ao processar a requisicao.", req);
    }

    // ---------------------------------------------------------------- apoio

    private ProblemDetail montar(HttpStatus status, String titulo, String detalhe, HttpServletRequest req) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        problema.setType(URI.create("https://campusgigs.com/erros/" + status.value()));
        problema.setProperty("timestamp", OffsetDateTime.now().toString());
        problema.setProperty("caminho", req.getRequestURI());
        return problema;
    }

    /** Usado tambem pelos handlers da camada de seguranca (401/403 no filtro). */
    public static Map<String, Object> corpoDeErro(HttpStatus status, String titulo, String detalhe, String caminho) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("type", "https://campusgigs.com/erros/" + status.value());
        corpo.put("title", titulo);
        corpo.put("status", status.value());
        corpo.put("detail", detalhe);
        corpo.put("timestamp", OffsetDateTime.now().toString());
        corpo.put("caminho", caminho);
        return corpo;
    }
}

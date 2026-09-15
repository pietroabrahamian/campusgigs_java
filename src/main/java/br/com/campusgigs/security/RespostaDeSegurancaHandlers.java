package br.com.campusgigs.security;

import br.com.campusgigs.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Respostas 401 e 403 geradas dentro da cadeia de filtros (antes do controller),
 * no mesmo formato JSON do GlobalExceptionHandler.
 */
@Component
public class RespostaDeSegurancaHandlers {

    private final ObjectMapper objectMapper;

    public RespostaDeSegurancaHandlers(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** 401 - nao autenticado: sem token, token invalido ou expirado. */
    public AuthenticationEntryPoint entryPoint() {
        return (request, response, authException) -> {
            String motivo = (String) request.getAttribute(JwtAuthenticationFilter.ATRIBUTO_ERRO);
            if (motivo == null) {
                motivo = "Esta operacao exige autenticacao. Envie o header Authorization: Bearer <token>.";
            }
            escrever(request, response, HttpStatus.UNAUTHORIZED, "Nao autenticado", motivo);
        };
    }

    /** 403 - autenticado, mas o papel nao cobre a operacao. */
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, ex) ->
                escrever(request, response, HttpStatus.FORBIDDEN, "Acesso negado",
                        "Seu papel nao permite executar esta operacao.");
    }

    private void escrever(HttpServletRequest request, HttpServletResponse response,
                          HttpStatus status, String titulo, String detalhe) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(),
                GlobalExceptionHandler.corpoDeErro(status, titulo, detalhe, request.getRequestURI()));
    }
}

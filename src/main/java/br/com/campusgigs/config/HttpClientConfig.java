package br.com.campusgigs.config;

import br.com.campusgigs.client.ViaCepClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;

/**
 * Monta o proxy da interface @HttpExchange sobre um RestClient.
 *
 * Os timeouts sao explicitos de proposito: sem eles, uma lentidao do ViaCEP
 * seguraria a thread do nosso request ate o limite do container.
 */
@Configuration
public class HttpClientConfig {

    @Bean
    public ViaCepClient viaCepClient(RestClient.Builder builder,
                                     @Value("${campusgigs.viacep.url}") String url,
                                     @Value("${campusgigs.viacep.connect-timeout-segundos}") long connectTimeout,
                                     @Value("${campusgigs.viacep.read-timeout-segundos}") long readTimeout) {

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(connectTimeout));
        requestFactory.setReadTimeout(Duration.ofSeconds(readTimeout));

        RestClient restClient = builder
                .baseUrl(url)
                .requestFactory(requestFactory)
                .build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(ViaCepClient.class);
    }
}

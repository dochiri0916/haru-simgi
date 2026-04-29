package com.dochiri.security.internalapi;

import com.dochiri.errorhandling.BaseException;
import com.dochiri.errorhandling.ErrorCode;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Optional;
import java.util.function.Consumer;

public class InternalRestClient {

    private final RestClient restClient;
    private final LoadBalancerClient loadBalancerClient;
    private final String internalApiToken;

    public InternalRestClient(LoadBalancerClient loadBalancerClient, InternalApiClientProperties properties) {
        this.restClient = RestClient.builder().build();
        this.loadBalancerClient = loadBalancerClient;
        this.internalApiToken = properties.token();
    }

    public <Req, Res> Res exchange(InternalRpcRequest<Req, Res> request) {
        ServiceInstance instance = loadBalancerClient.choose(request.serviceName());
        if (instance == null) {
            throw new BaseException(request.unavailableErrorCode());
        }

        try {
            RestClient.RequestBodySpec spec = restClient.method(request.method())
                    .uri(instance.getUri() + request.path())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(InternalApiTokenAuthenticationFilter.INTERNAL_API_TOKEN_HEADER, internalApiToken);

            if (request.headerCustomizer() != null) {
                request.headerCustomizer().accept(spec);
            }

            if (request.body() != null) {
                spec.body(request.body());
            }

            return spec.retrieve().body(request.responseType());
        } catch (RestClientException exception) {
            throw new BaseException(request.unavailableErrorCode(), exception);
        }
    }

    public <Res> Optional<Res> tryExchange(InternalRpcRequest<?, Res> request) {
        ServiceInstance instance = loadBalancerClient.choose(request.serviceName());
        if (instance == null) {
            return Optional.empty();
        }

        try {
            RestClient.RequestBodySpec spec = restClient.method(request.method())
                    .uri(instance.getUri() + request.path())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(InternalApiTokenAuthenticationFilter.INTERNAL_API_TOKEN_HEADER, internalApiToken);

            if (request.headerCustomizer() != null) {
                request.headerCustomizer().accept(spec);
            }

            if (request.body() != null) {
                spec.body(request.body());
            }

            return Optional.ofNullable(spec.retrieve().body(request.responseType()));
        } catch (RestClientResponseException exception) {
            return Optional.empty();
        } catch (RestClientException exception) {
            return Optional.empty();
        }
    }

    public record InternalRpcRequest<Req, Res>(
            String serviceName,
            String path,
            HttpMethod method,
            Req body,
            Class<Res> responseType,
            ErrorCode unavailableErrorCode,
            Consumer<RestClient.RequestBodySpec> headerCustomizer
    ) {

        public static <Req, Res> InternalRpcRequest<Req, Res> post(
                String serviceName,
                String path,
                Req body,
                Class<Res> responseType,
                ErrorCode unavailableErrorCode
        ) {
            return new InternalRpcRequest<>(serviceName, path, HttpMethod.POST, body, responseType, unavailableErrorCode, null);
        }

        public static <Req, Res> InternalRpcRequest<Req, Res> patch(
                String serviceName,
                String path,
                Req body,
                Class<Res> responseType,
                ErrorCode unavailableErrorCode
        ) {
            return new InternalRpcRequest<>(serviceName, path, HttpMethod.PATCH, body, responseType, unavailableErrorCode, null);
        }

        public InternalRpcRequest<Req, Res> withHeaders(Consumer<RestClient.RequestBodySpec> customizer) {
            return new InternalRpcRequest<>(serviceName, path, method, body, responseType, unavailableErrorCode, customizer);
        }
    }
}

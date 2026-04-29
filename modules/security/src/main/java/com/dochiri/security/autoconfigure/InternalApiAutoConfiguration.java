package com.dochiri.security.autoconfigure;

import com.dochiri.security.internalapi.InternalApiClientProperties;
import com.dochiri.security.internalapi.InternalApiServerProperties;
import com.dochiri.security.internalapi.InternalApiTokenAuthenticationFilter;
import com.dochiri.security.internalapi.InternalRestClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class InternalApiAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "internal-api.server", name = "token")
    @EnableConfigurationProperties(InternalApiServerProperties.class)
    static class ServerConfiguration {

        @Bean
        InternalApiTokenAuthenticationFilter internalApiTokenAuthenticationFilter(
                InternalApiServerProperties internalApiServerProperties
        ) {
            return new InternalApiTokenAuthenticationFilter(internalApiServerProperties);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "internal-api.client", name = "token")
    @EnableConfigurationProperties(InternalApiClientProperties.class)
    static class ClientConfiguration {

        @Bean
        InternalRestClient internalRestClient(
                LoadBalancerClient loadBalancerClient,
                InternalApiClientProperties internalApiClientProperties
        ) {
            return new InternalRestClient(loadBalancerClient, internalApiClientProperties);
        }
    }
}

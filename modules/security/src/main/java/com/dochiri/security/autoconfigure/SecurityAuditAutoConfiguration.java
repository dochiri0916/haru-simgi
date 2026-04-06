package com.dochiri.security.autoconfigure;

import com.dochiri.security.audit.SecurityAuditorAware;
import com.dochiri.security.properties.SecurityProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "org.springframework.data.domain.AuditorAware")
class SecurityAuditAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(type = "org.springframework.data.domain.AuditorAware")
    AuditorAware<Long> auditorAware(SecurityProperties securityProperties) {
        return new SecurityAuditorAware(securityProperties.systemUserId());
    }

}



package com.example.keyvalue.config;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.adapters.springboot.KeycloakBaseSpringBootConfiguration;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.adapters.springsecurity.AdapterDeploymentContextFactoryBean;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticatedActionsFilter;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticationProcessingFilter;
import org.keycloak.adapters.springsecurity.filter.KeycloakPreAuthActionsFilter;
import org.keycloak.adapters.springsecurity.filter.KeycloakSecurityContextRequestFilter;
import org.keycloak.adapters.springsecurity.management.HttpSessionManager;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.RegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;


/**
 * Keycloak authentication integration for Spring Boot 2
 */

//@Configuration
//@EnableWebSecurity

//@Configuration
//@KeycloakConfiguration
//@ConditionalOnProperty(value = "keycloak.enabled", matchIfMissing = true)
//@EnableConfigurationProperties(KeycloakSpringBootProperties.class)

@KeycloakConfiguration
@EnableConfigurationProperties(KeycloakSpringBootProperties.class)
public class KeycloakAutoConfiguration extends KeycloakWebSecurityConfigurerAdapter {


    /**
     * Registers the KeycloakAuthenticationProvider with the authentication manager.
     */
//    @Autowired
//    public void configureGlobal(AuthenticationManagerBuilder auth) {
//        KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
//        keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
//        auth.authenticationProvider(keycloakAuthenticationProvider);
//    }

    @Bean
    public GrantedAuthoritiesMapper grantedAuthoritiesMapper() {
        SimpleAuthorityMapper mapper = new SimpleAuthorityMapper();
        mapper.setConvertToUpperCase(true);
        return mapper;
    }

    @Override
    protected KeycloakAuthenticationProvider keycloakAuthenticationProvider() {
        final KeycloakAuthenticationProvider provider = super.keycloakAuthenticationProvider();
        provider.setGrantedAuthoritiesMapper(grantedAuthoritiesMapper());
        return provider;
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(keycloakAuthenticationProvider());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
//        http.cors().and().csrf().disable()
//                .authorizeRequests()
//                .antMatchers("/user/**").hasRole("USER")
//                .antMatchers("/movies/**").hasRole("USER")
//                .antMatchers("/keycloak/**").permitAll()
//                .anyRequest().permitAll();

        http.cors()
                .and()
                .csrf()
                .disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .sessionAuthenticationStrategy(sessionAuthenticationStrategy())
                .and()
                .authorizeRequests()
                .antMatchers("/user/**").hasRole("USER")
                .antMatchers("/movies/**").hasRole("USER")
                .antMatchers("/keycloak/**").permitAll()
                .anyRequest().denyAll();

    }


    @Bean
    public KeycloakDeployment keycloakDeployment() {
        return new KeycloakDeployment();
    }

    /**
     * Load Keycloak configuration from application.properties or application.yml, rather than keycloak.json.
     */
    //Keycloak Spring Security integration resolves the keycloak configuration
    @Bean
    @Primary
    public KeycloakConfigResolver keycloakConfigResolver() {
        return new KeycloakSpringBootConfigResolver();
    }

    @Bean
    public RegistrationBean keycloakAuthenticationProcessingFilterRegistrationBean(KeycloakAuthenticationProcessingFilter filter) {
        RegistrationBean registrationBean = new FilterRegistrationBean((Filter) filter);
        registrationBean.setEnabled(false);
        return registrationBean;
    }

    @Bean
    public RegistrationBean keycloakPreAuthActionsFilterRegistrationBean(KeycloakPreAuthActionsFilter filter) {
        RegistrationBean registrationBean = new FilterRegistrationBean(filter);
        registrationBean.setEnabled(false);
        return registrationBean;
    }

    @Bean
    @Override
    protected KeycloakAuthenticatedActionsFilter keycloakAuthenticatedActionsRequestFilter() {
        return super.keycloakAuthenticatedActionsRequestFilter();
    }

    //
//    @Bean
//    public RegistrationBean keycloakAuthenticatedActionsFilterBean(KeycloakAuthenticatedActionsFilter filter) {
//        RegistrationBean registrationBean = new FilterRegistrationBean(filter);
//        registrationBean.setEnabled(false);
//        return registrationBean;
//    }

//    @Bean
//    public RegistrationBean keycloakSecurityContextRequestFilterBean(KeycloakSecurityContextRequestFilter filter) {
//        RegistrationBean registrationBean = new FilterRegistrationBean(filter);
//        registrationBean.setEnabled(false);
//        return registrationBean;
//    }

    /**
     * Defines the session authentication strategy.
     */
    //stateless REST service we do not really want to have sessions, therefore we utilize the NullAuthenticatedSessionStrategy
    @Bean
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new NullAuthenticatedSessionStrategy();
    }

    @Bean
    @Override
    @ConditionalOnMissingBean(HttpSessionManager.class)
    protected HttpSessionManager httpSessionManager() {
        return new HttpSessionManager();
    }

    /**
     * In order to make {@code BookControllerTest} works, the static class {@link CustomKeycloakBaseSpringBootConfiguration}
     * and the annotation {@link EnableConfigurationProperties} were added here.
     * <p>
     * Without them, a NullPointerException will be throw in
     * {@link org.keycloak.adapters.KeycloakDeploymentBuilder#(AdapterConfig)}, because the
     * {@code adapterConfig} parameter is null
     */
    @Configuration
    static class CustomKeycloakBaseSpringBootConfiguration extends KeycloakBaseSpringBootConfiguration {
    }

//    @Bean
//    public ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
//        return new ServletListenerRegistrationBean<HttpSessionEventPublisher>(new HttpSessionEventPublisher());
//    }

//    @Autowired
//    private ApplicationContext environment;

//    @Bean
//    @ConditionalOnProperty(name="authentication", havingValue="keycloak")
//    public AdapterDeploymentContext adapterDeploymentContext() throws Exception {
//        AdapterConfig cfg = new AdapterConfig();
//        cfg.setRealm(environment.getEnvironment().getProperty("keycloak.realm"));
//        cfg.setAuthServerUrl(environment.getEnvironment().getProperty("keycloak.auth-server-url"));
//        cfg.setResource(environment.getEnvironment().getProperty("keycloak.resource"));
//        Map<String,Object> credentials = new HashMap<>();
//        credentials.put("secret", environment.getEnvironment().getProperty("keycloak.credentials-secret"));
//        cfg.setCredentials(credentials);
//
//
//        KeycloakDeployment dep;
//         dep = KeycloakDeploymentBuilder.build(cfg);
//        AdapterDeploymentContextFactoryBean factoryBean = new AdapterDeploymentContextFactoryBean(new KeycloakConfigResolver() {
//            @Override
//            public KeycloakDeployment resolve(HttpFacade.Request facade) {
//                return dep;
//            }
//
//        });
//        factoryBean.afterPropertiesSet();
//        return factoryBean.getObject();
//    }

}



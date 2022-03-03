package fi.hiq.reference.spring_boot_reference.config;

import fi.hiq.reference.spring_boot_reference.security.JwtAuthenticationEntryPoint;
import fi.hiq.reference.spring_boot_reference.security.JwtRequestFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.annotation.Resource;

// <#SECURITY>
@Configuration
@EnableWebSecurity
@Order(100)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
  @Resource
  private JwtRequestFilter jwtRequestFilter;
  @Resource
  private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        // HTTPS is required everywhere
        .requiresChannel(channel -> channel.anyRequest().requiresSecure())
        // Customize authorization rules
        .authorizeRequests()
        // Define paths that can be accessed without authorization / authentication
        .antMatchers("/authenticate").permitAll()
        // Any other path needs authentication
        .anyRequest().authenticated()
        .and()
        // Example for CSRF protection. Use CSRF token repository that adds CSRF token to cookie
        //.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
        // For this example disable CSRF protection
        .csrf(csrf -> csrf.disable())
        // Enable cors
        .cors()
        .and()
        // Set entrypoint that returns status 401 if JWT is invalid
        .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)
        .and()
        // Don't store session state
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

    // Add JWT request filter
    http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
  }

}

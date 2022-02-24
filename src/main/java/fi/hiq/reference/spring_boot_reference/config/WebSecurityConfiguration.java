package fi.hiq.reference.spring_boot_reference.config;

import fi.hiq.reference.spring_boot_reference.security.UserDetailServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;

// <#SECURITY>
@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

  @Resource
  private UserDetailServiceImpl userDetailsService;
  @Value("${referencecomponent.ldap.user-dn-pattern}")
  private String userDnPattern;
  @Value("${referencecomponent.ldap.group-search-base}")
  private String groupSearchBase;
  @Value("${referencecomponent.ldap.url}")
  private String ldapUrl;
  @Value("${referencecomponent.ldap.password-attribute}")
  private String ldapPasswordAttribute;

  // Define PasswordEncoder that will be used for encrypting passwords.
  // Use secure algorithm, such as bcrypt.
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.$2Y);
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    // <#SELF-MANAGED-CREDENTIALS>
    // Configure local credentials
    auth
        .userDetailsService(userDetailsService)
        .passwordEncoder(passwordEncoder());

    // <#LDAP>
    // Configure LDAP authentication
    auth
        .ldapAuthentication()
        .userDnPatterns(userDnPattern)
        .groupSearchBase(groupSearchBase)
        .contextSource()
        .url(ldapUrl)
        .and()
        .passwordCompare()
        .passwordEncoder(passwordEncoder())
        .passwordAttribute(ldapPasswordAttribute);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        // HTTPS is required everywhere
        .requiresChannel(channel -> channel.anyRequest().requiresSecure())
        // Customize authorization rules
        .authorizeRequests()
        // Define paths that can be accessed without authorization / authentication
        .antMatchers("/favicon.ico").permitAll()
        // Any other path needs authentication
        .anyRequest().authenticated()
        .and()
        // Let Spring generate login page
        .formLogin()
        // Redirect user to this URL after successful login
        .defaultSuccessUrl("/", true)
        .and()
        // Use basic auth
        .httpBasic() // TODO: Better authentication method
        .and()
        // Example for CSRF protection. Use CSRF token repository that adds CSRF token to cookie
        //.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
        // For this example disable CSRF protection
        .csrf(csrf -> csrf.disable())
        // Enable cors
        .cors()
        .and()
        // Add logout support, let Spring generate logout endpoint
        .logout()
        // After logout redirect user to this URL
        .logoutSuccessUrl("/");
  }
}

package fi.hiq.reference.spring_boot_reference.config;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import fi.hiq.reference.spring_boot_reference.security.UserDetailServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;

// <#SECURITY>
@Configuration
@EnableWebSecurity
@Order(101)
public class AuthenticationConfiguration extends WebSecurityConfigurerAdapter {
  @Resource
  private UserDetailServiceImpl userDetailsService;
  @Resource
  private InMemoryDirectoryServer ldapServer;
  @Value("${referencecomponent.ldap.user-dn-pattern}")
  private String userDnPattern;
  @Value("${referencecomponent.ldap.group-search-base}")
  private String groupSearchBase;
  @Value("${referencecomponent.ldap.url}")
  private String ldapUrl;
  @Value("${referencecomponent.ldap.password-attribute}")
  private String ldapPasswordAttribute;

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
        //.userDetailsContextMapper(ldapUserDetailsMapper())
        .userDnPatterns(userDnPattern)
        .groupSearchBase(groupSearchBase)
        .contextSource()
        // Embedded LDAP server uses random port, so replace the port placeholder with correct port
        .url(ldapUrl.replace("{port}", String.valueOf(ldapServer.getListenPort())))
        .and()
        .passwordCompare()
        .passwordEncoder(passwordEncoder())
        .passwordAttribute(ldapPasswordAttribute);
  }

  // Define PasswordEncoder that will be used for encrypting passwords.
  // Use secure algorithm, such as bcrypt.
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.$2Y);
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }
}

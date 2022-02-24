package fi.hiq.reference.spring_boot_reference.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

// <#SECURITY> <#SELF-MANAGED-CREDENTIALS>
// UserDetailsService is necessary if you need to load users from local storage (e.g. credentials file)
@Component
public class UserDetailServiceImpl implements UserDetailsService {

  // Resource pointing to local credentials file
  @Value("classpath:local-credentials/local-credentials.yaml")
  private Resource localCredentialsResource;

  private Map<String, Credential> credentialsMap = new HashMap<>();

  // When server starts load credentials in from credentials file
  @PostConstruct
  private void loadCredentials() throws IOException {
    // Use Jackson for mapping YAML into Credential list
    ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
    List<Credential> credentials = Arrays.asList(objectMapper.readValue(localCredentialsResource.getFile(), Credential[].class));

    // Check that credentials does not have multiple users with same username
    long uniqueUsernames = credentials.stream()
        .map(Credential::getUsername)
        .distinct()
        .count();
    if (uniqueUsernames != credentials.size()) {
      throw new IllegalArgumentException("Local credential file contains multiple users with the same username");
    } else {
      // Map credentials by username
      credentialsMap = credentials.stream()
          .collect(Collectors.toMap(Credential::getUsername, Function.identity()));
    }
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Credential credential = credentialsMap.get(username);

    if (credential == null) {
      throw new UsernameNotFoundException("user not found");
    } else {
      // Map credential roles to GrantedAuthorities
      Set<SimpleGrantedAuthority> authorities = credential.getRoles().stream()
          .map(SimpleGrantedAuthority::new)
          .collect(Collectors.toSet());

      return new User(credential.getUsername(), credential.getPassword(), authorities);
    }
  }

  // Wrapper class for loading in local credential
  @Getter
  private static class Credential {
    private final String username;
    private final String password;
    private final List<String> roles;

    @JsonCreator
    public Credential(@JsonProperty("username") String username,
                      @JsonProperty("password") String password,
                      @JsonProperty("roles") List<String> roles) {
      this.username = username;
      this.password = password;
      this.roles = roles;
    }
  }

}

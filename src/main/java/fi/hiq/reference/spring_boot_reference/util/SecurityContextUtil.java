package fi.hiq.reference.spring_boot_reference.util;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

@UtilityClass
public class SecurityContextUtil {

  public static String getUsernameOfRequestClient() {
    return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
        .map(Authentication::getPrincipal)
        .filter(principalObject -> principalObject instanceof UserDetails)
        .map(principalObject -> (UserDetails) principalObject)
        .map(UserDetails::getUsername)
        .orElse(null);
  }
}

package fi.hiq.reference.spring_boot_reference.controller;

import fi.hiq.reference.spring_boot_reference.security.JwtService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class AuthenticationController {
  @Resource
  private AuthenticationManager authenticationManager;
  @Resource
  private JwtService jwtService;

  @PostMapping("/authenticate")
  public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest request) {
    Authentication authentication;
    try {
      authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
    } catch (AuthenticationException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    Object principal = authentication.getPrincipal();
    if (principal instanceof UserDetails) {
      return ResponseEntity.ok(jwtService.generateToken((UserDetails) principal, request.getPassword()));
    } else {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }

  @Getter
  @AllArgsConstructor
  private static class JwtRequest {
    private final String username;
    private final String password;
  }
}

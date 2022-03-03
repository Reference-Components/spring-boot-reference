package fi.hiq.reference.spring_boot_reference.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
  @Resource
  private AuthenticationManager authenticationManager;
  @Resource
  private JwtService jwtService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    final String requestTokenHeader = request.getHeader("Authorization");

    String username = null;
    String password = null;
    String jwtToken = null;
    if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
      jwtToken = requestTokenHeader.substring(7);
      username = jwtService.getUsernameFromToken(jwtToken);
      password = jwtService.getPasswordFromToken(jwtToken);
    }

    if (StringUtils.hasLength(username)
        && StringUtils.hasLength(password)
        && SecurityContextHolder.getContext().getAuthentication() == null) {

      UserDetails userDetails = null;
      Object principal = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password)).getPrincipal();
      if (principal instanceof UserDetails) {
        userDetails = (UserDetails) principal;
      }

      if (userDetails != null && jwtService.isTokenValid(jwtToken, userDetails)) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());

        token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(token);
      }
    }

    chain.doFilter(request, response);
  }
}

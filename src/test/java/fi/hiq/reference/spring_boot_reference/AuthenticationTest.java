package fi.hiq.reference.spring_boot_reference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.annotation.Resource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AuthenticationTest {
  @Resource
  private MockMvc mockMvc;

  @LocalServerPort
  private int port;

  private String serverUrl;

  @BeforeEach
  void init() {
    serverUrl = String.format("https://localhost:%d", port);
  }

  @Test
  void givenCredentialsAreInvalid_whenAuthenticating_thenReturnUnauthorized() throws Exception {
    mockMvc.perform(post(serverUrl + "/authenticate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(generateRequestBody("ben", "benswrongpassword")))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void givenCredentialsAreValid_whenAuthenticating_thenReturnOK() throws Exception {
    mockMvc.perform(post(serverUrl + "/authenticate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(generateRequestBody("ben", "benspassword")))
        .andExpect(status().isOk());
  }

  @Test
  void givenJWTIsValid_whenGettingResource_thenReturnResource() throws Exception {
    String jwt = mockMvc.perform(post(serverUrl + "/authenticate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(generateRequestBody("local1", "local1password")))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    mockMvc.perform(get(serverUrl + "/")
            .header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(jwt)))
        .andExpectAll(
            status().isOk(),
            content().string("This is response from '/'. Request was made by 'local1'."));
  }

  @Test
  void givenJWTIsInvalid_whenGettingResource_thenReturnUnauthorized() throws Exception {
    String jwt = mockMvc.perform(post(serverUrl + "/authenticate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(generateRequestBody("local1", "local1password")))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    mockMvc.perform(get(serverUrl + "/")
            .header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(jwt).concat("invalid")))
        .andExpect(status().isUnauthorized());
  }

  private String generateRequestBody(String username, String password) {
    return String.format("{ \"username\": \"%s\", \"password\": \"%s\" }", username, password);
  }

}

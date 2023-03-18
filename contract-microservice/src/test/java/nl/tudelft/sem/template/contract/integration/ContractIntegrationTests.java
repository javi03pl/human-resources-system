package nl.tudelft.sem.template.contract.integration;

import nl.tudelft.sem.template.contract.authentication.AuthManager;
import nl.tudelft.sem.template.contract.authentication.JwtTokenVerifier;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@ExtendWith(SpringExtension.class)
// activate profiles to have spring use mocks during auto-injection of certain beans.
@ActiveProfiles({"test", "mockTokenVerifier", "mockAuthenticationManager"})
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class ContractIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private transient JwtTokenVerifier mockJwtTokenVerifier;

    @Autowired
    private transient AuthManager mockAuthenticationManager;


//    @Test
//    public void helloWorld() throws Exception {
//        // Arrange
//        // Notice how some custom parts of authorisation need to be mocked.
//        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
//        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
//        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
//        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");
//        when(mockJwtTokenVerifier.getRoleFromToken(anyString())).thenReturn("HR");
//
//        // Act
//        // Still include Bearer token as AuthFilter itself is not mocked
//        ResultActions result = mockMvc.perform(get("/propose")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer MockedToken"));
//
//        // Assert
//        result.andExpect(status().isOk());
//
//        String response = result.andReturn().getResponse().getContentAsString();
//
//        assertThat(response).isEqualTo("Hello ExampleUser");
//    }


//    @Test
//    public void proposeTest() throws Exception {
//        // Arrange
//        // Notice how some custom parts of authorisation need to be mocked.
//        // Otherwise, the integration test would never be able to authorise as the authorisation server is offline.
//        when(mockAuthenticationManager.getNetId()).thenReturn("ExampleUser");
//        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
//        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ExampleUser");
//
//
//        String body =
//        "{\"hoursPerWeek\":\"3\",\"startDate\":\"2020-01-01\",
//        \"endDate\":\"2023-01-01\",\"vacationDays\":\"4\",\"candidateNetId\":\"test\"}";
//
//
//        ResultActions result = mockMvc.perform(post("/propose")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(body)
//                .header("Authorization", "Bearer MockedToken"));
//
//        // Assert
//        result.andExpect(status().isOk());
//
//        String response = result.andReturn().getResponse().getContentAsString();
//
//        System.out.println("test123412");
//        System.out.println(response);
//
//        assertThat(response).isEqualTo("Hello ExampleUser");
//    }
}

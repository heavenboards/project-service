package heavenboards.project.service.project.integration;

import security.service.util.test.SecurityTestUtil;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import transfer.contract.api.UserApi;
import transfer.contract.domain.project.ProjectTo;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Тест нахождения всех проектов пользователя.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@RequiredArgsConstructor
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Sql(
    scripts = "classpath:sql/project/create.sql",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
    config = @SqlConfig(encoding = "UTF-8")
)
@Sql(
    scripts = "classpath:sql/clear-all.sql",
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
    config = @SqlConfig(encoding = "UTF-8")
)
public class FindAllUserProjectIntegrationTest {
    /**
     * Utility-класс с настройкой security для тестов.
     */
    @Autowired
    private SecurityTestUtil securityTestUtil;

    /**
     * Mock api-клиента для сервиса пользователей.
     */
    @MockBean
    private UserApi userApi;

    /**
     * Порт приложения.
     */
    @LocalServerPort
    private int port;

    /**
     * Конфигурация перед тестами.
     */
    @BeforeAll
    public void init() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost:" + port + "/api/v1";
        RestAssured.defaultParser = Parser.JSON;
    }

    /**
     * Тест нахождения всех проектов.
     */
    @Test
    @DisplayName("Тест нахождения всех проектов")
    public void findAllUserProjectsTest() {
        securityTestUtil.securityContextHelper();
        Mockito.when(userApi.findUserByUsername(Mockito.any()))
            .thenReturn(securityTestUtil.getAuthenticatedUser());
        Response response = findAllUserProjectResponse();
        List<ProjectTo> projects = response
            .jsonPath().getList(".", ProjectTo.class);

        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Assertions.assertNotNull(projects);
        Assertions.assertEquals(2, projects.size());
        Assertions.assertTrue(projects.stream().map(ProjectTo::getId)
            .toList().containsAll(Set.of(
                UUID.fromString("f91b820e-7721-43ad-b188-1cbd6c0b9d42"),
                UUID.fromString("dba22a9b-afe1-4002-8158-98d7d6585dfd")
            )));
    }

    /**
     * Получить ответ со всеми проектами пользователя.
     *
     * @return ответ со всеми проектами пользователя
     */
    private Response findAllUserProjectResponse() {
        return RestAssured
            .given()
            .contentType("application/json")
            .header(new Header(HttpHeaders.AUTHORIZATION, securityTestUtil.authHeader()))
            .when()
            .get("/project");
    }
}

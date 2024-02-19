package heavenboards.project.service.integration;

import heavenboards.project.service.SecurityTestUtil;
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
import transfer.contract.domain.common.OperationResult;
import transfer.contract.domain.project.ProjectOperationErrorCode;
import transfer.contract.domain.project.ProjectOperationResultTo;
import transfer.contract.domain.project.ProjectTo;

/**
 * Тест создания проекта.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@RequiredArgsConstructor
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Sql(
    scripts = "classpath:sql/clear-all.sql",
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
    config = @SqlConfig(encoding = "UTF-8")
)
public class ProjectCreateIntegrationTest {
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
     * Тест валидного создания проекта.
     */
    @Test
    @DisplayName("Тест валидного создания проекта")
    public void validProjectCreateTest() {
        securityTestUtil.securityContextHelper();
        Mockito.when(userApi.findUserByUsername(Mockito.any()))
            .thenReturn(securityTestUtil.getAuthenticatedUser());
        Response response = createProjectAndGetResponse();
        ProjectOperationResultTo operationResult = response
            .getBody()
            .as(ProjectOperationResultTo.class);

        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Assertions.assertNotNull(operationResult);
        Assertions.assertEquals(OperationResult.OK, operationResult.getOperationResult());
    }

    /**
     * Тест создания проекта с существующим именем у пользователя.
     */
    @Test
    @DisplayName("Тест создания проекта с существующим именем у пользователя")
    public void existingProjectNameCreateTest() {
        securityTestUtil.securityContextHelper();
        Mockito.when(userApi.findUserByUsername(Mockito.any()))
            .thenReturn(securityTestUtil.getAuthenticatedUser());
        createProjectAndGetResponse();
        Response response = createProjectAndGetResponse();
        ProjectOperationResultTo operationResult = response
            .getBody()
            .as(ProjectOperationResultTo.class);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
        Assertions.assertNotNull(operationResult);
        Assertions.assertNotNull(operationResult.getOperationErrorCode());
        Assertions.assertEquals(OperationResult.FAILED, operationResult.getOperationResult());
        Assertions.assertEquals(ProjectOperationErrorCode.NAME_ALREADY_EXIST,
            operationResult.getOperationErrorCode());
    }

    /**
     * Оправить запрос на создание проекта и получить ответ.
     *
     * @return ответ
     */
    private Response createProjectAndGetResponse() {
        return RestAssured
            .given()
            .contentType("application/json")
            .header(new Header(HttpHeaders.AUTHORIZATION, securityTestUtil.authHeader()))
            .body(ProjectTo.builder()
                .name("Project One")
                .build())
            .when()
            .post("/project");
    }
}
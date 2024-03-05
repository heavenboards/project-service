package heavenboards.project.service.project.integration;

import heavenboards.project.service.project.domain.ProjectEntity;
import heavenboards.project.service.project.domain.ProjectRepository;
import heavenboards.project.service.project.domain.ProjectUserRepository;
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
import transfer.contract.domain.common.OperationStatus;
import transfer.contract.domain.project.ProjectOperationErrorCode;
import transfer.contract.domain.project.ProjectOperationResultTo;
import transfer.contract.domain.project.ProjectTo;
import transfer.contract.domain.user.UserTo;

import java.util.List;
import java.util.Optional;

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
     * Репозиторий для проектов.
     */
    @Autowired
    private ProjectRepository projectRepository;

    /**
     * Репозиторий для сущности связывающей проект и пользователя.
     */
    @Autowired
    private ProjectUserRepository projectUserRepository;

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
        UserTo creator = securityTestUtil.getAuthenticatedUser();
        Mockito.when(userApi.findUserByUsername(Mockito.any()))
            .thenReturn(creator);
        Response response = createProjectAndGetResponse(creator);
        ProjectOperationResultTo operationResult = response
            .getBody()
            .as(ProjectOperationResultTo.class);

        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Assertions.assertEquals(OperationStatus.OK, operationResult.getStatus());
        Assertions.assertNotNull(operationResult.getProjectId());

        Optional<ProjectEntity> project = projectRepository.findById(operationResult.getProjectId());
        Assertions.assertTrue(project.isPresent());
        Assertions.assertEquals(1, project.get().getUsers().size());
        Assertions.assertEquals(securityTestUtil.getAuthenticatedUser().getId(),
            project.get().getUsers().get(0).getUserId());
        Assertions.assertTrue(projectUserRepository.existsById(project.get()
            .getUsers().get(0).getId()));
    }

    /**
     * Тест создания проекта с существующим именем у пользователя.
     */
    @Test
    @DisplayName("Тест создания проекта с существующим именем у пользователя")
    public void existingProjectNameCreateTest() {
        securityTestUtil.securityContextHelper();
        UserTo creator = securityTestUtil.getAuthenticatedUser();
        Mockito.when(userApi.findUserByUsername(Mockito.any()))
            .thenReturn(creator);

        Response successResponse = createProjectAndGetResponse(creator);
        ProjectOperationResultTo successOperationResult = successResponse
            .getBody().as(ProjectOperationResultTo.class);

        Response errorResponse = createProjectAndGetResponse(creator);
        ProjectOperationResultTo errorOperationResult = errorResponse
            .getBody().as(ProjectOperationResultTo.class);

        Assertions.assertEquals(HttpStatus.OK.value(), errorResponse.getStatusCode());
        Assertions.assertEquals(OperationStatus.FAILED, errorOperationResult.getStatus());
        Assertions.assertEquals(List.of(ProjectOperationResultTo.ProjectOperationErrorTo.builder()
            .failedProjectId(successOperationResult.getProjectId())
            .errorCode(ProjectOperationErrorCode.NAME_ALREADY_EXIST)
            .build()), errorOperationResult.getErrors());
    }

    /**
     * Оправить запрос на создание проекта и получить ответ.
     *
     * @param creator - создатель проекта
     * @return ответ
     */
    private Response createProjectAndGetResponse(UserTo creator) {
        return RestAssured
            .given()
            .contentType("application/json")
            .header(new Header(HttpHeaders.AUTHORIZATION, securityTestUtil.authHeader()))
            .body(ProjectTo.builder()
                .name("Project One")
                .positionWeight(1000)
                .users(List.of(creator))
                .build())
            .when()
            .post("/project");
    }
}

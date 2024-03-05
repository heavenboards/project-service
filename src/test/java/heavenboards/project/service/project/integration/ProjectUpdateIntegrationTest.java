package heavenboards.project.service.project.integration;

import heavenboards.project.service.project.domain.ProjectEntity;
import heavenboards.project.service.project.domain.ProjectRepository;
import heavenboards.project.service.project.domain.ProjectUserEntity;
import heavenboards.project.service.project.domain.ProjectUserRepository;
import heavenboards.project.service.project.mapping.ProjectMapper;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import security.service.util.test.SecurityTestUtil;
import transfer.contract.api.UserApi;
import transfer.contract.domain.common.OperationStatus;
import transfer.contract.domain.project.ProjectOperationErrorCode;
import transfer.contract.domain.project.ProjectOperationResultTo;
import transfer.contract.domain.project.ProjectTo;
import transfer.contract.domain.user.UserTo;
import transfer.contract.exception.BaseErrorCode;
import transfer.contract.exception.ClientApplicationException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Тест обновления проекта.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@RequiredArgsConstructor
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Sql(
    scripts = "classpath:sql/project/createForUpdate.sql",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
    config = @SqlConfig(encoding = "UTF-8")
)
@Sql(
    scripts = "classpath:sql/clear-all.sql",
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
    config = @SqlConfig(encoding = "UTF-8")
)
public class ProjectUpdateIntegrationTest {
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
     * Маппер для проектов.
     */
    @Autowired
    private ProjectMapper projectMapper;

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
     * Тест валидного обновления проекта.
     */
    @Test
    @DisplayName("Тест валидного обновления проекта")
    public void validProjectUpdateTest() {
        securityTestUtil.securityContextHelper();
        UserTo requestSender = securityTestUtil.getAuthenticatedUser();

        UUID deletedProjectUserId = UUID.fromString("752b4e33-5f33-49e7-b21b-e5dda577e49a");
        List<UserTo> projectUsersBeforeUpdate = List.of(
            requestSender,
            UserTo.builder()
                .id(UUID.fromString("6cd9bb02-f8db-4e73-873d-485508e86fe9"))
                .username("Second User")
                .build(),
            UserTo.builder()
                .id(deletedProjectUserId)
                .username("Third User")
                .build()
        );

        Mockito.when(userApi.findUserByUsername(requestSender.getUsername()))
            .thenReturn(requestSender);
        Mockito.when(userApi.findUsersByIds(Mockito.any()))
            .thenReturn(projectUsersBeforeUpdate);

        UUID updatableProjectId = UUID.fromString("f91b820e-7721-43ad-b188-1cbd6c0b9d42");
        ProjectEntity projectEntity = projectRepository
            .findById(updatableProjectId)
            .orElseThrow(() -> new ClientApplicationException(BaseErrorCode.NOT_FOUND,
                String.format("Проект с идентификатором %s не найден", updatableProjectId)));

        ProjectTo updatableProject = projectMapper.mapFromEntity(new ProjectTo(), projectEntity);
        Assertions.assertEquals(3, updatableProject.getUsers().size());

        UUID newProjectUserUserId = UUID.fromString("053b658d-58fb-47a9-992b-257babe70ef8");
        List<UserTo> projectUsersAfterUpdate = List.of(
            requestSender,
            UserTo.builder()
                .id(UUID.fromString("6cd9bb02-f8db-4e73-873d-485508e86fe9"))
                .username("Second User")
                .build(),
            UserTo.builder()
                .id(newProjectUserUserId)
                .username("New Third User")
                .build()
        );

        updatableProject.setUsers(projectUsersAfterUpdate);
        updatableProject.setName("New Name");
        updatableProject.setPositionWeight(2000);

        Response response = updateProjectAndGetResponse(updatableProject);
        ProjectOperationResultTo operationResult = response.getBody()
            .as(ProjectOperationResultTo.class);

        Assertions.assertEquals(OperationStatus.OK, operationResult.getStatus());
        Assertions.assertEquals(updatableProjectId, operationResult.getProjectId());

        projectEntity = projectRepository
            .findById(updatableProjectId)
            .orElseThrow(() -> new ClientApplicationException(BaseErrorCode.NOT_FOUND,
                String.format("Проект с идентификатором %s не найден", updatableProjectId)));

        Assertions.assertEquals("New Name", projectEntity.getName());
        Assertions.assertEquals(2000, projectEntity.getPositionWeight());
        Assertions.assertEquals(3, projectEntity.getUsers().size());

        List<UUID> newProjectUserUserIds = projectEntity.getUsers().stream()
            .map(ProjectUserEntity::getUserId).toList();

        Assertions.assertTrue(newProjectUserUserIds.containsAll(List.of(
            requestSender.getId(),
            UUID.fromString("6cd9bb02-f8db-4e73-873d-485508e86fe9"),
            newProjectUserUserId
        )));

        Assertions.assertFalse(projectUserRepository.existsById(deletedProjectUserId));

        Optional<UUID> newProjectUserId = projectEntity.getUsers().stream()
            .filter(projectUser -> projectUser.getUserId().equals(newProjectUserUserId))
            .map(ProjectUserEntity::getId)
            .findAny();

        Assertions.assertTrue(newProjectUserId.isPresent());
        Assertions.assertTrue(projectUserRepository.existsById(newProjectUserId.get()));
    }

    /**
     * Тест обновления проекта, в котором ты не состоишь.
     */
    @Test
    @DisplayName("Тест обновления проекта, в котором ты не состоишь")
    public void updateNotYourProjectTest() {
        securityTestUtil.securityContextHelper();
        UserTo requestSender = securityTestUtil.getAuthenticatedUser();

        // Ставим рандомный UUID, имитируем, что пользователя нет в проекте
        requestSender.setId(UUID.randomUUID());

        Mockito.when(userApi.findUserByUsername(requestSender.getUsername()))
            .thenReturn(requestSender);

        UUID updatableProjectId = UUID.fromString("f91b820e-7721-43ad-b188-1cbd6c0b9d42");
        ProjectEntity projectEntity = projectRepository
            .findById(updatableProjectId)
            .orElseThrow(() -> new ClientApplicationException(BaseErrorCode.NOT_FOUND,
                String.format("Проект с идентификатором %s не найден", updatableProjectId)));

        ProjectTo updatableProject = projectMapper.mapFromEntity(new ProjectTo(), projectEntity);

        updatableProject.setName("New Name");
        updatableProject.setPositionWeight(2000);

        Response response = updateProjectAndGetResponse(updatableProject);
        ProjectOperationResultTo operationResult = response.getBody()
            .as(ProjectOperationResultTo.class);

        Assertions.assertEquals(OperationStatus.FAILED, operationResult.getStatus());
        Assertions.assertEquals(List.of(ProjectOperationResultTo.ProjectOperationErrorTo.builder()
            .failedProjectId(updatableProjectId)
            .errorCode(ProjectOperationErrorCode.YOU_ARE_NOT_A_MEMBER)
            .build()), operationResult.getErrors());
    }

    /**
     * Оправить запрос на обновление проекта и получить ответ.
     *
     * @param updatedProject - обновленные данные проекта
     * @return ответ
     */
    private Response updateProjectAndGetResponse(ProjectTo updatedProject) {
        return RestAssured
            .given()
            .contentType("application/json")
            .header(new Header(HttpHeaders.AUTHORIZATION, securityTestUtil.authHeader()))
            .body(updatedProject)
            .when()
            .put("/project");
    }
}

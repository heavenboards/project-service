package heavenboards.project.service.project.controller;

import heavenboards.project.service.project.service.ProjectCreateUseCase;
import heavenboards.project.service.project.service.ProjectFindUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import transfer.contract.domain.project.ProjectOperationResultTo;
import transfer.contract.domain.project.ProjectTo;

import java.util.List;

/**
 * Контроллер для взаимодействия с проектами.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/project")
@Tag(name = "ProjectController", description = "Контроллер для взаимодействия с проектами")
public class ProjectController {
    /**
     * Сервис для проектов.
     */
    private final ProjectFindUseCase projectFindUseCase;

    /**
     * Use case создания проекта.
     */
    private final ProjectCreateUseCase projectCreateUseCase;

    /**
     * Получить все проекты пользователя.
     *
     * @return все проекты пользователя
     */
    @GetMapping
    @Operation(summary = "Получить все проекты пользователя")
    public List<ProjectTo> getAllProjects() {
        return projectFindUseCase.getAllProjects();
    }

    /**
     * Создать проект.
     *
     * @param project - данные проекта.
     * @return результат операции создания
     */
    @PostMapping
    @Operation(summary = "Создать проект")
    public ProjectOperationResultTo createProject(final @Valid @RequestBody ProjectTo project) {
        return projectCreateUseCase.createProject(project);
    }
}

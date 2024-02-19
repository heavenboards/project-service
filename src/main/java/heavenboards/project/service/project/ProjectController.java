package heavenboards.project.service.project;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import transfer.contract.domain.project.ProjectOperationResultTo;
import transfer.contract.domain.project.ProjectTo;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/project")
public class ProjectController {
    /**
     * Сервис для проектов.
     */
    private final ProjectService projectService;

    /**
     * Получить все проекты пользователя.
     *
     * @return все проекты пользователя
     */
    @GetMapping
    public List<ProjectTo> getAllProjects() {
        return projectService.getAllProjects();
    }

    /**
     * Создать проект.
     *
     * @param project - данные проекта.
     * @return результат операции создания
     */
    @PostMapping
    public ResponseEntity<?> createProject(final @Valid @RequestBody ProjectTo project) {
        ProjectOperationResultTo result = projectService.createProject(project);
        return ResponseEntity
            .status(result.getHttpStatus())
            .body(result);
    }
}

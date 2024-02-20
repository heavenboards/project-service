package heavenboards.project.service.project;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import transfer.contract.domain.common.OperationStatus;
import transfer.contract.domain.project.ProjectOperationErrorCode;
import transfer.contract.domain.project.ProjectOperationResultTo;
import transfer.contract.domain.project.ProjectTo;
import transfer.contract.domain.user.UserTo;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Сервис для проектов.
 */
@Service
@RequiredArgsConstructor
public class ProjectService {
    /**
     * Репозиторий для проектов.
     */
    private final ProjectRepository projectRepository;

    /**
     * Репозиторий для сущности связывающей проект и пользователя.
     */
    private final ProjectUserRepository projectUserRepository;

    /**
     * Маппер для проектов.
     */
    private final ProjectMapper projectMapper;

    /**
     * Получить все проекты пользователя.
     *
     * @return все проекты пользователя
     */
    public List<ProjectTo> getAllProjects() {
        var user = (UserTo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<ProjectUserEntity> userProjects = projectUserRepository.findAllByUserId(user.getId());

        return userProjects.stream()
            .map(projectUser -> projectMapper
                .mapFromEntity(new ProjectTo(), projectUser.getProject()))
            .collect(Collectors.toList());
    }

    /**
     * Создать проект.
     *
     * @param project - данные проекта.
     * @return результат операции создания
     */
    public ProjectOperationResultTo createProject(final ProjectTo project) {
        var user = (UserTo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (projectRepository.existsByNameAndUserId(project.getName(), user.getId())) {
            UUID failedEntityId = projectRepository.findIdByName(project.getName(), user.getId());
            return ProjectOperationResultTo.builder()
                .status(OperationStatus.FAILED)
                .entityId(failedEntityId)
                .errors(List.of(ProjectOperationResultTo.ProjectOperationErrorTo.builder()
                    .failedEntityId(failedEntityId)
                    .errorCode(ProjectOperationErrorCode.NAME_ALREADY_EXIST)
                    .build()))
                .build();
        }

        ProjectEntity projectEntity = ProjectEntity.builder()
            .name(project.getName())
            .build();

        projectEntity.setProjectUsers(Set.of(ProjectUserEntity.builder()
            .userId(user.getId())
            .project(projectEntity)
            .build()));

        projectRepository.save(projectEntity);
        return ProjectOperationResultTo.builder()
            .entityId(projectEntity.getId())
            .build();
    }
}

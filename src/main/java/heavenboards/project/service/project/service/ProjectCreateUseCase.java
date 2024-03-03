package heavenboards.project.service.project.service;

import heavenboards.project.service.project.domain.ProjectEntity;
import heavenboards.project.service.project.domain.ProjectRepository;
import heavenboards.project.service.project.mapping.ProjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import transfer.contract.domain.common.OperationStatus;
import transfer.contract.domain.project.ProjectOperationErrorCode;
import transfer.contract.domain.project.ProjectOperationResultTo;
import transfer.contract.domain.project.ProjectTo;
import transfer.contract.domain.user.UserTo;

import java.util.List;
import java.util.UUID;

/**
 * Use case создания проекта.
 */
@Service
@RequiredArgsConstructor
public class ProjectCreateUseCase {
    /**
     * Репозиторий для проектов.
     */
    private final ProjectRepository projectRepository;

    /**
     * Маппер для проектов.
     */
    private final ProjectMapper projectMapper;

    /**
     * Создать проект.
     *
     * @param project - данные проекта.
     * @return результат операции создания
     */
    @Transactional
    public ProjectOperationResultTo createProject(final ProjectTo project) {
        var user = (UserTo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (projectRepository.existsByNameAndUserId(project.getName(), user.getId())) {
            UUID failedEntityId = projectRepository
                .findProjectIdByNameAndUserId(project.getName(), user.getId());
            return ProjectOperationResultTo.builder()
                .status(OperationStatus.FAILED)
                .errors(List.of(ProjectOperationResultTo.ProjectOperationErrorTo.builder()
                    .failedProjectId(failedEntityId)
                    .errorCode(ProjectOperationErrorCode.NAME_ALREADY_EXIST)
                    .build()))
                .build();
        }

        ProjectEntity entity = projectMapper.mapFromTo(new ProjectEntity(), project);
        return ProjectOperationResultTo.builder()
            .projectId(projectRepository.save(entity).getId())
            .build();
    }
}

package heavenboards.project.service.project.service;

import heavenboards.project.service.project.domain.ProjectEntity;
import heavenboards.project.service.project.domain.ProjectRepository;
import heavenboards.project.service.project.domain.ProjectUserEntity;
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
import transfer.contract.exception.BaseErrorCode;
import transfer.contract.exception.ClientApplicationException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use case обновления проекта.
 */
@Service
@RequiredArgsConstructor
public class ProjectUpdateUseCase {
    /**
     * Репозиторий для проектов.
     */
    private final ProjectRepository projectRepository;

    /**
     * Маппер для проектов.
     */
    private final ProjectMapper projectMapper;

    /**
     * Обновить проект.
     *
     * @param project - данные проекта.
     * @return результат операции обновления
     */
    @Transactional
    public ProjectOperationResultTo updateProject(final ProjectTo project) {
        Optional<ProjectEntity> entity = projectRepository.findById(project.getId());
        if (entity.isEmpty()) {
            throw new ClientApplicationException(BaseErrorCode.NOT_FOUND,
                String.format("Проект с идентификатором %s не найден", project.getId()));
        }

        var user = (UserTo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Set<UUID> projectUserIds = entity.get().getUsers().stream()
            .map(ProjectUserEntity::getUserId)
            .collect(Collectors.toSet());
        if (!projectUserIds.contains(user.getId())) {
            return ProjectOperationResultTo.builder()
                .status(OperationStatus.FAILED)
                .errors(List.of(ProjectOperationResultTo.ProjectOperationErrorTo.builder()
                    .failedProjectId(project.getId())
                    .errorCode(ProjectOperationErrorCode.YOU_ARE_NOT_A_MEMBER)
                    .build()))
                .build();
        }

        ProjectEntity updatedEntity = projectMapper.mapFromTo(entity.get(), project);
        return ProjectOperationResultTo.builder()
            .projectId(updatedEntity.getId())
            .build();
    }
}

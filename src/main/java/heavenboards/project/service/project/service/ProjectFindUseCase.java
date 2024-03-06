package heavenboards.project.service.project.service;

import heavenboards.project.service.project.domain.ProjectRepository;
import heavenboards.project.service.project.domain.ProjectUserEntity;
import heavenboards.project.service.project.domain.ProjectUserRepository;
import heavenboards.project.service.project.mapping.ProjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import transfer.contract.domain.project.ProjectTo;
import transfer.contract.domain.user.UserTo;
import transfer.contract.exception.BaseErrorCode;
import transfer.contract.exception.ClientApplicationException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Сервис для проектов.
 */
@Service
@RequiredArgsConstructor
public class ProjectFindUseCase {
    /**
     * Репозиторий для сущности связывающей проект и пользователя.
     */
    private final ProjectUserRepository projectUserRepository;

    /**
     * Репозиторий для проектов.
     */
    private final ProjectRepository projectRepository;

    /**
     * Маппер для проектов.
     */
    private final ProjectMapper projectMapper;

    /**
     * Поиск проекта по идентификатору.
     *
     * @param projectId - идентификатор проекта
     * @return данные проекта
     */
    @Transactional(readOnly = true)
    public ProjectTo findProjectById(final UUID projectId) {
        return projectRepository.findById(projectId)
            .map(entity -> projectMapper.mapFromEntity(new ProjectTo(), entity))
            .orElseThrow(() -> new ClientApplicationException(BaseErrorCode.NOT_FOUND,
                String.format("Проект с идентификатором %s не найден!", projectId)));
    }

    /**
     * Получить все проекты пользователя.
     *
     * @return все проекты пользователя
     */
    @Transactional(readOnly = true)
    public List<ProjectTo> findUserProjects() {
        var user = (UserTo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<ProjectUserEntity> userProjects = projectUserRepository.findAllByUserId(user.getId());

        return userProjects.stream()
            .map(projectUser -> projectMapper
                .mapFromEntity(new ProjectTo(), projectUser.getProject()))
            .collect(Collectors.toList());
    }
}

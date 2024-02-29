package heavenboards.project.service.project.service;

import heavenboards.project.service.project.domain.ProjectUserEntity;
import heavenboards.project.service.project.domain.ProjectUserRepository;
import heavenboards.project.service.project.mapping.ProjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import transfer.contract.domain.project.ProjectTo;
import transfer.contract.domain.user.UserTo;

import java.util.List;
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
}

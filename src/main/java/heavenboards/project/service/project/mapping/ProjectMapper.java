package heavenboards.project.service.project.mapping;

import heavenboards.project.service.project.domain.ProjectEntity;
import heavenboards.project.service.project.domain.ProjectUserEntity;
import heavenboards.project.service.project.domain.ProjectUserRepository;
import lombok.RequiredArgsConstructor;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import transfer.contract.api.UserApi;
import transfer.contract.domain.project.ProjectTo;
import transfer.contract.domain.user.UserTo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Маппер для проектов.
 */
@Mapper(componentModel = "spring")
@RequiredArgsConstructor
public abstract class ProjectMapper {
    /**
     * Репозиторий для сущности связывающей проект и пользователя.
     */
    private ProjectUserRepository projectUserRepository;

    /**
     * Api-клиент для сервиса пользователей.
     */
    private UserApi userApi;

    /**
     * Маппинг из entity в to.
     *
     * @param to - to-модель проекта, которой мы проставляем поля
     * @param entity - entity
     * @return to с проставленными полями
     */
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "boards", ignore = true)
    public abstract ProjectTo mapFromEntity(@MappingTarget ProjectTo to,
                                            ProjectEntity entity);

    /**
     * Маппинг из to в entity.
     *
     * @param entity - сущность которой проставляем поля
     * @param to     - to-модель проекта
     * @return сущность с проставленными полями
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "users", ignore = true)
    public abstract ProjectEntity mapFromTo(@MappingTarget ProjectEntity entity,
                                            ProjectTo to);

    /**
     * После маппинга из entity в to - проставляем users.
     *
     * @param to     - to-модель проекта, которой проставляются поля
     * @param entity - сущность проекта
     */
    @AfterMapping
    @SuppressWarnings("unused")
    protected void afterMappingFromEntity(final @MappingTarget ProjectTo to,
                                          final ProjectEntity entity) {
        Set<UUID> ids = entity.getUsers().stream()
            .map(ProjectUserEntity::getUserId)
            .collect(Collectors.toSet());

        to.setUsers(userApi.findUsersByIds(ids));
    }

    /**
     * После маппинга из to в entity - проставляем users.
     *
     * @param entity - сущность которой проставляем поя
     * @param to     - to-модель проекта
     */
    @AfterMapping
    @SuppressWarnings("unused")
    protected void afterMappingFromTo(final @MappingTarget ProjectEntity entity,
                                      final ProjectTo to) {
        var user = (UserTo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Новые пользователи проекта
        List<ProjectUserEntity> newProjectUsers = new ArrayList<>();
        for (UserTo projectUser : to.getUsers()) {
            newProjectUsers.add(ProjectUserEntity.builder()
                .userId(projectUser.getId())
                .project(entity)
                .build());
        }

        // Удаляем пользователей, которые были до изменения, но которых нет в новом списке
        for (ProjectUserEntity projectUser : entity.getUsers()) {
            if (!newProjectUsers.contains(projectUser)) {
                projectUserRepository.deleteById(projectUser.getId());
            }
        }

        entity.setUsers(newProjectUsers);
    }

    /**
     * Внедрение бина репозитория для ProjectUserEntity.
     *
     * @param repository - бин ProjectUserRepository
     */
    @Autowired
    public void setProjectUserRepository(final ProjectUserRepository repository) {
        this.projectUserRepository = repository;
    }

    /**
     * Внедрение бина api для сервиса пользователей.
     *
     * @param api - бин UserApi
     */
    @Autowired
    public void setUserApi(final UserApi api) {
        this.userApi = api;
    }
}

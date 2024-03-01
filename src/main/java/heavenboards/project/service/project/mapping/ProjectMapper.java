package heavenboards.project.service.project.mapping;

import heavenboards.project.service.project.domain.ProjectEntity;
import heavenboards.project.service.project.domain.ProjectUserEntity;
import lombok.RequiredArgsConstructor;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.security.core.context.SecurityContextHolder;
import transfer.contract.domain.project.ProjectTo;
import transfer.contract.domain.user.UserTo;

import java.util.Set;

/**
 * Маппер для проектов.
 */
@Mapper(componentModel = "spring")
@RequiredArgsConstructor
public abstract class ProjectMapper {
    /**
     * Маппинг из entity в to.
     *
     * @param entity - entity
     * @return to с проставленными полями
     */
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "boards", ignore = true)
    public abstract ProjectTo mapFromEntity(ProjectEntity entity);

    /**
     * Маппинг из to в entity.
     *
     * @param entity - сущность которой проставляем поля
     * @param to     - to-модель проекта
     * @return сущность с проставленными полями
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "projectUsers", ignore = true)
    public abstract ProjectEntity mapFromTo(@MappingTarget ProjectEntity entity,
                                            ProjectTo to);

    /**
     * После маппинга из to в entity - проставляем projectUsers.
     *
     * @param entity - сущность которой проставляем поя
     * @param to     - to-модель проекта
     */
    @AfterMapping
    @SuppressWarnings("unused")
    protected void afterMappingFromTo(final @MappingTarget ProjectEntity entity,
                                      final ProjectTo to) {
        var user = (UserTo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        entity.setProjectUsers(Set.of(ProjectUserEntity.builder()
            .userId(user.getId())
            .project(entity)
            .build()));
    }
}

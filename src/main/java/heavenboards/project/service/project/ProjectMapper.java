package heavenboards.project.service.project;

import lombok.RequiredArgsConstructor;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import transfer.contract.api.UserApi;
import transfer.contract.domain.project.ProjectTo;
import transfer.contract.domain.user.UserTo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Маппер для проектов.
 */
@Mapper(componentModel = "spring")
@RequiredArgsConstructor
public abstract class ProjectMapper {
    /**
     * Api-клиент для сервиса пользователей.
     */
    private UserApi userApi;

    /**
     * Маппинг из entity в to.
     *
     * @param to     - to
     * @param entity - entity
     * @return to с проставленными полями
     */
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "boards", ignore = true)
    protected abstract ProjectTo mapFromEntity(@MappingTarget ProjectTo to,
                                               ProjectEntity entity);

    /**
     * После маппинга из entity в to. Проставляем пользователей.
     *
     * @param to     - to
     * @param entity - entity
     */
    @AfterMapping
    @SuppressWarnings("unused")
    protected void afterMappingFromEntity(final @MappingTarget ProjectTo to,
                                          final ProjectEntity entity) {
        List<UserTo> users = userApi.findUsersByIds(entity.getProjectUsers().stream()
            .map(ProjectUserEntity::getUserId).collect(Collectors.toSet()));

        to.setUsers(users);
    }

    /**
     * Внедрение бина userApi.
     * @param userApiBean - бин userApi
     */
    @Autowired
    public void setUserApi(final UserApi userApiBean) {
        this.userApi = userApiBean;
    }
}

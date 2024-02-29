package heavenboards.project.service.project.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для сущности связывающей проект и пользователя.
 */
public interface ProjectUserRepository extends JpaRepository<ProjectUserEntity, UUID> {
    /**
     * Найти все сущности по идентификатору пользователя.
     *
     * @param userId - идентификатор пользователя
     * @return все сущности по пользователю
     */
    List<ProjectUserEntity> findAllByUserId(UUID userId);
}

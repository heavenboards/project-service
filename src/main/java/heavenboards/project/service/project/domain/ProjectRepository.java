package heavenboards.project.service.project.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

/**
 * Репозиторий для проектов.
 */
public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {
    /**
     * Проверка существования проекта по пользователю и названию.
     *
     * @param name   - название
     * @param userId - идентификатор пользователя
     * @return существует ли у пользователя проект с таким названием
     */
    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END FROM "
        + "(SELECT * FROM project_user_entity WHERE user_id = :userId) pu "
        + "INNER JOIN (SELECT * FROM project_entity WHERE name = :name) p ON p.id = pu.project_id",
        nativeQuery = true)
    boolean existsByNameAndUserId(String name, UUID userId);

    /**
     * Получить идентификатор проекта по названию и пользователю.
     *
     * @param name   - название
     * @param userId - идентификатор пользователя
     * @return идентификатор проекта у пользователя
     */
    @Query(value = "SELECT p.id FROM "
        + "(SELECT * FROM project_user_entity WHERE user_id = :userId) pu "
        + "INNER JOIN (SELECT * FROM project_entity WHERE name = :name) p ON p.id = pu.project_id",
        nativeQuery = true)
    UUID findIdByName(String name, UUID userId);
}

package heavenboards.project.service.project.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.UuidGenerator;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Сущность проекта.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Accessors(chain = true)
@Entity
@Table(name = "project_entity")
public class ProjectEntity {
    /**
     * Идентификатор.
     */
    @Id
    @UuidGenerator
    private UUID id;

    /**
     * Название.
     */
    private String name;

    /**
     * Вес позиции проекта.
     * Нужен для определения порядка отображения проектов на UI.
     */
    private Integer positionWeight;

    /**
     * Идентификаторы участников.
     */
    @Builder.Default
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private Set<ProjectUserEntity> projectUsers = new HashSet<>();

    /**
     * Сравнение двух объектов через id.
     *
     * @param another - объект для сравнения
     * @return равны ли объекты
     */
    @Override
    public boolean equals(final Object another) {
        if (this == another) {
            return true;
        }

        if (another == null || getClass() != another.getClass()) {
            return false;
        }

        ProjectEntity that = (ProjectEntity) another;
        return Objects.equals(id, that.id);
    }

    /**
     * Хеш код идентификатора.
     *
     * @return хеш код идентификатора
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Переопределение toString() для избежания циклических отображений.
     * @return строковое представление проекта
     */
    @Override
    public String toString() {
        return "ProjectEntity{"
            + "id=" + id
            + ", name='" + name + '\''
            + ", positionWeight=" + positionWeight
            + '}';
    }
}

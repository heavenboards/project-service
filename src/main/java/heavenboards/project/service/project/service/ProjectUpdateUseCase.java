package heavenboards.project.service.project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import transfer.contract.domain.project.ProjectOperationResultTo;
import transfer.contract.domain.project.ProjectTo;

/**
 * Use case обновления проекта.
 */
@Service
@RequiredArgsConstructor
public class ProjectUpdateUseCase {
    /**
     * Обновить проект.
     *
     * @param project - данные проекта.
     * @return результат операции обновления
     */
    @Transactional
    public ProjectOperationResultTo updateProject(final ProjectTo project) {
        return ProjectOperationResultTo.builder().build();
    }
}

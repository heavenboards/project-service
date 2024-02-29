package heavenboards.project.service;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Главный класс для запуска сервиса.
 */
@SpringBootApplication(scanBasePackages = {
    "heavenboards.project.service",
    "security.service",
    "transfer.contract"
})
@EnableFeignClients(basePackages = "transfer.contract")
@OpenAPIDefinition(
    info = @Info(
        title = "project-service",
        version = "1.0.0",
        description = "Микросервис для взаимодействия с данными проектов"
    )
)
public class ProjectServiceApplication {
    /**
     * Главный метод для запуска сервиса.
     *
     * @param args - аргументы запуска
     */
    public static void main(final String[] args) {
        SpringApplication.run(ProjectServiceApplication.class, args);
    }
}

package heavenboards.project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import security.service.jwt.JwtTokenGenerator;
import transfer.contract.domain.user.UserRole;
import transfer.contract.domain.user.UserTo;

import java.util.UUID;

/**
 * Utility-класс с настройкой security для тестов.
 */
@Component
@RequiredArgsConstructor
public class SecurityTestUtil {
    /**
     * Класс для генерации JWT-токенов.
     */
    private final JwtTokenGenerator tokenGenerator;

    /**
     * Получить аутентифицированного пользователя.
     *
     * @return данные аутентифицированного пользователя
     */
    public UserTo getAuthenticatedUser() {
        return UserTo.builder()
            .id(UUID.fromString("4b15db5a-d772-4826-aec7-2772fb036e1d"))
            .username("username")
            .password("$2a$10$NIC5fTNK1Rt6ANhk4JW.befENq3T7m8FJysyM2P5OvoTwSHINxea2")
            .role(UserRole.USER)
            .firstName("Ivan")
            .lastName("Ivanov")
            .build();
    }

    /**
     * Сохранение контекста аутентификации для тестов.
     */
    public void securityContextHelper() {
        UserTo user = getAuthenticatedUser();
        var authenticationToken = new UsernamePasswordAuthenticationToken(
            user, null, user.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    /**
     * Сгенерировать Bearer-токен.
     *
     * @return Bearer-токен
     */
    public String authHeader() {
        var user = (UserTo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return "Bearer " + tokenGenerator.generate(user);
    }
}

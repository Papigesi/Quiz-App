package com.example.quizapp.quizappdemo.security;

import com.example.quizapp.quizappdemo.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Controller/service katmanında oturum açmış kullanıcıyı okumak için yardımcı.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static QuizUserPrincipal currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || !(auth.getPrincipal() instanceof QuizUserPrincipal principal)) {
            throw new UnauthorizedException("Oturum açmanız gerekiyor");
        }
        return principal;
    }

    public static Long currentUserId() {
        return currentPrincipal().getId();
    }
}

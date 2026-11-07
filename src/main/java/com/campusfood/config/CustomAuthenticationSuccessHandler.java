package com.campusfood.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final RequestCache requestCache = new HttpSessionRequestCache();

    public CustomAuthenticationSuccessHandler() {
        setDefaultTargetUrl("/catalogo");
        setAlwaysUseDefaultTargetUrl(false);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // Si el usuario autenticado es ADMINISTRADOR, redirigir siempre al panel de administración
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        if (isAdmin) {
            clearAuthenticationAttributes(request);
            getRedirectStrategy().sendRedirect(request, response, "/admin");
            return;
        }

        // Si el usuario intentó acceder previamente a una ruta protegida (ej. /carrito, /panel, etc.)
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }

        // Para estudiantes universitarios que inician sesión directamente:
        // Redirigir al catálogo para que exploren y compren alimentos
        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, "/catalogo");
    }
}


package br.edu.cpa.cpainteligente.security;

import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtils {

    // Recupera o institutionId que guardamos nas "credenciais" do token
    // Lembra que no JwtAuthFilter fizemos:
    //   new UsernamePasswordAuthenticationToken(userId, institutionId, ...)
    // userId ficou no "principal", institutionId ficou nas "credentials"
    public static String getInstitutionId() {
        return (String) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getCredentials();
    }

    public static String getUserId() {
        return (String) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    private AuthUtils() {} // impede instanciação — só métodos estáticos
}
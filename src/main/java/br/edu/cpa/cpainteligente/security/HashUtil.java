package br.edu.cpa.cpainteligente.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class HashUtil {

    // Usa o JWT secret como "pepper" — sem ele, mesmo sabendo o número,
    // não consegue reverter o hash. Conceito similar ao seu projeto Node.js.
    private final String pepper;

    public HashUtil(@Value("${app.jwt.secret}") String pepper) {
        this.pepper = pepper;
    }

    public String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String salted = value + pepper;
            byte[] bytes = digest.digest(salted.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 não disponível", e);
        }
    }
}
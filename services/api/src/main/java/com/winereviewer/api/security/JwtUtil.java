package com.winereviewer.api.security;

import com.winereviewer.api.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecureDigestAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Utilitário para geração e validação de tokens JWT.
 * <p>
 * Responsável por:
 * - Gerar tokens JWT com userId no subject
 * - Validar tokens (assinatura, expiração)
 * - Extrair informações (userId) do token
 * <p>
 * <strong>Como funciona JWT:</strong>
 * <ul>
 *   <li>JWT = JSON Web Token</li>
 *   <li>Estrutura: header.payload.signature (3 partes separadas por ponto)</li>
 *   <li>Header: algoritmo de assinatura (HS256)</li>
 *   <li>Payload: dados do usuário (userId, timestamps)</li>
 *   <li>Signature: hash criptográfico que garante integridade</li>
 * </ul>
 * <p>
 * <strong>Por que usar JWT:</strong>
 * <ul>
 *   <li>Stateless: servidor não guarda sessão em memória</li>
 *   <li>Seguro: assinado digitalmente (impossível falsificar sem chave secreta)</li>
 *   <li>Autocontido: carrega todas as informações necessárias dentro dele</li>
 * </ul>
 *
 * @author lucas
 * @date 20/10/2025
 */
@Slf4j
@Component
public class JwtUtil {

    // Campos imutáveis injetados via construtor
    private final String secret;
    private final Long expirationMs;

    /**
     * Construtor com injeção de JwtProperties.
     * <p>
     * Extrai os valores das properties e armazena em campos imutáveis (final).
     * Isso garante thread-safety e facilita testes.
     *
     * @param properties configurações JWT carregadas do application.yml
     */
    public JwtUtil(JwtProperties properties) {
        this.secret = properties.getSecret();
        this.expirationMs = properties.getExpiration();
        log.info("JwtUtil inicializado com expiração de {} ms", expirationMs);
    }

    /**
     * Gera um token JWT para um usuário.
     * <p>
     * <strong>O que este método faz:</strong>
     * <ol>
     *   <li>Captura timestamp atual para calcular expiração</li>
     *   <li>Monta o payload com userId, issuedAt, expiration</li>
     *   <li>Assina o token com a chave secreta usando HS256</li>
     *   <li>Retorna string JWT completa (header.payload.signature)</li>
     * </ol>
     * <p>
     * <strong>Estrutura do token gerado:</strong>
     * <pre>
     * {
     *   "sub": "123e4567-e89b-12d3-a456-426614174000",  ← userId
     *   "iat": 1697712000,                              ← issued at (timestamp criação)
     *   "exp": 1697715600                               ← expiration (timestamp expiração)
     * }
     * </pre>
     * <p>
     * <strong>Por que usar 'subject' (sub):</strong>
     * É o campo padrão JWT para identificar o "dono" do token. No nosso caso, o userId.
     *
     * @param userId ID do usuário (será armazenado no claim 'subject')
     * @return token JWT assinado (String longa tipo "eyJhbGciOiJIUzI1Ni...")
     */
    public String generateToken(UUID userId) {
        // 1. Captura momento atual
        final var now = new Date();

        // 2. Calcula momento de expiração (agora + expirationMs)
        final var expirationDate = new Date(now.getTime() + expirationMs);

        log.debug("Gerando JWT para userId: {} (expira em: {})", userId, expirationDate);

        // 3. Constrói o token usando builder do jjwt
        return Jwts.builder()
                .subject(userId.toString())           // ← Payload: userId no campo 'sub'
                .issuedAt(now)                        // ← Payload: timestamp de criação
                .expiration(expirationDate)           // ← Payload: timestamp de expiração
                .signWith(getSigningKey())  // ← Assina com chave secreta (HS256 = HMAC-SHA256)
                .compact();                               // ← Gera string final (header.payload.signature)
    }

    /**
     * Extrai o userId (subject) de um token JWT.
     * <p>
     * <strong>O que este método faz:</strong>
     * <ol>
     *   <li>Valida a assinatura do token (garante que não foi adulterado)</li>
     *   <li>Verifica se não expirou (compara exp com agora)</li>
     *   <li>Decodifica o payload e extrai o campo 'subject'</li>
     *   <li>Converte String → UUID</li>
     * </ol>
     * <p>
     * <strong>Como funciona a validação:</strong>
     * - O token tem 3 partes: header.payload.signature
     * - A signature é um hash de (header + payload + chave secreta)
     * - Se alguém alterar o payload, a signature não vai bater
     * - Sem a chave secreta, é impossível gerar uma signature válida
     * <p>
     * <strong>Quando lança exceção:</strong>
     * - Token expirado → ExpiredJwtException
     * - Signature inválida → SignatureException
     * - Token malformado → MalformedJwtException
     *
     * @param token JWT a ser decodificado (sem o prefixo "Bearer ")
     * @return UUID do usuário extraído do token
     * @throws io.jsonwebtoken.JwtException se token for inválido, expirado ou adulterado
     */
    public UUID extractUserId(String token) {
        // Extrai todos os claims do token (valida assinatura e expiração automaticamente)
        final Claims claims = extractAllClaims(token);

        // O userId está no campo 'subject' (definido no generateToken)
        final String userIdString = claims.getSubject();

        log.debug("UserId extraído do token: {}", userIdString);

        // Converte String → UUID
        return UUID.fromString(userIdString);
    }

    /**
     * Valida se um token JWT é válido.
     * <p>
     * <strong>O que este método faz:</strong>
     * - Tenta decodificar o token
     * - Se conseguir, token é válido (assinatura OK + não expirado)
     * - Se der exceção, token é inválido
     * <p>
     * <strong>Casos de uso:</strong>
     * - Usado pelo JwtAuthenticationFilter para verificar se request tem token válido
     * - Se retornar false, request é rejeitado com 401 Unauthorized
     *
     * @param token JWT a ser validado
     * @return true se válido, false se inválido/expirado
     */
    public boolean validateToken(String token) {
        try {
            // Tenta extrair claims (se der erro, token é inválido)
            extractAllClaims(token);
            log.debug("Token validado com sucesso");
            return true;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("Token expirado: {}", e.getMessage());
            return false;
        } catch (io.jsonwebtoken.JwtException e) {
            log.warn("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    // Private helper methods (ordenados por invocação)

    /**
     * Extrai todos os claims (dados) do token JWT.
     * <p>
     * <strong>Este é o método-chave que faz a "mágica":</strong>
     * <ol>
     *   <li>Recebe token (string gigante tipo "eyJhbGci...")</li>
     *   <li>Separa em 3 partes: header, payload, signature</li>
     *   <li>Usa a chave secreta para validar signature</li>
     *   <li>Se signature bater, decodifica payload (Base64)</li>
     *   <li>Retorna objeto Claims com todos os dados</li>
     * </ol>
     * <p>
     * <strong>Por que validar signature:</strong>
     * - Imagine que alguém alterou o userId no payload de "user-123" para "user-456"
     * - A signature foi gerada com "user-123", então não vai bater com "user-456"
     * - O método vai lançar SignatureException
     * - Isso garante que NINGUÉM pode falsificar tokens sem a chave secreta
     * <p>
     * <strong>Verificação de expiração:</strong>
     * - O método também compara o campo 'exp' com o timestamp atual
     * - Se agora > exp, lança ExpiredJwtException
     *
     * @param token JWT a ser decodificado
     * @return Claims contendo subject, issuedAt, expiration, etc.
     * @throws io.jsonwebtoken.JwtException se token for inválido
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())     // ← jjwt 0.12.x usa verifyWith() ao invés de setSigningKey()
                .build()
                .parseSignedClaims(token)        // ← jjwt 0.12.x usa parseSignedClaims() ao invés de parseClaimsJws()
                .getPayload();                   // ← jjwt 0.12.x usa getPayload() ao invés de getBody()
    }

    /**
     * Gera a chave criptográfica usada para assinar o JWT.
     * <p>
     * <strong>O que este método faz:</strong>
     * - Pega a string secreta do application.yml
     * - Converte para byte array (UTF-8)
     * - Gera uma SecretKey compatível com algoritmo HS256
     * <p>
     * <strong>Por que precisa disso:</strong>
     * - O algoritmo HS256 (HMAC-SHA256) exige uma SecretKey específica
     * - Não dá para usar String diretamente, precisa converter
     * - A biblioteca jjwt faz validações (ex: tamanho mínimo 256 bits)
     * <p>
     * <strong>IMPORTANTE - Segurança:</strong>
     * - A chave DEVE ter pelo menos 256 bits (32 caracteres)
     * - Em produção, SEMPRE usar variável de ambiente
     * - NUNCA commitar chave real no código
     * - Se a chave vazar, TODOS os tokens podem ser falsificados!
     *
     * @return SecretKey para assinar/validar JWTs
     */
    private SecretKey getSigningKey() {
        // Converte String → byte[] → SecretKey
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}

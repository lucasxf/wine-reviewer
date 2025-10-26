package com.winereviewer.api.config;

import lombok.AllArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Configuração de beans AWS para integração com serviços da Amazon Web Services.
 * <p>
 * <strong>Responsabilidades:</strong>
 * <ul>
 *   <li>Criar e configurar {@link S3Client} para upload de arquivos</li>
 *   <li>Carregar credentials e região via {@link AwsProperties}</li>
 *   <li>Fornecer bean gerenciado pelo Spring para injeção de dependências</li>
 * </ul>
 * <p>
 * <strong>Configuração de credentials:</strong>
 * - Carregadas via {@link AwsProperties} de application.yml ou variáveis de ambiente
 * - Nunca commitar credentials no código (use env vars!)
 * - Rotacionar chaves periodicamente (boas práticas de segurança)
 * <p>
 * <strong>Serviços AWS configurados:</strong>
 * <ul>
 *   <li>{@link S3Client} - Upload/download de arquivos no S3 (implementado)</li>
 *   <li>SecretsManagerClient - Gerenciamento de secrets (futuro)</li>
 *   <li>CloudWatchClient - Logs e métricas (futuro)</li>
 * </ul>
 * <p>
 * <strong>Exemplo de uso:</strong>
 * <pre>
 * @Service
 * public class S3Service {
 *     private final S3Client s3Client; // ← Injetado automaticamente
 *
 *     public S3Service(S3Client s3Client) {
 *         this.s3Client = s3Client;
 *     }
 * }
 * </pre>
 *
 * @author lucas
 * @date 26/10/2025 06:12
 * @see AwsProperties
 * @see S3Client
 */
@Configuration
@AllArgsConstructor
@EnableConfigurationProperties({ AwsProperties.class })
public class AwsConfig {

    private final AwsProperties properties;

    /**
     * Cria e configura o cliente S3 da AWS.
     * <p>
     * <strong>O que este bean faz:</strong>
     * <ol>
     *   <li>Configura região AWS (ex: sa-east-1)</li>
     *   <li>Configura credentials (access key + secret key)</li>
     *   <li>Retorna S3Client pronto para uso</li>
     * </ol>
     * <p>
     * <strong>Credentials Provider:</strong>
     * - Implementa interface anônima de {@link AwsCredentials}
     * - Busca access key e secret key de {@link AwsProperties}
     * - Properties carregadas de application.yml ou env vars
     * <p>
     * <strong>Região:</strong>
     * - sa-east-1 (São Paulo) para dev/prod brasileiro
     * - Usar região próxima aos usuários reduz latência
     * <p>
     * <strong>Segurança:</strong>
     * - NUNCA commitar credentials no código
     * - Usar variáveis de ambiente em produção
     * - Rotacionar chaves periodicamente
     * <p>
     * <strong>Uso:</strong>
     * - Injetado automaticamente em {@link com.winereviewer.api.service.impl.S3Service}
     * - Gerenciado pelo Spring (singleton, thread-safe)
     *
     * @return {@link S3Client} configurado e pronto para uso
     */
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(() -> new AwsCredentials() {
                    @Override
                    public String accessKeyId() {
                        return properties.getAccessKeyId();
                    }

                    @Override
                    public String secretAccessKey() {
                        return properties.getSecretAccessKey();
                    }
                })
                .build();
    }

}

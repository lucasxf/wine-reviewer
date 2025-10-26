package com.winereviewer.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties de configuração para AWS (Amazon Web Services).
 * <p>
 * Carrega configurações do prefixo "aws" em application.yml ou variáveis de ambiente.
 * <p>
 * <strong>Campos configuráveis:</strong>
 * <ul>
 *   <li><strong>accessKeyId:</strong> AWS Access Key ID (IAM user)</li>
 *   <li><strong>secretAccessKey:</strong> AWS Secret Access Key (IAM user)</li>
 *   <li><strong>region:</strong> Região AWS (ex: sa-east-1 para São Paulo)</li>
 *   <li><strong>s3BucketName:</strong> Nome do bucket S3 para upload de imagens</li>
 * </ul>
 * <p>
 * <strong>Configuração em application.yml:</strong>
 * <pre>
 * aws:
 *   access-key-id: ${AWS_ACCESS_KEY_ID}      # ← Variável de ambiente
 *   secret-access-key: ${AWS_SECRET_ACCESS_KEY}
 *   region: sa-east-1
 *   s3-bucket-name: wine-reviewer-bucket
 * </pre>
 * <p>
 * <strong>Segurança CRÍTICA:</strong>
 * <ul>
 *   <li>❌ NUNCA commitar credentials hardcoded no código</li>
 *   <li>✅ SEMPRE usar variáveis de ambiente para credentials</li>
 *   <li>✅ Rotacionar chaves periodicamente (IAM > Security Credentials)</li>
 *   <li>✅ Usar IAM roles em produção (EC2, ECS, Lambda)</li>
 *   <li>✅ Aplicar princípio de menor privilégio (policy mínima necessária)</li>
 * </ul>
 * <p>
 * <strong>Variáveis de ambiente (desenvolvimento local):</strong>
 * <pre>
 * # Linux/macOS
 * export AWS_ACCESS_KEY_ID=AKIA...
 * export AWS_SECRET_ACCESS_KEY=...
 *
 * # Windows PowerShell
 * $env:AWS_ACCESS_KEY_ID="AKIA..."
 * $env:AWS_SECRET_ACCESS_KEY="..."
 * </pre>
 * <p>
 * <strong>Policy IAM recomendada (S3 apenas):</strong>
 * <pre>
 * {
 *   "Version": "2012-10-17",
 *   "Statement": [{
 *     "Effect": "Allow",
 *     "Action": ["s3:PutObject", "s3:GetObject"],
 *     "Resource": "arn:aws:s3:::wine-reviewer-bucket/*"
 *   }]
 * }
 * </pre>
 *
 * @author lucas
 * @date 26/10/2025 06:07
 * @see AwsConfig
 * @see ConfigurationProperties
 */
@Getter
@Setter
@ConfigurationProperties("aws")
public class AwsProperties {

    /**
     * AWS Access Key ID do IAM user.
     * <p>
     * <strong>Exemplo:</strong> AKIAIOSFODNN7EXAMPLE
     * <p>
     * <strong>Segurança:</strong>
     * - Carregar de variável de ambiente: ${AWS_ACCESS_KEY_ID}
     * - NUNCA commitar no código
     * - Rotacionar periodicamente
     */
    private String accessKeyId;

    /**
     * AWS Secret Access Key do IAM user.
     * <p>
     * <strong>Exemplo:</strong> wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
     * <p>
     * <strong>Segurança:</strong>
     * - Carregar de variável de ambiente: ${AWS_SECRET_ACCESS_KEY}
     * - NUNCA commitar no código
     * - NUNCA logar ou expor
     * - Rotacionar periodicamente
     */
    private String secretAccessKey;

    /**
     * Região AWS onde os recursos estão hospedados.
     * <p>
     * <strong>Valores comuns:</strong>
     * - sa-east-1 (São Paulo, Brasil)
     * - us-east-1 (N. Virginia, EUA)
     * - eu-west-1 (Irlanda, Europa)
     * <p>
     * <strong>Recomendação:</strong>
     * Usar região próxima aos usuários para reduzir latência.
     */
    private String region;

    /**
     * Nome do bucket S3 para armazenar imagens de reviews.
     * <p>
     * <strong>Exemplo:</strong> wine-reviewer-bucket
     * <p>
     * <strong>Configuração recomendada:</strong>
     * - Bucket privado (block public access)
     * - Versionamento habilitado (backup)
     * - Lifecycle policy para arquivos antigos
     * - CORS configurado para uploads diretos (futuro)
     */
    private String s3BucketName;

}

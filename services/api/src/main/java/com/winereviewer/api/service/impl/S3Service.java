package com.winereviewer.api.service.impl;

import com.winereviewer.api.application.dto.response.FileUploadResponse;
import com.winereviewer.api.config.AwsProperties;
import com.winereviewer.api.exception.FileTooLargeException;
import com.winereviewer.api.exception.FileUploadException;
import com.winereviewer.api.exception.InvalidFileException;
import com.winereviewer.api.exception.UnsupportedFileTypeException;
import com.winereviewer.api.service.FileUploadService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;

/**
 * Implementação de {@link FileUploadService} que faz upload de arquivos para AWS S3.
 * <p>
 * <strong>Responsabilidades:</strong>
 * <ul>
 *   <li>Validar arquivo (tamanho, tipo MIME, não vazio)</li>
 *   <li>Fazer upload para S3 usando AWS SDK v2</li>
 *   <li>Gerar URL completa do arquivo no S3</li>
 *   <li>Retornar {@link FileUploadResponse} com metadados</li>
 * </ul>
 * <p>
 * <strong>Validações aplicadas:</strong>
 * <ul>
 *   <li>Arquivo não pode estar vazio (0 bytes) → {@link InvalidFileException}</li>
 *   <li>Tamanho máximo: 10MB → {@link FileTooLargeException}</li>
 *   <li>Tipos permitidos: image/jpeg, image/png, image/webp → {@link UnsupportedFileTypeException}</li>
 * </ul>
 * <p>
 * <strong>Configuração AWS:</strong>
 * - Credentials e região carregados via {@link AwsProperties} (application.yml)
 * - Bucket S3 configurável por ambiente (dev, prod)
 * - S3Client injetado via {@link com.winereviewer.api.config.AwsConfig}
 * <p>
 * <strong>Tratamento de erros:</strong>
 * - {@link S3Exception}: Problemas com AWS (credentials, bucket, permissões) → {@link FileUploadException}
 * - {@link IOException}: Erro ao ler stream do arquivo → {@link FileUploadException}
 * <p>
 * <strong>Futuras melhorias:</strong>
 * - Adicionar geração de nomes únicos (UUID + timestamp) para evitar colisões
 * - Implementar pre-signed URLs para uploads diretos do cliente
 * - Adicionar suporte para múltiplos buckets (dev, prod)
 * - Implementar compressão de imagens antes do upload
 *
 * @author lucas
 * @date 26/10/2025 08:02
 */
@Slf4j
@Service
@AllArgsConstructor
public class S3Service implements FileUploadService {

    /**
     * Tipos MIME permitidos para upload.
     * Apenas imagens são aceitas no MVP.
     */
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    /**
     * Tamanho máximo de arquivo em bytes (10MB).
     * Limite definido para MVP (free tier AWS S3).
     */
    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10MB

    private final S3Client s3Client;
    private final AwsProperties awsProperties;

    /**
     * Faz upload de arquivo para AWS S3 com validações.
     * <p>
     * <strong>Fluxo:</strong>
     * <ol>
     *   <li>Valida arquivo (tamanho, tipo, não vazio)</li>
     *   <li>Constrói {@link PutObjectRequest} com metadados</li>
     *   <li>Envia para S3 via {@link S3Client#putObject}</li>
     *   <li>Gera URL completa do arquivo</li>
     *   <li>Retorna {@link FileUploadResponse}</li>
     * </ol>
     * <p>
     * <strong>Formato da URL gerada:</strong>
     * <pre>
     * https://{bucket}.s3.{region}.amazonaws.com/{key}
     * Exemplo: https://wine-reviewer-bucket.s3.sa-east-1.amazonaws.com/wine-photo.jpg
     * </pre>
     *
     * @param file arquivo enviado pelo cliente (MultipartFile)
     * @return {@link FileUploadResponse} com URL completa e metadados
     * @throws InvalidFileException se arquivo estiver vazio
     * @throws FileTooLargeException se arquivo exceder 10MB
     * @throws UnsupportedFileTypeException se MIME type não for permitido
     * @throws FileUploadException se ocorrer erro durante upload para S3
     */
    @Override
    public FileUploadResponse upload(MultipartFile file) {
        // 1. Validar arquivo
        validateFile(file);

        final String fileName = file.getOriginalFilename();
        final String bucketKey = fileName; // Por enquanto, key = fileName (futuro: gerar UUID único)

        log.info("Iniciando upload de arquivo \"{}\" ({} bytes, {}) para S3",
                fileName, file.getSize(), file.getContentType());

        try {
            // 2. Construir request do S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .key(bucketKey)
                    .bucket(awsProperties.getS3BucketName())
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            // 3. Enviar para S3
            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            // 4. Gerar URL completa do arquivo no S3
            String fileUrl = buildS3Url(bucketKey);

            log.info("Upload concluído com sucesso: {}", fileUrl);

            // 5. Retornar response com metadados
            return new FileUploadResponse(
                    fileName,
                    fileUrl,
                    bucketKey,
                    file.getSize(),
                    file.getContentType(),
                    Instant.now()
            );

        } catch (S3Exception e) {
            String errorMessage = e.awsErrorDetails() != null
                    ? e.awsErrorDetails().errorMessage()
                    : e.getMessage();
            log.error("Erro ao fazer upload para S3: {}", errorMessage, e);
            throw new FileUploadException("Erro ao fazer upload para S3: " + errorMessage, e);
        } catch (IOException e) {
            log.error("Erro ao ler arquivo durante upload: {}", e.getMessage(), e);
            throw new FileUploadException("Erro ao processar arquivo durante upload", e);
        }
    }

    // Private helper methods

    /**
     * Valida arquivo antes de fazer upload.
     * <p>
     * Verifica:
     * - Arquivo não vazio
     * - Tamanho dentro do limite
     * - MIME type permitido
     *
     * @param file arquivo a ser validado
     * @throws InvalidFileException se arquivo estiver vazio
     * @throws FileTooLargeException se arquivo exceder limite
     * @throws UnsupportedFileTypeException se MIME type não for permitido
     */
    private void validateFile(MultipartFile file) {
        // Validação 1: Arquivo não pode estar vazio
        if (file.isEmpty()) {
            throw new InvalidFileException("Arquivo vazio não pode ser enviado");
        }

        // Validação 2: Tamanho máximo de 10MB
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            long maxSizeMB = MAX_FILE_SIZE_BYTES / 1024 / 1024;
            throw new FileTooLargeException(
                    String.format("Arquivo excede o tamanho máximo de %d MB (tamanho: %.2f MB)",
                            maxSizeMB, file.getSize() / 1024.0 / 1024.0)
            );
        }

        // Validação 3: MIME type deve ser permitido
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new UnsupportedFileTypeException(
                    String.format("Tipo de arquivo não suportado: %s. Tipos permitidos: %s",
                            contentType, ALLOWED_MIME_TYPES)
            );
        }
    }

    /**
     * Constrói URL completa do arquivo no S3.
     * <p>
     * <strong>Formato:</strong> https://{bucket}.s3.{region}.amazonaws.com/{key}
     * <p>
     * <strong>Exemplo:</strong>
     * <pre>
     * bucket: wine-reviewer-bucket
     * region: sa-east-1
     * key: wine-photo.jpg
     * URL: https://wine-reviewer-bucket.s3.sa-east-1.amazonaws.com/wine-photo.jpg
     * </pre>
     *
     * @param bucketKey chave do arquivo no bucket
     * @return URL completa do arquivo no S3
     */
    private String buildS3Url(String bucketKey) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                awsProperties.getS3BucketName(),
                awsProperties.getRegion(),
                bucketKey
        );
    }

}

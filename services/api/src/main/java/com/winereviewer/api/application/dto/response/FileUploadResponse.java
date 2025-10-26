package com.winereviewer.api.application.dto.response;

import java.time.Instant;

/**
 * DTO de resposta para upload de arquivo no S3.
 * <p>
 * <strong>Campos retornados:</strong>
 * <ul>
 *   <li><strong>fileName:</strong> Nome original do arquivo (ex: "wine-photo.jpg")</li>
 *   <li><strong>fileUrl:</strong> URL completa do S3 para acesso direto (ex: "https://bucket.s3.region.amazonaws.com/key")</li>
 *   <li><strong>bucketKey:</strong> Chave do arquivo no bucket S3 (usado para deletar depois)</li>
 *   <li><strong>fileSizeBytes:</strong> Tamanho do arquivo em bytes</li>
 *   <li><strong>contentType:</strong> MIME type do arquivo (ex: "image/jpeg")</li>
 *   <li><strong>uploadedAt:</strong> Timestamp UTC do upload</li>
 * </ul>
 * <p>
 * <strong>Por que retornar URL completa:</strong>
 * <ul>
 *   <li>Frontend não precisa saber estrutura do S3 (bucket, região, formato)</li>
 *   <li>URL pode ser usada diretamente em {@code <img>} tags ou {@code CachedNetworkImage} (Flutter)</li>
 *   <li>Facilita migração futura para Supabase ou outro storage</li>
 *   <li>Suporta pre-signed URLs no futuro (mesmo formato)</li>
 * </ul>
 * <p>
 * <strong>Exemplo de uso no Flutter:</strong>
 * <pre>
 * CachedNetworkImage(
 *   imageUrl: response.fileUrl, // ← URL completa pronta para usar!
 *   placeholder: (context, url) => CircularProgressIndicator(),
 * )
 * </pre>
 *
 * @param fileName Nome original do arquivo
 * @param fileUrl URL completa para acessar o arquivo no S3
 * @param bucketKey Chave do arquivo no bucket (para deletar depois)
 * @param fileSizeBytes Tamanho em bytes
 * @param contentType MIME type (image/jpeg, image/png, image/webp)
 * @param uploadedAt Timestamp UTC do upload
 *
 * @author lucas
 * @date 26/10/2025 08:18
 */
public record FileUploadResponse(
        String fileName,
        String fileUrl,
        String bucketKey,
        long fileSizeBytes,
        String contentType,
        Instant uploadedAt) {
}

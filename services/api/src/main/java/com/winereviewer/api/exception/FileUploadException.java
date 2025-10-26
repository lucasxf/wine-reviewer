package com.winereviewer.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception lançada quando ocorre erro durante o upload para S3.
 * <p>
 * <strong>Casos de uso:</strong>
 * <ul>
 *   <li>Falha de comunicação com AWS S3</li>
 *   <li>Credenciais AWS inválidas ou expiradas</li>
 *   <li>Bucket S3 não existe ou sem permissão</li>
 *   <li>IOException durante leitura do stream do arquivo</li>
 *   <li>Erro inesperado durante upload (catch genérico)</li>
 * </ul>
 * <p>
 * <strong>HTTP Status:</strong> 500 Internal Server Error
 * <p>
 * <strong>Exemplo de uso:</strong>
 * <pre>
 * try {
 *     s3Client.putObject(request, body);
 * } catch (IOException | S3Exception e) {
 *     throw new FileUploadException("Erro ao fazer upload para S3", e);
 * }
 * </pre>
 *
 * @author lucas
 * @date 26/10/2025
 */
public class FileUploadException extends DomainException {

    /**
     * Construtor com mensagem customizada.
     *
     * @param message descrição do erro de upload
     */
    public FileUploadException(String message) {
        super(message);
    }

    /**
     * Construtor com mensagem e causa raiz (throwable).
     * <p>
     * Preserva a stack trace original para debugging.
     *
     * @param message descrição do erro de upload
     * @param cause exceção original (IOException, S3Exception, etc.)
     */
    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Retorna HTTP 500 Internal Server Error para erros de upload.
     * <p>
     * Usuário não pode fazer nada para resolver (problema no servidor).
     *
     * @return HttpStatus.INTERNAL_SERVER_ERROR
     */
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}

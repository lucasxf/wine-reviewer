package com.winereviewer.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception lançada quando um arquivo excede o tamanho máximo permitido.
 * <p>
 * <strong>Limites configurados:</strong>
 * <ul>
 *   <li>Servlet: 10MB (spring.servlet.multipart.max-file-size)</li>
 *   <li>Validação S3Service: 10MB (MAX_FILE_SIZE constant)</li>
 * </ul>
 * <p>
 * <strong>HTTP Status:</strong> 400 Bad Request
 * <p>
 * <strong>Exemplo de uso:</strong>
 * <pre>
 * if (file.getSize() > MAX_FILE_SIZE) {
 *     throw new FileTooLargeException(
 *         String.format("Arquivo excede o tamanho máximo de %d MB", MAX_FILE_SIZE / 1024 / 1024)
 *     );
 * }
 * </pre>
 *
 * @author lucas
 * @date 26/10/2025
 */
public class FileTooLargeException extends DomainException {

    /**
     * Construtor com mensagem customizada.
     *
     * @param message descrição do limite excedido
     */
    public FileTooLargeException(String message) {
        super(message);
    }

    /**
     * Retorna HTTP 400 Bad Request para arquivos muito grandes.
     *
     * @return HttpStatus.BAD_REQUEST
     */
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

}

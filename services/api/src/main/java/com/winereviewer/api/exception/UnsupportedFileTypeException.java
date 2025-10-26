package com.winereviewer.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception lançada quando o tipo MIME do arquivo não é permitido.
 * <p>
 * <strong>Tipos permitidos (images apenas):</strong>
 * <ul>
 *   <li>image/jpeg</li>
 *   <li>image/png</li>
 *   <li>image/webp</li>
 * </ul>
 * <p>
 * <strong>Tipos bloqueados (exemplos):</strong>
 * <ul>
 *   <li>application/x-executable (executáveis)</li>
 *   <li>text/html (HTML potencialmente malicioso)</li>
 *   <li>application/pdf (não suportado no MVP)</li>
 * </ul>
 * <p>
 * <strong>HTTP Status:</strong> 400 Bad Request
 * <p>
 * <strong>Exemplo de uso:</strong>
 * <pre>
 * if (!ALLOWED_TYPES.contains(file.getContentType())) {
 *     throw new UnsupportedFileTypeException(
 *         "Tipo de arquivo não suportado: " + file.getContentType()
 *     );
 * }
 * </pre>
 *
 * @author lucas
 * @date 26/10/2025
 */
public class UnsupportedFileTypeException extends DomainException {

    /**
     * Construtor com mensagem customizada.
     *
     * @param message descrição do tipo MIME rejeitado
     */
    public UnsupportedFileTypeException(String message) {
        super(message);
    }

    /**
     * Retorna HTTP 400 Bad Request para tipos de arquivo não suportados.
     *
     * @return HttpStatus.BAD_REQUEST
     */
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

}

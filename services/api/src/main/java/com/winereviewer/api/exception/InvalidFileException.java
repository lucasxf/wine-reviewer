package com.winereviewer.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception lançada quando um arquivo enviado é inválido.
 * <p>
 * <strong>Casos de uso:</strong>
 * <ul>
 *   <li>Arquivo vazio (0 bytes)</li>
 *   <li>Nome de arquivo inválido ou ausente</li>
 *   <li>Arquivo corrompido ou malformado</li>
 * </ul>
 * <p>
 * <strong>HTTP Status:</strong> 400 Bad Request
 * <p>
 * <strong>Exemplo de uso:</strong>
 * <pre>
 * if (file.isEmpty()) {
 *     throw new InvalidFileException("Arquivo vazio não pode ser enviado");
 * }
 * </pre>
 *
 * @author lucas
 * @date 26/10/2025
 */
public class InvalidFileException extends DomainException {

    /**
     * Construtor com mensagem customizada.
     *
     * @param message descrição do problema com o arquivo
     */
    public InvalidFileException(String message) {
        super(message);
    }

    /**
     * Retorna HTTP 400 Bad Request para arquivos inválidos.
     *
     * @return HttpStatus.BAD_REQUEST
     */
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

}

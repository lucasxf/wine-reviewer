package com.winereviewer.api.controller;

import com.winereviewer.api.application.dto.response.FileUploadResponse;
import com.winereviewer.api.exception.FileTooLargeException;
import com.winereviewer.api.exception.FileUploadException;
import com.winereviewer.api.exception.InvalidFileException;
import com.winereviewer.api.exception.UnsupportedFileTypeException;
import com.winereviewer.api.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller responsável pelo upload de arquivos (imagens de reviews).
 * <p>
 * <strong>Endpoint disponível:</strong>
 * - POST /files/upload - Upload de imagem para S3
 * <p>
 * <strong>⚠️ IMPORTANTE - STATUS ATUAL (MVP):</strong>
 * - Endpoint é PÚBLICO (sem autenticação)
 * - Marcado como {@code @Deprecated} para lembrar de adicionar auth no futuro
 * - Ideal para testes via Postman durante desenvolvimento
 * <p>
 * <strong>📋 TODO - Quando integrar com reviews:</strong>
 * <ol>
 *   <li>Remover {@code @Deprecated}</li>
 *   <li>Adicionar {@code @AuthenticationPrincipal UserDetails userDetails} no método</li>
 *   <li>Logar userId de quem fez upload (auditoria)</li>
 *   <li>Associar imagem com review_id (campo image_url na tabela review)</li>
 * </ol>
 * <p>
 * <strong>Validações aplicadas:</strong>
 * <ul>
 *   <li>Arquivo não vazio</li>
 *   <li>Tamanho máximo: 10MB (servlet config + S3Service validation)</li>
 *   <li>Tipos permitidos: image/jpeg, image/png, image/webp</li>
 * </ul>
 * <p>
 * <strong>Tratamento de erros:</strong>
 * - Exceptions são capturadas e delegadas para {@link com.winereviewer.api.exception.GlobalExceptionHandler}
 * - Retorna JSON padronizado com mensagem de erro
 * <p>
 * <strong>Exemplo de uso (Postman):</strong>
 * <pre>
 * POST http://localhost:8080/files/upload
 * Content-Type: multipart/form-data
 * Body: form-data
 *   - key: "file"
 *   - value: [selecionar arquivo .jpg/.png/.webp]
 * </pre>
 *
 * @author lucas
 * @date 26/10/2025 08:14
 * @see FileUploadService
 * @see com.winereviewer.api.service.impl.S3Service
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/files/upload")
@Tag(name = "File Upload", description = "API de upload de arquivos para reviews")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    /**
     * Faz upload de imagem para AWS S3.
     * <p>
     * <strong>⚠️ TEMPORARY - PUBLIC ENDPOINT FOR MVP TESTING</strong>
     * <p>
     * Este endpoint NÃO requer autenticação para facilitar testes via Postman.
     * Quando for integrar com o fluxo de reviews, adicione autenticação:
     * <pre>
     * public ResponseEntity<FileUploadResponse> upload(
     *     @RequestParam("file") MultipartFile file,
     *     @AuthenticationPrincipal UserDetails userDetails // ← Adicionar isso!
     * ) {
     *     UUID userId = UUID.fromString(userDetails.getUsername());
     *     log.info("Upload realizado pelo usuário: {}", userId);
     *     // ... rest of implementation
     * }
     * </pre>
     * <p>
     * <strong>Fluxo de upload:</strong>
     * <ol>
     *   <li>Cliente envia arquivo via form-data (key="file")</li>
     *   <li>S3Service valida arquivo (tamanho, tipo, não vazio)</li>
     *   <li>S3Service faz upload para AWS S3</li>
     *   <li>Backend retorna URL completa do arquivo</li>
     *   <li>Cliente usa URL para exibir imagem (CachedNetworkImage no Flutter)</li>
     * </ol>
     *
     * @param file arquivo a ser enviado (JPEG, PNG ou WEBP, máx 10MB)
     * @return {@link FileUploadResponse} com URL completa do arquivo no S3
     * @throws InvalidFileException se arquivo estiver vazio
     * @throws FileTooLargeException se arquivo exceder 10MB
     * @throws UnsupportedFileTypeException se tipo MIME não for permitido
     * @throws FileUploadException se ocorrer erro durante upload para S3
     */
    @Deprecated(since = "0.1.0", forRemoval = true) // ← Remove quando adicionar auth!
    @Operation(
            summary = "Upload de imagem para review",
            description = """
                    Faz upload de imagem para AWS S3 e retorna URL completa.

                    **⚠️ Endpoint público (sem autenticação) - Apenas para testes MVP**

                    **Validações:**
                    - Arquivo não vazio
                    - Tamanho máximo: 10MB
                    - Tipos permitidos: image/jpeg, image/png, image/webp

                    **Fluxo:**
                    1. Cliente envia arquivo via multipart/form-data
                    2. Backend valida e faz upload para S3
                    3. Backend retorna URL completa (https://bucket.s3.region.amazonaws.com/key)
                    4. Cliente usa URL para exibir imagem

                    **TODO:** Adicionar autenticação quando integrar com reviews
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Upload realizado com sucesso",
                    content = @Content(schema = @Schema(implementation = FileUploadResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Arquivo inválido (vazio, muito grande, ou tipo não suportado)"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro ao fazer upload para S3 (problema no servidor)"
            )
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> upload(@RequestParam("file") MultipartFile file) {
        // Validate file parameter is present (handles missing multipart parameter)
        if (file == null) {
            throw new InvalidFileException("Parâmetro 'file' é obrigatório");
        }

        log.info("Recebida requisição de upload: {} ({} bytes, {})",
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        try {
            // Delega validação e upload para S3Service
            final FileUploadResponse response = fileUploadService.upload(file);

            log.info("Upload concluído com sucesso: {}", response.fileUrl());

            return ResponseEntity.ok(response);

        } catch (InvalidFileException | FileTooLargeException | UnsupportedFileTypeException e) {
            // Exceptions de validação (400 Bad Request) - GlobalExceptionHandler vai tratar
            log.warn("Validação falhou para arquivo {}: {}", file.getOriginalFilename(), e.getMessage());
            throw e;

        } catch (FileUploadException e) {
            // Erro de upload S3 (500 Internal Server Error) - GlobalExceptionHandler vai tratar
            log.error("Erro ao fazer upload de {}: {}", file.getOriginalFilename(), e.getMessage());
            throw e;

        } catch (Exception e) {
            // Qualquer outro erro inesperado
            log.error("Erro inesperado durante upload de {}", file.getOriginalFilename(), e);
            throw new FileUploadException("Erro inesperado durante upload", e);
        }
    }

}

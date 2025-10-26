package com.winereviewer.api.service;

import com.winereviewer.api.application.dto.response.FileUploadResponse;
import com.winereviewer.api.exception.FileTooLargeException;
import com.winereviewer.api.exception.FileUploadException;
import com.winereviewer.api.exception.InvalidFileException;
import com.winereviewer.api.exception.UnsupportedFileTypeException;
import org.springframework.web.multipart.MultipartFile;

/**
 * Interface para serviços de upload de arquivos.
 * <p>
 * <strong>Implementações disponíveis:</strong>
 * <ul>
 *   <li>{@link com.winereviewer.api.service.impl.S3Service} - Upload para AWS S3 (implementação atual)</li>
 *   <li>SupabaseService - Upload para Supabase Storage (futuro)</li>
 * </ul>
 * <p>
 * <strong>Contrato da interface:</strong>
 * - Aceita {@link MultipartFile} do Spring
 * - Retorna {@link FileUploadResponse} com URL completa e metadados
 * - Valida arquivo antes de upload (tamanho, tipo, não vazio)
 * - Lança exceptions tipadas para diferentes erros
 * <p>
 * <strong>Exceptions lançadas:</strong>
 * <ul>
 *   <li>{@link InvalidFileException} - Arquivo vazio ou inválido</li>
 *   <li>{@link FileTooLargeException} - Arquivo excede tamanho máximo</li>
 *   <li>{@link UnsupportedFileTypeException} - MIME type não permitido</li>
 *   <li>{@link FileUploadException} - Erro durante upload (S3, IO, etc.)</li>
 * </ul>
 *
 * @author lucas
 * @date 26/10/2025 08:00
 */
public interface FileUploadService {

    /**
     * Faz upload de arquivo para storage (S3, Supabase, etc.).
     * <p>
     * Implementações devem:
     * <ol>
     *   <li>Validar arquivo (tamanho, tipo, não vazio)</li>
     *   <li>Fazer upload para storage</li>
     *   <li>Retornar URL completa do arquivo</li>
     * </ol>
     *
     * @param file arquivo enviado pelo cliente
     * @return {@link FileUploadResponse} com URL completa e metadados
     * @throws InvalidFileException se arquivo estiver vazio ou inválido
     * @throws FileTooLargeException se arquivo exceder tamanho máximo
     * @throws UnsupportedFileTypeException se MIME type não for permitido
     * @throws FileUploadException se ocorrer erro durante upload
     */
    FileUploadResponse upload(MultipartFile file);

}

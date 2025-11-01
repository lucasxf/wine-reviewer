package com.winereviewer.api.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for FileUploadController.
 * <p>
 * <strong>What we're testing:</strong>
 * <ul>
 *   <li>POST /files/upload - Upload valid/invalid files</li>
 *   <li>File validation (empty, size, MIME type)</li>
 *   <li>Response structure (FileUploadResponse DTO)</li>
 *   <li>HTTP status codes (200 OK, 400 Bad Request, 500 Internal Server Error)</li>
 * </ul>
 * <p>
 * <strong>Mocking strategy:</strong>
 * - S3Client is mocked (no real AWS S3 calls in tests)
 * - Real PostgreSQL container via Testcontainers (inherited from AbstractIntegrationTest)
 * - MockMvc for HTTP requests (full Spring context)
 * <p>
 * <strong>Authentication:</strong>
 * - Endpoint is currently PUBLIC (marked as @Deprecated in controller)
 * - When authentication is added, use authenticated(userId) helper from AbstractIntegrationTest
 *
 * @author lucas
 * @date 26/10/2025
 */
@DisplayName("FileUploadController - Integration Tests")
class FileUploadControllerIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("POST /files/upload - Should upload valid JPEG file (200 OK)")
    void shouldUploadValidJpegFile() throws Exception {
        // Given: Valid JPEG file
        MockMultipartFile file = new MockMultipartFile(
                "file", // Parameter name must match @RequestParam("file")
                "wine-photo.jpg",
                "image/jpeg",
                "fake-jpeg-content".getBytes()
        );

        // Mock S3 successful upload
        PutObjectResponse putObjectResponse = (PutObjectResponse) PutObjectResponse.builder()
                .sdkHttpResponse(SdkHttpResponse.builder().statusCode(200).build())
                .build();
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(putObjectResponse);

        // When & Then: POST /files/upload
        mockMvc.perform(MockMvcRequestBuilders.multipart("/files/upload")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("wine-photo.jpg"))
                .andExpect(jsonPath("$.fileUrl").isNotEmpty())
                .andExpect(jsonPath("$.fileUrl").value(org.hamcrest.Matchers.containsString("s3")))
                .andExpect(jsonPath("$.bucketKey").value("wine-photo.jpg"))
                .andExpect(jsonPath("$.fileSizeBytes").value(file.getSize()))
                .andExpect(jsonPath("$.contentType").value("image/jpeg"))
                .andExpect(jsonPath("$.uploadedAt").isNotEmpty());
    }

    @Test
    @DisplayName("POST /files/upload - Should upload valid PNG file (200 OK)")
    void shouldUploadValidPngFile() throws Exception {
        // Given: Valid PNG file
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "wine-label.png",
                "image/png",
                new byte[1024] // 1KB
        );

        // Mock S3 response
        PutObjectResponse putObjectResponse = (PutObjectResponse) PutObjectResponse.builder()
                .sdkHttpResponse(SdkHttpResponse.builder().statusCode(200).build())
                .build();
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(putObjectResponse);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/files/upload")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contentType").value("image/png"))
                .andExpect(jsonPath("$.fileSizeBytes").value(1024));
    }

    @Test
    @DisplayName("POST /files/upload - Should upload valid WEBP file (200 OK)")
    void shouldUploadValidWebpFile() throws Exception {
        // Given: Valid WEBP file
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "wine-bottle.webp",
                "image/webp",
                new byte[2048] // 2KB
        );

        // Mock S3 response
        PutObjectResponse putObjectResponse = (PutObjectResponse) PutObjectResponse.builder()
                .sdkHttpResponse(SdkHttpResponse.builder().statusCode(200).build())
                .build();
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(putObjectResponse);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/files/upload")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contentType").value("image/webp"));
    }

    @Test
    @DisplayName("POST /files/upload - Should reject empty file (400 Bad Request)")
    void shouldRejectEmptyFile() throws Exception {
        // Given: Empty file (0 bytes)
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        // When & Then: Should return 400 Bad Request
        mockMvc.perform(MockMvcRequestBuilders.multipart("/files/upload")
                        .file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("vazio")));
    }

    @Test
    @DisplayName("POST /files/upload - Should reject file exceeding 10MB (400 Bad Request)")
    void shouldRejectFileTooLarge() throws Exception {
        // Given: File larger than 10MB
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "huge-image.jpg",
                "image/jpeg",
                largeContent
        );

        // When & Then: Should return 400 Bad Request
        mockMvc.perform(MockMvcRequestBuilders.multipart("/files/upload")
                        .file(largeFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("10 MB")));
    }

    @Test
    @DisplayName("POST /files/upload - Should reject unsupported MIME type PDF (400 Bad Request)")
    void shouldRejectPdfFile() throws Exception {
        // Given: PDF file (not allowed)
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "fake-pdf-content".getBytes()
        );

        // When & Then: Should return 400 Bad Request
        mockMvc.perform(MockMvcRequestBuilders.multipart("/files/upload")
                        .file(pdfFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("application/pdf")));
    }

    @Test
    @DisplayName("POST /files/upload - Should reject executable file (400 Bad Request)")
    void shouldRejectExecutableFile() throws Exception {
        // Given: Executable file (security risk)
        MockMultipartFile execFile = new MockMultipartFile(
                "file",
                "malware.exe",
                "application/x-executable",
                "fake-exe-content".getBytes()
        );

        // When & Then: Should return 400 Bad Request
        mockMvc.perform(MockMvcRequestBuilders.multipart("/files/upload")
                        .file(execFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("application/x-executable"
                )));
    }

    @Test
    @DisplayName("POST /files/upload - Should reject file with null content type (400 Bad Request)")
    void shouldRejectFileWithoutContentType() throws Exception {
        // Given: File without content type
        MockMultipartFile fileWithoutType = new MockMultipartFile(
                "file",
                "unknown.file",
                null, // No content type
                "content".getBytes()
        );

        // When & Then: Should return 400 Bad Request
        mockMvc.perform(MockMvcRequestBuilders.multipart("/files/upload")
                        .file(fileWithoutType))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("null")));
    }

    @Test
    @DisplayName("POST /files/upload - Should handle missing file parameter (400 Bad Request)")
    void shouldHandleMissingFileParameter() throws Exception {
        // When & Then: POST without "file" parameter
        mockMvc.perform(MockMvcRequestBuilders.multipart("/files/upload")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

}

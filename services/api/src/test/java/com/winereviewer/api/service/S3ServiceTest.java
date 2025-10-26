package com.winereviewer.api.service;

import com.winereviewer.api.application.dto.response.FileUploadResponse;
import com.winereviewer.api.config.AwsProperties;
import com.winereviewer.api.exception.FileTooLargeException;
import com.winereviewer.api.exception.FileUploadException;
import com.winereviewer.api.exception.InvalidFileException;
import com.winereviewer.api.exception.UnsupportedFileTypeException;
import com.winereviewer.api.service.impl.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for S3Service.
 * <p>
 * <strong>What we're testing:</strong>
 * <ul>
 *   <li>File validations (empty, size, MIME type)</li>
 *   <li>S3Client interaction (PutObjectRequest construction)</li>
 *   <li>URL generation (S3 bucket URL format)</li>
 *   <li>Exception handling (S3Exception, IOException)</li>
 * </ul>
 * <p>
 * <strong>Mocking strategy:</strong>
 * - S3Client is mocked (no real AWS calls)
 * - AwsProperties is real object with test values
 * - MultipartFile is Spring's MockMultipartFile
 *
 * @author lucas
 * @date 26/10/2025
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("S3Service - Unit Tests")
class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    private S3Service s3Service;
    private AwsProperties awsProperties;

    @BeforeEach
    void setUp() {
        // Setup AwsProperties with test values
        awsProperties = new AwsProperties();
        awsProperties.setRegion("sa-east-1");
        awsProperties.setS3BucketName("wine-reviewer-bucket-test");

        s3Service = new S3Service(s3Client, awsProperties);
    }

    @Test
    @DisplayName("Should upload valid JPEG file successfully")
    void shouldUploadValidJpegFile() throws IOException {
        // Given: Valid JPEG file
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "wine-photo.jpg",
                "image/jpeg",
                "fake-image-content".getBytes()
        );

        // Mock S3 successful response
        PutObjectResponse putObjectResponse = (PutObjectResponse) PutObjectResponse.builder()
                .sdkHttpResponse(SdkHttpResponse.builder().statusCode(200).build())
                .build();
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(putObjectResponse);

        // When: Upload file
        FileUploadResponse response = s3Service.upload(file);

        // Then: Response should contain correct data
        assertThat(response).isNotNull();
        assertThat(response.fileName()).isEqualTo("wine-photo.jpg");
        assertThat(response.fileUrl()).startsWith("https://wine-reviewer-bucket-test.s3.sa-east-1.amazonaws.com/");
        assertThat(response.bucketKey()).isEqualTo(response.fileName()); // Simple key = fileName for now
        assertThat(response.fileSizeBytes()).isEqualTo(file.getSize());
        assertThat(response.contentType()).isEqualTo("image/jpeg");
        assertThat(response.uploadedAt()).isNotNull();

        // Verify S3Client was called with correct parameters
        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));

        PutObjectRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.bucket()).isEqualTo("wine-reviewer-bucket-test");
        assertThat(capturedRequest.key()).isEqualTo("wine-photo.jpg");
        assertThat(capturedRequest.contentType()).isEqualTo("image/jpeg");
        assertThat(capturedRequest.contentLength()).isEqualTo(file.getSize());
    }

    @Test
    @DisplayName("Should upload valid PNG file successfully")
    void shouldUploadValidPngFile() throws IOException {
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
        FileUploadResponse response = s3Service.upload(file);
        assertThat(response.contentType()).isEqualTo("image/png");
        assertThat(response.fileSizeBytes()).isEqualTo(1024L);
    }

    @Test
    @DisplayName("Should upload valid WEBP file successfully")
    void shouldUploadValidWebpFile() throws IOException {
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
        FileUploadResponse response = s3Service.upload(file);
        assertThat(response.contentType()).isEqualTo("image/webp");
    }

    @Test
    @DisplayName("Should reject empty file")
    void shouldRejectEmptyFile() {
        // Given: Empty file (0 bytes)
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        // When & Then: Should throw InvalidFileException
        assertThatThrownBy(() -> s3Service.upload(emptyFile))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("Arquivo vazio");

        // Verify S3Client was never called
        verifyNoInteractions(s3Client);
    }

    @Test
    @DisplayName("Should reject file exceeding size limit (10MB)")
    void shouldRejectFileTooLarge() {
        // Given: File larger than 10MB
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "huge-image.jpg",
                "image/jpeg",
                largeContent
        );

        // When & Then: Should throw FileTooLargeException
        assertThatThrownBy(() -> s3Service.upload(largeFile))
                .isInstanceOf(FileTooLargeException.class)
                .hasMessageContaining("10 MB");

        verifyNoInteractions(s3Client);
    }

    @Test
    @DisplayName("Should reject unsupported MIME type (PDF)")
    void shouldRejectUnsupportedMimeType() {
        // Given: PDF file (not allowed)
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "fake-pdf-content".getBytes()
        );

        // When & Then: Should throw UnsupportedFileTypeException
        assertThatThrownBy(() -> s3Service.upload(pdfFile))
                .isInstanceOf(UnsupportedFileTypeException.class)
                .hasMessageContaining("application/pdf");

        verifyNoInteractions(s3Client);
    }

    @Test
    @DisplayName("Should reject executable file")
    void shouldRejectExecutableFile() {
        // Given: Executable file (security risk)
        MockMultipartFile execFile = new MockMultipartFile(
                "file",
                "malware.exe",
                "application/x-executable",
                "fake-exe-content".getBytes()
        );

        // When & Then: Should throw UnsupportedFileTypeException
        assertThatThrownBy(() -> s3Service.upload(execFile))
                .isInstanceOf(UnsupportedFileTypeException.class)
                .hasMessageContaining("application/x-executable");

        verifyNoInteractions(s3Client);
    }

    @Test
    @DisplayName("Should reject file with null content type")
    void shouldRejectNullContentType() {
        // Given: File with null content type
        MockMultipartFile fileWithoutType = new MockMultipartFile(
                "file",
                "unknown.file",
                null, // No content type
                "content".getBytes()
        );

        // When & Then: Should throw UnsupportedFileTypeException
        assertThatThrownBy(() -> s3Service.upload(fileWithoutType))
                .isInstanceOf(UnsupportedFileTypeException.class)
                .hasMessageContaining("null");

        verifyNoInteractions(s3Client);
    }

    @Test
    @DisplayName("Should handle S3Exception and wrap in FileUploadException")
    void shouldHandleS3Exception() throws IOException {
        // Given: Valid file but S3Client throws exception
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "content".getBytes()
        );

        // Mock S3 throwing exception (e.g., invalid credentials, bucket not found)
        // Note: awsErrorDetails() can be null in real S3Exception, so handle gracefully
        S3Exception s3Exception = (S3Exception) S3Exception.builder()
                .message("Access Denied - Bucket does not exist or invalid credentials")
                .statusCode(403)
                .build();
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(s3Exception);

        // When & Then: Should wrap S3Exception in FileUploadException
        assertThatThrownBy(() -> s3Service.upload(file))
                .isInstanceOf(FileUploadException.class)
                .hasMessageContaining("Erro ao fazer upload")
                .hasCause(s3Exception);
    }

    @Test
    @DisplayName("Should handle IOException during file read")
    void shouldHandleIOException() throws IOException {
        // Given: MultipartFile that throws IOException on getInputStream()
        MultipartFile faultyFile = mock(MultipartFile.class);
        when(faultyFile.isEmpty()).thenReturn(false);
        when(faultyFile.getSize()).thenReturn(1024L);
        when(faultyFile.getContentType()).thenReturn("image/jpeg");
        when(faultyFile.getOriginalFilename()).thenReturn("test.jpg");
        when(faultyFile.getInputStream()).thenThrow(new IOException("Disk read error"));

        // When & Then: Should wrap IOException in FileUploadException
        assertThatThrownBy(() -> s3Service.upload(faultyFile))
                .isInstanceOf(FileUploadException.class)
                .hasMessageContaining("Erro ao processar arquivo")
                .hasCauseInstanceOf(IOException.class);

        verifyNoInteractions(s3Client);
    }

    @Test
    @DisplayName("Should generate correct S3 URL format")
    void shouldGenerateCorrectS3Url() throws IOException {
        // Given: Valid file
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "content".getBytes()
        );

        // Mock S3 response
        PutObjectResponse putObjectResponse = (PutObjectResponse) PutObjectResponse.builder()
                .sdkHttpResponse(SdkHttpResponse.builder().statusCode(200).build())
                .build();
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(putObjectResponse);

        // When
        FileUploadResponse response = s3Service.upload(file);

        // Then: URL should follow AWS S3 format
        String expectedUrlPattern = String.format(
                "https://%s.s3.%s.amazonaws.com/",
                awsProperties.getS3BucketName(),
                awsProperties.getRegion()
        );
        assertThat(response.fileUrl()).startsWith(expectedUrlPattern);
        assertThat(response.fileUrl()).endsWith("test-image.jpg");
    }

    @Test
    @DisplayName("Should set uploadedAt timestamp close to now")
    void shouldSetUploadedAtTimestamp() throws IOException {
        // Given: Valid file
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "content".getBytes()
        );

        // Mock S3 response
        PutObjectResponse putObjectResponse = (PutObjectResponse) PutObjectResponse.builder()
                .sdkHttpResponse(SdkHttpResponse.builder().statusCode(200).build())
                .build();
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(putObjectResponse);

        // When
        Instant before = Instant.now();
        FileUploadResponse response = s3Service.upload(file);
        Instant after = Instant.now();

        // Then: uploadedAt should be between before and after
        assertThat(response.uploadedAt()).isBetween(before, after);
    }

}

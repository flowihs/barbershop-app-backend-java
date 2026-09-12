package com.github.barbershop.storage.service;

import com.github.barbershop.storage.dto.UploadResult;
import com.github.barbershop.common.error.exception.StorageException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StorageServiceImpl implements StorageService {

    @Value("${supabase.s3.endpoint}")
    private String endpoint;

    @Value("${supabase.s3.region}")
    private String region;

    @Value("${supabase.s3.access-key}")
    private String accessKey;

    @Value("${supabase.s3.secret-key}")
    private String secretKey;

    @Value("${supabase.s3.bucket}")
    private String bucket;

    @Value("${supabase.s3.public-url-base}")
    private String publicUrlBase;

    private S3Client s3;

    @PostConstruct
    public void init() {
        AwsBasicCredentials creds = AwsBasicCredentials.create(accessKey, secretKey);
        s3 = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .forcePathStyle(true)
                .credentialsProvider(StaticCredentialsProvider.create(creds))
                .region(Region.of(region))
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .build();
    }

    @Override
    public UploadResult uploadImage(MultipartFile file) {
        try {
            byte[] imageBytes = file.getBytes();
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";

            String uuid = UUID.randomUUID().toString();
            String key = uuid + extension;

            String contentType = file.getContentType();
            if (contentType == null) {
                contentType = "image/jpeg";
            }

            PutObjectRequest putReq = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3.putObject(putReq, RequestBody.fromBytes(imageBytes));

            String publicUrl = String.format("%s/%s/%s", publicUrlBase.replaceAll("/$", ""), bucket, key);
            return new UploadResult(key, publicUrl);

        } catch (Exception e) {
            throw new StorageException("Ошибка при загрузке изображения в хранилище: " + e.getMessage());
        }
    }

    @Override
    public List<UploadResult> uploadImages(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new StorageException("Список изображений не может быть пустым");
        }

        return files.stream()
                .map(this::uploadImage)
                .collect(Collectors.toList());
    }
}

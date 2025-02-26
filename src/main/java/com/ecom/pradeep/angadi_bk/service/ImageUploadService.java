package com.ecom.pradeep.angadi_bk.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

@Service
public class ImageUploadService {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;


//    private static final String BUCKET_NAME = "your-s3-bucket";
    private static final String UPLOAD_DIR = "uploads/";

    public ImageUploadService(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    public String uploadImage1(MultipartFile file) {
        try {
            Path uploadPath = Path.of(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + fileName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }

//    public String uploadImage(MultipartFile file) {
//        try {
//            File tempFile = File.createTempFile("upload-", file.getOriginalFilename());
//            file.transferTo(tempFile);
//
//            String fileName = "products/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
//            PutObjectResponse putObjectResponse = s3Client.putObject(new PutObjectRequest(bucketName, fileName, tempFile));
//
//            return s3Client.getUrl(bucketName, fileName).toString();
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to upload file", e);
//        }
//    }

    public void uploadFile(String bucketName, String key, byte[] data) {
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build(),
                RequestBody.fromBytes(data));

    }

    public String uploadImage(File file) {
        try {
            String fileName = "products/" + System.currentTimeMillis() + "_" + file.getName();

            // ✅ Upload file to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();
            PutObjectResponse putObjectResponse =s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));


            if (putObjectResponse.sdkHttpResponse().isSuccessful()) {
                // ✅ Generate a Presigned URL (valid for 7 days)
                GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofDays(7))
                        .getObjectRequest(r -> r.bucket(bucketName).key(fileName))
                        .build();

                PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
                return presignedRequest.url().toString();
            } else {
                throw new RuntimeException("S3 upload failed!");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    public String uploadImage(MultipartFile multipartFile) {
        try {
            // ✅ Convert MultipartFile to File
            File file = convertMultipartFileToFile(multipartFile);
            String fileName = "products/" + System.currentTimeMillis() + "_" + multipartFile.getOriginalFilename();

            // ✅ Upload file to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));


            // ✅ Generate a Presigned URL (valid for 7 days)
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofDays(7))
                    .getObjectRequest(r -> r.bucket(bucketName).key(fileName))
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

            // ✅ Delete temporary file after upload
            file.delete();

            return presignedRequest.url().toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    private File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
        File file = File.createTempFile("temp", multipartFile.getOriginalFilename());
        file.deleteOnExit(); // Ensures cleanup
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(multipartFile.getBytes());
        }
        return file;
    }


}

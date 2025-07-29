package webproject_2team.lunch_matching.util;

import webproject_2team.lunch_matching.dto.UploadResultDTO;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnailator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@Log4j2
public class UploadUtil {

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.accessKey}")
    private String minioAccessKey;

    @Value("${minio.secretKey}")
    private String minioSecretKey;

    @Value("${minio.bucketName}")
    private String minioBucketName;

    private S3Client s3Client;

    @PostConstruct
    public void init() {
        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(minioUrl))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(minioAccessKey, minioSecretKey)))
                .region(Region.US_EAST_1) // MinIO는 리전 개념이 없지만, S3 SDK 사용을 위해 아무 리전이나 설정
                .forcePathStyle(true) // MinIO와 같은 S3 호환 스토리지 사용 시 필요
                .build();
    }

    @Async
    public CompletableFuture<List<UploadResultDTO>> uploadFiles(List<MultipartFile> files) {

        List<UploadResultDTO> resultList = new ArrayList<>();

        for (MultipartFile multipartFile : files) {

            if (multipartFile.isEmpty()) {
                continue;
            }

            String originalName = multipartFile.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();

            boolean isImage = false;
            try {
                String encodedFileName = URLEncoder.encode(originalName, "UTF-8").replaceAll("\\+", "%20");
                String objectKey = uuid + "_" + encodedFileName;

                // 원본 파일 업로드
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(minioBucketName)
                        .key(objectKey)
                        .contentType(multipartFile.getContentType())
                        .build();
                s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(
                        multipartFile.getInputStream(), multipartFile.getSize()));

                // 이미지 파일 여부 확인 및 썸네일 생성 후 업로드
                if (multipartFile.getContentType() != null && multipartFile.getContentType().startsWith("image")) {
                    isImage = true;
                    String thumbnailObjectKey = "s_" + objectKey;

                    ByteArrayOutputStream thumbnailOutputStream = new ByteArrayOutputStream();
                    Thumbnailator.createThumbnail(multipartFile.getInputStream(), thumbnailOutputStream, 200, 200);
                    ByteArrayInputStream thumbnailInputStream = new ByteArrayInputStream(thumbnailOutputStream.toByteArray());

                    PutObjectRequest thumbnailPutObjectRequest = PutObjectRequest.builder()
                            .bucket(minioBucketName)
                            .key(thumbnailObjectKey)
                            .contentType(multipartFile.getContentType())
                            .build();
                    s3Client.putObject(thumbnailPutObjectRequest, RequestBody.fromInputStream(
                            thumbnailInputStream, thumbnailOutputStream.size()));
                }
            } catch (IOException e) {
                log.error("파일 업로드 중 오류 발생: " + originalName, e);
            }

            resultList.add(UploadResultDTO.builder()
                    .uuid(uuid)
                    .fileName(originalName) // 원본 파일명 저장
                    .img(isImage)
                    .build());
        }
        return CompletableFuture.completedFuture(resultList);
    }

    public ResponseInputStream<GetObjectResponse> getFileFromMinio(String objectKey) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(minioBucketName)
                    .key(objectKey)
                    .build();
            return s3Client.getObject(getObjectRequest);
        } catch (Exception e) {
            log.error("MinIO에서 파일(" + objectKey + ") 가져오기 실패: " + e.getMessage(), e);
            throw new RuntimeException("MinIO에서 파일 가져오기 실패", e);
        }
    }

    private String encodeFileNameForMinio(String fileName) throws java.io.UnsupportedEncodingException {
        return URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
    }

    public void deleteFileFromMinio(String objectKey) {
        try {
            // objectKey received here is like "UUID_test1 (1).jpg" or "s_UUID_test1 (1).jpg"

            boolean isThumbnail = objectKey.startsWith("s_");

            String uuidPart;
            String originalFileNamePart; // This is the DECODED original filename

            if (isThumbnail) {
                String temp = objectKey.substring(2); // Remove "s_"
                int firstUnderscore = temp.indexOf('_');
                uuidPart = temp.substring(0, firstUnderscore);
                originalFileNamePart = temp.substring(firstUnderscore + 1);
            } else {
                int firstUnderscore = objectKey.indexOf('_');
                uuidPart = objectKey.substring(0, firstUnderscore);
                originalFileNamePart = objectKey.substring(firstUnderscore + 1);
            }

            // Now, encode the originalFileNamePart for MinIO
            String encodedOriginalFileNamePart = encodeFileNameForMinio(originalFileNamePart);

            String finalOriginalObjectKey = uuidPart + "_" + encodedOriginalFileNamePart;
            String finalThumbnailObjectKey = "s_" + uuidPart + "_" + encodedOriginalFileNamePart;

            // 원본 파일 삭제 시도
            DeleteObjectRequest deleteOriginalRequest = DeleteObjectRequest.builder()
                    .bucket(minioBucketName)
                    .key(finalOriginalObjectKey)
                    .build();
            s3Client.deleteObject(deleteOriginalRequest);
            log.info("Deleted original file from MinIO: " + finalOriginalObjectKey);

            // 썸네일 파일 삭제 시도
            DeleteObjectRequest deleteThumbnailRequest = DeleteObjectRequest.builder()
                    .bucket(minioBucketName)
                    .key(finalThumbnailObjectKey)
                    .build();
            s3Client.deleteObject(deleteThumbnailRequest);
            log.info("Deleted thumbnail file from MinIO: " + finalThumbnailObjectKey);

        } catch (Exception e) {
            log.error("Error deleting file from MinIO: " + objectKey, e);
        }
    }
}
package webproject_2team.lunch_matching.controller;

import webproject_2team.lunch_matching.util.UploadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@RestController
@RequiredArgsConstructor
@Log4j2
public class FileController {

    private final UploadUtil uploadUtil;

    /*
    // MinIO에서 파일 가져오기
    // 문제점: 이전에는 Content-Type이 MediaType.IMAGE_JPEG로 고정되어 있어
    // JPEG가 아닌 다른 이미지 형식(PNG, GIF 등)이나 다른 파일 형식(PDF 등)을 요청할 경우
    // 브라우저에서 올바르게 표시되지 않거나 다운로드되지 않는 문제가 있었음.
    // 해결: MinIO에서 가져온 파일의 실제 Content-Type을 동적으로 설정하도록 수정.
     */


    private String getMinioObjectKey(String decodedFileName) throws UnsupportedEncodingException {
        int lastUnderscore = decodedFileName.lastIndexOf('_');
        if (lastUnderscore == -1) {
            return URLEncoder.encode(decodedFileName, "UTF-8").replaceAll("\\+", "%20");
        }

        String prefixPart = decodedFileName.substring(0, lastUnderscore + 1);
        String originalFileNamePart = decodedFileName.substring(lastUnderscore + 1);

        String encodedOriginalFileName = URLEncoder.encode(originalFileNamePart, "UTF-8").replaceAll("\\+", "%20");

        return prefixPart + encodedOriginalFileName;
    }


    @GetMapping("/view/{fileName}")
    public ResponseEntity<byte[]> getFile(@PathVariable String fileName) {
        try {
            String objectKey = getMinioObjectKey(fileName);
            log.info("Attempting to get file from MinIO with objectKey: " + objectKey);

            ResponseInputStream<GetObjectResponse> is = uploadUtil.getFileFromMinio(objectKey);
            byte[] data = is.readAllBytes();
            log.info("File data size: " + data.length + " bytes");
            String contentType = is.response().contentType();
            log.info("Content-Type from MinIO: " + contentType);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(data);
        } catch (Exception e) {
            log.error("파일 조회 실패: " + fileName, e);
            return ResponseEntity.notFound().build();
        }
    }


    @DeleteMapping("/removeFile/{fileName}")
    public ResponseEntity<String> removeFile(@PathVariable String fileName) {
        try {
            String objectKey = getMinioObjectKey(fileName);
            log.info("Attempting to delete file from MinIO with objectKey: " + objectKey);

            uploadUtil.deleteFileFromMinio(objectKey);
            return ResponseEntity.ok("File deleted successfully");
        } catch (Exception e) {
            log.error("파일 삭제 실패: " + fileName, e);
            return ResponseEntity.status(500).body("Failed to delete file");
        }
    }
}

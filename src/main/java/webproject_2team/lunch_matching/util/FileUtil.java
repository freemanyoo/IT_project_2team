package webproject_2team.lunch_matching.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class FileUtil {

    private final String uploadDir = "D:\\501CLASS\\Boot_Spring\\board\\src\\main\\resources\\static\\images\\";

    public String saveFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null;
        }

        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String savedFileName = UUID.randomUUID().toString() + extension;

        Path path = Paths.get(uploadDir + savedFileName);
        Files.write(path, file.getBytes());

        return "/images/" + savedFileName;
    }
}

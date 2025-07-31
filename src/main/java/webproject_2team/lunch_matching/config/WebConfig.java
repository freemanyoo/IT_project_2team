package webproject_2team.lunch_matching.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.beans.factory.annotation.Value;
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${com.busanit501.upload.path}") // application.properties에서 경로를 가져옴
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // "/uploads/**" 경로로 들어오는 요청을 실제 파일 시스템의 uploadPath로 매핑
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///" + uploadPath + "/");
        // 윈도우 경로에 역슬래시가 있다면 슬래시로 변경하거나 추가 처리 필요할 수 있음
        // file:///C:/upload/profile/  와 같이 슬래시로 끝나는 것이 중요
    }
}
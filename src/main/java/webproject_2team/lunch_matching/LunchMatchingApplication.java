package webproject_2team.lunch_matching;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("webproject_2team.lunch_matching.mapper")
public class LunchMatchingApplication {

	public static void main(String[] args) {
		SpringApplication.run(LunchMatchingApplication.class, args);
	}

}

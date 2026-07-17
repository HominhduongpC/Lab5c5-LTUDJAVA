package vn.edu.gdu.springjpalab;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // Chạy trên H2, không cần MySQL
class SpringJpaLabApplicationTests {

	@Test
	void contextLoads() {
	}

}

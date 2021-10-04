package security.ttaallkk;

import java.util.TimeZone;
import javax.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class TtaallkkApplication {

	@PostConstruct
	void timeZoneInit() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul")); // 타임존 설정(한국)
	}

	public static void main(String[] args) {
		SpringApplication.run(TtaallkkApplication.class, args);
	}

}

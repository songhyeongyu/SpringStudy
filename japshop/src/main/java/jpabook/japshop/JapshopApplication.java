package jpabook.japshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.sql.DataSource;

@SpringBootApplication
public class JapshopApplication {
	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(JapshopApplication.class, args);
	}
}

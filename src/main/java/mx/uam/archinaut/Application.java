package mx.uam.archinaut;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan("mx.uam.archinaut.model")
@ComponentScan(basePackages = "mx.uam.archinaut")
public class Application {
	public static void main(String[] args) {
		new SpringApplicationBuilder(Application.class).web(WebApplicationType.NONE).headless(true).run(args);
	}
}

package iuh.fit.se.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "iuh.fit.se.ecommerce.entity")
@EnableJpaRepositories(basePackages = "iuh.fit.se.ecommerce.repository")
public class SpringbootEcommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootEcommerceApplication.class, args);
    }

}


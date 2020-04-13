package cn.bit.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

/**
 * Created by terry on 2018/1/14.
 */
@Configuration
@ComponentScan(basePackages = "cn.bit")
@ImportResource(value = "classpath:spring/spring-context.xml")
@SpringBootApplication
//@SpringBootApplication(exclude={DataSourceAutoConfiguration.class,HibernateJpaAutoConfiguration.class})
public class ApiApplication {


    public static void main(String[] args)               {
        SpringApplication.run(ApiApplication.class, args);
    }

    @Bean
    public MethodValidationPostProcessor mvp(){
        return new MethodValidationPostProcessor();
    }

}

# AWS Service

- required aws.accessKeyId, aws.secretAccessKey in `resources/application.properties`

##Usage

```java
@ComponentScan("com.ziponia.aws")
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
```
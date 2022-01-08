package app.prime.jupiter.integration;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.Assert.assertEquals;

@CucumberContextConfiguration
@SpringBootTest
@Testcontainers
public class CucumberBootstrap {
    private RedisBackedCache underTest;

    // container {
    @Container
    public static GenericContainer redis = new GenericContainer(DockerImageName.parse("redis:5.0.3-alpine"))
            .withReuse(true)
            .withExposedPorts(6379);
    // }

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("redis.host", redis::getHost);
        registry.add("redis.port", redis::getFirstMappedPort);
    }

    @Before
    public void beforeEachScenario(){
        redis.start();
        String address = redis.getHost();
        Integer port = redis.getFirstMappedPort();
        // Now we have an address and port for Redis, no matter where it is running
        underTest = new RedisBackedCache(address, port);

    }


    @Given("^the bag is empty$")
    public void the_bag_is_empty() {
        underTest.put("test", "example");
        String retrieved = underTest.get("test");
        assertEquals("example", retrieved);
    }
}

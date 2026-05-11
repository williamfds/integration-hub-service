package com.williamfds.integrationhub;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Set;

@SpringBootTest
public abstract class AbstractIntegrationIT {

    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("integration_hub")
            .withUsername("hub")
            .withPassword("hub");

    @SuppressWarnings("resource")
    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @SuppressWarnings("resource")
    static final RabbitMQContainer RABBIT = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3-management-alpine"));

    static {
        POSTGRES.start();
        REDIS.start();
        RABBIT.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        registry.add("spring.rabbitmq.host", RABBIT::getHost);
        registry.add("spring.rabbitmq.port", RABBIT::getAmqpPort);
        registry.add("spring.rabbitmq.username", RABBIT::getAdminUsername);
        registry.add("spring.rabbitmq.password", RABBIT::getAdminPassword);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StringRedisTemplate redis;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private List<Queue> queues;

    @Autowired
    private RabbitListenerEndpointRegistry listenerRegistry;

    @BeforeEach
    void cleanState() throws InterruptedException {
        listenerRegistry.stop();
        waitForOrdersToSettle();

        jdbcTemplate.execute("TRUNCATE TABLE orders");
        deleteKeysMatching("webhook:*");
        deleteKeysMatching("ratelimit:*");
        queues.forEach(q -> rabbitAdmin.purgeQueue(q.getName(), true));

        listenerRegistry.start();
    }

    private void waitForOrdersToSettle() throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5_000;
        Long last = countOrders();
        int stable = 0;
        while (stable < 300 && System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
            Long current = countOrders();
            if (current.equals(last)) {
                stable += 50;
            } else {
                stable = 0;
                last = current;
            }
        }
    }

    private Long countOrders() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders", Long.class);
        return count == null ? 0L : count;
    }

    private void deleteKeysMatching(String pattern) {
        Set<String> keys = redis.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redis.delete(keys);
        }
    }
}

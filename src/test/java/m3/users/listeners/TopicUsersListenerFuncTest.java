package m3.users.listeners;

import m3.lib.enums.SocNetType;
import m3.users.dto.rq.AuthRqDto;
import m3.users.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

//@SpringBootTest(properties = {"spring.autoconfigure.exclude= " +
//        "  org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration, " +
//        "  org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
//        "  org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration"})
@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
@ActiveProfiles("test")
public class TopicUsersListenerFuncTest {

    @MockBean
    UserService userService;

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topicName}")
    private String topicName;

    @Test
    public void testOne() {

        SocNetType socNetType = SocNetType.VK;
        Long socNetUserId = 1000L;
        Long appId = 100L;
        Long connectionId = 200L;
        String authKey = "authKey";

        kafkaTemplate.send("topic-users", AuthRqDto
                .builder()
                .socNetType(socNetType)
                .socNetUserId(socNetUserId)
                .appId(appId)
                .connectionId(connectionId)
                .authKey(authKey)
                .build());

        verify(userService, timeout(100000)).auth(any());
        //@todo how to wait until kafka consume some
    }
}

package m3.users.listeners;

import m3.lib.enums.SocNetType;
import m3.users.dto.rq.AuthRqDto;
import m3.users.dto.rs.AuthSuccessRsDto;
import m3.users.services.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

//@SpringBootTest(properties = {"spring.autoconfigure.exclude= " +
//        "  org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration, " +
//        "  org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
//        "  org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration"})
@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = {
        "offsets.topic.num.partitions=1"
//        "listeners=PLAINTEXT://localhost:19092",
//        "port=19092"
})
@ActiveProfiles("test")
public class TopicUsersListenerFuncTest {

    @MockBean
    UserService userService;

    @MockBean
    CommonErrorHandler commonLoggingErrorHandler;

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topicName}")
    private String topicName;

    @Test
    public void testOne() throws InterruptedException {

        SocNetType socNetType = SocNetType.VK;
        Long socNetUserId = 1000L;
        Long appId = 100L;
        Long connectionId = 200L;
        String authKey = "authKey";

        doReturn(new AuthSuccessRsDto())
                .when(userService)
                .auth(any());

        kafkaTemplate.send("topic-users", AuthRqDto
                .builder()
                .socNetType(socNetType)
                .socNetUserId(socNetUserId)
                .appId(null)
                .connectionId(connectionId)
                .authKey(authKey)
                .build());
        //@Todo argument capture
        //@todo fixtures
        var argumentCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(commonLoggingErrorHandler, timeout(1000)).handleOne(argumentCaptor.capture(), any(), any(), any());

        argumentCaptor.getValue();
        // catch the error!
        // is it a throwing exception
        // that's got o errro handler
        // and of the end to...
        //  verify(userService, timeout(1000)).auth(any());

        //@todo how to wait until kafka consume some
    }
}

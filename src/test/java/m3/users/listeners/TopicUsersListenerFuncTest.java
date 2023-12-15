package m3.users.listeners;

import m3.lib.enums.SocNetType;
import m3.users.dto.rq.AuthRqDto;
import m3.users.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.test.annotation.DirtiesContext;

import java.util.stream.Stream;

import static m3.lib.enums.SocNetType.VK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {
        TopicUsersListener.class,
        ValidationAutoConfiguration.class})
@ComponentScan({"m3.lib.kafka"})
@EnableKafka
@EmbeddedKafka(partitions = 1, brokerProperties = {"offsets.topic.num.partitions=1"})
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
    void testPosit34ive() {
        kafkaTemplate.send("topic-users",
                buildAuthRqDto(VK, 1L, 2L, "auhKey", 3L)
        );

        verify(userService, timeout(5000))
                .auth(any());
    }

    @ParameterizedTest
    @MethodSource("testOneSource")
    void testOne(AuthRqDto rqDto, String expectedErrMsg1, String expectedErrMsg2) throws InterruptedException {
        // given - when

        System.out.println(">>>>" + rqDto.toString());
        kafkaTemplate.send("topic-users", rqDto);
        doReturn(true)
                .when(commonLoggingErrorHandler)
                .handleOne(any(), any(), any(), any());

        // @todo fixtures
        // then
        var argumentCaptor = ArgumentCaptor.forClass(Exception.class);

        verify(commonLoggingErrorHandler, timeout(55000))
                .handleOne(argumentCaptor.capture(), any(), any(), any());

        Exception e = argumentCaptor.getValue();
        System.out.println("------");
        System.out.println(e.getCause().getCause().getMessage());
        assertArgumentNotValidException(e.getCause().getCause(), expectedErrMsg1, expectedErrMsg2);
    }

    @Test
    public void testOne2() throws InterruptedException {
        // given - when

        var rqDto1 = buildAuthRqDto(VK, 10011L, 345L, null, 34L);
        System.out.println(">>>>" + rqDto1.toString());
        kafkaTemplate.send("topic-users", rqDto1);

        var rqDto2 = buildAuthRqDto(VK, 10022L, 345L, null, 34L);
        System.out.println(">>>>" + rqDto2.toString());
        kafkaTemplate.send("topic-users", rqDto2);

        // @todo fixtures
        // then
        var argumentCaptor = ArgumentCaptor.forClass(Exception.class);

        verify(commonLoggingErrorHandler, timeout(55000))
                .handleOne(argumentCaptor.capture(), any(), any(), any());

        verify(commonLoggingErrorHandler, timeout(55000))
                .handleOne(argumentCaptor.capture(), any(), any(), any());

        Exception e = argumentCaptor.getValue();
        System.out.println("------");
        System.out.println(e.getCause().getCause().getMessage());
        assertArgumentNotValidException(e.getCause().getCause(),
                "Field error in object 'authRqDto' on field 'authKey': rejected value [null]",
                "must not be blank");
        Thread.sleep(15000);
    }

    public static Stream<Arguments> testOneSource() {
        return Stream.of(
                Arguments.of(buildAuthRqDto(VK, 2L, null, "authKey2", 4L), "Field error in object 'authRqDto' on field 'appId': rejected value [null]", "must not be null"),
                Arguments.of(buildAuthRqDto(null, 1L, 2L, "authKey1", 3L), "Could not resolve method parameter at index 0 in public m3.users.dto.rs.AuthSuccessRsDto m3.users.listeners.TopicUsersListener.auth(m3.users.dto.rq.AuthRqDto): 1 error(s): [Field error in object 'authRqDto' on field 'socNetType': rejected value [null]", "must not be null")
        );
    }

    private static AuthRqDto buildAuthRqDto(
            SocNetType socNetType, Long socNetUserId, Long appId, String authKey, Long connectionId) {
        return AuthRqDto.builder()
                .socNetType(socNetType)
                .socNetUserId(socNetUserId)
                .appId(appId)
                .authKey(authKey)
                .connectionId(connectionId)
                .build();
    }


    private static void assertArgumentNotValidException(Throwable e, String errMsg1, String errMsg2) {
        assertThat(e)
                .isInstanceOf(MethodArgumentNotValidException.class)
                .hasMessageContainingAll(
                        "Could not resolve method parameter",
                        "public m3.users.dto.rs.AuthSuccessRsDto m3.users.listeners.TopicUsersListener.auth(m3.users.dto.rq.AuthRqDto)",
                        errMsg1,
                        errMsg2
                );
    }

    private static void assertListenerMethodCouldNotBeInvoked(Throwable e) {
        assertThat(e)
                .isInstanceOf(ListenerExecutionFailedException.class)
                .hasMessageContaining("Listener method could not be invoked with the incoming message");
    }

    private static void assertListenerErrorHandlerForIncomingMessage(Throwable e) {
        assertThat(e)
                .isInstanceOf(ListenerExecutionFailedException.class)
                .hasMessageContainingAll(
                        "Listener error handler threw an exception for the incoming message",
                        "Method [public m3.users.dto.rs.AuthSuccessRsDto m3.users.listeners.TopicUsersListener.auth(m3.users.dto.rq.AuthRqDto)]",
                        "Bean [m3.users.listeners.TopicUsersListener@" /*@29ceefb3]*/);
    }
}


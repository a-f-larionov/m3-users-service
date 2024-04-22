package m3.users.validation;

import m3.lib.enums.SocNetType;
import m3.users.BaseSpringBootTest;
import m3.users.dto.rq.AuthRqDto;
import m3.users.dto.rs.AuthSuccessRsDto;
import m3.users.listeners.TopicUsersListener;
import m3.users.services.UserService;
import org.junit.jupiter.api.Disabled;
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
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

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
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class TopicUsersListenerAuthValidationTest extends BaseSpringBootTest {
    @Value("${spring.kafka.topicName}")
    private String topicName;
    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;
    @SpyBean
    CommonErrorHandler commonErrorHandler;
    @MockBean
    UserService userService;

    @Test
    @Disabled
    void positive() {
        // given
        doReturn(new AuthSuccessRsDto())
                .when(userService)
                .auth(any());
        // when
        kafkaTemplate.send(topicName,
                buildAuthRqDto(VK, 1L, 2L, "auhKey", 3L)
        );
        // then
        verify(userService, timeout(3000))
                .auth(any());
    }

    @ParameterizedTest
    @MethodSource("testSource")
    @Disabled
    void negative(AuthRqDto rqDto, String expectedErrMsg1, String expectedErrMsg2) {
        // given - when
        var argumentCaptor = ArgumentCaptor.forClass(Exception.class);
        kafkaTemplate.send("topic-users", rqDto);

        // then
        verify(commonErrorHandler, timeout(3000))
                .handleRemaining(argumentCaptor.capture(), any(), any(), any());

        Exception e = argumentCaptor.getValue();
        assertArgumentNotValidExceptionMessages(e.getCause(), expectedErrMsg1, expectedErrMsg2);
    }

    private static Stream<Arguments> testSource() {
        return Stream.of(
                Arguments.of(buildAuthRqDto(null, 2L, 3L, "authKey4", 5L),
                        "Field error in object 'authRqDto' on field 'socNetType': rejected value [null]", "must not be null"),
                Arguments.of(buildAuthRqDto(VK, null, 3L, "authKey4", 5L),
                        "Field error in object 'authRqDto' on field 'socNetUserId': rejected value [null]", "must not be null"),
                Arguments.of(buildAuthRqDto(VK, 2L, null, "authKey4", 5L),
                        "Field error in object 'authRqDto' on field 'appId': rejected value [null]", "must not be null"),
                Arguments.of(buildAuthRqDto(VK, 2L, 3L, null, 4L),
                        "Field error in object 'authRqDto' on field 'authKey': rejected value [null]", "must not be blank"),
                Arguments.of(buildAuthRqDto(VK, 2L, 3L, " ", 4L),
                        "Field error in object 'authRqDto' on field 'authKey': rejected value [ ]", "must not be blank"),
                Arguments.of(buildAuthRqDto(VK, 2L, 3L, " ", null),
                        "Field error in object 'authRqDto' on field 'connectionId': rejected value [null]", "must not be null")
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

    private static void assertArgumentNotValidExceptionMessages(Throwable e, String errMsg1, String errMsg2) {
        assertThat(e)
                .isInstanceOf(MethodArgumentNotValidException.class)
                .hasMessageContainingAll(
                        "Could not resolve method parameter",
                        "public m3.users.dto.rs.AuthSuccessRsDto m3.users.listeners.TopicUsersListener.auth(m3.users.dto.rq.AuthRqDto)",
                        errMsg1,
                        errMsg2
                );
    }
}


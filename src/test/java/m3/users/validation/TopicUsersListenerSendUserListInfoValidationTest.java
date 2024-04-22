package m3.users.validation;

import m3.users.BaseSpringBootTest;
import m3.users.dto.rq.SendUserListInfoRqDto;
import m3.users.dto.rs.UpdateUserListInfoRsDto;
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
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {
        TopicUsersListener.class,
        ValidationAutoConfiguration.class, KafkaAutoConfiguration.class})
@ComponentScan({"m3.lib.kafka"})
@EnableKafka
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class TopicUsersListenerSendUserListInfoValidationTest extends BaseSpringBootTest {

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
        doReturn(new UpdateUserListInfoRsDto())
                .when(userService)
                .getUsers(any(),anyList());
        // when
        kafkaTemplate.send(topicName,
                buildRqDto((long) (Math.random() * 10000), List.of(1L, 2L, 3L)));
        // then
        verify(userService, timeout(3000))
                .getUsers(any(), anyList());
    }

    @ParameterizedTest
    @MethodSource("testSource")
    public void negative(SendUserListInfoRqDto rqDto, String expectedErrMsg1, String expectedErrMsg2) {
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
                Arguments.of(buildRqDto(null, List.of(1L)),
                        "on field 'userId': rejected value [null]", "must not be null"),
                Arguments.of(buildRqDto(1L, null),
                        "'rq' on field 'ids'", "rejected value [null]"),
                Arguments.of(buildRqDto(1L, new ArrayList<>()),
                        "on field 'ids': rejected value [[]]", "must not be empty")
        );
    }

    private static SendUserListInfoRqDto buildRqDto(Long userId, List<Long> userIds) {
        return SendUserListInfoRqDto.builder()
                .userId(userId)
                .ids(userIds)
                .build();
    }

    private static void assertArgumentNotValidExceptionMessages(Throwable e, String errMsg1, String errMsg2) {
        assertThat(e)
                .isInstanceOf(MethodArgumentNotValidException.class)
                .hasMessageContainingAll(
                        "Could not resolve method parameter",
                        "public m3.users.dto.rs.UpdateUserListInfoRsDto m3.users.listeners.TopicUsersListener.sendUserListInfo(m3.users.dto.rq.SendUserListInfoRqDto)",
                        errMsg1,
                        errMsg2
                );
    }
}

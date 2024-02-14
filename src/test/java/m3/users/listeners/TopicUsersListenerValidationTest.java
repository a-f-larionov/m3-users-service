package m3.users.listeners;

import m3.lib.enums.SocNetType;
import m3.users.BaseSpringBootTest;
import m3.users.dto.rq.AuthRqDto;
import m3.users.dto.rs.AuthSuccessRsDto;
import m3.users.services.UserService;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.hibernate.metamodel.internal.AbstractDynamicMapInstantiator;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static m3.lib.enums.SocNetType.VK;
import static m3.lib.enums.SocNetType.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {
        TopicUsersListener.class,
        ValidationAutoConfiguration.class})
@ComponentScan({"m3.lib.kafka"})
@EnableKafka
@ActiveProfiles("test")
public class TopicUsersListenerValidationTest extends BaseSpringBootTest {

    @Autowired
    ApplicationContext applicationContext;

    @Value("${spring.kafka.topicName}")
    private String topicName;

    @MockBean
    UserService userService;

    @MockBean
    CommonErrorHandler commonErrorHandler;

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @BeforeEach
    public void beforEach() {
        System.out.printf("----BEFORE EACH");
    }

    @Test
    void positive() {
        deleteTopic("topic-users");
        // given
        doReturn(new AuthSuccessRsDto())
                .when(userService)
                .auth(any());

        // when
        kafkaTemplate.send(topicName,
                buildAuthRqDto(VK, 1L, 2L, "auhKey", 3L)
        );

        // then
        verify(userService, timeout(6000))
                .auth(any());
    }

    private static void deleteTopic(String topicName) {
        String groupName = "group_1";
        Properties config = new Properties();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:" + BaseSpringBootTest.kafkaContainer.getMappedPort(9093));
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupName);
        config.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        AdminClient adminClient = KafkaAdminClient.create(config);
//        var c = new KafkaConsumer(config);
        //c.subscribe(List.of("topic-users"));

        //TopicPartition topicPartition = new TopicPartition("topic-users", 0);
        //c.assign(List.of(topicPartition));
//        Long endOffset = (Long) c.endOffsets(List.of(topicPartition))
//                .get(topicPartition);
//        DeleteTopicsResult deleteTopicsResult = adminClient.deleteTopics(List.of(topicName));
//        while (deleteTopicsResult.all().isDone()) {
//
//        }
//        AlterConsumerGroupOffsetsResult group1 = adminClient
//                .alterConsumerGroupOffsets(groupName,
//                        Map.of(new TopicPartition(topicName, 0), new OffsetAndMetadata(endOffset)));
//        while (group1.all().isDone()) {
//
//        }
//        try {
////            ConsumerRecords poll = c.poll(Duration.ofSeconds(19L));
////            c.seekToEnd(List.of(topicPartition));
////            c.commitSync();
//        } catch (Throwable t) {
//            System.out.printf(t.getMessage());
//        }

//        Map<TopicPartition, OffsetAndMetadata> topicPartitionOffsetAndMetadataMap;
//        try {
//            topicPartitionOffsetAndMetadataMap = adminClient.listConsumerGroupOffsets(groupName).partitionsToOffsetAndMetadata().get();
//        } catch (InterruptedException | ExecutionException e) {
//            throw new RuntimeException(e);
//        }

    }

    @ParameterizedTest
    @MethodSource("testSource")
    void negative(AuthRqDto rqDto, String expectedErrMsg1, String expectedErrMsg2) {
        deleteTopic("topic-users");
        // given - when
        kafkaTemplate.send("topic-users", rqDto);
        doReturn(true)
                .when(commonErrorHandler)
                .handleOne(any(), any(), any(), any());

        // then
        var argumentCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(commonErrorHandler, timeout(5000))
                .handleOne(argumentCaptor.capture(), any(), any(), any());

        Exception e = argumentCaptor.getValue();
        assertArgumentNotValidException(e.getCause(), expectedErrMsg1, expectedErrMsg2);
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
}


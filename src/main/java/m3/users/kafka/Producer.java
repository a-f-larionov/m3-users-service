package m3.users.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import static m3.users.kafka.ConfigAdmin.topicName;

@Component
public class Producer {
    @Autowired
    public KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String msg) {
        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send("topic5", msg);

        future.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onFailure(Throwable ex) {
                System.out.println("failure send message to kafka: " + ex.getMessage());
            }

            @Override
            public void onSuccess(SendResult<String, String> result) {

            }
        });

    }
}

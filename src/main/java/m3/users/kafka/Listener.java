package m3.users.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class Listener {

    @Autowired
    private Producer producer;

    @KafkaListener(topics = "topic4", groupId = "foo")
    public void listenGroupFoo(String message) {

        System.out.println("Received Message in group foo: " + message);

        producer.sendMessage(message);
    }
}

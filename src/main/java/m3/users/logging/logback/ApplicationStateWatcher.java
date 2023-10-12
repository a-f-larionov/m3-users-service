package m3.users.logging.logback;


import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j(topic = "users-service")
public class ApplicationStateWatcher {

    @EventListener(ApplicationReadyEvent.class)
    public void readyEvent() {
        log.warn("Ready to survive ⭐!");
    }

    @EventListener(ApplicationFailedEvent.class)
    public void failedEvent() {
        log.warn("Failed to run ❌");
    }

    @PreDestroy
    public void preDestroy(){
        log.warn("Pre destroy state \uD83D\uDCA4");
    }
}

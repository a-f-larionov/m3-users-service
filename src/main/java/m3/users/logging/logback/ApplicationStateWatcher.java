package m3.users.logging.logback;


import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j(topic = "watcher")
public class ApplicationStateWatcher {

    @EventListener(ApplicationReadyEvent.class)
    public void readyEvent() {
        log.info("Ready to survive ⭐!");
    }

    @EventListener(ApplicationFailedEvent.class)
    public void failedEvent() {
        log.info("Failed to run ❌");
    }

    @PreDestroy
    public void preDestroy() {
        log.info("Pre destroy state \uD83D\uDCA4");
    }
}

package m3.users.logging.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import lombok.Setter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TelegramAppender extends AppenderBase<ILoggingEvent> {

    @Setter
    private Encoder<ILoggingEvent> encoder;
    @Setter
    private String token;
    @Setter
    private String chatId;

    @Override
    protected void append(ILoggingEvent eventObject) {
        // Skip messages here
        if (eventObject.getMessage().equals("These configurations '{}' were supplied but are not used yet.")) {
            return;
        }
        byte[] encodedBytes = encoder.encode(eventObject);
        String encodedMessage = new String(encodedBytes, StandardCharsets.UTF_8);
        sendToTelegram(encodedMessage);
    }

    private void sendToTelegram(String encodedString) {
        var endpoint = "https://api.telegram.org/bot"
                + token
                + "/sendMessage" +
                "?chat_id=" + chatId +
                "&text=" + URLEncoder.encode(encodedString, StandardCharsets.UTF_8);

        endpoint = endpoint.substring(0, Math.min(endpoint.length(), 1024));

        ProcessBuilder pb = new ProcessBuilder();
        pb.command("curl", endpoint, "--ssl-no-revoke");
        Process p;
        try {
            p = pb.start();
            p.waitFor();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
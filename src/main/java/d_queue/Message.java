package d_queue;

import static java.time.ZoneOffset.UTC;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message implements Serializable {

    private final String message;
    private final long epochSecond;

    public Message(String message) {
        this.message = message;
        this.epochSecond = LocalDateTime.now().toEpochSecond(UTC);
    }

    @Override
    public String toString() {
        return LocalDateTime
                .ofEpochSecond(epochSecond, 0, UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                + " " + message;
    }
}

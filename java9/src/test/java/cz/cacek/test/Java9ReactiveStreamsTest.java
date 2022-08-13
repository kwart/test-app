package cz.cacek.test;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

/**
 * Check the <a href="https://www.baeldung.com/java-9-reactive-streams">Baeldung tutorial</a>.
 */
public class Java9ReactiveStreamsTest {

    @Test
    public void reactiveStreams() throws Exception {
        // given
        SubmissionPublisher<String> publisher = new SubmissionPublisher<>();
        MySubscriber subscriber = new MySubscriber();
        publisher.subscribe(subscriber);
        List<String> items = List.of("ahoj", "hello", "merhaba", "cau", "bye");

        // when
        assertEquals(1, publisher.getNumberOfSubscribers());
        items.forEach(publisher::submit);
        publisher.close();

        // then
        await().atMost(1000, TimeUnit.MILLISECONDS).until(() -> subscriber.completed);
        assertEquals(3, subscriber.letterACounter.get());
    }

    public static class MySubscriber implements Subscriber<String> {

        Subscription subscription;
        AtomicLong letterACounter = new AtomicLong();
        Throwable lastError;
        boolean completed;

        @Override
        public void onSubscribe(Subscription subscription) {
            this.subscription = subscription;
            subscription.request(1L);
        }

        @Override
        public void onNext(String item) {
            if (item.contains("a")) {
                letterACounter.incrementAndGet();
            }
            subscription.request(1L);
        }

        @Override
        public void onError(Throwable throwable) {
            lastError = throwable;
            throwable.printStackTrace();
        }

        @Override
        public void onComplete() {
            completed = true;
        }
    }
}

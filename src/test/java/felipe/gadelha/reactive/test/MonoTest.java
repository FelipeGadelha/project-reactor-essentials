package felipe.gadelha.reactive.test;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class MonoTest {

    private static final Logger log = LoggerFactory.getLogger(MonoTest.class.getSimpleName());

    @BeforeAll
    public static void setUp() {
        BlockHound.install();
    }

    @Test
    public void blockHoundWorks() {
        Mono.delay(Duration.ofSeconds(1))
                .doOnNext(it -> {
                    try {
                        Thread.sleep(10);
                    }
                    catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .block();
//        try {
//            FutureTask<?> task = new FutureTask<>(() -> {
//                Thread.sleep(0);
//                return "";
//            });
//            Schedulers.parallel().schedule(task);
//            task.get(10, TimeUnit.SECONDS);
//            Assertions.fail("should fail");
//        } catch (Exception e) {
//            Assertions.assertTrue(e.getCause() instanceof BlockingOperationError);
//        }
    }

    @Test
    public void monoSubscriber(){
        String name = "Felipe Gadelha";
        Mono<String> mono = Mono.just(name)
                .log(); // publisher

        mono.subscribe();
        log.info("-------------------------------");
        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();
    }
    @Test
    public void monoSubscriberConsumer(){

        String name = "Felipe Gadelha";
        Mono<String> mono = Mono.just(name)
                .log(); // publisher

        mono.subscribe(s -> log.info("Value {}", s));
        log.info("-------------------------------");
        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();
    }
    @Test
    public void monoSubscriberConsumerError(){

        String name = "Felipe Gadelha";
        Mono<String> mono = Mono.just(name)
                .map(s -> {throw new RuntimeException("Testing mono with error");});

        mono.subscribe(s -> log.info("Name {}", s), s -> log.error("Something bad happened"));
        mono.subscribe(s -> log.info("Name {}", s), Throwable::printStackTrace);
        log.info("-------------------------------");
        StepVerifier.create(mono)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    public void monoSubscriberConsumerComplete(){

        String name = "Felipe Gadelha";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase); // publisher

        mono.subscribe(
                s -> log.info("Value {}", s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED!")
        );
        log.info("-------------------------------");
        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerSubscription(){

        String name = "Felipe Gadelha";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase); // publisher

        mono.subscribe(
                s -> log.info("Value {}", s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED!"),
                Subscription::cancel
        );

        log.info("-------------------------------");

//        StepVerifier.create(mono)
//                .expectNext(name.toUpperCase())
//                .verifyComplete();
    }

    @Test
    public void monoDoOnMethods(){

        String name = "Felipe Gadelha";
        Mono<Object> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase)
                .doOnSubscribe(subscription -> log.info("Subscribed"))
                .doOnRequest(longNumber -> log.info("Request Received, starting doing something..."))
                .doOnNext(s -> log.info("Value is here. Executing doOnNext {}", s))
                .flatMap(s -> Mono.empty())
                .doOnNext(s -> log.info("Value is here. Executing doOnNext {}", s))
                .doOnSuccess(s -> log.info("doOnSuccess executed {}", s))
                ;

        mono.subscribe(
                s -> log.info("Value {}", s),
                Throwable::printStackTrace,
                () -> log.info("FINISHED!")
        );
        log.info("-------------------------------");
//        StepVerifier.create(mono)
//                .expectNext(name.toUpperCase())
//                .verifyComplete();
    }

    @Test
    public void monoDoOnError(){
        String name = "Felipe Gadelha";

        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal argument exception"))
                .doOnError(e -> log.error("Error message: {}", e.getMessage()))
                .onErrorResume(s -> {
                    log.info("Inside On Error Resume");
                    return Mono.just(name);
                })
                .log();

        log.info("-------------------------------");
        StepVerifier.create(error)
//                .expectError(IllegalArgumentException.class)
                .expectNext("Felipe Gadelha")
                .expectComplete()
                .verify();
    }
}

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=teacurran_dissipate-server&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=teacurran_dissipate-server)

running in docker-compose:
```
docker compose up
```

for an interactive shell for development/testing:

```
docker-compose run api
```


building native:
```bash
DOCKER_BUILDKIT=0 docker build -f src/main/docker/Dockerfile.native -t dissipate-api-native:latest .
```


Native Notes:

```bash
export GRAALVM_HOME=$HOME/.sdkman/candidates/java/24.ea.3-graal
```


Hosting:

* Minio - S3 compatible storage
  * JBOD - Just a Bunch of Disks design
  * write to SSD cluster then move to HDD to reads
* Postgres - Database
* RabbitMQ - Message Queue
* SMS - Must be hosted External
  * Twilio
  * Sendbird - probably cheaper for small volume
* Stripe - Payments
* 


## Some things I've been trying:
  
```java

  // way of using CompletableFuture to test async code
  @Test
  void registerByEmail(UniAsserter asserter) {
    String email = "create-" + new Random().nextInt() + "@grilledcheese.com";

    CompletableFuture<RegisterResponse> message = new CompletableFuture<>();
    client.register(RegisterRequest.newBuilder()
        .setEmail("tea@grilledcheese.com").build())
      .subscribe().with(response -> {
        LOGGER.info("Response: " + response);
        message.complete(response);
      });

    Assertions.assertThrows(ExecutionException.class, () ->
      message.get(5, TimeUnit.SECONDS));
  }
  
  @Test
  void registerByEmail(UniAsserter asserter) {
    String email = "test-" + new Random().nextInt() + "X-invalid-email.co.uk";

    //  one way to test
    //  doesn 't work with reactive transactions
    UniAssertSubscriber<RegisterResponse> subscriber = client.register(RegisterRequest.newBuilder()
        .setEmail(email).build())
      .subscribe().withSubscriber(UniAssertSubscriber.create());
    subscriber.awaitFailure().assertFailedWith(StatusRuntimeException.class, "INVALID_ARGUMENT: The email address is invalid.");
  }
  
  // UniAaserter can be used to test a Reactive db transaction
  // within this method, database tranasactions will work, but grpc client calls will not
  @Test
  @TestReactiveTransaction
  void registerByEmail(UniAsserter asserter) {
    asserter.execute(() -> {
      new SessionValidation().persistAndFlush();
    });
  }

  @Test
  void registerByEmail(UniAsserter asserter) {

    //     why doesn't this work?
    Uni<Message> uniMessage = Multi.createBy()
      .repeating()
      .supplier(() -> mailbox.findFirst(email))
      .withDelay(Duration.ofMillis(500))
      .atMost(4)
      .onFailure().invoke(throwable -> LOGGER.error("Error: " + throwable.getMessage(), throwable))
      .toUni()
      .until(value -> {
      LOGGER.info("Value: " + value);
      return Objects.nonNull(value);
     }).toUni();

  }
```

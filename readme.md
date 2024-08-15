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



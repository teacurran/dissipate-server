[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=teacurran_dissipate-server&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=teacurran_dissipate-server)

## prerequisites
* [Google cloud SDK](https://cloud.google.com/sdk/docs/install)

running in docker-compose:
```
gcloud auth application-default login
docker-compose up
```

for an interactive shell for development/testing:

```
docker-compose run api
```


building native:
```
DOCKER_BUILDKIT=0 docker build -f src/main/docker/Dockerfile.native -t dissipate-api-native:latest .
```



```
gcloud projects add-iam-policy-binding 510014216261 --member='serviceAccount:510014216261@cloudbuild.gserviceaccount.com' --role='roles/secretmanager.secretAccessor'
```


export GRAALVM_HOME=$HOME/.sdkman/candidates/java/24.ea.3-graal

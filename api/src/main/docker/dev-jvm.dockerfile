####
# This Dockerfile is used in order to build a container that runs the Quarkus application in JVM mode
#
# Before building the container image run:
#
# ./mvnw package
#
# Then, build the image with:
#
# docker build -f src/main/docker/dev-jvm.dockerfile -t quarkus/getting-started-jvm .
#
# Then run the container using:
#
# docker run -i --rm -p 8080:8080 quarkus/getting-started-jvm
#
# If you want to include the debug port into your docker image
# you will have to expose the debug port (default 5005) like this :  EXPOSE 8080 5005
#
# Then run the container using :
#
# docker run -i --rm -p 8080:8080 quarkus/getting-started-jvm
#
# This image uses the `run-java.sh` script to run the application.
# This scripts computes the command line to execute your Java application, and
# includes memory/GC tuning.
# You can configure the behavior using the following environment properties:
# - JAVA_OPTS: JVM options passed to the `java` command (example: "-verbose:class")
# - JAVA_OPTS_APPEND: User specified Java options to be appended to generated options
#   in JAVA_OPTS (example: "-Dsome.property=foo")
# - JAVA_MAX_MEM_RATIO: Is used when no `-Xmx` option is given in JAVA_OPTS. This is
#   used to calculate a default maximal heap memory based on a containers restriction.
#   If used in a container without any memory constraints for the container then this
#   option has no effect. If there is a memory constraint then `-Xmx` is set to a ratio
#   of the container available memory as set here. The default is `50` which means 50%
#   of the available memory is used as an upper boundary. You can skip this mechanism by
#   setting this value to `0` in which case no `-Xmx` option is added.
# - JAVA_INITIAL_MEM_RATIO: Is used when no `-Xms` option is given in JAVA_OPTS. This
#   is used to calculate a default initial heap memory based on the maximum heap memory.
#   If used in a container without any memory constraints for the container then this
#   option has no effect. If there is a memory constraint then `-Xms` is set to a ratio
#   of the `-Xmx` memory as set here. The default is `25` which means 25% of the `-Xmx`
#   is used as the initial heap size. You can skip this mechanism by setting this value
#   to `0` in which case no `-Xms` option is added (example: "25")
# - JAVA_MAX_INITIAL_MEM: Is used when no `-Xms` option is given in JAVA_OPTS.
#   This is used to calculate the maximum value of the initial heap memory. If used in
#   a container without any memory constraints for the container then this option has
#   no effect. If there is a memory constraint then `-Xms` is limited to the value set
#   here. The default is 4096MB which means the calculated value of `-Xms` never will
#   be greater than 4096MB. The value of this variable is expressed in MB (example: "4096")
# - JAVA_DIAGNOSTICS: Set this to get some diagnostics information to standard output
#   when things are happening. This option, if set to true, will set
#  `-XX:+UnlockDiagnosticVMOptions`. Disabled by default (example: "true").
# - JAVA_DEBUG: If set remote debugging will be switched on. Disabled by default (example:
#    true").
# - JAVA_DEBUG_PORT: Port used for remote debugging. Defaults to 5005 (example: "8787").
# - CONTAINER_CORE_LIMIT: A calculated core limit as described in
#   https://www.kernel.org/doc/Documentation/scheduler/sched-bwc.txt. (example: "2")
# - CONTAINER_MAX_MEMORY: Memory limit given to the container (example: "1024").
# - GC_MIN_HEAP_FREE_RATIO: Minimum percentage of heap free after GC to avoid expansion.
#   (example: "20")
# - GC_MAX_HEAP_FREE_RATIO: Maximum percentage of heap free after GC to avoid shrinking.
#   (example: "40")
# - GC_TIME_RATIO: Specifies the ratio of the time spent outside the garbage collection.
#   (example: "4")
# - GC_ADAPTIVE_SIZE_POLICY_WEIGHT: The weighting given to the current GC time versus
#   previous GC times. (example: "90")
# - GC_METASPACE_SIZE: The initial metaspace size. (example: "20")
# - GC_MAX_METASPACE_SIZE: The maximum metaspace size. (example: "100")
# - GC_CONTAINER_OPTIONS: Specify Java GC to use. The value of this variable should
#   contain the necessary JRE command-line options to specify the required GC, which
#   will override the default of `-XX:+UseParallelGC` (example: -XX:+UseG1GC).
# - HTTPS_PROXY: The location of the https proxy. (example: "myuser@127.0.0.1:8080")
# - HTTP_PROXY: The location of the http proxy. (example: "myuser@127.0.0.1:8080")
# - NO_PROXY: A comma separated lists of hosts, IP addresses or domains that can be
#   accessed directly. (example: "foo.example.com,bar.example.com")
#
###

# debian needed for protoc to work. it isn't compiled for alpine.
FROM maven:3.9-amazoncorretto-21-debian

# RUN apk add --update maven make protobuf-dev

#RUN yum -y update
#RUN yum -y install wget
#
#RUN wget http://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo
#RUN sed -i s/\$releasever/7/g /etc/yum.repos.d/epel-apache-maven.repo
#RUN yum install -y apache-maven

#RUN yum -y install protobuf.x86_64

# Create a group and user
# RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Tell docker that all future commands should run as the appuser user

RUN apt-get update && apt-get install -y docker

RUN mkdir -p /app
# RUN chown appuser /app

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en'

RUN echo \
    "<settings xmlns='http://maven.apache.org/SETTINGS/1.0.0\' \
    xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' \
    xsi:schemaLocation='http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd'> \
        <localRepository>/app/.m2/repository</localRepository> \
        <interactiveMode>true</interactiveMode> \
        <usePluginRegistry>false</usePluginRegistry> \
        <offline>false</offline> \
    </settings>" \
    > /usr/share/maven/conf/settings.xml

# USER appuser
WORKDIR /app

# COPY ./pom.xml /app/
# # COPY ./.env /usr/src/app/
# COPY ./src /app/src/

#
#RUN chown 1001 /app \
#    && chmod "g+rwX" /app \
#    && chown 1001:root /app
#USER 1001
#
#
#RUN mkdir target

# We make four distinct layers so if there are application changes the library layers can be re-used
# COPY --chown=185 target/quarkus-app/lib/ /deployments/lib/
# COPY --chown=185 target/quarkus-app/*.jar /deployments/
# COPY --chown=185 target/quarkus-app/app/ /deployments/app/
# COPY --chown=185 target/quarkus-app/quarkus/ /deployments/quarkus/

# ENTRYPOINT "mvn-entrypoint.sh"

EXPOSE 8080
# USER 185

ENV AB_JOLOKIA_OFF=""
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
# ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

CMD ["mvn", "-Dquarkus.http.host=0.0.0.0", "quarkus:dev"]


FROM gradle:7.6-jdk17 as builder
WORKDIR /workdir
COPY build.gradle settings.gradle ./
COPY src/main ./src/main
RUN bash -c "gradle build -x test"


FROM amazoncorretto:17
COPY --from=builder /workdir/build/libs/m3-users-service-0.0.1-SNAPSHOT.jar .
ENTRYPOINT bash -c "java -jar m3-users-service-0.0.1-SNAPSHOT.jar"
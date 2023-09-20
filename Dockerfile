FROM gradle:7.6-jdk17 as builder
WORKDIR /workdir
COPY build.gradle settings.gradle ./
COPY src/main ./src/main
RUN bash -c "gradle build -x test"

FROM amazoncorretto:17
COPY --from=builder /workdir/build/libs/auth-0.0.1-SNAPSHOT.jar .
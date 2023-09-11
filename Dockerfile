FROM gradle:7.6-jdk17 as builder
WORKDIR /var/workdir
COPY . .
RUN bash -c "gradle build -x test"

FROM amazoncorretto:17
COPY --from=builder /var/workdir/build/libs/auth-0.0.1-SNAPSHOT.jar .
FROM gradle:8.4-jdk17 as gradle-cache
ENV GRADLE_USER_HOME /home/gradle/cache_home
WORKDIR /home/gradle/java-code
RUN mkdir -p /home/gradle/cache_home
COPY build.gradle settings.gradle /home/gradle/java-code/
RUN gradle clean build --no-daemon > /dev/null 2>&1 || true

FROM gradle:8.4-jdk17 as builder
COPY --from=gradle-cache /home/gradle/cache_home /home/gradle/.gradle/
COPY build.gradle settings.gradle ./
COPY src/main ./src/main
RUN bash -c "gradle build -x test"

FROM amazoncorretto:17
COPY --from=builder /home/gradle/build/libs/m3-users-service-0.0.1-SNAPSHOT.jar .
ENTRYPOINT ["java", "-jar" , "m3-users-service-0.0.1-SNAPSHOT.jar"]
FROM amazoncorretto:17
ADD ./target/m3-users-service-1.0-SNAPSHOT.jar .
ENTRYPOINT ["java", "-Xmx40m", "-Xms10m", "-Xss200k", "-jar" , "m3-users-service-1.0-SNAPSHOT.jar"]
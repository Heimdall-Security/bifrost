FROM eclipse-temurin:21.0.6_7-jre-ubi9-minimal
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=prod
ENV ARTIFACTORY_PATH=path
RUN echo $ARTIFACTORY_PATH
COPY build/libs/*.jar /app.jar
CMD ["java", "-jar", "/app.jar"]
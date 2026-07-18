FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY app/cloudops-demo/target/cloudops-demo-1.0.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java","-jar","app.jar"]

FROM eclipse-temurin:24
WORKDIR /opt/app
COPY ./jar/FairDataPointSchemaTool-1.0.jar /opt/app
ENTRYPOINT ["java", "-jar", "FairDataPointSchemaTool-1.0.jar"]

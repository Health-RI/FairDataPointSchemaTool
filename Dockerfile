FROM maven:3-eclipse-temurin-24 AS build
WORKDIR /tmp
COPY . .
RUN mvn -q install:install-file -Dfile=./xls2rdf-lib-3.2.1.jar -DgroupId=fr.sparna.rdf.xls2rdf -DartifactId=xls2rdf-pom -Dversion=3.2.1 -Dpackaging=jar 
RUN mvn -q package -DskipTests=true

FROM eclipse-temurin:24
WORKDIR /opt/app
COPY --from=build /tmp/target/FairDataPointSchemaTool-1.0.jar /opt/app
ENTRYPOINT ["java", "-jar", "FairDataPointSchemaTool-1.0.jar"]

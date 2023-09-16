ARG IMAGE_NAME=search_engine

ARG IMAGE_VERSION=1.0

FROM openjdk:17-jdk

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE}  searchEngine.jar

ENTRYPOINT ["java","-Dspring.datasource.url=jdbc:mysql://${mysql_host}:${mysql_port}/${mysql_db}","-Dspring.datasource.username=${mysql_user}","-Dspring.datasource.password=${mysql_password}","-jar","searchEngine.jar"]

EXPOSE 8080
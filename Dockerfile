FROM openjdk:14-alpine
COPY build/libs/spot-price-service-*-all.jar spot-price-service.jar
EXPOSE 8080
CMD ["java", "-Dcom.sun.management.jmxremote", "-Xmx128m", "-jar", "spot-price-service.jar"]
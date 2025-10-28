# Sử dụng JDK 17
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# Copy Maven Wrapper + pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# ⚠️ FIX: cấp quyền thực thi cho mvnw
RUN chmod +x mvnw

# Tải dependency trước (cache build)
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Build app
RUN ./mvnw package -DskipTests

# Expose port 8080
EXPOSE 8080

# Run app
ENTRYPOINT ["java", "-jar", "target/ecommerce-0.0.1-SNAPSHOT.jar"]

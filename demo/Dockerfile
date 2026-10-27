# Stage 1: Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# คัดลอกไฟล์ที่จำเป็นสำหรับ Maven wrapper และ dependencies
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# โหลด Dependencies ล่วงหน้า (ช่วยประหยัดเวลา build ครั้งต่อไป)
RUN ./mvnw dependency:go-offline

# คัดลอก Source code และทำการ Build
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# คัดลอกไฟล์ .jar ที่ build เสร็จแล้วจาก Stage 1
COPY --from=builder /app/target/*.jar app.jar

# กำหนด Port (ค่าเริ่มต้น Spring Boot คือ 8080)
EXPOSE 8080

# คำสั่งรันแอปพลิเคชัน
ENTRYPOINT ["java", "-jar", "app.jar"]

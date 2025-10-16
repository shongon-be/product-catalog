# Sử dụng một base image Java chính thức. Chọn phiên bản phù hợp với dự án của bạn.
FROM openjdk:17-slim

# Đặt thư mục làm việc bên trong container
WORKDIR /app

# Copy file pom.xml và các file cần thiết để build dependencies
COPY .mvn/ .mvn
COPY mvnw .
COPY pom.xml .

# Tải dependencies về (tận dụng cache layer của Docker)
RUN ./mvnw dependency:go-offline

# Copy toàn bộ source code của ứng dụng vào
COPY src ./src

# Build ứng dụng, tạo ra file .jar và bỏ qua việc chạy test
RUN ./mvnw package -DskipTests

# Thiết lập cổng mà ứng dụng sẽ chạy
EXPOSE 8083

# Lệnh để chạy ứng dụng khi container khởi động
# Đường dẫn đến file JAR có thể khác tùy vào cấu hình pom.xml của bạn
ENTRYPOINT ["java", "-jar", "target/catalog-0.0.1-SNAPSHOT.jar"]
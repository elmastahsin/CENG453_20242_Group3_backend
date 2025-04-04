# Build Stage: Uygulamayı derlemek için kullanılan aşama
FROM openjdk:17-jdk AS build-stage

# Çalışma dizinini ayarla
WORKDIR /usr/app

# Proje dosyalarını kopyala
COPY . /usr/app

# Maven Wrapper script'ine çalıştırma izni ver
RUN chmod +x mvnw

# Maven Wrapper'ın doğru çalıştığından emin olmak için bir kontrol yap
RUN ./mvnw --version

# Uygulamayı derle (testleri atlayarak)
RUN ./mvnw clean package -DskipTests

# Final Stage: Çalıştırılabilir görüntüyü oluştur
FROM openjdk:17-jdk-slim

# Çalışma dizinini ayarla
WORKDIR /usr/app

# Build stage'den oluşturulan JAR dosyasını kopyala
COPY --from=build-stage /usr/app/target/*.jar /usr/app/app.jar

# Uygulamayı çalıştır
CMD ["java", "-jar", "app.jar"]

# Uygulamanın dışarıya açacağı port (Spring Boot varsayılan olarak 8080 kullanır)
EXPOSE 8080
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /src
COPY src/main/java ./src/main/java
RUN mkdir -p /build/classes \
    && javac -Xlint:all -Werror -d /build/classes $(find src/main/java -name '*.java' -print) \
    && jar --create --file /build/sky-rules.jar --main-class com.skycoin4444.rules.RuleCli -C /build/classes .

FROM eclipse-temurin:21-jre-jammy
RUN useradd --system --uid 10001 --create-home appuser
WORKDIR /app
COPY --from=builder /build/sky-rules.jar ./sky-rules.jar
USER 10001:10001
ENTRYPOINT ["java", "-jar", "/app/sky-rules.jar"]

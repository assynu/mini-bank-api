FROM eclipse-temurin:21-jre

RUN useradd --system --uid 1001 minibank
USER minibank

WORKDIR /app
COPY --chmod=755 build/install/mini-bank-api/ ./

EXPOSE 8080
ENTRYPOINT ["./bin/mini-bank-api"]
FROM golang:1.23-alpine AS builder

WORKDIR /app

RUN apk add --no-cache git

COPY go.mod go.sum ./

RUN go mod download

COPY . .

RUN CGO_ENABLED=0 GOOS=linux go build -o questionreviewer ./main.go

FROM alpine:latest AS runtime

WORKDIR /app

RUN adduser -D -u 10001 appuser

COPY --from=builder /app/questionreviewer .

EXPOSE 8080

USER appuser

ENTRYPOINT ["./questionreviewer"]

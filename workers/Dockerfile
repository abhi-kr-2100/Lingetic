FROM golang:1.24.1-alpine AS builder

WORKDIR /app

COPY go.mod go.sum ./
RUN go mod download

COPY . .

RUN CGO_ENABLED=0 go build -ldflags="-s -w" -o workers ./main.go

FROM ubuntu:noble AS runner

WORKDIR /app

# Copy only the compiled binary from the builder stage
COPY --from=builder /app/workers .

ENTRYPOINT ["./workers"]

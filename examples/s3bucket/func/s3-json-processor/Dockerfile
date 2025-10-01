# --- build stage ---
FROM --platform=$BUILDPLATFORM golang:1.22 AS builder
WORKDIR /workspace

# Cache deps
COPY go.mod go.sum ./
RUN go mod download

# Copy source and build a static linux/amd64 binary
COPY . .
ARG TARGETOS=linux
ARG TARGETARCH=amd64
ENV CGO_ENABLED=0 GOOS=$TARGETOS GOARCH=$TARGETARCH GOAMD64=v1
RUN go build -o /function .

# Make absolutely sure it's executable by any UID (OpenShift runs random UID)
RUN chmod 0755 /function

# --- runtime stage ---
# scratch = nothing but your binary; perfect for static Go
FROM scratch
WORKDIR /
COPY --from=builder /function /function

# Knative expects the app to listen on $PORT (8080 default)
ENV PORT=8080
EXPOSE 8080

# Do NOT set USER; OpenShift will assign a random nonroot UID
ENTRYPOINT ["/function"]

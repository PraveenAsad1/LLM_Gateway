# LLM Gateway

A production-ready **Spring Boot 4** API gateway that routes chat completion requests to multiple LLM providers (OpenAI, Anthropic) with automatic failover, token-bucket rate limiting, and request logging.

---

## Architecture

```mermaid
flowchart TD
    Client(["🖥️ Client"])
    Filter["RequestLoggingFilter\n(logs method, URI, status, latency)"]
    Controller["ChatController\nPOST /api/chat"]
    RateLimit{"RateLimiterService\nTokenBucket\n(capacity=20, refill=5/s)"}
    Router["FailoverRouter"]
    Primary["primaryProvider\nOpenAiProvider\ngpt-4o-mini"]
    Backup["backupProvider\nAnthropicProvider\nclaude-3-haiku"]
    Mock["MockLlmProvider\n(used when API key absent)"]
    EH["GlobalExceptionHandler"]
    OpenAI(["☁️ OpenAI API"])
    Anthropic(["☁️ Anthropic API"])

    Client -->|"HTTP POST"| Filter
    Filter --> Controller
    Controller -->|"tryConsume()"| RateLimit
    RateLimit -->|"429 Too Many Requests"| EH
    RateLimit -->|"token available"| Router
    Router -->|"primary call"| Primary
    Primary -->|"success"| Client
    Primary -->|"ProviderUnavailableException"| Router
    Router -->|"fallback call"| Backup
    Backup -->|"success"| Client
    Backup -->|"both down"| EH
    EH -->|"503 / 500 / 429 JSON"| Client
    Primary <-->|"RestClient"| OpenAI
    Backup <-->|"RestClient"| Anthropic
    Primary -. "if key missing" .-> Mock
    Backup -. "if key missing" .-> Mock
```

---

## Features

| Feature | Details |
|---|---|
| **Failover routing** | Automatically retries backup provider when primary throws `ProviderUnavailableException` |
| **Token-bucket rate limiting** | 20-token burst capacity, refills at 5 tokens/sec (configurable) |
| **Real LLM providers** | OpenAI Chat Completions API & Anthropic Messages API via Spring `RestClient` |
| **Mock providers** | Safe fallback when API keys are not configured — app always starts |
| **Request logging** | Every request logged with method, URI, remote IP, HTTP status, and latency |
| **Global error handling** | Clean JSON error responses: `503` (providers down), `429` (rate limited), `500` (unexpected) |

---

## Project Structure

```
src/main/java/com/praveen/llm_gateway/
├── LlmGatewayApplication.java          # Spring Boot entry point
│
├── api/
│   ├── ChatController.java             # POST /api/chat
│   ├── exception/
│   │   ├── ErrorResponse.java          # JSON error payload record
│   │   └── GlobalExceptionHandler.java # @ControllerAdvice — 429 / 503 / 500
│   └── filter/
│       └── RequestLoggingFilter.java   # Servlet filter — logs all requests
│
├── config/
│   └── ProviderConfig.java             # Wires primary/backup provider beans
│
├── model/
│   ├── GatewayRequest.java             # record(apiKey, prompt, model)
│   └── GatewayResponse.java            # record(content, providerUsed, promptTokens, completionTokens)
│
├── provider/
│   ├── LlmProvider.java                # Interface: GatewayResponse call(GatewayRequest)
│   ├── OpenAiProvider.java             # Calls OpenAI /v1/chat/completions
│   ├── AnthropicProvider.java          # Calls Anthropic /v1/messages
│   ├── MockLlmProvider.java            # Stub — used when API key is absent
│   └── ProviderUnavailableException.java
│
├── ratelimit/
│   ├── TokenBucket.java                # Thread-safe token bucket implementation
│   ├── RateLimiterService.java         # @Service wrapping TokenBucket
│   └── RateLimitExceededException.java # Extends ResponseStatusException (429)
│
└── router/
    └── FailoverRouter.java             # Routes primary → backup on failure
```

---

## Quick Start

### Prerequisites

- Java 17+ (tested on Java 25)
- Maven wrapper included (`./mvnw`)

### 1. Clone & configure

```bash
git clone https://github.com/PraveenAsad1/LLM_Gateway.git
cd LLM_Gateway
```

Set your API keys as environment variables (optional — mocks are used if absent):

```bash
export OPENAI_API_KEY=sk-...
export ANTHROPIC_API_KEY=sk-ant-...
```

### 2. Run

```bash
./mvnw spring-boot:run
```

The server starts on **http://localhost:8080**.

### 3. Send a request

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "apiKey": "your-key",
    "prompt": "Explain the difference between REST and GraphQL",
    "model": "gpt-4o-mini"
  }'
```

**Success response (200):**
```json
{
  "content": "REST uses fixed endpoints...",
  "providerUsed": "openai/gpt-4o-mini",
  "promptTokens": 18,
  "completionTokens": 142
}
```

**Rate limit response (429):**
```json
{
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Please slow down and try again shortly."
}
```

**All providers down (503):**
```json
{
  "error": "Service Unavailable",
  "message": "All LLM providers are currently unavailable. Please try again later."
}
```

---

## Configuration

All settings live in `src/main/resources/application.properties`:

```properties
# Rate limiting
gateway.ratelimit.capacity=20          # burst capacity (tokens)
gateway.ratelimit.refill-per-second=5  # sustained throughput

# OpenAI
gateway.openai.api-key=${OPENAI_API_KEY:}
gateway.openai.model=gpt-4o-mini
gateway.openai.timeout-seconds=30

# Anthropic
gateway.anthropic.api-key=${ANTHROPIC_API_KEY:}
gateway.anthropic.model=claude-3-haiku-20240307
gateway.anthropic.timeout-seconds=30
gateway.anthropic.version=2023-06-01
```

---

## Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Spring Boot | 4.1.1 | Application framework |
| Spring Web (Tomcat) | 6.x | REST API + Servlet filter |
| Spring `RestClient` | 6.1+ | HTTP calls to LLM providers |
| Java Records | 16+ | Immutable DTOs |
| SLF4J / Logback | — | Structured logging |
| Maven Wrapper | — | Build tooling |

---

## Failover Flow

```
Request arrives
    │
    ▼
RateLimiterService.tryConsume()
    │ token available                  │ bucket empty
    ▼                                  ▼
FailoverRouter.route()           429 Too Many Requests
    │
    ├─► primaryProvider.call()
    │       │ success → return response
    │       │ ProviderUnavailableException ↓
    │
    └─► backupProvider.call()
            │ success → return response
            │ ProviderUnavailableException ↓
            503 Service Unavailable
```

---

## License

MIT
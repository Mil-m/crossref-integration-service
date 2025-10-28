# Crossref DOI Lookup API (Spring Boot)

## Overview

A REST API built with **Spring Boot** that accepts a list of DOIs and retrieves publication details from the [Crossref API](https://api.crossref.org/swagger-ui/index.html#).

The service provides:
- Title
- Authors
- Publication date
- Peer review information (if available)

---

## Quick Start

### Build
```bash
./gradlew clean build
```

### Run
```bash
docker run --name pg-demo -e POSTGRES_USER=user -e POSTGRES_PASSWORD=pass -e POSTGRES_DB=crossref -p 5432:5432 -d postgres:16

./gradlew bootRun
```

Once started, the service will be available at:
```
http://localhost:8080

docker exec -it pg-demo psql -U user -d crossref -c \
"SELECT doi, title, authors, published, peer_reviewed FROM articles ORDER BY doi DESC LIMIT 5;"
```

---

## API Usage

**Endpoint**
```
POST /get-article-info-by-doi
```

**Request Body**
```json
{
  "dois": [
    "10.1038/s41586-020-2649-2",
    "10.1109/5.771073"
  ]
}
```

**Example**
```bash
curl -s -X POST http://localhost:8080/get-article-info-by-doi   -H "Content-Type: application/json"   -d '{"dois":["10.1038/s41586-020-2649-2","10.1109/5.771073"]}' | jq .
```

---

**Run Tests**
```bash
./gradlew clean test

k6 run src/load/k6-doi-post.js
```

---

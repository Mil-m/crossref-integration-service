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
./gradlew bootRun
```

Once started, the service will be available at:
```
http://localhost:8080
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

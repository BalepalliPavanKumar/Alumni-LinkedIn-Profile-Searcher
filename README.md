# Alumni LinkedIn Profile Searcher

Spring Boot REST API that searches for alumni LinkedIn profiles via PhantomBuster
and persists the results in PostgreSQL. No UI — test with Postman/curl.

## Stack

- Java 21, Spring Boot 4 (Spring Web MVC + WebClient for outbound calls)
- Spring Data JPA + PostgreSQL (H2 in-memory for tests)
- PhantomBuster REST API v2 for LinkedIn scraping
- JUnit 5 + Mockito + AssertJ + MockMvc

## Architecture

```
controller   -> AlumniController                    (REST endpoints)
service      -> AlumniService / AlumniServiceImpl    (orchestration, dedupe/persist)
phantombuster-> PhantomBusterClient / Impl           (adapter over the PhantomBuster API)
                LinkedInSearchQueryBuilder           (builds the launch argument/search URL)
                PhantomBusterResponseParser           (parses scraped JSON -> DTOs)
mapper       -> AlumniMapper                         (PhantomBuster DTO <-> entity <-> response DTO)
repository   -> AlumniRepository                     (Spring Data JPA)
entity       -> Alumni
dto          -> AlumniSearchRequest, AlumniResponseDto, ApiResponse<T>
exception    -> GlobalExceptionHandler + custom exceptions
config       -> PhantomBusterProperties, AppConfig (WebClient bean)
```

`PhantomBusterClient` is an interface so the service layer and its tests never
depend on the concrete HTTP/polling mechanics of the third-party API.

## Setup

1. Create a PostgreSQL database:
   ```sql
   CREATE DATABASE alumni_db;
   ```
2. Copy `.env.example` to `.env` and fill in your DB credentials and PhantomBuster
   API key + agent ID (create a LinkedIn Search Export phantom in your PhantomBuster
   account and grab its agent id from the phantom's settings page).
3. Export the variables (or use an IDE run-config / `--env-file` equivalent) before
   starting the app — `application.yml` reads all of them with sensible local
   defaults if unset (except the PhantomBuster key/agent id, which must be set for
   `/api/alumni/search` to actually work).

## Run

```
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080` by default.

## Test

```
./mvnw test
```

Tests run against an in-memory H2 database (`src/test/resources/application.yml`)
and mock the PhantomBuster client, so no real PostgreSQL or PhantomBuster
credentials are needed to run the suite.

## API

### `POST /api/alumni/search`

```json
{
  "university": "University of XYZ",
  "designation": "Software Engineer",
  "passoutYear": 2020
}
```

`passoutYear` is optional. Response:

```json
{
  "status": "success",
  "data": [
    {
      "name": "John Doe",
      "currentRole": "Software Engineer",
      "university": "University of XYZ",
      "location": "New York, NY",
      "linkedinHeadline": "Passionate Software Engineer at XYZ Corp",
      "profileUrl": "https://linkedin.com/in/johndoe",
      "passoutYear": 2020
    }
  ]
}
```

Re-running a search that scrapes an already-saved LinkedIn profile URL updates
that row instead of creating a duplicate.

### `GET /api/alumni/all`

Returns every alumni profile saved so far, in the same envelope shape.

### Errors

Every error response has the shape `{"status": "error", "message": "..."}`:

| Situation                                   | HTTP status |
|----------------------------------------------|-------------|
| Missing/blank `university` or `designation`  | 400         |
| PhantomBuster launch/poll/parse failure       | 502         |
| Unexpected server error                       | 500         |

## Notes on the PhantomBuster integration

PhantomBuster phantoms are configured per-account (you pick which LinkedIn
scraper phantom to run and get its `agentId`); this project targets the
standard v2 REST flow (`agents/launch` -> poll `containers/fetch-output` ->
`containers/fetch-result-object`) and tolerates the small field-naming
differences between phantom types (`PhantomBusterProfileDto` aliases the
common variants: `fullName`/`name`, `title`/`headline`, `jobTitle`/`company`,
etc.). If your specific phantom's output uses different field names, extend
the `@JsonAlias` lists in `PhantomBusterProfileDto`.

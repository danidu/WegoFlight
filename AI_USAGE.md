# 1. ask cursor to generate the rate limiter on the exact API
- prompt : "please add rate limit using bucket4j. i want to have rate limit for /api/v1/flights/search API. please for the rate limiter setup number put it in config"

# 2. ask cursor to generate the outbond caching, and circuit breaker to partner.
- prompt : "i want to build search aggregator on this api /api/v1/flights/search. request body must have Origin and destination airports (IATA codes), Departure date (and optional return date for round trips), Passenger counts (adults, children, infants), Cabin class preference (economy, premium economy, business, first), Optional filters (direct flights only, maximum number of stops, preferred airlines, maximum results to return). output will be like : Unique search identifier, List of flight options, each containing: (Flight segments (carrier, flight number, departure/arrival airports and times, duration, aircraft type), Pricing information (total price, currency, breakdown by passenger type, taxesand fees), Which providers returned this flight option, Ranking score), Metadata about the search (Which providers were queried and which responded, Search execution time, Any warnings about partial results). i want to hit multiple aggregator API(Amadeus, Sabre, Travelport), let assume they have same request and response but different API(create dummy api for this). please add cache for that outbound, cause they have rate limit. please add shutdown mechanism also if there is something wrong with their API(put it in config for how long and how much)"

# 3. ask cursor to add the validation
- prompt : "please handling this : Invalid input data (bad airport codes, invalid dates, invalid passenger counts), Rate limiting scenarios, All providers unavailable, System overload conditions

# 4. ask cursor to add the retry mechanism
- prompt : "please add retry mechanism for all outbound request"

# 5. ask cursor to add the circuit breaker
- prompt : "please add circuit breaker for all outbound request"

# 6. since i dont subscribe github copilot, i ask chat gpt to do code review. Here the review points:
## 6.1. IDE Files Committed to Repository
- .idea/* files are committed in this PR.
- These are local IDE configuration files and should not be versioned.
- Please remove them from the repository and add .idea/ to .gitignore.
- Impact: This creates unnecessary noise in the repo and causes conflicts across developers.
## 6.2. Missing Test Coverage
- There are no tests included under src/test/java.
- At minimum, this PR should include:
  - Unit tests for rate-limiting logic 
  - Controller tests validating request/response behavior
  - Validation tests for request DTOs
- Impact: Without tests, core functionality (especially rate limiting) is hard to verify and maintain.
## 6.3. Rate Limit Error Response Consistency
- When a request is rate-limited, the response format appears inconsistent with typical API responses.
- Consider returning a structured JSON error (e.g. { code, message })
- Align HTTP status usage clearly (429 Too Many Requests)
- Impact: Inconsistent error responses complicate client integration and observability.
## 6.4. Distributed Rate Limiting Consideration
- The current rate limiter appears to be in-memory.
- This works for a single instance
- It will not work correctly in multi-instance deployments (e.g. Kubernetes, ECS)
- Suggestion: Document this limitation clearly or consider Redis-backed rate limiting if scalability is a goal.
## 6.5. Configuration Validation & Defaults
- Rate limit configuration should be:
- Validated at startup (e.g. no zero/negative values)
- Provided with safe defaults
- This avoids runtime surprises and misconfiguration.
## 6.6. Logging & Observability
- When a request is rejected due to rate limiting:
- Add structured logging
- Consider logging key identifiers (API key, endpoint, client IP if applicable)
- This will be useful for monitoring and debugging abuse cases.
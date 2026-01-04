# Problem-Solving Approach

## Problem Analysis

### Key Challenges Identified

1. **Multiple External Systems Integration**
   - Challenge: Integrate with 3 different flight aggregator APIs (Amadeus, Sabre, Travelport)
   - Complexity: Different response formats, error handling, rate limits
   - Solution: Abstract provider interface, normalize responses, implement circuit breakers

2. **Performance Requirements**
   - Challenge: Users expect fast results (< 500ms)
   - Complexity: Sequential calls would take ~900ms (3 providers × 300ms)
   - Solution: Parallel execution reduces to ~300ms (3x improvement)

3. **Data Quality & Consistency**
   - Challenge: Same flight appears from multiple providers with different formats
   - Complexity: Need to deduplicate and normalize
   - Solution: Key-based deduplication (carrier + flight number + departure time)

4. **Reliability & Fault Tolerance**
   - Challenge: External APIs can fail, causing system-wide failures
   - Complexity: Need graceful degradation
   - Solution: Circuit breaker pattern, fallback mechanisms, error aggregation

5. **Rate Limiting Protection**
   - Challenge: External APIs have rate limits
   - Complexity: Need to protect both inbound and outbound calls
   - Solution: Caching for outbound, rate limiter for inbound

## Architectural Decisions

### Decision 1: Parallel vs Sequential Queries

**Problem**: How to query multiple providers efficiently?

**Options Considered**:
1. Sequential queries (one after another)
2. Parallel queries (all at once)
3. Staged parallel (some in parallel, some sequential)

**Decision**: Parallel queries using CompletableFuture

**Rationale**:
- **Speed**: 3x faster (300ms vs 900ms)
- **User Experience**: Faster response times
- **Resource Efficiency**: Better CPU utilization

**Trade-offs**:
- ✅ Faster response times
- ✅ Better resource utilization
- ⚠️ More complex error handling
- ⚠️ Requires thread pool management

**Implementation**: Fixed thread pool sized to number of providers

---

### Decision 2: Caching Strategy

**Problem**: How to reduce API calls and protect against rate limits?

**Options Considered**:
1. No caching
2. In-memory cache (Caffeine)
3. Distributed cache (Redis)
4. Database cache

**Decision**: In-memory Caffeine cache with 5-minute TTL

**Rationale**:
- **Simplicity**: No external dependencies
- **Performance**: Sub-millisecond access
- **Cost**: Reduces API calls by ~80%
- **Rate Limit Protection**: Prevents hitting provider limits

**Trade-offs**:
- ✅ Fast, simple, cost-effective
- ✅ Protects against rate limits
- ⚠️ Stale data (5 minutes)
- ⚠️ Not distributed (single instance only)

**Future Enhancement**: Redis for distributed caching

---

### Decision 3: Circuit Breaker Configuration

**Problem**: How to handle provider failures gracefully?

**Options Considered**:
1. No circuit breaker (fail fast)
2. Simple retry logic
3. Circuit breaker with configurable thresholds

**Decision**: Resilience4j circuit breaker with configurable thresholds

**Rationale**:
- **Fault Tolerance**: Prevents cascading failures
- **Fast Failure**: Returns immediately when circuit is open
- **Automatic Recovery**: Half-open state tests provider recovery
- **Configurable**: Thresholds can be tuned based on provider behavior

**Configuration**:
- Failure rate: 50% (opens if 50% of calls fail)
- Wait duration: 30 seconds (how long to wait before retrying)
- Sliding window: 10 calls (tracks last 10 calls)

**Trade-offs**:
- ✅ Prevents system overload
- ✅ Fast failure (no waiting for timeouts)
- ✅ Automatic recovery
- ⚠️ May reject valid requests during transient failures

---

### Decision 4: Ranking Algorithm

**Problem**: How to rank flights when multiple factors matter?

**Options Considered**:
1. Price only
2. Duration only
3. Simple weighted average
4. Machine learning model

**Decision**: Multi-factor weighted ranking

**Factors & Weights**:
- Price: 40% (lower is better)
- Duration: 30% (shorter is better)
- Stops: 20% (fewer is better)
- Provider Reliability: 10% (more providers = more reliable)

**Rationale**:
- **Balanced**: Considers multiple factors, not just price
- **User Preferences**: Prioritizes direct flights and shorter durations
- **Quality Indicator**: Provider reliability indicates data confidence
- **Tunable**: Weights can be adjusted based on user feedback

**Trade-offs**:
- ✅ Balanced results
- ✅ Considers user preferences
- ⚠️ Weights may need tuning
- ⚠️ Doesn't learn from user behavior (future: ML)

---

### Decision 5: Filtering vs Ranking

**Problem**: Should filters be hard requirements or soft preferences?

**Options Considered**:
1. Filters only (strict)
2. Ranking only (soft)
3. Filters first, then rank

**Decision**: Filters first, then rank

**Rationale**:
- **Performance**: Filtering reduces dataset before expensive ranking
- **User Intent**: Filters are hard requirements (direct flights, max stops)
- **Clarity**: Clear separation between "must have" and "nice to have"

**Filter Order**:
1. Direct flights only
2. Maximum stops
3. Preferred airlines

**Trade-offs**:
- ✅ Efficient (filters before ranking)
- ✅ Clear user intent
- ⚠️ Strict filtering may return empty results

---

### Decision 6: Deduplication Strategy

**Problem**: How to identify the same flight from different providers?

**Options Considered**:
1. No deduplication
2. Exact match (all fields)
3. Key-based (carrier + flight number + departure time)

**Decision**: Key-based deduplication

**Key Format**: `{carrier}-{flightNumber}-{departureAirport}-{arrivalAirport}-{departureTime}`

**Rationale**:
- **Accuracy**: Carrier, flight number, and departure time uniquely identify a flight
- **Provider Merging**: When duplicate found, merge provider lists
- **Reliability Indicator**: More providers = more reliable data

**Trade-offs**:
- ✅ Accurate deduplication
- ✅ Shows provider reliability
- ⚠️ Slight performance overhead

---

## Balancing Competing Concerns

### Speed vs Cost

**Challenge**: Fast responses vs API call costs

**Solution**:
- **Caching**: Reduces API calls by 80% (cost savings)
- **Parallel Queries**: Faster responses (better UX)
- **Cache TTL**: 5 minutes balances freshness vs cost

**Balance**: Prioritize speed with cost-conscious caching

---

### Reliability vs Performance

**Challenge**: Fault tolerance vs response time

**Solution**:
- **Circuit Breaker**: Fast failure (no waiting for timeouts)
- **Parallel Queries**: Continue with other providers if one fails
- **Graceful Degradation**: Return partial results with warnings

**Balance**: Prioritize reliability with fast failure mechanisms

---

### Data Quality vs Flexibility

**Challenge**: Strict validation vs user flexibility

**Solution**:
- **Whitelist Validation**: Only valid codes accepted
- **Clear Error Messages**: Users know exactly what's wrong
- **Comprehensive Validation**: Dates, passenger counts, etc.

**Balance**: Prioritize data quality with helpful error messages

---

## Error Handling Strategy

### Multi-Layered Approach

1. **Input Validation** (Layer 1)
   - Reject invalid requests early
   - Return 400 Bad Request with details
   - Prevents unnecessary processing

2. **Rate Limiting** (Layer 2)
   - Protect system from overload
   - Return 429 Too Many Requests
   - Prevents abuse

3. **Circuit Breaker** (Layer 3)
   - Prevent cascading failures
   - Fast failure with fallback
   - Automatic recovery

4. **Provider Error Handling** (Layer 4)
   - Log and continue with other providers
   - Aggregate errors in metadata
   - Return partial results

5. **All Providers Down** (Layer 5)
   - Return empty results with warning
   - Inform user of issue
   - System continues operating

---

## Testing Strategy

### Unit Tests (Recommended)
- Provider services: Mock external APIs
- Aggregator service: Test ranking, filtering, deduplication
- Validators: Test input validation logic
- Ranking algorithm: Test scoring logic

### Integration Tests (Recommended)
- End-to-end API tests
- Circuit breaker behavior
- Cache behavior
- Error scenarios

### Test Coverage Areas
1. **Happy Path**: All providers return results
2. **Partial Failure**: Some providers fail
3. **Complete Failure**: All providers fail
4. **Edge Cases**: Empty results, invalid inputs, rate limiting
5. **Performance**: Response time under load

---

## Code Organization Principles

### Separation of Concerns
- **Controllers**: Handle HTTP requests/responses
- **Services**: Business logic
- **DTOs**: Data structures
- **Validators**: Input validation
- **Config**: Configuration and infrastructure

### Single Responsibility
- Each class has one clear purpose
- Provider implementations are independent
- Aggregator service orchestrates, doesn't implement

### Dependency Injection
- All dependencies injected via constructor
- Makes testing easier (can mock dependencies)
- Follows Spring best practices

---

## Communication & Documentation

### Code Documentation
- JavaDoc comments on all public methods
- Inline comments for complex logic
- Clear variable and method names

### API Documentation
- Swagger/OpenAPI annotations
- Interactive API documentation
- Request/response examples

### Design Documentation
- DESIGN.md: Detailed architectural decisions
- APPROACH.md: Problem-solving approach (this file)
- README.md: Quick start and usage

---

## Version Control & Commit Hygiene

### Commit Strategy
- Atomic commits (one logical change per commit)
- Clear commit messages
- Feature branches for major changes

### Code Quality
- Consistent formatting
- No linter errors
- Follows Java conventions

---

## AI Tools Usage

### Development Workflow
- Used AI for boilerplate code generation
- Code review and optimization suggestions
- Documentation generation
- Test case suggestions

### Best Practices
- Review all AI-generated code
- Understand the code before committing
- Customize AI suggestions to fit project needs
- Use AI as a tool, not a replacement for understanding

---

## Conclusion

This approach demonstrates:
1. **Problem Analysis**: Identified key challenges upfront
2. **Architectural Thinking**: Made informed decisions with trade-offs
3. **Practical Implementation**: Balanced competing concerns
4. **Documentation**: Clear communication of decisions
5. **Modern Practices**: Leveraged tools and best practices

The solution prioritizes:
- **Reliability**: Circuit breakers, error handling, graceful degradation
- **Performance**: Caching, parallel queries, efficient algorithms
- **Maintainability**: Clear code, good documentation, separation of concerns
- **Scalability**: Stateless design, configurable resources


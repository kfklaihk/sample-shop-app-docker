## Plan: Scale AtSea Shop with Redis, RabbitMQ, Enhanced Auth & Stripe

Upgrade the monolithic Spring Boot application from basic JWT auth to enterprise-grade security with Spring Security, add distributed caching (Redis) for product catalog performance, message queuing (RabbitMQ) for async order processing during peak load, and integrate Stripe for real payment processing. Implement a proper login/register landing page with JWT refresh tokens, role-based access control, and restrict all catalog/order operations to authenticated users.

### Steps

1. **Add New Dependencies & Core Infrastructure**
   - Update [pom.xml](pom.xml): Add Spring Security, Spring Data Redis (Lettuce), Spring AMQP, Stripe Java SDK, JWT libraries with refresh token support
   - Update [docker-compose.yml](docker-compose.yml): Add Redis (Alpine) and RabbitMQ (Alpine) services with health checks
   - Create new config classes: [RedisConfig.java](app/src/main/java/com/docker/config/RedisConfig.java), [RabbitMQConfig.java](app/src/main/java/com/docker/config/RabbitMQConfig.java), [StripeConfig.java](app/src/main/java/com/docker/config/StripeConfig.java)

2. **Enhance Authentication & Authorization (Spring Security + JWT Refresh Tokens)**
   - Replace hardcoded JWT secret with [JwtConfig.java](app/src/main/java/com/docker/config/JwtConfig.java) using externalized properties
   - Update [SecurityConfig.java](app/src/main/java/com/docker/config/SecurityConfig.java): Enable password hashing (BCryptPasswordEncoder), CSRF protection, stateless session, permit-all for `/api/auth/**` only
   - Create [JwtTokenProvider.java](app/src/main/java/com/docker/security/JwtTokenProvider.java): Access token (15 min), refresh token (7 days), token validation/extraction logic
   - Create [JwtAuthenticationFilter.java](app/src/main/java/com/docker/security/JwtAuthenticationFilter.java): Extract/validate JWT from Authorization header, populate SecurityContext
   - Update [CustomerEntity.java](app/src/main/java/com/docker/entity/CustomerEntity.java): Add password hashing migration, roles column (ROLE_USER/ROLE_ADMIN)

3. **Create Landing Page & Authentication Endpoints**
   - Create [AuthController.java](app/src/main/java/com/docker/controller/AuthController.java): `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/refresh-token`, `POST /api/auth/logout`
   - Create [AuthRequest/Response DTOs](app/src/main/java/com/docker/dto/): `LoginRequest`, `RegisterRequest`, `AuthResponse` (accessToken, refreshToken, expiresIn)
   - Create `RefreshToken` entity and repository for token blacklisting during logout
   - Update React landing page: Replace [index.html](app/react-app/public/index.html) with login/register form, JWT token storage (localStorage), conditional routing based on auth state

4. **Implement Redis Caching for Product Catalog**
   - Create [CacheService.java](app/src/main/java/com/docker/service/CacheService.java): Warm cache on startup, TTL=1 hour for product catalog
   - Update [ProductController.java](app/src/main/java/com/docker/controller/ProductController.java): Annotate `@Cacheable("products")` on getAllProducts(), cache invalidation on product updates
   - Create cache pre-loader in `@PostConstruct` to load all products into Redis on app startup
   - Update [docker-compose.yml](docker-compose.yml) Redis service: Set `maxmemory-policy allkeys-lru` for eviction

5. **Add RabbitMQ for Async Order Processing & Peak Load Handling**
   - Create [OrderQueue.java](app/src/main/java/com/docker/config/OrderQueue.java): Define queues/exchanges for `orders.created`, `orders.processing`, `orders.completed`
   - Create [OrderProducer.java](app/src/main/java/com/docker/service/OrderProducer.java): Publish order events to RabbitMQ on order creation
   - Create [OrderConsumer.java](app/src/main/java/com/docker/service/OrderConsumer.java): Async order processing listener with retry logic and DLQ
   - Update [OrderService.java](app/src/main/java/com/docker/service/OrderService.java): Call OrderProducer instead of synchronous payment processing
   - Create `OrderStatus` entity to track async order state (PENDING → PROCESSING → COMPLETED → FAILED)

6. **Integrate Stripe Payment Gateway**
   - Create [StripeService.java](app/src/main/java/com/docker/service/StripeService.java): Create payment intent, handle webhook for payment_intent.succeeded/failed events
   - Create [PaymentController.java](app/src/main/java/com/docker/controller/PaymentController.java): `POST /api/payments/create-intent`, `POST /api/payments/webhook` (webhook endpoint)
   - Store `stripeCustomerId` in [CustomerEntity.java](app/src/main/java/com/docker/entity/CustomerEntity.java)
   - Update [OrderConsumer.java](app/src/main/java/com/docker/service/OrderConsumer.java): Charge customer via Stripe instead of legacy payment gateway
   - Update [application.yml](app/src/main/resources/application.yml): Add `stripe.api-key`, `stripe.webhook-secret` properties

7. **Add Role-Based Access Control (RBAC) & Restrict Resources**
   - Update [SecurityConfig.java](app/src/main/java/com/docker/config/SecurityConfig.java): Add `.authorizeHttpRequests()` chain: `/api/products/**` requires `ROLE_USER`, `/api/orders/**` requires `ROLE_USER`, `/api/admin/**` requires `ROLE_ADMIN`
   - Add `@PreAuthorize("hasRole('USER')")` to [ProductController.java](app/src/main/java/com/docker/controller/ProductController.java), [OrderController.java](app/src/main/java/com/docker/controller/OrderController.java), [CheckoutController.java](app/src/main/java/com/docker/controller/CheckoutController.java)
   - Update React: Add route guards in [App.js](app/react-app/src/containers/App.js), check JWT token presence before rendering catalog/checkout
   - Create `RoleUtil` to extract roles from JWT and conditionally render admin features

8. **Update Frontend for Authentication & New UX**
   - Create `AuthPage.js` component with login/register forms and JWT token state management in Redux
   - Create `ProtectedRoute.js` to redirect unauthenticated users to login
   - Update [ProductsContainer.js](app/react-app/src/containers/ProductsContainer.js): Add JWT token to all API requests in Authorization header
   - Update [CheckoutContainer.js](app/react-app/src/containers/CheckoutContainer.js): Create Stripe integration via `@stripe/react-stripe-js`, show payment form (card element)
   - Create payment action in Redux to handle `/api/payments/create-intent` and submit card to Stripe
   - Update [App.js](app/react-app/src/containers/App.js) routing: Render AuthPage if not authenticated, else show main shop

9. **Update Database & Docker Compose Configuration**
   - Update [docker-compose.yml](docker-compose.yml): Add Redis and RabbitMQ services, update app environment variables for Redis/RabbitMQ connection strings, add `STRIPE_API_KEY` secret
   - Update [application.yml](app/src/main/resources/application.yml): Add Spring Data Redis (`spring.redis.host`, `.port`, `.timeout`), Spring AMQP (`spring.rabbitmq.host`, `.username`, `.password`), JWT configuration, Stripe keys
   - Create migration script to hash existing customer passwords using BCrypt
   - Update [init-db.sql](database/docker-entrypoint-initdb.d/init-db.sql): Add `refresh_token` and `stripe_customer_id` columns to customer table, `order_status` table for async tracking

10. **Add Monitoring & Error Handling**
    - Create [GlobalExceptionHandler.java](app/src/main/java/com/docker/exception/GlobalExceptionHandler.java): Handle `JwtValidationException`, `StripeException`, `RabbitMQException` with proper HTTP responses
    - Add structured logging to OrderConsumer with order tracking IDs
    - Create health check endpoints for Redis (`/health/redis`), RabbitMQ (`/health/rabbitmq`), Stripe (`/health/stripe`)
    - Update [docker-compose.yml](docker-compose.yml) with healthchecks for all services

### Further Considerations

1. **Token Refresh Strategy**: Access tokens expire in 15 min (short-lived). Frontend must call `/api/auth/refresh-token` with refresh token to get new access token. Implement token blacklist in Redis on logout to prevent reuse.

2. **Backward Compatibility**: Current customers without passwords—run DB migration to set temporary passwords, notify users to change them on first login, or accept unauthenticated product browsing but require auth for checkout.

3. **Load Testing for Peak Scenarios**: Use Apache JMeter or Locust to simulate panic buying (1000s concurrent orders). RabbitMQ + Redis should reduce API response time from seconds to ~200ms. Monitor queue depth and adjust consumer replicas in docker-compose.

4. **Stripe Webhook Security**: Stripe sends events to `/api/payments/webhook`. Verify webhook signature using Stripe SDK to prevent spoofing. Store webhook events in DB for idempotency and audit.

5. **Redis Cluster vs Single Instance**: Current plan uses single Redis for dev. For production, consider Redis Sentinel (HA) or Redis Cluster (sharding) if catalog exceeds memory limits.

6. **Environment Secrets Management**: Store `stripe.api-key`, `jwt.secret`, RabbitMQ credentials in Docker secrets or external vault (AWS Secrets Manager, HashiCorp Vault), not in `application.yml`.

7. **Database Connection Pool Tuning**: With async order processing, Hikari connection pool size may need increase. Monitor with metrics like `hikaricp.connections.active`, adjust `spring.datasource.hikari.maximum-pool-size` (default 10).

8. **Frontend State Management**: Redux store will hold JWT token + refresh token. Implement auto-token-refresh interceptor on 401 response before retrying request.

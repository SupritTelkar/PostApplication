# Setup Instructions
### Prerequisites
1. **Java** (JDK 21)
2. **Maven**
3. **Redis** (Installed and running)
4. **Database** (MySQL)
5. **IDE** (e.g., IntelliJ IDEA, Eclipse)

# Database Configuration
****spring.datasource.url=jdbc:mysql://localhost:3306/yourdb****

spring.datasource.username=yourusername

spring.datasource.password=yourpassword

# Redis Configuration
spring.redis.host=localhost

spring.redis.port=6379

# 📋 API Endpoints
### Post Endpoints
* Create Posts: POST /api/post
* Create Multple Posts: POST /api/post/multiple
* Get All Posts: GET /api/post
* Get Post by ID: GET /api/post/{id}
* Update Post: PUT /api/post/{id}
* Delete Post: DELETE /api/post/{id}

# Performace Testing Tool
Used Jmeter to test the application performance and latency locally 

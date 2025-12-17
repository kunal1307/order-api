# VZ Order API (Assignment)

Spring Boot microservice that creates and retrieves customer orders.
Email validation is performed against the external ReqRes Users API.

## Tech stack
- Java 21
- Spring Boot
- Spring MVC + WebClient (for ReqRes integration)
- Spring Data JPA
- PostgreSQL
- Lombok
- OpenAPI (YAML served via Swagger UI)

## API Endpoints
- `POST /api/orders`  
  Request: `{ "productId": "TV-1", "email": "george.bluth@reqres.in" }`  
  Responses: `201, 400, 409, 422, 502`

- `GET /api/orders?email=<email>`  
  Responses: `200, 400`

Swagger UI:
- `http://localhost:8080/swagger-ui.html`

OpenAPI contract:
- `http://localhost:8080/openapi/openapi.yaml`

## How to run locally (without Docker)
### Prerequisites
- Java 21
- Maven
- PostgreSQL running locally

### Database setup
Create a PostgreSQL database and user (example):

- DB: `DB_NAME`
- User: `DB_USERNAME`
- Password: `DB_PASSWORD`

###REQRES API Key
- api-key: `REQRES_APIKEY`

if you have an api key then use it else create an account at **https://app.reqres.in/** and then create an api key to use it here.

Update `src/main/resources/application.yaml` if your local DB differs.

### Start the app

Using terminal at machine

**bash**

`mvn clean generate-sources`

`mvn clean compile`

`SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/<dbname> \
SPRING_DATASOURCE_USERNAME=<USERNAME> \
SPRING_DATASOURCE_PASSWORD='<PASSWORD>' \
INTEGRATION_REQRES_API_KEY='REQRES_APIKEY' \
mvn clean spring-boot:run`

for verifying you need to check the postgres desktop app and run the query

_SELECT * FROM orders;_


## How to run with Docker
### Prerequisites
- Docker Desktop
- Docker Compose

**Docker**

`mvn clean generate-sources`

`mvn clean compile`

`mvn clean package -DskipTests`

Run the below command in you project folder to remove the old image if any

`docker compose down -v`

### Start the app
execute below command finally to make you application up

`REQRES_API_KEY=<your API KEY> docker compose up --build`

for verifying DB data when using Docker

`docker exec -it order-postgres psql -U orders -d orders`

then below query:

_SELECT count(*) FROM orders;
SELECT * FROM orders ORDER BY created_at DESC;_

and then run the below query to verify

_SELECT * FROM orders;_


want to reset and clean start the docker part then run below commands 

`docker compose down -v`

`REQRES_API_KEY=<your API KEY> docker compose up --build`


After running, you application using anyone option either bash or docker you verify by accessing the end points and hitting those through post man 
which can be imported in postman, [Postman Collection](order-api-vz-assignment.postman_collection.json) is in the project folder.

### Run tests (integration tests with Testcontainers)
Prerequisite: Docker Desktop running


`mvn -Dtest=OrderApiIntegrationTest clean test surefire-report:report`

If you get a version mismatch issue look at this of [stackoverflow page](https://stackoverflow.com/questions/79817033/sudden-docker-error-about-client-api-version) for the fix. I used it for my fix 

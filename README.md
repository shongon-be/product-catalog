# 📦 Product Catalog API
Backend API for managing product catalogs, providing filtering, sorting, and search features. Developed with Spring Boot following a clean layered architecture.

---

## 🚀 Features
- CRUD operations for products.
- Advanced filtering and sorting (by name, price, category).
- Database integration with MongoDB.
- RESTful APIs with proper status codes & error handling.
- CORS enabled for frontend integration.

---

## 🛠️ Tech Stack
- Backend: Java 17, Spring Boot, Spring Data - MongoDB, Spring Validation.
- Database: MongoDB.
- Other: Lombok, MapStruct, JUnit/Mockito, Test Containers.

---

## 📐 Architecture
- **Pattern**: layered (controller → service → repository).
- Controller Layer: Handle REST endpoints.
- Service Layer: Business logic (filtering, sorting).
- Repository Layer: Data persistence (MongoDB).
- Mapper Layer: DTO ↔ Entity conversion using MapStruct.
- Diagram:

<p align="center">
  <img src="docs/img/system-architecture-0.1.0.png" alt="System Architecture diagram" height="300"><br>
  <i>System Architecture Diagram</i>
</p>


---

## ⚙️ Installation & Setup
**Prerequisites**
- JDK 17+
- Maven


**1. Clone**

```bash
    git clone https://github.com/shongon-be/product-catalog.git
```

**2. Run locally**
```bash
    ./mvnw clean install
    ./mvnw spring-boot:run
```
UI will be available at: http://127.0.0.1:5500/frontend/index.html

API will be available at: http://localhost:8083/product-catalog/

--- 

## 📖 API Documentation

See details at [API-DOCS.md](docs/API-DOCS.md)

---

## 📊 Diagrams
### Filter & Sort Products (Business Flow)
<p align="center">
  <img src="docs/img/sq-filter-sort.png" alt="Sequence Diagram Filter & Sort Products" height="350"><br>
  <i>Sequence diagram showing request flow from Controller → Service → Repository</i>
</p>

---

## 🧪 Testing
- Unit tests with JUnit & Mockito.
- Integration tests for controller & repository with Test Containers.

```bash
    ./mvnw test
```

---

## 🗺️ Roadmap  

- [ ] Implement caching with **Redis** for `viewAllProducts` endpoint.
- [ ] Integrate **Elasticsearch** for advanced product search.  
- [ ] Dockerize application with **Docker** & **Docker Compose**.  
- [ ] Deploy containerized project to **Railway** / **Render** / **AWS ECS** (after Dockerization).  

---

## 👨‍💻 Author
👤 Nguyen Tran Hong Son - SageDev

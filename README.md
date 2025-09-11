# 📦 Product Catalog API
**A clean and production-ready backend API** for managing product catalogs, featuring filtering, sorting, caching, searching and containerization.

Built with **Spring Boot** and designed to showcase **Clean Code practices, Git workflow, MongoDB indexing optimization, Redis caching, Elasticsearch fuzzy searching and auto complete suggesting**.

---

## 🎯 Project Goals
- Demonstrate **clean architecture & code readability**.
- Apply **Redis caching** to improve performance (ViewAllProducts & FilterByCategory).
- Optimize **MongoDB indexes** for faster queries.
- Apply **Elasticsearch for fuzzy search & auto-complete** suggestions.
- Practice **team-ready Git workflow** (feature branching, PR reviews, semantic commits).
- Package & run with **Docker & Docker Compose**.

---

## 📌 Current Status

- ✅ MVP done (CRUD, filtering, sorting, caching).

- ⏳ Elasticsearch fuzzy search & autocomplete (in progress).

- 🚧 CI/CD pipeline (planned)

---

## 💻 Live Demo
- **API Documentation**: [Postman Docs](https://documenter.getpostman.com/view/38175419/2sB3HondyG) 
- **Postman Collection**: [📂 product-catalog.postman_collection.json](assets/resource/product-catalog.postman_collection.json)
---

## 🚀 Features
- CRUD operations for products.
- Advanced **filtering and sorting** (by name, price, category).
- **Caching with Redis** for high-performance API responses.
- **MongoDB Indexing** for query optimization.
- **RESTful Design** with proper status codes & error handling.
- **CORS** enabled for frontend integration.
- **Git Workflow**: feature-branch → PR → code review → merge.

---

## 🛠️ Tech Stack
- **Backend**: Java 17, Spring Boot, Spring Data MongoDB, Spring Validation
- **Database**: MongoDB (with custom indexes), Redis (caching)
- **Search**: ⏳ **Elasticsearch** (planned for fuzzy search & autocomplete)
- **Testing**: JUnit 5, Mockito, Testcontainers
- **Container**: Docker, Docker Compose.
- **Other**: Lombok, MapStruct, Git, GitHub.

---

## 📑 API Endpoints (Summary)

| Method | Endpoint           | Description                 |
| ------ | ------------------ | --------------------------- |
| GET    | `/products`        | Get all products            |
| GET    | `/products/{id}`   | Get product by ID           |
| POST   | `/products`        | Create new product          |
| PUT    | `/products/{id}`   | Update product by ID        |
| DELETE | `/products/{id}`   | Delete product by ID        |
| GET    | `/products/filter` | Filter products by category |
| GET    | `/products/sort`   | Sort products (name/price)  |


---

## 📐 Architecture
- **Pattern**: monolith (controller → service → repository → cache).
- Controller Layer: Handle REST endpoints.
- Service Layer: Business logic (filtering, sorting).
- Caching Layer: Redis with TTL & invalidation strategy
- Repository Layer: Data persistence (MongoDB).
- Mapper Layer: DTO ↔ Entity conversion using MapStruct.
- Diagram:

<p align="center">
  <img src="docs/assets/img/Product-Catalog-System-Diagram-0.2.0.drawio.png" alt="System Architecture diagram" width="400"><br>
  <i>System Architecture Diagram</i>
</p>


---

## ⚙️ Installation & Setup
**1. Prerequisites**
- JDK 17+
- Maven
- Docker & Docker Compose

**2. Clone**

```bash
git clone https://github.com/shongon-be/product-catalog.git
cd product-catalog
```

**3. Set up .env**
```env
MONGO_DATABASE="your-db"
MONGO_USER="username"
MONGO_PASSWORD="password"
MONGO_CLUSTER="cluster"

REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=root
```

**4. Run docker-compose**
```bash
docker-compose up -d
```

**5. Run locally**
```bash
mvn clean install
mvn spring-boot:run
```

- **API**: http://localhost:8083/product-catalog/

- **UI (demo)**: http://127.0.0.1:5500/frontend/index.html

---

## 📊 Diagrams
### 1. Filter & Sort Products
<p align="center">
  <img src="docs/assets/img/Product-Catalog-sq-filter.drawio.png" alt="Sequence Diagram Filter Products" width="400"><br>
  <i> Filter Products sequence diagram.</i>
</p>

<p align="center">
  <img src="docs/assets/img/Product-Catalog-sq-sort.drawio.png" alt="Sequence Diagram ort Products" width="400"><br>
  <i> Sort Products sequence diagram.</i>
</p>

### 2. Cache Products (viewAllProducts & filterCategory)
<p align="center">
  <img src="docs/assets/img/Product-Catalog-sq-cache-viewAllProducts.drawio.png" alt="Sequence Diagram Cache viewAllProducts" width="400"><br>
  <i>Cache viewAllProducts sequence diagram.</i>
</p>

<p align="center">
  <img src="docs/assets/img/Product-Catalog-sq-cache-filterCategory.drawio.png" alt="Sequence Diagram Cache filterCategory" width="400"><br>
  <i>Cache filterCategory sequence diagram.</i>
</p>

<p align="center">
  <img src="docs/assets/img/Product-Catalog-sq-cache-invalidtionCUD.drawio.png" alt="Sequence Diagram Cache invalidationCUD" width="400"><br>
  <i>Cache invalidationCUD sequence diagram.</i>
</p>

---

## 🧪 Testing & CI/CD

- **Unit/Integration tests**: 89 tests, Coverage: **100%**.
  - Unit tests (Service, Controller) with JUnit 5 & Mockito.
  - Integration tests with Testcontainers (MongoDB & Redis).
- **GitHub Actions**: build/test on PR, Docker image publish (planned).

---

## 🤝 Contributing

1. Fork the repo.

2. Create a feature branch: `git checkout -b feature/your-feature`

3. Commit with semantic messages. (`feat:`, `fix:`, `chore:`)

4. Open a PR → code review → merge.

---

## 🚧 Known Issues / Limitations 

- Authentication & authorization **not implemented**.
- Elasticsearch search features are **not yet integrated**.
- Frontend demo is **static HTML/CSS/JS only** (not a full UI).

---

## 🗺️ Roadmap  

- [x] Implement caching with **Redis** for `viewAllProducts` and `filterCategory` endpoints.

- [ ] Integrate **Elasticsearch** for for fuzzy search & auto-complete.

- [ ] Dockerize application with **Docker** & **Docker Compose**.  

- [ ] Deploy containerized project to **Railway** / **Render** / **AWS ECS**. 

- [ ] Setup CI/CD with GitHub Actions.

---

## 👨‍💻 Author
👤 **Nguyen Tran Hong Son** - Backend Developer (Java, Spring Boot)

📧 Contact: [LinkedIn](https://www.linkedin.com/in/son-nguyen-850585351/)

---

## 📜 License

Distributed under the MIT License. See `LICENSE` for more info.

---
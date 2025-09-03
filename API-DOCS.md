# Product Catalog

# 📌 Product Catalog API

**Backend**: Spring Boot 3, Maven.

**Database**: MongoDB, Elasticsearch (search-engine).

**Tool test**: Postman.

---

## 🔑 Base URL

```json
http://localhost:8083/product-catalog
```

---

## 📚 Authentication

- Currently API **does not require auth** (personal demo only).
- If JWT/Basic Auth is required later, then specify here.

---

## 📂 Response Format

- Response Format:

```json
{
  "code": <HTTP_CODE>,
  "message": "Success",
  "result": {...}
}
```

- Error Response Format:

```json
{
  "code": <statusCode>,
  "errors": ["Error message 1", "Error message 2"]
}
```

---

## 🛒 Endpoints

### View All Products

- **GET** `/products`
- **Query params:**
    - `page` (default: 0)
    - `size` (default: 20)
- **Response 200**

    ```java
    {
      "code": 200,
      "message": "Success",
      "result": {
        "content": [
          {
            "id": "68ad8b8f1f76bd5e1eb753b9",
            "name": "iPhone 15 Pro",
            "description": "Apple flagship smartphone with A17 chip",
            "price": 1199.99,
            "category": "ELECTRONICS"
          }
        ],
        "totalPages": 3,
        "totalElements": 60
      }
    }
    ```


### Get Product by ID

- **GET** `/products/{id}`
- Response **200**

    ```java
    {
      "code": 200,
      "message": "Success",
      "result": {
        "id": "68ad8b8f1f76bd5e1eb753b9",
        "name": "iPhone 15 Pro",
        "description": "Apple flagship smartphone with A17 chip",
        "price": 1299.99,
        "category": "ELECTRONICS"
      }
    }
    ```

- Response **400**

    ```java
    {
      "code": 400,
      "errors": ["Invalid productId format. Must be a valid Mongo ObjectId"]
    }
    ```

- Response **404**

    ```java
    {
      "code": 404,
      "errors": ["Product not found"]
    }
    ```


### Create New Product

- **POST** `/products`
- **Body**

    ```json
    {
      "name": "MacBook Pro",
      "description": "Apple laptop M3 Pro",
      "price": 2500.0,
      "category": "ELECTRONICS"
    }
    ```

- Response **201**

    ```json
    {
      "code": 201,
      "message": "Success",
      "result": { "message": "Create product successfully!" }
    }
    ```

- Response **400**

    ```json
    {
      "code": 400,
      "errors": ["Product name must be between 2 and 100 characters"]
    }
    ```

- Response **409**

    ```json
    {
      "code": 409,
      "errors": ["Product already exists"]
    }
    ```


### Update Product

- **PUT** `/products/{id}`
- Body

    ```json
    {
      "name": "iPhone 15 Pro",
      "description": "Apple flagship smartphone with A17 chip",
      "price": 1299.99,
      "category": "ELECTRONICS"
    }
    ```

- Response **200**

    ```json
    {
      "code": 200,
      "message": "Success",
      "result": { "message": "Update product successfully!" }
    }
    ```

- Response **400** & **409** giống **Create New Product.**

### Delete Product

- **DELETE** `/products/{id}`
- Response **200**

    ```json
    {
      "code": 200,
      "message": "Success"
    }
    ```

- Response **400**

    ```json
    { "code": 400, "errors": ["Invalid productId format. Must be a valid Mongo ObjectId"] }
    ```

- Response **404**

    ```json
    { "code": 404, "errors": ["Product not found"] }
    ```


### Filter & Sort Products

- **GET** `/products/filter`
- **Query Params:**
    - `category` → filter theo category (`ELECTRONICS, CLOTHING, FOOD, BOOKS, HOME, PREMIUM`)
    - `field` → `NAME | PRICE`
    - `direction` → `ASC | DESC`
    - `page` (default: 0)
    - `size` (default: 20)
- Response **200**

    ```json
    {
      "code": 200,
      "message": "Success",
      "result": {
        "content": [
          {
            "id": "68b2f3f6d8d00296671c9e57",
            "name": "Sony WH-1000XM5",
            "description": "Noise cancelling wireless headphones",
            "price": 399.99,
            "category": "ELECTRONICS"
          }
        ],
        "pageable": { "pageNumber": 0, "pageSize": 20 },
        "totalElements": 60,
        "totalPages": 3
      }
    }
    ```

- Response **400** (Invalid sort field - name/price)

    ```json
    {
        "code": 400,
        "errors": [
            "Parameter 'field' has invalid value 'WRONG'"
        ]
    }
    ```


---

## 🧪 Postman Collection

👉 **File collection**: [product-catalog.postman_collection.json](product-catalog.postman_collection.json)

---

## 📑 API Endpoints Summary

| Method | Endpoint | Description                      | Params / Body | Success Response | Error Cases |
| --- | --- |----------------------------------| --- | --- | --- |
| GET | `/products` | Get product list                 | `page`, `size` (optional) | 200 OK | – |
| GET | `/products/{id}` | Get product details by productID | Path: `id` | 200 OK | 400, 404 |
| POST | `/products` | Create new product               | Body: `{name, description, price, category}` | 201 Created | 400, 409 |
| PUT | `/products/{id}` | Update product through productID | Path: `id`, Body JSON | 200 OK | 400, 404, 409 |
| DELETE | `/products/{id}` | Delete product through productID | Path: `id` | 200 OK | 400, 404 |
| GET | `/products/filter` | Filter + Sort products           | Params: `category, field, direction, page, size` | 200 OK | 400, Invalid Category: ‘empty list’ |
// app.js - Fixed version

const BASE_URL = "http://localhost:8083/product-catalog";

let currentPage = 0;
let pageSize = 20;
let editingId = null;
let currentSort = { field: null, direction: null };
let currentCategory = "";

// ===================== INIT =====================
document.addEventListener("DOMContentLoaded", () => {
    fetchProducts();

    // Create button
    document.getElementById("btn-create").addEventListener("click", () => {
        openCreate();
    });

    // Save form
    document.getElementById("product-form").addEventListener("submit", (e) => {
        e.preventDefault();
        saveProduct();
    });

    // Sort
    document.getElementById("sort-name").addEventListener("click", () => {
        toggleSort("NAME");
    });
    document.getElementById("sort-price").addEventListener("click", () => {
        toggleSort("PRICE");
    });

    // Filter category
    document.getElementById("filter-category").addEventListener("change", (e) => {
        currentCategory = e.target.value;
        currentPage = 0; // Reset về trang đầu
        fetchProducts();
    });

    // pageSize
    document.getElementById("page-size").addEventListener("change", (e) => {
        pageSize = parseInt(e.target.value, 10);
        currentPage = 0; // reset về trang đầu
        fetchProducts();
    });

    // Init category dropdown
    initCategoryOptions();
});

// ===================== API FUNCTIONS =====================
async function fetchProducts(page = currentPage) {
    try {
        toggleLoading(true);
        let url = "";

        // LOGIC PHÂN BIỆT ENDPOINT
        if (hasSort()) {
            // Có sort -> dùng /products/sort (không cache)
            url = buildSortUrl(page);
        } else if (hasFilter()) {
            // Chỉ filter category -> dùng /products/filter (có cache)
            url = buildFilterUrl(page);
        } else {
            // Không có gì -> dùng /products (có cache)
            url = buildDefaultUrl(page);
        }

        console.log("Fetching from:", url);

        const res = await fetch(url);
        const data = await res.json();

        if (res.ok) {
            renderProducts(data.result.content);
            renderPagination(data.result.totalPages, page);
        } else {
            console.error("Error fetching:", data.errors);
        }
    } catch (err) {
        console.error("Error fetching products:", err);
    } finally {
        toggleLoading(false);
    }
}

// HELPER FUNCTIONS FOR URL BUILDING
function hasSort() {
    return currentSort.field !== null && currentSort.direction !== null;
}

function hasFilter() {
    return currentCategory && currentCategory !== "";
}

function buildDefaultUrl(page) {
    return `${BASE_URL}/products?page=${page}&size=${pageSize}`;
}

function buildFilterUrl(page) {
    const params = new URLSearchParams();
    params.append("category", currentCategory);
    params.append("page", page);
    params.append("size", pageSize);
    return `${BASE_URL}/products/filter?${params.toString()}`;
}

function buildSortUrl(page) {
    const params = new URLSearchParams();
    if (currentCategory) params.append("category", currentCategory);
    params.append("field", currentSort.field);
    params.append("direction", currentSort.direction);
    params.append("page", page);
    params.append("size", pageSize);
    return `${BASE_URL}/products/sort?${params.toString()}`;
}

async function createProduct(product) {
    const res = await fetch(`${BASE_URL}/products`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(product),
    });
    const data = await res.json();
    if (res.ok) {
        fetchProducts();
        bootstrap.Modal.getInstance(document.getElementById("product-modal")).hide();
    } else {
        alert(data.errors.join(", "));
    }
}

async function updateProduct(id, product) {
    const res = await fetch(`${BASE_URL}/products/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(product),
    });
    const data = await res.json();
    if (res.ok) {
        fetchProducts();
        bootstrap.Modal.getInstance(document.getElementById("product-modal")).hide();
    } else {
        alert(data.errors.join(", "));
    }
}

async function deleteProduct(id) {
    if (!confirm("Are you sure to delete?")) return;
    const res = await fetch(`${BASE_URL}/products/${id}`, { method: "DELETE" });
    if (res.ok) {
        fetchProducts();
    } else {
        const data = await res.json();
        alert(data.errors.join(", "));
    }
}

// ===================== UI HANDLERS =====================
function renderProducts(products) {
    const tbody = document.getElementById("product-table-body");
    tbody.innerHTML = "";

    const cards = document.getElementById("product-cards");
    cards.innerHTML = "";

    if (!products || products.length === 0) {
        document.getElementById("empty-state").classList.remove("d-none");
        return;
    }
    document.getElementById("empty-state").classList.add("d-none");

    products.forEach((p, index) => {
        const row = `
        <tr>
            <td>${index + 1 + currentPage * pageSize}</td>
            <td>${p.name}</td>
            <td>${p.description}</td>
            <td>$${p.price.toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</td>
            <td>${p.category}</td>
            <td>
            <button class="btn btn-sm btn-warning me-1" 
                onclick="openEdit('${p.id}', '${p.name}', '${p.description}', ${p.price}, '${p.category}')">Edit</button>
            <button class="btn btn-sm btn-danger" 
                onclick="deleteProduct('${p.id}')">Delete</button>
            </td>
        </tr>
    `;
        tbody.insertAdjacentHTML("beforeend", row);

        // Mobile card
        const card = `
        <div class="card mb-2">
            <div class="card-body">
            <h5 class="card-title">${index + 1 + currentPage * pageSize}. ${p.name}</h5>
            <p class="card-text">${p.description}</p>
            <p class="card-text"><strong>$${p.price.toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</strong></p>
            <p class="card-text"><span class="badge bg-secondary">${p.category}</span></p>
            <button class="btn btn-sm btn-warning me-1" 
                onclick="openEdit('${p.id}', '${p.name}', '${p.description}', ${p.price}, '${p.category}')">Edit</button>
            <button class="btn btn-sm btn-danger" 
                onclick="deleteProduct('${p.id}')">Delete</button>
            </div>
        </div>
    `;
        cards.insertAdjacentHTML("beforeend", card);
    });
}

function renderPagination(totalPages, current) {
    const pagination = document.querySelector("#pagination ul");
    pagination.innerHTML = "";

    for (let i = 0; i < totalPages; i++) {
        const li = document.createElement("li");
        li.className = `page-item ${i === current ? "active" : ""}`;
        li.innerHTML = `<a class="page-link" href="#">${i + 1}</a>`;
        li.addEventListener("click", (e) => {
            e.preventDefault();
            currentPage = i;
            fetchProducts();
            window.scrollTo({ top: 0, behavior: "smooth" });
        });
        pagination.appendChild(li);
    }
}

function openCreate() {
    editingId = null;
    document.getElementById("modal-title").textContent = "Create Product";
    document.getElementById("product-form").reset();
    new bootstrap.Modal(document.getElementById("product-modal")).show();
}

function openEdit(id, name, desc, price, category) {
    editingId = id;
    document.getElementById("modal-title").textContent = "Edit Product";
    document.getElementById("product-name").value = name;
    document.getElementById("product-description").value = desc;
    document.getElementById("product-price").value = price;
    document.getElementById("product-category").value = category;
    new bootstrap.Modal(document.getElementById("product-modal")).show();
}

function saveProduct() {
    const product = {
        name: document.getElementById("product-name").value,
        description: document.getElementById("product-description").value,
        price: parseFloat(document.getElementById("product-price").value),
        category: document.getElementById("product-category").value,
    };
    if (editingId) {
        updateProduct(editingId, product);
    } else {
        createProduct(product);
    }
}

function toggleSort(field) {
    if (currentSort.field !== field) {
        // Nếu click sang cột khác -> bắt đầu ASC
        currentSort.field = field;
        currentSort.direction = "ASC";
    } else {
        // Nếu click cùng cột -> cycle ASC -> DESC -> CLEAR
        if (currentSort.direction === "ASC") {
            currentSort.direction = "DESC";
        } else if (currentSort.direction === "DESC") {
            currentSort = { field: null, direction: null };
        } else {
            currentSort.direction = "ASC";
        }
    }

    currentPage = 0; // Reset về trang đầu khi sort
    updateSortIcons();
    fetchProducts();
}

function updateSortIcons() {
    const nameIcon = document.getElementById("icon-name");
    const priceIcon = document.getElementById("icon-price");

    // reset về mặc định
    nameIcon.className = "fas fa-sort";
    priceIcon.className = "fas fa-sort";

    if (currentSort.field === "NAME") {
        nameIcon.className = currentSort.direction === "ASC"
            ? "fas fa-sort-amount-up"
            : "fas fa-sort-amount-down";
    }

    if (currentSort.field === "PRICE") {
        priceIcon.className = currentSort.direction === "ASC"
            ? "fas fa-sort-amount-up"
            : "fas fa-sort-amount-down";
    }
}

function toggleLoading(show) {
    const spinner = document.getElementById("loading-spinner");
    if (show) spinner.classList.remove("d-none");
    else spinner.classList.add("d-none");
}

function initCategoryOptions() {
    const categories = ["ELECTRONICS", "CLOTHING", "FOOD", "BOOKS", "HOME", "PREMIUM"];
    const filterSelect = document.getElementById("filter-category");
    const formSelect = document.getElementById("product-category");

    categories.forEach((c) => {
        const opt1 = document.createElement("option");
        opt1.value = c;
        opt1.textContent = c;
        filterSelect.appendChild(opt1);

        const opt2 = document.createElement("option");
        opt2.value = c;
        opt2.textContent = c;
        formSelect.appendChild(opt2);
    });
}
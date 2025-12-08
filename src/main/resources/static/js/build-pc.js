const componentMeta = {
    'CPU': { title: 'Vi xử lý (CPU)', icon: 'bi-cpu' },
    'MAINBOARD': { title: 'Bo mạch chủ (Mainboard)', icon: 'bi-motherboard' },
    'RAM': { title: 'Bộ nhớ trong (RAM)', icon: 'bi-memory' },
    'VGA': { title: 'Card màn hình (VGA)', icon: 'bi-gpu-card' },
    'STORAGE': { title: 'Ổ cứng (SSD/HDD)', icon: 'bi-device-hdd' },
    'PSU': { title: 'Nguồn máy tính (PSU)', icon: 'bi-plug' },
    'COOLING': { title: 'Tản nhiệt (Cooling)', icon: 'bi-fan' },
    'CASE': { title: 'Vỏ máy tính (Case)', icon: 'bi-pc-display' },
    'MONITOR': { title: 'Màn hình (Monitor)', icon: 'bi-display' }
};

const componentTypes = Object.keys(componentMeta);


let selectedConfig = {};
let currentProductList = [];
let currentSelectingType = null;
let productModal = null;


document.addEventListener('DOMContentLoaded', () => {
    //Modal
    const modalEl = document.getElementById('productModal');
    if (modalEl) {
        productModal = new bootstrap.Modal(modalEl);
    }

    //Render giao diện chính
    initBuilder();

    const btnAddCart = document.getElementById('add-to-cart');
    const btnPrint = document.getElementById('print-config');
    const btnClear = document.getElementById('clear-config');

    if (btnAddCart) btnAddCart.addEventListener('click', handleAddToCart);
    if (btnPrint) btnPrint.addEventListener('click', handlePrintConfig);
    if (btnClear) btnClear.addEventListener('click', () => {
        if(confirm('Bạn có chắc muốn xóa toàn bộ cấu hình?')) {
            resetInterface();
        }
    });

    //Tìm kiếm & Sắp xếp trong Modal
    const searchInput = document.getElementById('modal-search-input');
    const sortSelect = document.getElementById('modal-sort-select');

    if (searchInput) {
        searchInput.addEventListener('input', handleFilterAndSort);
    }
    if (sortSelect) {
        sortSelect.addEventListener('change', handleFilterAndSort);
    }
});

//LOGIC RENDER BUILDER ---
function initBuilder() {
    const container = document.getElementById('buildPC-container');
    if (!container) return;
    container.innerHTML = '';

    componentTypes.forEach(type => {
        const row = document.createElement('div');
        row.id = `row-${type}`;
        row.className = 'component-row';
        row.innerHTML = renderEmptyRow(type);
        container.appendChild(row);
    });
    updateSummary();
}

function resetInterface() {
    selectedConfig = {};
    initBuilder();
    updateSummary();
}

function renderEmptyRow(type) {
    const meta = componentMeta[type];
    return `
        <div class="comp-icon"><i class="bi ${meta.icon}"></i></div>
        <div class="comp-info">
            <div class="comp-name">${meta.title}</div>
            <div class="comp-placeholder">Vui lòng chọn linh kiện</div>
        </div>
        <div class="comp-actions">
            <button class="btn btn-select rounded-pill" onclick="openModal('${type}')">
                <i class="bi bi-plus-lg"></i> Chọn
            </button>
        </div>
    `;
}

function renderSelectedRow(type, product) {
    const meta = componentMeta[type];
    return `
        <img src="${product.image || 'https://via.placeholder.com/80'}" class="comp-img-selected" alt="${product.name}" onerror="this.src='https://via.placeholder.com/80'">
        <div class="comp-info">
            <div class="comp-name text-primary">${meta.title}</div>
            <div class="fw-bold text-dark mb-1">${product.name}</div>
            <div class="comp-price">${formatCurrency(product.price)}</div>
        </div>
        <div class="comp-actions">
            <button class="btn btn-sm btn-outline-secondary mb-1" onclick="openModal('${type}')">
                <i class="bi bi-pencil"></i> Đổi
            </button>
            <button class="btn btn-sm btn-outline-danger" onclick="removeProduct('${type}')">
                <i class="bi bi-trash"></i> Xóa
            </button>
        </div>
    `;
}

// MODAL
async function openModal(type) {
    if (!productModal) return;
    currentSelectingType = type;
    document.getElementById('modalTitle').innerText = `Chọn ${componentMeta[type].title}`;

    //filter
    const searchInput = document.getElementById('modal-search-input');
    const sortSelect = document.getElementById('modal-sort-select');
    if(searchInput) searchInput.value = '';
    if(sortSelect) sortSelect.value = 'default';
    currentProductList = [];

    // loading
    const modalBody = document.getElementById('modal-product-list');
    modalBody.innerHTML = '<div class="text-center w-100 py-5"><div class="spinner-border text-primary"></div><p>Đang tải sản phẩm...</p></div>';

    productModal.show();

    try {
        const res = await fetch(`/api/products/type/${type}`);
        if (!res.ok) throw new Error('API Error');
        const products = await res.json();

        if (!products || products.length === 0) {
            modalBody.innerHTML = '<div class="col-12 text-center py-4 text-muted">Không tìm thấy sản phẩm nào.</div>';
            return;
        }

        currentProductList = products;
        renderProductsToModal(currentProductList, type);

    } catch (err) {
        console.error(err);
        modalBody.innerHTML = '<div class="text-danger text-center w-100 py-4">Lỗi tải dữ liệu hoặc không có kết nối.</div>';
    }
}

function handleFilterAndSort() {
    const keyword = document.getElementById('modal-search-input').value.toLowerCase().trim();
    const sortValue = document.getElementById('modal-sort-select').value;
    const type = currentSelectingType;

    let filteredProducts = currentProductList.filter(p => {
        const name = p.name ? p.name.toLowerCase() : '';
        return name.includes(keyword);
    });

    if (sortValue === 'price_asc') {
        filteredProducts.sort((a, b) => (a.priceAfterDiscount || a.price || 0) - (b.priceAfterDiscount || b.price || 0));
    } else if (sortValue === 'price_desc') {
        filteredProducts.sort((a, b) => (b.priceAfterDiscount || b.price || 0) - (a.priceAfterDiscount || a.price || 0));
    }

    if (filteredProducts.length === 0) {
        document.getElementById('modal-product-list').innerHTML = `
            <div class="col-12 text-center py-5 text-muted">
                <i class="bi bi-search display-4"></i>
                <p class="mt-2">Không tìm thấy sản phẩm phù hợp.</p>
            </div>`;
    } else {
        renderProductsToModal(filteredProducts, type);
    }
}

function renderProductsToModal(products, type) {
    const modalBody = document.getElementById('modal-product-list');
    modalBody.innerHTML = products.map(p => {
        const price = p.priceAfterDiscount || p.price || 0;
        const img = p.mainImage || 'https://via.placeholder.com/150';
        const safeName = escapeHtml(p.name);

        return `
        <div class="col-md-3 col-6">
            <div class="card product-item-card h-100" onclick="selectProduct('${type}', '${p.id}', '${safeName}', ${price}, '${img}')">
                <img src="${img}" class="card-img-top" alt="${safeName}">
                <div class="card-body p-2 d-flex flex-column">
                    <h6 class="card-title text-truncate" title="${safeName}" style="font-size: 0.9rem;">${p.name}</h6>
                    <div class="mt-auto">
                        <p class="card-text text-danger fw-bold mb-2">${formatCurrency(price)}</p>
                        <button class="btn btn-sm btn-danger w-100">Thêm</button>
                    </div>
                </div>
            </div>
        </div>`;
    }).join('');
}

// select and remove product
function selectProduct(type, id, name, price, image) {
    selectedConfig[type] = { id, name, price: Number(price), image };
    const row = document.getElementById(`row-${type}`);
    if (row) row.innerHTML = renderSelectedRow(type, selectedConfig[type]);
    updateSummary();
    productModal.hide();
}

function removeProduct(type) {
    delete selectedConfig[type];
    document.getElementById(`row-${type}`).innerHTML = renderEmptyRow(type);
    updateSummary();
}

function updateSummary() {
    const summaryList = document.getElementById('config-summary-list');
    const totalEl = document.getElementById('config-total');
    let total = 0;
    let html = '';

    const keys = Object.keys(selectedConfig);
    if (keys.length === 0) {
        summaryList.innerHTML = '<p class="text-center text-muted fst-italic py-3">Chưa có linh kiện nào được chọn</p>';
        totalEl.innerText = '0₫';
        return;
    }

    keys.forEach(type => {
        const item = selectedConfig[type];
        total += item.price;
        html += `
            <div class="d-flex justify-content-between mb-2 small border-bottom pb-2">
                <div style="width: 70%">
                    <span class="fw-bold text-muted" style="font-size: 0.8rem">${type}:</span><br>
                    <span>${item.name}</span>
                </div>
                <div class="fw-bold text-danger text-end" style="width: 30%">
                    ${formatCurrency(item.price)}
                </div>
            </div>
        `;
    });

    summaryList.innerHTML = html;
    totalEl.innerText = formatCurrency(total);
}

//add cart
async function handleAddToCart() {
    const btn = document.getElementById('add-to-cart');
    const items = Object.values(selectedConfig);

    if (items.length === 0) {
        showAlert('Vui lòng chọn linh kiện trước!', 'warning');
        return;
    }

    // Check Auth
    if (typeof apiClient === 'undefined') {
        alert('Lỗi: Chưa load api.js'); return;
    }
    if (!apiClient.isAuthenticated()) {
        showAlert('Vui lòng đăng nhập để thêm vào giỏ hàng', 'warning');
        if (typeof showLoginModal === 'function') showLoginModal();
        else window.location.href = '/login.html';
        return;
    }

    // Get User ID
    let user = apiClient.getUser();
    let userId = user ? user.id : null;
    if(!userId) {
        try {
            user = await apiClient.request('/users/profile', {method: 'GET', noAuthRedirect: true});
            if(user) userId = user.id;
        } catch(e) {}
    }
    if(!userId) { showAlert('Lỗi xác thực người dùng. Đăng nhập lại.', 'error'); return; }

    // Loading effect
    const oldText = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Đang thêm...';

    try {
        const promises = items.map(item => {
            return apiClient.request('/cart/add', {
                method: 'POST',
                body: JSON.stringify({
                    userId: Number(userId),
                    productId: Number(item.id),
                    quantity: 1
                })
            });
        });

        await Promise.all(promises);

        // // Thông báo thành công
        // showAlert(`Đã thêm thành công ${items.length} linh kiện vào giỏ hàng!`, 'success');

        // Refesh
        resetInterface();

        // update cart
        if (typeof updateCartBadge === 'function') await updateCartBadge();

        if (confirm(`Đã thêm thành công ${items.length} linh kiện vào giỏ hàng!\nBạn có muốn xem giỏ hàng ngay không?`)) {
            window.location.href = '/cart.html';
        }
    } catch (err) {
        console.error(err);
        showAlert('Có lỗi khi thêm vào giỏ hàng.', 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = oldText;
    }
}

// BILL
function handlePrintConfig() {
    const items = Object.values(selectedConfig);
    if (items.length === 0) {
        showAlert('Vui lòng chọn linh kiện để in báo giá!', 'warning');
        return;
    }

    const now = new Date();
    document.getElementById('print-date').innerText = `Ngày tạo: ${now.toLocaleDateString('vi-VN')} ${now.toLocaleTimeString('vi-VN')}`;

    const tbody = document.getElementById('print-table-body');
    tbody.innerHTML = '';
    let total = 0;
    let index = 1;

    componentTypes.forEach(type => {
        if (selectedConfig[type]) {
            const item = selectedConfig[type];
            total += item.price;
            const typeName = componentMeta[type].title.split('(')[0];

            tbody.innerHTML += `
                <tr>
                    <td class="text-center">${index++}</td>
                    <td>${typeName}</td>
                    <td>${item.name}</td>
                    <td class="text-center">1</td>
                    <td class="text-end fw-bold">${formatCurrency(item.price)}</td>
                </tr>
            `;
        }
    });

    document.getElementById('print-total-price').innerText = formatCurrency(total);
    window.print();
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
}

function escapeHtml(text) {
    if (!text) return "";
    return text.replace(/["']/g, "").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}


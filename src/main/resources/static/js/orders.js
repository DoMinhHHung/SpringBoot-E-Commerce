// orders.js - Danh sách đơn hàng của user (Redesigned)

let currentPage = 0;
let pageSize = 10;
let currentStatus = null;
let searchQuery = '';

document.addEventListener('DOMContentLoaded', async function() {
    if (!apiClient || !apiClient.isAuthenticated()) {
        window.location.href = '/login.html?redirect=' + encodeURIComponent(window.location.pathname);
        return;
    }

    setupEventListeners();
    await loadOrders();
});

function setupEventListeners() {
    // Tab clicks
    document.querySelectorAll('.order-tab').forEach(tab => {
        tab.addEventListener('click', async (e) => {
            // Update active tab
            document.querySelectorAll('.order-tab').forEach(t => t.classList.remove('active'));
            e.target.classList.add('active');
            
            // Update status filter
            currentStatus = e.target.dataset.status || null;
            currentPage = 0;
            await loadOrders();
        });
    });

    // Search button
    document.getElementById('btn-search-order').addEventListener('click', async () => {
        searchQuery = document.getElementById('order-search-input').value.trim();
        currentPage = 0;
        await loadOrders();
    });

    // Search on Enter key
    document.getElementById('order-search-input').addEventListener('keypress', async (e) => {
        if (e.key === 'Enter') {
            searchQuery = e.target.value.trim();
            currentPage = 0;
            await loadOrders();
        }
    });
}

async function loadOrders() {
    try {
        showLoading();

        const params = new URLSearchParams({
            page: currentPage,
            size: pageSize
        });
        
        if (currentStatus) {
            params.append('status', currentStatus);
        }
        
        if (searchQuery) {
            params.append('search', searchQuery);
        }

        const response = await apiClient.request(`/orders?${params}`, {
            method: 'GET'
        });

        renderOrders(response.content || response.data || []);
        renderPagination(response.totalPages || 0, response.totalElements || 0);
    } catch (error) {
        console.error('Error loading orders:', error);
        showError('Lỗi tải danh sách đơn hàng: ' + error.message);
    }
}

function showLoading() {
    document.getElementById('orders-list').innerHTML = `
        <div class="text-center py-5">
            <div class="spinner-border text-danger" role="status">
                <span class="visually-hidden">Loading...</span>
            </div>
            <p class="mt-3 text-muted">Đang tải danh sách đơn hàng...</p>
        </div>
    `;
}

function showError(message) {
    document.getElementById('orders-list').innerHTML = `
        <div class="alert alert-danger">
            <i class="bi bi-exclamation-triangle"></i> ${message}
        </div>
    `;
}

function renderOrders(orders) {
    const container = document.getElementById('orders-list');
    
    if (!orders || orders.length === 0) {
        container.innerHTML = `
            <div class="orders-empty-state">
                <i class="bi bi-inbox"></i>
                <h5>Chưa có đơn hàng</h5>
                <p>Bạn chưa có đơn hàng nào trong danh sách này</p>
                <a href="/index.html" class="btn btn-danger">
                    <i class="bi bi-cart" style="font-size: 1rem; color: white;"></i> Mua sắm ngay
                </a>
            </div>
        `;
        return;
    }

    container.innerHTML = orders.map(order => renderOrderCard(order)).join('');
}

function renderOrderCard(order) {
    const statusInfo = getStatusInfo(order.status);
    const items = order.items || [];
    
    return `
        <div class="modern-order-card">
            <!-- Header -->
            <div class="order-card-header">
                <div class="order-header-left">
                    <i class="bi bi-shop order-shop-icon"></i>
                    <span class="order-code">#${order.orderCode}</span>
                    <span class="order-date">
                        <i class="bi bi-calendar3"></i> ${formatDate(order.createdAt)}
                    </span>
                </div>
                <div class="order-header-right">
                    <span class="status-badge status-${order.status.toLowerCase()}">
                        ${statusInfo.icon}
                        ${statusInfo.label}
                    </span>
                </div>
            </div>

            <!-- Body -->
            <div class="order-card-body">
                ${items.length > 0 ? items.map(item => `
                    <div class="order-product-item">
                        <img src="${item.productImage || '/images/placeholder.png'}" 
                             alt="${item.productName}" 
                             class="order-product-image"
                             onerror="this.src='/images/placeholder.png'">
                        <div class="order-product-info">
                            <div class="order-product-name">${item.productName}</div>
                            <div class="order-product-quantity">x${item.quantity}</div>
                        </div>
                        <div class="order-product-price">
                            ${item.discountAmount && item.discountAmount > 0 ? 
                                `<span class="order-price-old">${formatPrice(item.unitPrice)}</span>` : ''}
                            <span class="order-price-new">${formatPrice(item.totalPrice)}</span>
                        </div>
                    </div>
                `).join('') : `
                    <div class="text-muted text-center py-3">
                        <i class="bi bi-box"></i> ${order.itemCount || 0} sản phẩm
                    </div>
                `}
            </div>

            <!-- Footer -->
            <div class="order-card-footer">
                <div class="order-total-section">
                    <span class="order-total-label">Thành tiền:</span>
                    <span class="order-total-amount">${formatPrice(order.totalAmount)}</span>
                </div>
                <div class="order-actions">
                    <button class="btn-order-action primary" onclick="viewOrderDetail(${order.orderCode})">
                        Xem chi tiết
                    </button>
                    ${order.status === 'DELIVERED' ? 
                        `<button class="btn-order-action" onclick="rateOrder(${order.orderCode})">
                            Đánh giá
                        </button>` : ''}
                    ${order.status === 'PENDING' ? 
                        `<button class="btn-order-action" onclick="cancelOrder(${order.orderCode})">
                            Hủy đơn
                        </button>` : ''}
                </div>
            </div>
        </div>
    `;
}

function getStatusInfo(status) {
    const statusMap = {
        'PENDING': { label: 'Chờ xác nhận', icon: '<i class="bi bi-clock-history"></i>' },
        'CONFIRMED': { label: 'Đã xác nhận', icon: '<i class="bi bi-check-circle"></i>' },
        'PROCESSING': { label: 'Đang xử lý', icon: '<i class="bi bi-arrow-repeat"></i>' },
        'SHIPPED': { label: 'Đang vận chuyển', icon: '<i class="bi bi-truck"></i>' },
        'DELIVERED': { label: 'Giao hàng thành công', icon: '<i class="bi bi-check-circle-fill"></i>' },
        'CANCELLED': { label: 'Đã hủy', icon: '<i class="bi bi-x-circle"></i>' },
        'REFUNDED': { label: 'Đã hoàn tiền', icon: '<i class="bi bi-arrow-counterclockwise"></i>' }
    };
    return statusMap[status] || { label: status, icon: '<i class="bi bi-question-circle"></i>' };
}

function rateOrder(orderCode) {
    showAlert('Chức năng đánh giá đang được phát triển', 'info');
}

function cancelOrder(orderCode) {
    if (confirm('Bạn có chắc muốn hủy đơn hàng này?')) {
        showAlert('Đã gửi yêu cầu hủy đơn hàng', 'success');
    }
}

function renderPagination(totalPages, totalElements) {
    const pagination = document.getElementById('pagination');
    if (totalPages <= 1) {
        pagination.innerHTML = '';
        return;
    }

    let html = '';
    
    // Previous button
    html += `
        <li class="page-item ${currentPage === 0 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="changePage(${currentPage - 1}); return false;">Trước</a>
        </li>
    `;

    // Page numbers
    for (let i = 0; i < totalPages; i++) {
        if (i === 0 || i === totalPages - 1 || (i >= currentPage - 2 && i <= currentPage + 2)) {
            html += `
                <li class="page-item ${i === currentPage ? 'active' : ''}">
                    <a class="page-link" href="#" onclick="changePage(${i}); return false;">${i + 1}</a>
                </li>
            `;
        } else if (i === currentPage - 3 || i === currentPage + 3) {
            html += '<li class="page-item disabled"><span class="page-link">...</span></li>';
        }
    }

    // Next button
    html += `
        <li class="page-item ${currentPage >= totalPages - 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="changePage(${currentPage + 1}); return false;">Sau</a>
        </li>
    `;

    pagination.innerHTML = html;
}

async function changePage(page) {
    currentPage = page;
    await loadOrders();
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function viewOrderDetail(orderCode) {
    window.location.href = `/order-detail.html?orderCode=${orderCode}`;
}

function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN');
}


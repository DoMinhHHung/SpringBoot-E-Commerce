// orders.js - Danh sách đơn hàng của user

let currentPage = 0;
let pageSize = 10;
let currentStatus = null;

document.addEventListener('DOMContentLoaded', async function() {
    if (!apiClient || !apiClient.isAuthenticated()) {
        window.location.href = '/login.html?redirect=' + encodeURIComponent(window.location.pathname);
        return;
    }

    setupEventListeners();
    await loadOrders();
});

function setupEventListeners() {
    document.getElementById('btn-apply-filter').addEventListener('click', async () => {
        currentPage = 0;
        await loadOrders();
    });

    document.getElementById('btn-reset-filter').addEventListener('click', () => {
        document.getElementById('filter-status').value = '';
        currentStatus = null;
        currentPage = 0;
        loadOrders();
    });
}

async function loadOrders() {
    try {
        const status = document.getElementById('filter-status').value;
        currentStatus = status || null;

        const params = new URLSearchParams({
            page: currentPage,
            size: pageSize
        });
        if (currentStatus) {
            params.append('status', currentStatus);
        }

        const response = await apiClient.request(`/orders?${params}`, {
            method: 'GET'
        });

        renderOrders(response.content || response.data || []);
        renderPagination(response.totalPages || 0, response.totalElements || 0);
    } catch (error) {
        console.error('Error loading orders:', error);
        document.getElementById('orders-list').innerHTML = `
            <div class="alert alert-danger">
                <i class="bi bi-exclamation-triangle"></i> Lỗi tải danh sách đơn hàng: ${error.message}
            </div>
        `;
    }
}

function renderOrders(orders) {
    const container = document.getElementById('orders-list');
    
    if (!orders || orders.length === 0) {
        container.innerHTML = `
            <div class="card">
                <div class="card-body text-center py-5">
                    <i class="bi bi-inbox" style="font-size: 3rem; color: #ccc;"></i>
                    <p class="mt-3 text-muted">Bạn chưa có đơn hàng nào</p>
                    <a href="/index.html" class="btn btn-primary">
                        <i class="bi bi-cart"></i> Mua sắm ngay
                    </a>
                </div>
            </div>
        `;
        return;
    }

    container.innerHTML = orders.map(order => {
        const statusBadge = getStatusBadge(order.status, order.statusLabel);
        
        return `
            <div class="card mb-3 order-card" style="cursor: pointer;" onclick="viewOrderDetail(${order.orderCode})">
                <div class="card-body">
                    <div class="row align-items-center">
                        <div class="col-md-3">
                            <h6 class="mb-1">Mã đơn hàng</h6>
                            <strong>#${order.orderCode}</strong>
                        </div>
                        <div class="col-md-2">
                            <h6 class="mb-1">Số lượng</h6>
                            <span>${order.itemCount} sản phẩm</span>
                        </div>
                        <div class="col-md-2">
                            <h6 class="mb-1">Tổng tiền</h6>
                            <strong class="text-danger">${formatPrice(order.totalAmount)}</strong>
                        </div>
                        <div class="col-md-2">
                            <h6 class="mb-1">Ngày đặt</h6>
                            <span class="text-muted small">${formatDate(order.createdAt)}</span>
                        </div>
                        <div class="col-md-2 text-center">
                            ${statusBadge}
                        </div>
                        <div class="col-md-1 text-end">
                            <i class="bi bi-chevron-right text-muted"></i>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

function getStatusBadge(status, statusLabel) {
    const badges = {
        'PENDING': '<span class="badge bg-warning">Đang chờ xác nhận</span>',
        'CONFIRMED': '<span class="badge bg-info">Đã xác nhận</span>',
        'PROCESSING': '<span class="badge bg-primary">Đang xử lý</span>',
        'SHIPPED': '<span class="badge bg-primary">Đang vận chuyển</span>',
        'DELIVERED': '<span class="badge bg-success">Đã nhận hàng</span>',
        'CANCELLED': '<span class="badge bg-secondary">Đã hủy</span>',
        'REFUNDED': '<span class="badge bg-danger">Đã hoàn tiền</span>'
    };
    return badges[status] || `<span class="badge bg-secondary">${statusLabel}</span>`;
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


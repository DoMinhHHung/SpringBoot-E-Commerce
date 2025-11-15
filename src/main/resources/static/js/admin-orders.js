// admin-orders.js - Quản lý đơn hàng cho admin

let currentPage = 0;
let pageSize = 20;
let currentStatus = null;

document.addEventListener('DOMContentLoaded', async function() {
    if (!apiClient || !apiClient.isAuthenticated()) {
        window.location.href = '/login.html?redirect=' + encodeURIComponent(window.location.pathname);
        return;
    }

    try {
        const user = await apiClient.getProfile();
        if (!user) {
            window.location.href = '/login.html';
            return;
        }
        
        const isAdmin = Array.isArray(user.role) 
            ? user.role.indexOf('ADMIN') !== -1 || user.role.indexOf('ROLE_ADMIN') !== -1
            : user.role === 'ADMIN' || user.role === 'ROLE_ADMIN';
            
        if (!isAdmin) {
            alert('Không có quyền truy cập');
            window.location.href = '/index.html';
            return;
        }

        document.getElementById('admin-welcome').textContent = 
            'Xin chào, ' + (user.fullName || user.email);

        setupEventListeners();
        await loadOrders();
    } catch (err) {
        console.error('Error initializing:', err);
        showAlert('Lỗi tải dữ liệu: ' + (err.message || err), 'error');
    }
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

    document.getElementById('btn-confirm-update').addEventListener('click', async () => {
        await updateOrderStatus();
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

        const response = await apiClient.request(`/admin/orders?${params}`, {
            method: 'GET'
        });

        renderOrders(response.content || response.data || []);
        renderPagination(response.totalPages || 0, response.totalElements || 0);
    } catch (error) {
        console.error('Error loading orders:', error);
        document.getElementById('orders-tbody').innerHTML = 
            '<tr><td colspan="7" class="text-center text-danger">Lỗi tải dữ liệu</td></tr>';
    }
}

function renderOrders(orders) {
    const tbody = document.getElementById('orders-tbody');
    
    if (!orders || orders.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">Không có dữ liệu</td></tr>';
        return;
    }

        tbody.innerHTML = orders.map(order => {
        const statusBadge = getStatusBadge(order.status, order.statusLabel);
        const userName = order.userName || order.userEmail || '-';
        
        return `
            <tr>
                <td><strong>#${order.orderCode}</strong></td>
                <td>${userName}</td>
                <td>${order.itemCount} SP</td>
                <td class="fw-bold text-danger">${formatPrice(order.totalAmount)}</td>
                <td>${statusBadge}</td>
                <td>${formatDateTime(order.createdAt)}</td>
                <td>
                    <button class="btn btn-sm btn-info me-1" onclick="viewOrderDetail(${order.orderCode})">
                        <i class="bi bi-eye"></i>
                    </button>
                    <button class="btn btn-sm btn-primary" onclick="showUpdateStatusModal(${order.orderCode}, '${order.status}', '${order.statusLabel.replace(/'/g, "\\'")}')">
                        <i class="bi bi-pencil"></i> Sửa
                    </button>
                </td>
            </tr>
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
    window.open(`/order-detail.html?orderCode=${orderCode}`, '_blank');
}

function showUpdateStatusModal(orderCode, currentStatus, currentStatusLabel) {
    document.getElementById('update-order-code').value = orderCode;
    document.getElementById('current-status').value = currentStatusLabel;
    document.getElementById('new-status').value = '';
    document.getElementById('update-notes').value = '';
    
    const modal = new bootstrap.Modal(document.getElementById('updateStatusModal'));
    modal.show();
}

async function updateOrderStatus() {
    const orderCode = document.getElementById('update-order-code').value;
    const newStatus = document.getElementById('new-status').value;
    const notes = document.getElementById('update-notes').value;

    if (!newStatus) {
        showAlert('Vui lòng chọn trạng thái mới', 'error');
        return;
    }

    try {
        const btn = document.getElementById('btn-confirm-update');
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Đang cập nhật...';

        await apiClient.request(`/admin/orders/${orderCode}/status`, {
            method: 'PUT',
            body: JSON.stringify({
                status: newStatus,
                notes: notes || null
            })
        });

        showAlert('Cập nhật trạng thái đơn hàng thành công', 'success');
        
        const modal = bootstrap.Modal.getInstance(document.getElementById('updateStatusModal'));
        modal.hide();

        await loadOrders();
    } catch (error) {
        console.error('Error updating order status:', error);
        showAlert('Lỗi cập nhật trạng thái: ' + error.message, 'error');
    } finally {
        const btn = document.getElementById('btn-confirm-update');
        btn.disabled = false;
        btn.innerHTML = '<i class="bi bi-check-circle"></i> Cập nhật';
    }
}

function formatDateTime(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleString('vi-VN');
}


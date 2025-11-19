// admin-transactions.js - Quản lý lịch sử giao dịch

let currentPage = 0;
let pageSize = 20;
let currentFilters = {};

document.addEventListener('DOMContentLoaded', async function() {
    // Check authentication and admin role
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

        // document.getElementById('admin-welcome').textContent = 
            // 'Xin chào, ' + (user.fullName || user.email);

        // Setup event listeners
        setupEventListeners();
        
        // Load initial data
        await loadTransactions();
        await loadSummary();
    } catch (err) {
        console.error('Error initializing:', err);
        showAlert('Lỗi tải dữ liệu: ' + (err.message || err), 'error');
    }
});

function setupEventListeners() {
    document.getElementById('btn-apply-filter').addEventListener('click', async () => {
        currentPage = 0;
        await loadTransactions();
        await loadSummary();
    });

    document.getElementById('btn-reset-filter').addEventListener('click', () => {
        document.getElementById('filter-type').value = '';
        document.getElementById('filter-status').value = '';
        document.getElementById('filter-start-date').value = '';
        document.getElementById('filter-end-date').value = '';
        currentPage = 0;
        currentFilters = {};
        loadTransactions();
        loadSummary();
    });

    document.getElementById('btn-export').addEventListener('click', () => {
        exportToExcel();
    });
}

async function loadTransactions() {
    try {
        // Build filters
        const type = document.getElementById('filter-type').value;
        const status = document.getElementById('filter-status').value;
        const startDate = document.getElementById('filter-start-date').value;
        const endDate = document.getElementById('filter-end-date').value;

        currentFilters = {};
        if (type) currentFilters.type = type;
        if (status) currentFilters.status = status;
        if (startDate) {
            // Convert date to ISO format with time
            currentFilters.startDate = new Date(startDate + 'T00:00:00').toISOString();
        }
        if (endDate) {
            currentFilters.endDate = new Date(endDate + 'T23:59:59').toISOString();
        }

        // Build query string
        const params = new URLSearchParams({
            page: currentPage,
            size: pageSize
        });
        
        Object.keys(currentFilters).forEach(key => {
            if (currentFilters[key]) {
                params.append(key, currentFilters[key]);
            }
        });

        const response = await apiClient.request(`/admin/transactions?${params}`, {
            method: 'GET'
        });

        renderTransactions(response.content || response.data || []);
        renderPagination(response.totalPages || 0, response.totalElements || 0);
    } catch (error) {
        console.error('Error loading transactions:', error);
        showAlert('Lỗi tải danh sách giao dịch: ' + error.message, 'error');
        document.getElementById('transactions-tbody').innerHTML = 
            '<tr><td colspan="9" class="text-center text-danger">Lỗi tải dữ liệu</td></tr>';
    }
}

function renderTransactions(transactions) {
    const tbody = document.getElementById('transactions-tbody');
    
    if (!transactions || transactions.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="text-center text-muted">Không có dữ liệu</td></tr>';
        return;
    }

    tbody.innerHTML = transactions.map(transaction => {
        const amountClass = transaction.amount >= 0 ? 'text-success' : 'text-danger';
        const amountSign = transaction.amount >= 0 ? '+' : '';
        const statusBadge = getStatusBadge(transaction.status);
        const typeBadge = getTypeBadge(transaction.type);
        
        return `
            <tr>
                <td><code>${transaction.transactionCode || transaction.id}</code></td>
                <td>${transaction.userEmail || transaction.userName || 'N/A'}</td>
                <td>${typeBadge}</td>
                <td class="${amountClass} fw-bold">${amountSign}${formatPrice(Math.abs(transaction.amount))}</td>
                <td>${getPaymentMethodLabel(transaction.paymentMethod)}</td>
                <td>${statusBadge}</td>
                <td>
                    ${transaction.orderCode 
                        ? `<a href="/admin/orders.html?orderCode=${transaction.orderCode}" class="text-primary">#${transaction.orderCode}</a>`
                        : '-'
                    }
                </td>
                <td>${formatDateTime(transaction.createdAt)}</td>
                <td>
                    <button class="btn btn-sm btn-outline-info" onclick="viewTransactionDetail(${transaction.id})">
                        <i class="bi bi-eye"></i>
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

function getStatusBadge(status) {
    const badges = {
        'SUCCESS': '<span class="badge bg-success">Thành công</span>',
        'PENDING': '<span class="badge bg-warning">Đang chờ</span>',
        'FAILED': '<span class="badge bg-danger">Thất bại</span>',
        'CANCELLED': '<span class="badge bg-secondary">Đã hủy</span>',
        'PROCESSING': '<span class="badge bg-info">Đang xử lý</span>'
    };
    return badges[status] || `<span class="badge bg-secondary">${status}</span>`;
}

function getTypeBadge(type) {
    const badges = {
        'PAYMENT': '<span class="badge bg-primary">Thanh toán</span>',
        'REFUND': '<span class="badge bg-success">Hoàn tiền</span>',
        'DEPOSIT': '<span class="badge bg-info">Nạp tiền</span>',
        'WITHDRAWAL': '<span class="badge bg-warning">Rút tiền</span>'
    };
    return badges[type] || `<span class="badge bg-secondary">${type}</span>`;
}

function getPaymentMethodLabel(method) {
    const labels = {
        'PAYOS': 'PayOS',
        'COD': 'Thanh toán khi nhận',
        'BANK_TRANSFER': 'Chuyển khoản'
    };
    return labels[method] || method || '-';
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
    await loadTransactions();
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

async function loadSummary() {
    try {
        const type = document.getElementById('filter-type').value;
        const status = document.getElementById('filter-status').value;
        const startDate = document.getElementById('filter-start-date').value;
        const endDate = document.getElementById('filter-end-date').value;

        const params = new URLSearchParams();
        if (type) params.append('type', type);
        if (status) params.append('status', status);
        if (startDate) {
            params.append('startDate', new Date(startDate + 'T00:00:00').toISOString());
        }
        if (endDate) {
            params.append('endDate', new Date(endDate + 'T23:59:59').toISOString());
        }

        const response = await apiClient.request(`/admin/transactions/summary?${params}`, {
            method: 'GET'
        });

        document.getElementById('stat-total-transactions').textContent = 
            formatNumber(response.totalTransactions || 0);
        document.getElementById('stat-total-revenue').textContent = 
            formatPrice(response.totalRevenue || 0);
        document.getElementById('stat-total-expense').textContent = 
            formatPrice(response.totalExpense || 0);
        document.getElementById('stat-pending').textContent = 
            formatNumber(response.pendingTransactions || 0);
    } catch (error) {
        console.error('Error loading summary:', error);
    }
}

function viewTransactionDetail(transactionId) {
    // TODO: Implement modal to show transaction details
    alert('Chi tiết giao dịch #' + transactionId);
}

function exportToExcel() {
    // TODO: Implement export to Excel
    alert('Tính năng xuất Excel đang được phát triển');
}

function formatDateTime(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleString('vi-VN');
}

function formatNumber(num) {
    return new Intl.NumberFormat('vi-VN').format(num);
}


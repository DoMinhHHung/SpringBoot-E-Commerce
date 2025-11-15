// order-detail.js - Chi tiết đơn hàng

document.addEventListener('DOMContentLoaded', async function() {
    if (!apiClient || !apiClient.isAuthenticated()) {
        window.location.href = '/login.html?redirect=' + encodeURIComponent(window.location.pathname);
        return;
    }

    const urlParams = new URLSearchParams(window.location.search);
    const orderCode = urlParams.get('orderCode');

    if (!orderCode) {
        showError('Không tìm thấy mã đơn hàng');
        return;
    }

    try {
        await loadOrderDetail(orderCode);
    } catch (error) {
        console.error('Error loading order detail:', error);
        showError('Lỗi tải thông tin đơn hàng: ' + error.message);
    }
});

async function loadOrderDetail(orderCode) {
    try {
        const response = await apiClient.request(`/orders/${orderCode}`, {
            method: 'GET'
        });

        if (!response) {
            showError('Không tìm thấy đơn hàng');
            return;
        }

        renderOrderDetail(response);
    } catch (error) {
        console.error('Error:', error);
        if (error.status === 404 || error.status === 403) {
            showError('Không tìm thấy đơn hàng hoặc bạn không có quyền xem đơn hàng này');
        } else {
            showError('Lỗi tải thông tin đơn hàng: ' + error.message);
        }
    }
}

function renderOrderDetail(order) {
    // Hide loading, show content
    document.getElementById('loading-state').classList.add('d-none');
    document.getElementById('order-content').classList.remove('d-none');

    // Order header
    document.getElementById('order-code-display').textContent = '#' + order.orderCode;
    document.getElementById('order-date').textContent = formatDateTime(order.createdAt);
    document.getElementById('order-status-badge').textContent = order.statusLabel;
    document.getElementById('order-status-badge').className = 'badge fs-6 ' + getStatusBadgeClass(order.status);

    // Progress bar
    renderProgressBar(order.progressStep);

    // Order items
    renderOrderItems(order.items);

    // Shipping address
    renderShippingAddress(order.shippingAddress);

    // Payment info
    renderPaymentInfo(order.paymentInfo);

    // Order summary
    document.getElementById('subtotal').textContent = formatPrice(order.subtotal);
    document.getElementById('discount').textContent = '-' + formatPrice(order.discountAmount);
    document.getElementById('shipping-fee').textContent = formatPrice(order.shippingFee);
    document.getElementById('total-amount').textContent = formatPrice(order.totalAmount);

    // Order notes
    if (order.notes && order.notes.trim()) {
        document.getElementById('order-notes-section').classList.remove('d-none');
        document.getElementById('order-notes').textContent = order.notes;
    }
}

function renderProgressBar(progressStep) {
    const steps = document.querySelectorAll('.step');
    const progressFill = document.getElementById('progress-fill');
    
    // Calculate progress percentage
    const percentage = (progressStep / 3) * 100;
    progressFill.style.width = percentage + '%';

    // Update step states
    steps.forEach((step, index) => {
        step.classList.remove('active', 'completed');
        if (index < progressStep) {
            step.classList.add('completed');
        } else if (index === progressStep) {
            step.classList.add('active');
        }
    });
}

function renderOrderItems(items) {
    const container = document.getElementById('order-items-list');
    if (!items || items.length === 0) {
        container.innerHTML = '<p class="text-muted">Không có sản phẩm</p>';
        return;
    }

    container.innerHTML = items.map(item => {
        const placeholder = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="80" height="80"%3E%3Crect fill="%23ddd" width="80" height="80"/%3E%3Ctext fill="%23999" font-family="sans-serif" font-size="10" dy="10.5" font-weight="bold" x="50%25" y="50%25" text-anchor="middle"%3ENo Image%3C/text%3E%3C/svg%3E';
        const imgSrc = item.productImage && item.productImage !== 'null' ? item.productImage : placeholder;
        
        return `
            <div class="d-flex mb-3 pb-3 border-bottom">
                <img src="${imgSrc}" alt="${item.productName}" 
                     class="me-3" style="width: 80px; height: 80px; object-fit: cover; border-radius: 8px;"
                     onerror="this.onerror=null;this.src='${placeholder}'">
                <div class="flex-grow-1">
                    <h6 class="mb-1">${item.productName}</h6>
                    <p class="text-muted small mb-1">Mã SP: ${item.productId}</p>
                    <div class="d-flex justify-content-between align-items-center">
                        <span class="text-muted">Số lượng: ${item.quantity}</span>
                        <strong class="text-danger">${formatPrice(item.totalPrice)}</strong>
                    </div>
                    ${item.discountAmount > 0 ? `<small class="text-success">Giảm: ${formatPrice(item.discountAmount)}</small>` : ''}
                </div>
            </div>
        `;
    }).join('');
}

function renderShippingAddress(address) {
    const container = document.getElementById('shipping-address');
    if (!address) {
        container.innerHTML = '<p class="text-muted">Chưa có thông tin địa chỉ</p>';
        return;
    }

    container.innerHTML = `
        <p class="mb-1"><strong>${address.receiverName}</strong></p>
        <p class="mb-1">${address.receiverPhone}</p>
        <p class="mb-1">${address.addressDetail}</p>
        <p class="mb-0">${address.ward}, ${address.district}, ${address.province}</p>
    `;
}

function renderPaymentInfo(paymentInfo) {
    const container = document.getElementById('payment-info');
    if (!paymentInfo) {
        container.innerHTML = '<p class="text-muted">Chưa có thông tin thanh toán</p>';
        return;
    }

    const methodLabels = {
        'PAYOS': 'PayOS',
        'COD': 'Thanh toán khi nhận hàng',
        'BANK_TRANSFER': 'Chuyển khoản ngân hàng'
    };

    const statusLabels = {
        'PENDING': 'Đang chờ',
        'PAID': 'Đã thanh toán',
        'FAILED': 'Thất bại',
        'CANCELLED': 'Đã hủy',
        'REFUNDED': 'Đã hoàn tiền'
    };

    container.innerHTML = `
        <p class="mb-1"><strong>Phương thức:</strong> ${methodLabels[paymentInfo.paymentMethod] || paymentInfo.paymentMethod}</p>
        <p class="mb-1"><strong>Trạng thái:</strong> ${statusLabels[paymentInfo.paymentStatus] || paymentInfo.paymentStatus}</p>
        ${paymentInfo.transactionId ? `<p class="mb-0"><strong>Mã giao dịch:</strong> ${paymentInfo.transactionId}</p>` : ''}
    `;
}

function getStatusBadgeClass(status) {
    const classes = {
        'PENDING': 'bg-warning',
        'CONFIRMED': 'bg-info',
        'PROCESSING': 'bg-primary',
        'SHIPPED': 'bg-primary',
        'DELIVERED': 'bg-success',
        'CANCELLED': 'bg-secondary',
        'REFUNDED': 'bg-danger'
    };
    return classes[status] || 'bg-secondary';
}

function showError(message) {
    document.getElementById('loading-state').classList.add('d-none');
    document.getElementById('order-content').classList.add('d-none');
    document.getElementById('error-state').classList.remove('d-none');
    const errorMsg = document.querySelector('#error-state p');
    if (errorMsg) {
        errorMsg.textContent = message;
    }
}

function formatDateTime(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleString('vi-VN');
}


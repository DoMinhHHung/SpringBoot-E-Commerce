// checkout.js - handles checkout page logic

document.addEventListener('DOMContentLoaded', async function() {
    // Check if apiClient is available
    if (typeof apiClient === 'undefined') {
        console.error('apiClient not found. Make sure /js/api.js is loaded before /js/checkout.js');
        return;
    }

    // Check authentication
    if (!apiClient.isAuthenticated()) {
        document.getElementById('login-banner').classList.remove('d-none');
        showAlert('Vui lòng đăng nhập để thanh toán', 'warning');
        return;
    }

    let currentUser = apiClient.getUser();
    let userId = null;
    let cart = null;

    // Get user info
    if (currentUser && currentUser.id) {
        userId = currentUser.id;
    } else {
        try {
            const profile = await apiClient.getProfile();
            if (profile && profile.id) {
                apiClient.setUser(profile);
                currentUser = profile;
                userId = profile.id;
            }
        } catch (err) {
            showAlert('Không thể tải thông tin người dùng', 'error');
            if (err.status === 401) {
                setTimeout(() => {
                    if (typeof showLoginModal === 'function') {
                        showLoginModal();
                    } else {
                        window.location.href = '/login.html';
                    }
                }, 1000);
            }
            return;
        }
    }

    // Load cart
    await loadCart(userId);

    // Populate user info if available
    if (currentUser) {
        populateUserInfo(currentUser);
    }

    // Setup event listeners
    setupEventListeners();
});

async function loadCart(userId) {
    try {
        cart = await apiClient.request(`/cart/${userId}`, { method: 'GET' });
        
        if (!cart || !cart.items || cart.items.length === 0) {
            showAlert('Giỏ hàng trống. Vui lòng thêm sản phẩm vào giỏ hàng trước.', 'warning');
            setTimeout(() => {
                window.location.href = '/cart.html';
            }, 2000);
            return;
        }

        renderCartItems(cart.items);
        updateOrderSummary(cart);
        document.getElementById('place-order-btn').disabled = false;
    } catch (error) {
        console.error('Error loading cart:', error);
        showAlert('Không thể tải giỏ hàng: ' + error.message, 'error');
        if (error.status === 401) {
            setTimeout(() => {
                if (typeof showLoginModal === 'function') {
                    showLoginModal();
                } else {
                    window.location.href = '/login.html';
                }
            }, 1000);
        }
    }
}

function renderCartItems(items) {
    const container = document.getElementById('cart-items-list');
    container.innerHTML = '';

    items.forEach(item => {
        const itemDiv = document.createElement('div');
        itemDiv.className = 'cart-item';
        const placeholder60 = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="60" height="60"%3E%3Crect fill="%23ddd" width="60" height="60"/%3E%3Ctext fill="%23999" font-family="sans-serif" font-size="8" dy="10.5" font-weight="bold" x="50%25" y="50%25" text-anchor="middle"%3ENo Image%3C/text%3E%3C/svg%3E';
        const imgSrc = item.productImage && item.productImage !== 'null' 
            ? item.productImage 
            : placeholder60;
        
        itemDiv.innerHTML = `
            <img src="${imgSrc}" alt="${item.productName}" 
                 onerror="this.onerror=null;this.src='${placeholder60}'">
            <div class="flex-grow-1">
                <div class="d-flex justify-content-between align-items-start">
                    <div>
                        <h6 class="mb-1">${item.productName}</h6>
                        <small class="text-muted">Mã: ${item.productId}</small>
                    </div>
                    <button class="btn btn-sm btn-link text-danger" onclick="removeCartItem(${item.productId})">
                        <i class="bi bi-trash"></i>
                    </button>
                </div>
                <div class="d-flex justify-content-between align-items-center mt-2">
                    <div class="d-flex align-items-center">
                        <button class="btn btn-sm btn-outline-secondary" onclick="decreaseQuantity(${item.productId})">-</button>
                        <span class="mx-2">${item.quantity}</span>
                        <button class="btn btn-sm btn-outline-secondary" onclick="increaseQuantity(${item.productId})">+</button>
                    </div>
                    <div class="fw-bold">${formatPrice(item.totalPrice)}</div>
                </div>
            </div>
        `;
        container.appendChild(itemDiv);
    });
}

function updateOrderSummary(cart) {
    const subtotal = cart.totalPrice || 0;
    const shippingFee = 0; // TODO: Calculate shipping fee
    const total = subtotal + shippingFee;

    document.getElementById('subtotal').textContent = formatPrice(subtotal);
    document.getElementById('shipping-fee').textContent = shippingFee > 0 ? formatPrice(shippingFee) : '-';
    document.getElementById('total-amount').textContent = formatPrice(total);
}

function populateUserInfo(user) {
    if (user.fullName) {
        document.getElementById('receiverName').value = user.fullName;
    }
    if (user.phone) {
        document.getElementById('receiverPhone').value = user.phone;
    }
    if (user.email) {
        document.getElementById('receiverEmail').value = user.email;
    }
}

function setupEventListeners() {
    // Payment method selection
    document.querySelectorAll('.payment-method-option').forEach(option => {
        option.addEventListener('click', function() {
            document.querySelectorAll('.payment-method-option').forEach(opt => {
                opt.classList.remove('selected');
            });
            this.classList.add('selected');
            const radio = this.querySelector('input[type="radio"]');
            if (radio) radio.checked = true;
        });
    });

    // Place order button
    document.getElementById('place-order-btn').addEventListener('click', handlePlaceOrder);

    // Apply promo code
    document.getElementById('apply-promo').addEventListener('click', function() {
        const code = document.getElementById('promoCode').value;
        if (code) {
            showAlert('Mã khuyến mãi đang được xử lý...', 'info');
            // TODO: Implement promo code validation
        }
    });
}

async function handlePlaceOrder() {
    const btn = document.getElementById('place-order-btn');
    btn.disabled = true;
    const originalText = btn.textContent;
    btn.textContent = 'Đang xử lý...';

    try {
        // Validate form
        if (!validateShippingForm()) {
            btn.disabled = false;
            btn.textContent = originalText;
            return;
        }

        // Get selected payment method
        const paymentMethod = document.querySelector('input[name="paymentMethod"]:checked').value;

        // Build payment request
        const paymentRequest = {
            items: cart.items.map(item => ({
                productId: item.productId,
                quantity: item.quantity,
                unitPrice: item.unitPrice,
                discountAmount: 0 // TODO: Calculate discount if promo applied
            })),
            paymentMethod: paymentMethod,
            notes: document.getElementById('orderNotes').value || null,
            shippingAddressId: null // TODO: Save address and use ID
        };

        // Create payment
        const response = await apiClient.createPayment(paymentRequest);

        // Handle payment response based on method
        if (paymentMethod === 'PAYOS') {
            // Show payment modal with QR code, pass originalText để có thể reset button
            showPaymentModal(response, originalText);
        } else if (paymentMethod === 'BANK_TRANSFER' || paymentMethod === 'COD') {
            // For now, just show success message
            showAlert('Đơn hàng đã được tạo thành công! Mã đơn hàng: #' + response.orderCode, 'success');
            // Clear cart
            try {
                await apiClient.request(`/cart/clear/${apiClient.getUser().id}`, { method: 'DELETE' });
            } catch (e) {
                console.error('Error clearing cart:', e);
            }
            // Redirect to success page
            setTimeout(() => {
                window.location.href = `/payment-success.html?orderCode=${response.orderCode}`;
            }, 2000);
        }

    } catch (error) {
        console.error('Error placing order:', error);
        showAlert('Lỗi đặt hàng: ' + error.message, 'error');
        btn.disabled = false;
        btn.textContent = originalText;
    }
}

function validateShippingForm() {
    const requiredFields = ['receiverName', 'receiverPhone', 'receiverEmail', 'addressDetail', 'province', 'district', 'ward'];
    let isValid = true;

    requiredFields.forEach(fieldId => {
        const field = document.getElementById(fieldId);
        if (!field.value.trim()) {
            field.classList.add('is-invalid');
            isValid = false;
        } else {
            field.classList.remove('is-invalid');
        }
    });

    // Validate email format
    const email = document.getElementById('receiverEmail').value;
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (email && !emailRegex.test(email)) {
        document.getElementById('receiverEmail').classList.add('is-invalid');
        isValid = false;
    }

    // Validate phone format (Vietnamese phone)
    const phone = document.getElementById('receiverPhone').value;
    const phoneRegex = /^(0|\+84)[3-9]\d{8}$/;
    if (phone && !phoneRegex.test(phone.replace(/\s/g, ''))) {
        document.getElementById('receiverPhone').classList.add('is-invalid');
        isValid = false;
    }

    if (!isValid) {
        showAlert('Vui lòng điền đầy đủ và đúng định dạng thông tin bắt buộc', 'error');
    }

    return isValid;
}

function showPaymentModal(paymentResponse, originalButtonText) {
    // Remove existing modal if any
    const existing = document.getElementById('paymentModal');
    if (existing) existing.remove();
    
    // Create modal HTML
    const modalHTML = `
        <div class="modal fade" id="paymentModal" tabindex="-1" aria-labelledby="paymentModalLabel" aria-hidden="true" data-bs-backdrop="static">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="paymentModalLabel">
                            <i class="bi bi-credit-card"></i> Thanh toán đơn hàng
                        </h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body text-center" id="payment-modal-body">
                        <div class="mb-3">
                            <p class="mb-1">Mã đơn hàng: <strong>#${paymentResponse.orderCode}</strong></p>
                            <p class="mb-1">Tổng tiền: <strong class="text-danger fs-4">${formatPrice(paymentResponse.totalAmount)}</strong></p>
                        </div>
                        <hr>
                        <p class="mb-3"><strong>Quét mã QR để thanh toán:</strong></p>
                        <div class="mb-3 d-flex justify-content-center">
                            <div id="qrcode-${paymentResponse.orderCode}" class="border rounded p-2 bg-white"></div>
                        </div>
                        <div class="mb-3">
                            <p class="text-danger fw-bold mb-0" id="countdown-timer">
                                <i class="bi bi-clock"></i> Thời gian còn lại: <span id="countdown-text">15:00</span>
                            </p>
                        </div>
                        <p class="text-muted small mb-3">Hoặc click vào nút bên dưới để thanh toán</p>
                        <a href="${paymentResponse.checkoutUrl}" 
                           target="_blank" 
                           class="btn btn-primary btn-lg w-100 mb-3">
                            <i class="bi bi-box-arrow-up-right"></i> Thanh toán qua PayOS
                        </a>
                        <hr>
                        <div class="alert alert-info mb-0" id="payment-status-alert">
                            <i class="bi bi-info-circle"></i> 
                            <small>Đang chờ thanh toán... Hệ thống sẽ tự động cập nhật khi bạn thanh toán thành công.</small>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    // Add modal to body
    document.body.insertAdjacentHTML('beforeend', modalHTML);
    
    // Show modal
    const modal = new bootstrap.Modal(document.getElementById('paymentModal'));
    modal.show();

    // Countdown timer (15 minutes = 900 seconds)
    let countdownSeconds = 15 * 60; // 15 minutes
    const countdownElement = document.getElementById('countdown-text');
    
    const countdownInterval = setInterval(() => {
        countdownSeconds--;
        if (countdownSeconds <= 0) {
            clearInterval(countdownInterval);
            if (countdownElement) {
                countdownElement.textContent = '00:00';
                const countdownTimer = document.getElementById('countdown-timer');
                if (countdownTimer) {
                    countdownTimer.innerHTML = '<span class="text-danger">Hết thời gian thanh toán!</span>';
                }
            }
        } else {
            const minutes = Math.floor(countdownSeconds / 60);
            const seconds = countdownSeconds % 60;
            if (countdownElement) {
                countdownElement.textContent = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
            }
        }
    }, 1000);

    // Generate QR code using api.qrserver.com with qrCode data from PayOS
    setTimeout(() => {
        const qrCodeDiv = document.getElementById(`qrcode-${paymentResponse.orderCode}`);
        if (qrCodeDiv) {
            // Lấy qrCode data từ response (raw data string từ PayOS)
            const qrData = paymentResponse.qrCode;
            if (qrData) {
                // Encode qrData để dùng trong URL
                const encodedQrData = encodeURIComponent(qrData);
                // Tạo URL từ api.qrserver.com
                const qrImageUrl = `https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${encodedQrData}`;
                
                // Tạo img tag
                qrCodeDiv.innerHTML = `<img src="${qrImageUrl}" alt="QR Code" class="img-fluid" style="max-width: 300px; height: auto;">`;
            } else {
                qrCodeDiv.innerHTML = '<p class="text-danger">Không có dữ liệu QR code</p>';
            }
        }
    }, 100);

    // Clear cart after showing modal
    setTimeout(async () => {
        try {
            await apiClient.request(`/cart/clear/${apiClient.getUser().id}`, { method: 'DELETE' });
        } catch (e) {
            console.error('Error clearing cart:', e);
        }
    }, 1000);

    // Start polling for payment status
    let pollCount = 0;
    const maxPolls = 120; // Poll for 10 minutes (120 * 5 seconds)
    
    const pollInterval = setInterval(async () => {
        pollCount++;
        
        try {
            const statusResponse = await apiClient.getPaymentStatus(paymentResponse.orderCode);
            
            if (statusResponse.status === 'PAID') {
                clearInterval(pollInterval);
                clearInterval(countdownInterval);
                
                // Set flag to allow modal close
                const modalElement = document.getElementById('paymentModal');
                if (modalElement) {
                    modalElement.setAttribute('data-allow-close', 'true');
                }
                
                // Update modal body to show success message
                const modalBody = document.getElementById('payment-modal-body');
                modalBody.innerHTML = `
                    <div class="text-center py-4">
                        <div class="mb-4">
                            <i class="bi bi-check-circle-fill text-success" style="font-size: 4rem;"></i>
                        </div>
                        <h4 class="text-success mb-3">Thanh toán thành công!</h4>
                        <div class="mb-3">
                            <p class="mb-1">Mã đơn hàng: <strong>#${paymentResponse.orderCode}</strong></p>
                            <p class="mb-1">Tổng tiền: <strong class="text-danger fs-4">${formatPrice(paymentResponse.totalAmount)}</strong></p>
                        </div>
                        <p class="text-muted mb-4">Cảm ơn bạn đã mua hàng. Đơn hàng của bạn đang được xử lý.</p>
                        <p class="text-info small mb-3">Tự động chuyển về trang chủ sau <span id="redirect-countdown">3</span> giây...</p>
                        <button class="btn btn-success btn-lg" onclick="closePaymentModal()">
                            <i class="bi bi-check-circle"></i> OK
                        </button>
                    </div>
                `;
                
                // Hide close button in header
                const closeBtn = document.querySelector('#paymentModal .btn-close');
                if (closeBtn) closeBtn.style.display = 'none';
                
                // Auto close modal and redirect after 3 seconds
                let redirectSeconds = 3;
                const redirectCountdownElement = document.getElementById('redirect-countdown');
                const redirectCountdown = setInterval(() => {
                    redirectSeconds--;
                    if (redirectCountdownElement) {
                        redirectCountdownElement.textContent = redirectSeconds;
                    }
                    if (redirectSeconds <= 0) {
                        clearInterval(redirectCountdown);
                        closePaymentModal();
                    }
                }, 1000);
                
            } else if (status === 'CANCELLED' || status === 'FAILED') {
                console.log('❌ Payment failed or cancelled');
                clearInterval(pollInterval);
                const statusAlert = document.getElementById('payment-status-alert');
                if (statusAlert) {
                    statusAlert.className = 'alert alert-danger mb-0';
                    statusAlert.innerHTML = `
                        <i class="bi bi-x-circle"></i> 
                        <small>Thanh toán thất bại hoặc đã hủy. Vui lòng thử lại.</small>
                    `;
                }
            } else {
                // Log để debug nếu status không match
                console.log(`⏳ Payment status is: ${status || 'UNKNOWN'}, waiting for PAID... (Poll #${pollCount}/${maxPolls})`);
            }
        } catch (error) {
            console.error('❌ Error checking payment status:', error);
            console.error('Error details:', error.message);
            if (error.stack) {
                console.error('Stack trace:', error.stack);
            }
        }

        // Stop polling after max attempts
        if (pollCount >= maxPolls) {
            console.log('⏱️ Max polling attempts reached, stopping...');
            clearInterval(pollInterval);
        }
    }, 5000); // Check every 5 seconds

    // Intercept modal close events
    const modalElement = document.getElementById('paymentModal');

    modalElement.addEventListener('hide.bs.modal', function(event) {
        const allowClose = modalElement.getAttribute('data-allow-close') === 'true';
        if (!allowClose) {
            event.preventDefault();
            event.stopPropagation();
            
            const existingConfirmation = document.getElementById('closeConfirmationModal');
            if (!existingConfirmation) {
                showCloseConfirmation(modal, countdownInterval, pollInterval, originalButtonText);
            }
        } else {
            // Reset flag
            modalElement.removeAttribute('data-allow-close');
        }
    });

    // Clean up when modal is actually closed
    modalElement.addEventListener('hidden.bs.modal', () => {
        if (countdownInterval) clearInterval(countdownInterval);
        if (pollInterval) clearInterval(pollInterval);
        
        // Reset button to original state
        const btn = document.getElementById('place-order-btn');
        if (btn && originalButtonText) {
            btn.disabled = false;
            btn.textContent = originalButtonText;
        }
    });
}

// Function to show close confirmation dialog
function showCloseConfirmation(modal, countdownInterval, pollInterval, originalButtonText) {
    // Check if confirmation modal already exists and is showing
    const existing = document.getElementById('closeConfirmationModal');
    if (existing) {
        // Modal already showing, don't show again
        return;
    }

    const confirmationHTML = `
        <div class="modal fade" id="closeConfirmationModal" tabindex="-1" aria-hidden="true" data-bs-backdrop="static">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">
                            <i class="bi bi-exclamation-triangle text-warning"></i> Xác nhận
                        </h5>
                    </div>
                    <div class="modal-body text-center">
                        <p class="mb-3">Bạn có chắc chắn muốn đóng cửa sổ thanh toán?</p>
                        <p class="text-muted small mb-4">Nếu đóng, bạn có thể thanh toán lại sau.</p>
                        <div class="d-flex gap-2 justify-content-center">
                            <button class="btn btn-outline-secondary" onclick="stayOnPayment()">
                                <i class="bi bi-x-circle"></i> Ở lại
                            </button>
                            <button class="btn btn-danger" onclick="confirmClosePayment()">
                                <i class="bi bi-check-circle"></i> Thoát
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;

    document.body.insertAdjacentHTML('beforeend', confirmationHTML);
    const confirmationModal = new bootstrap.Modal(document.getElementById('closeConfirmationModal'));
    confirmationModal.show();

    // Store intervals and modal for cleanup
    window.paymentModalData = {
        countdown: countdownInterval,
        polling: pollInterval,
        originalButtonText: originalButtonText,
        modal: modal
    };
}

// Function to stay on payment
function stayOnPayment() {
    const confirmationModal = bootstrap.Modal.getInstance(document.getElementById('closeConfirmationModal'));
    if (confirmationModal) {
        confirmationModal.hide();
    }
    // Remove confirmation modal from DOM
    setTimeout(() => {
        const confirmationElement = document.getElementById('closeConfirmationModal');
        if (confirmationElement) {
            confirmationElement.remove();
        }
    }, 300);
}

// Function to confirm close payment
function confirmClosePayment() {
    const confirmationModal = bootstrap.Modal.getInstance(document.getElementById('closeConfirmationModal'));
    if (confirmationModal) {
        confirmationModal.hide();
    }

    // Clean up intervals
    if (window.paymentModalData) {
        if (window.paymentModalData.countdown) {
            clearInterval(window.paymentModalData.countdown);
        }
        if (window.paymentModalData.polling) {
            clearInterval(window.paymentModalData.polling);
        }
    }

    // Set flag to allow modal close
    const paymentModalElement = document.getElementById('paymentModal');
    if (paymentModalElement) {
        paymentModalElement.setAttribute('data-allow-close', 'true');
    }

    // Close payment modal
    if (window.paymentModalData && window.paymentModalData.modal) {
        window.paymentModalData.modal.hide();
    }

    // Reset button
    const btn = document.getElementById('place-order-btn');
    if (btn && window.paymentModalData && window.paymentModalData.originalButtonText) {
        btn.disabled = false;
        btn.textContent = window.paymentModalData.originalButtonText;
    }

    // Clean up
    delete window.paymentModalData;
    
    // Remove confirmation modal from DOM
    setTimeout(() => {
        const confirmationElement = document.getElementById('closeConfirmationModal');
        if (confirmationElement) {
            confirmationElement.remove();
        }
    }, 300);
}

// Function to close payment modal and redirect to home
function closePaymentModal() {
    const modal = bootstrap.Modal.getInstance(document.getElementById('paymentModal'));
    if (modal) {
        modal.hide();
    }
    // Redirect to home page after closing modal
    setTimeout(() => {
        window.location.href = `/index.html`;
    }, 300);
}

// Cart item quantity functions
async function increaseQuantity(productId) {
    const item = cart.items.find(i => i.productId === productId);
    if (item) {
        await updateCartQuantity(productId, item.quantity + 1);
    }
}

async function decreaseQuantity(productId) {
    const item = cart.items.find(i => i.productId === productId);
    if (item && item.quantity > 1) {
        await updateCartQuantity(productId, item.quantity - 1);
    }
}

async function updateCartQuantity(productId, quantity) {
    try {
        const userId = apiClient.getUser().id;
        await apiClient.request('/cart/update', {
            method: 'PUT',
            body: JSON.stringify({ userId, productId, quantity })
        });
        await loadCart(userId);
        showAlert('Cập nhật số lượng thành công', 'success');
    } catch (error) {
        showAlert('Cập nhật số lượng thất bại: ' + error.message, 'error');
    }
}

async function removeCartItem(productId) {
    if (!confirm('Bạn có chắc muốn xóa sản phẩm này?')) return;
    try {
        const userId = apiClient.getUser().id;
        await apiClient.request(`/cart/remove?userId=${userId}&productId=${productId}`, {
            method: 'DELETE'
        });
        await loadCart(userId);
        showAlert('Xóa sản phẩm thành công', 'success');
    } catch (error) {
        showAlert('Xóa sản phẩm thất bại: ' + error.message, 'error');
    }
}


// cart.js - handles loading and rendering the shopping cart using apiClient

document.addEventListener('DOMContentLoaded', async function () {
    // Wait until apiClient is available (api.js included before this file in cart.html)
    if (typeof apiClient === 'undefined') {
        console.error('apiClient not found. Make sure /js/api.js is loaded before /js/cart.js');
        return;
    }

    const cartItemsEl = document.getElementById('cart-items');
    const totalQuantityEl = document.getElementById('total-quantity');
    const totalPriceEl = document.getElementById('total-price');
    const cartContainer = document.getElementById('cart-container');

    // Determine current user: prefer cached local user, otherwise attempt to fetch profile from server
    let currentUser = apiClient.getUser();
    let userId = null;

    if (currentUser && currentUser.id) {
        userId = currentUser.id;
    } else {
        try {
            // Try to get profile from server (will work if server authenticates via cookie/session)
            const profile = await apiClient.getProfile();
            if (profile && profile.id) {
                apiClient.setUser(profile);
                currentUser = profile;
                userId = profile.id;
            }
        } catch (err) {
            // Not authenticated on server side either
            showAlert('Vui lòng đăng nhập để xem giỏ hàng', 'error');
            setTimeout(() => window.location.href = '/login.html', 800);
            return;
        }
    }

    async function fetchCart() {
        try {
            const cart = await apiClient.request(`/cart/${userId}`, { method: 'GET' });
            renderCart(cart);
        } catch (error) {
            console.error('Error fetching cart:', error);
            if (cartItemsEl) {
                cartItemsEl.innerHTML = `<tr><td colspan="5" class="text-center text-danger">${error.message}</td></tr>`;
            }
        }
    }

    function renderCart(cart) {
        if (!cartItemsEl) return;
        cartItemsEl.innerHTML = '';

        if (!cart || !Array.isArray(cart.items) || cart.items.length === 0) {
            cartItemsEl.innerHTML = `<tr><td colspan="5" class="text-center">Giỏ hàng trống</td></tr>`;
            totalQuantityEl.textContent = 0;
            totalPriceEl.textContent = formatPrice(0);
            updateCartBadge(0);
            return;
        }

        let totalQuantity = 0;

        cart.items.forEach(item => {
            totalQuantity += item.quantity;

            const tr = document.createElement('tr');

            tr.innerHTML = `
                <td class="d-flex align-items-center">
                    <img src="${item.productImage || '/images/placeholder-readme.txt'}" alt="" style="width:60px;height:60px;object-fit:cover;margin-right:12px;">
                    <div>
                        <div class="fw-bold">${item.productName}</div>
                        <div class="text-muted small">Mã: ${item.productId}</div>
                    </div>
                </td>
                <td>${formatPrice(item.unitPrice)}</td>
                <td>
                    <input type="number" min="1" value="${item.quantity}" class="form-control form-control-sm" style="width:80px;" data-product-id="${item.productId}">
                </td>
                <td class="item-total">${formatPrice(item.totalPrice)}</td>
                <td>
                    <button class="btn btn-danger btn-sm btn-remove" data-product-id="${item.productId}"><i class="bi bi-trash"></i></button>
                </td>
            `;

            cartItemsEl.appendChild(tr);
        });

        totalQuantityEl.textContent = totalQuantity;
        totalPriceEl.textContent = formatPrice(cart.totalPrice || 0);
        updateCartBadge(totalQuantity);

        // Attach event listeners
        cartItemsEl.querySelectorAll('input[type="number"]').forEach(input => {
            input.addEventListener('change', async (e) => {
                const newVal = parseInt(e.target.value, 10);
                const productId = e.target.getAttribute('data-product-id');
                if (isNaN(newVal) || newVal < 1) {
                    showAlert('Số lượng phải là số nguyên lớn hơn 0', 'error');
                    fetchCart();
                    return;
                }
                await updateQuantity(userId, productId, newVal);
            });
        });

        cartItemsEl.querySelectorAll('.btn-remove').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                const productId = e.currentTarget.getAttribute('data-product-id');
                await removeItem(userId, productId);
            });
        });

        // Ensure checkout controls exist
        renderCheckoutControls();
    }

    function renderCheckoutControls() {
        if (!cartContainer) return;
        let controls = document.getElementById('cart-controls');
        if (!controls) {
            controls = document.createElement('div');
            controls.id = 'cart-controls';
            controls.className = 'd-flex justify-content-end gap-2 mt-3';
            controls.innerHTML = `
                <button id="clear-cart" class="btn btn-outline-danger">Xóa toàn bộ</button>
                <button id="checkout" class="btn btn-primary">Thanh toán</button>
            `;
            cartContainer.insertAdjacentElement('afterend', controls);

            document.getElementById('clear-cart').addEventListener('click', async () => {
                if (!confirm('Bạn có chắc muốn xóa toàn bộ giỏ hàng?')) return;
                await clearCart(userId);
            });

            document.getElementById('checkout').addEventListener('click', () => {
                // In this project there is no checkout page yet; redirect or show message
                showAlert('Chức năng thanh toán chưa được triển khai trong bản demo này.', 'info');
            });
        }
    }

    async function updateQuantity(userId, productId, quantity) {
        try {
            await apiClient.request('/cart/update', {
                method: 'PUT',
                body: JSON.stringify({ userId: Number(userId), productId: Number(productId), quantity: Number(quantity) })
            });
            await fetchCart();
            showAlert('Cập nhật số lượng thành công', 'success');
        } catch (error) {
            console.error('updateQuantity error', error);
            showAlert('Cập nhật số lượng thất bại: ' + error.message, 'error');
        }
    }

    async function removeItem(userId, productId) {
        if (!confirm('Bạn có chắc muốn xóa sản phẩm này?')) return;
        try {
            await apiClient.request(`/cart/remove?userId=${userId}&productId=${productId}`, { method: 'DELETE' });
            await fetchCart();
            showAlert('Xóa sản phẩm thành công', 'success');
        } catch (error) {
            console.error('removeItem error', error);
            showAlert('Xóa sản phẩm thất bại: ' + error.message, 'error');
        }
    }

    async function clearCart(userId) {
        try {
            await apiClient.request(`/cart/clear/${userId}`, { method: 'DELETE' });
            await fetchCart();
            showAlert('Giỏ hàng đã được xóa', 'success');
        } catch (error) {
            console.error('clearCart error', error);
            showAlert('Xóa giỏ hàng thất bại: ' + error.message, 'error');
        }
    }

    function updateCartBadge(qty) {
        const badge = document.getElementById('cart-badge');
        if (badge) {
            badge.textContent = qty || 0;
        }
    }

    // Initial load
    fetchCart();
});

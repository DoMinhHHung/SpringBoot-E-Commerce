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
            // Try to get profile from server but avoid automatic redirect on 401
            const profile = await apiClient.request('/users/profile', { method: 'GET', noAuthRedirect: true });
            if (profile && profile.id) {
                apiClient.setUser(profile);
                currentUser = profile;
                userId = profile.id;
            }
        } catch (err) {
            // Not authenticated on server side either
            showAlert('Vui lòng đăng nhập để xem giỏ hàng', 'error');
            // Open login modal instead of redirecting away from the cart page
            if (typeof showLoginModal === 'function') {
                setTimeout(() => showLoginModal(), 200);
            } else {
                // fallback
                setTimeout(() => window.location.href = '/login.html', 800);
            }
            return;
        }
    }

    async function fetchCart() {
        try {
            let cart = await apiClient.request(`/cart/${userId}`, { method: 'GET', noAuthRedirect: true });
            // If server returned empty body (null), retry once quickly
            if (cart === null) {
                console.warn('fetchCart: empty response, retrying once');
                await new Promise(r => setTimeout(r, 200));
                cart = await apiClient.request(`/cart/${userId}`, { method: 'GET', noAuthRedirect: true });
            }
            if (cart === null) {
                console.warn('fetchCart: server returned no content for cart, rendering empty cart');
                cart = { items: [] };
            }
            renderCart(cart);
        } catch (error) {
            console.error('Error fetching cart:', error);
            // If unauthorized, open login modal instead of redirect
            if (error && error.status === 401) {
                if (typeof showLoginModal === 'function') {
                    showLoginModal();
                    return;
                }
            }
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

        // Aggregate items by productId to merge duplicates (sum quantities and totals)
        const map = new Map();
        cart.items.forEach(item => {
            const pid = item.productId;
            const unit = item.unitPrice || 0;
            const qty = Number(item.quantity) || 0;
            if (!map.has(pid)) {
                // clone to avoid mutating original
                map.set(pid, {
                    productId: pid,
                    productName: item.productName,
                    productImage: item.productImage,
                    unitPrice: unit,
                    quantity: qty,
                    totalPrice: (typeof item.totalPrice !== 'undefined' ? item.totalPrice : unit * qty)
                });
            } else {
                const existing = map.get(pid);
                existing.quantity += qty;
                // recalc totalPrice from unitPrice to avoid rounding issues
                existing.totalPrice = existing.unitPrice * existing.quantity;
            }
        });

        const aggregated = Array.from(map.values());

        let totalQuantity = 0;
        let totalPrice = 0;

        aggregated.forEach(item => {
            totalQuantity += item.quantity;
            totalPrice += Number(item.totalPrice || (item.unitPrice * item.quantity) || 0);

            const tr = document.createElement('tr');
            const placeholder60 = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="60" height="60"%3E%3Crect fill="%23ddd" width="60" height="60"/%3E%3Ctext fill="%23999" font-family="sans-serif" font-size="8" dy="10.5" font-weight="bold" x="50%25" y="50%25" text-anchor="middle"%3ENo Image%3C/text%3E%3C/svg%3E';
            const imgSrc = item.productImage && item.productImage !== 'null' ? item.productImage : placeholder60;
            tr.innerHTML = `
                <td class="d-flex align-items-center">
                    <img src="${imgSrc}" alt="" onerror="this.onerror=null;this.src='${placeholder60}'" style="width:60px;height:60px;object-fit:cover;margin-right:12px;">
                    <div>
                        <div class="fw-bold"><a href="/product-detail.html?id=${item.productId}" class="text-decoration-none text-dark">${item.productName}</a></div>
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
        totalPriceEl.textContent = formatPrice(totalPrice || 0);
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
                // Redirect to checkout page
                window.location.href = '/checkout.html';
            });
        }
    }

    async function updateQuantity(userId, productId, quantity) {
        try {
            const resp = await apiClient.request('/cart/update', {
                method: 'PUT',
                body: JSON.stringify({ userId: Number(userId), productId: Number(productId), quantity: Number(quantity) }),
                noAuthRedirect: true
            });
            console.log('updateQuantity response', resp);
            // Fetch latest cart (resp might be null or object)
            await fetchCart();
            showAlert('Cập nhật số lượng thành công', 'success');
        } catch (error) {
            console.error('updateQuantity error', error);
            if (error && error.status === 401) {
                if (typeof showLoginModal === 'function') { showLoginModal(); return; }
            }
            showAlert('Cập nhật số lượng thất bại: ' + error.message, 'error');
        }
    }

    async function removeItem(userId, productId) {
        if (!confirm('Bạn có chắc muốn xóa sản phẩm này?')) return;
        try {
            const resp = await apiClient.request(`/cart/remove?userId=${userId}&productId=${productId}`, { method: 'DELETE', noAuthRedirect: true });
            console.log('removeItem response', resp);
            await fetchCart();
            showAlert('Xóa sản phẩm thành công', 'success');
        } catch (error) {
            console.error('removeItem error', error);
            if (error && error.status === 401) {
                if (typeof showLoginModal === 'function') { showLoginModal(); return; }
            }
            showAlert('Xóa sản phẩm thất bại: ' + error.message, 'error');
        }
    }

    async function clearCart(userId) {
        try {
            await apiClient.request(`/cart/clear/${userId}`, { method: 'DELETE', noAuthRedirect: true });
            await fetchCart();
            showAlert('Giỏ hàng đã được xóa', 'success');
        } catch (error) {
            console.error('clearCart error', error);
            if (error && error.status === 401) {
                if (typeof showLoginModal === 'function') { showLoginModal(); return; }
            }
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

// Search Results Page Logic
let currentQuery = '';
let currentPage = 0;
let currentSort = 'default';
let totalProducts = 0;
let totalPages = 0;
const pageSize = 20;

// Initialize page
document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    currentQuery = urlParams.get('q') || '';
    currentPage = parseInt(urlParams.get('page') || '0');
    currentSort = urlParams.get('sort') || 'default';
    
    if (currentQuery) {
        document.getElementById('search-query').textContent = currentQuery;
        loadSearchResults();
        setupSortButtons();
    } else {
        showError('Vui lòng nhập từ khóa tìm kiếm');
    }
});

function setupSortButtons() {
    const sortButtons = document.querySelectorAll('.sort-btn');
    sortButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            const sort = this.getAttribute('data-sort');
            changeSort(sort);
        });
    });
    
    // Set active button
    updateActiveSortButton();
}

function updateActiveSortButton() {
    const sortButtons = document.querySelectorAll('.sort-btn');
    sortButtons.forEach(btn => {
        btn.classList.remove('btn-primary', 'active');
        btn.classList.add('btn-outline-secondary');
        
        if (btn.getAttribute('data-sort') === currentSort) {
            btn.classList.remove('btn-outline-secondary');
            btn.classList.add('btn-primary', 'active');
        }
    });
}

function changeSort(sort) {
    currentSort = sort;
    currentPage = 0;
    updateURL();
    loadSearchResults();
    updateActiveSortButton();
}

async function loadSearchResults() {
    const loadingEl = document.getElementById('search-loading');
    const gridEl = document.getElementById('search-results-grid');
    const emptyState = document.getElementById('empty-state');
    const summaryEl = document.getElementById('search-summary');
    
    if (loadingEl) loadingEl.style.display = 'block';
    if (gridEl) gridEl.innerHTML = '';
    if (emptyState) emptyState.classList.add('d-none');
    
    try {
        const response = await apiClient.searchProducts(currentQuery, currentPage, pageSize, currentSort);
        
        totalProducts = response.totalElements || 0;
        totalPages = response.totalPages || 0;
        
        // Update summary
        if (summaryEl) {
            summaryEl.innerHTML = `Có <span id="total-count">${totalProducts}</span> sản phẩm cho tìm kiếm`;
        }
        
        const products = response.content || [];
        
        if (products.length === 0) {
            if (emptyState) emptyState.classList.remove('d-none');
            renderPagination(0, 0);
        } else {
            displayProducts(products);
            renderPagination(totalPages, totalProducts);
        }
    } catch (error) {
        console.error('Error loading search results:', error);
        showAlert('Không thể tải kết quả tìm kiếm: ' + error.message, 'error');
        if (emptyState) emptyState.classList.remove('d-none');
    } finally {
        if (loadingEl) loadingEl.style.display = 'none';
    }
}

function displayProducts(products) {
    const gridEl = document.getElementById('search-results-grid');
    if (!gridEl) return;
    
    gridEl.innerHTML = products.map(product => {
        const discountPercent = product.priceAfterDiscount && product.priceAfterDiscount < product.price
            ? Math.round((1 - product.priceAfterDiscount / product.price) * 100)
            : 0;

        return `
            <div class="product-type-card-item">
                <div class="product-type-card">
                    ${discountPercent > 0 ? 
                        `<span class="product-type-discount-badge">-${discountPercent}%</span>` : ''}
                    <a href="/product-detail.html?id=${product.id}" class="product-type-image-wrapper">
                        <img src="${product.mainImage}" 
                             alt="${product.name}" 
                             class="product-type-product-image">
                    </a>
                    <div class="product-type-card-body">
                        <h5 class="product-type-product-title">${product.name}</h5>
                        <div class="product-type-product-price">
                            ${product.priceAfterDiscount && product.priceAfterDiscount < product.price ? 
                                `<span class="product-type-price-old">${formatPrice(product.price)}</span>
                                 <span class="product-type-price-new">${formatPrice(product.priceAfterDiscount)}</span>` :
                                `<span class="product-type-price-new">${formatPrice(product.price)}</span>`
                            }
                        </div>
                        <button class="product-type-cart-btn" onclick="addToCart(${product.id})" title="Thêm vào giỏ">
                            <i class="bi bi-cart-plus"></i>
                        </button>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

function renderPagination(totalPages, totalElements) {
    const pagination = document.getElementById('pagination');
    if (!pagination) return;
    
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

function changePage(page) {
    if (page < 0 || page >= totalPages) return;
    currentPage = page;
    updateURL();
    loadSearchResults();
    // Scroll to top
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function updateURL() {
    const params = new URLSearchParams();
    params.set('q', currentQuery);
    if (currentPage > 0) params.set('page', currentPage);
    if (currentSort !== 'default') params.set('sort', currentSort);
    
    const newURL = `/search-results.html?${params.toString()}`;
    window.history.pushState({}, '', newURL);
}

async function addToCart(productId) {
    if (!apiClient.isAuthenticated()) {
        showAlert('Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng', 'warning');
        setTimeout(() => showLoginModal(), 1000);
        return;
    }
    
    try {
        // Determine userId
        let user = apiClient.getUser();
        if (!user || !user.id) {
            user = await apiClient.getProfile();
            if (user) apiClient.setUser(user);
        }
        const userId = user && user.id;
        if (!userId) {
            showAlert('Không xác định được người dùng. Vui lòng đăng nhập lại.', 'error');
            setTimeout(() => showLoginModal(), 800);
            return;
        }

        const payload = { userId: Number(userId), productId: Number(productId), quantity: 1 };
        await apiClient.request('/cart/add', {
            method: 'POST',
            body: JSON.stringify(payload)
        });

        // Update cart badge
        if (typeof updateCartBadge === 'function') {
            await updateCartBadge();
        }

        showAlert('Sản phẩm đã được thêm vào giỏ hàng', 'success');
    } catch (err) {
        console.error('addToCart error', err);
        showAlert('Thêm vào giỏ hàng thất bại: ' + (err.message || err), 'error');
    }
}

function showError(message) {
    const gridEl = document.getElementById('search-results-grid');
    if (gridEl) {
        gridEl.innerHTML = `
            <div class="alert alert-danger" role="alert">
                <i class="bi bi-exclamation-triangle"></i> ${message}
            </div>
        `;
    }
}


// Products Page Logic
let currentType = '';
let allProducts = [];
let filteredProducts = [];
let selectedBrands = new Set();
let currentPriceRange = 'all';
let currentSort = 'default';

// Initialize page
document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    currentType = urlParams.get('type') || '';
    const brand = urlParams.get('brand') || '';
    
    if (brand) {
        selectedBrands.add(brand);
    }
    
    if (currentType) {
        loadProducts();
        loadBrands();
        updatePageTitle();
    } else {
        showError('Vui lòng chọn loại sản phẩm');
    }
});

async function loadProducts() {
    const loadingEl = document.getElementById('products-loading');
    const gridEl = document.getElementById('products-grid');
    const emptyState = document.getElementById('empty-state');
    
    if (loadingEl) loadingEl.style.display = 'block';
    if (gridEl) gridEl.innerHTML = '';
    if (emptyState) emptyState.classList.add('d-none');
    
    try {
        allProducts = await apiClient.getProductsByType(currentType);
        applyFilters();
    } catch (error) {
        console.error('Error loading products:', error);
        showAlert('Không thể tải sản phẩm: ' + error.message, 'error');
        if (emptyState) emptyState.classList.remove('d-none');
    } finally {
        if (loadingEl) loadingEl.style.display = 'none';
    }
}

async function loadBrands() {
    const brandFilter = document.getElementById('brand-filter');
    if (!brandFilter) return;
    
    try {
        // Get unique brands from products
        const brands = [...new Set(allProducts.map(p => p.brand).filter(Boolean))].sort();
        
        if (brands.length === 0) {
            brandFilter.innerHTML = '<p class="text-muted small">Không có thương hiệu</p>';
            return;
        }
        
        brandFilter.innerHTML = brands.map(brand => `
            <label class="filter-option">
                <input type="checkbox" name="brand" value="${brand}" 
                       ${selectedBrands.has(brand) ? 'checked' : ''} 
                       onchange="toggleBrand('${brand}')">
                <span>${brand}</span>
            </label>
        `).join('');
    } catch (error) {
        console.error('Error loading brands:', error);
        brandFilter.innerHTML = '<p class="text-muted small">Không thể tải thương hiệu</p>';
    }
}

function toggleBrand(brand) {
    if (selectedBrands.has(brand)) {
        selectedBrands.delete(brand);
    } else {
        selectedBrands.add(brand);
    }
    applyFilters();
}

function applyFilters() {
    // Get price range
    const priceRadio = document.querySelector('input[name="priceRange"]:checked');
    currentPriceRange = priceRadio ? priceRadio.value : 'all';
    
    // Get sort option
    const sortRadio = document.querySelector('input[name="sort"]:checked');
    currentSort = sortRadio ? sortRadio.value : 'default';
    
    // Apply filters
    filteredProducts = [...allProducts];
    
    // Filter by brand
    if (selectedBrands.size > 0) {
        filteredProducts = filteredProducts.filter(p => 
            p.brand && selectedBrands.has(p.brand)
        );
    }
    
    // Filter by price range
    if (currentPriceRange !== 'all') {
        const [min, max] = currentPriceRange.split('-').map(Number);
        filteredProducts = filteredProducts.filter(p => {
            const price = p.priceAfterDiscount || p.price || 0;
            return price >= min && price <= max;
        });
    }
    
    // Sort products
    switch (currentSort) {
        case 'price-asc':
            filteredProducts.sort((a, b) => {
                const priceA = a.priceAfterDiscount || a.price || 0;
                const priceB = b.priceAfterDiscount || b.price || 0;
                return priceA - priceB;
            });
            break;
        case 'price-desc':
            filteredProducts.sort((a, b) => {
                const priceA = a.priceAfterDiscount || a.price || 0;
                const priceB = b.priceAfterDiscount || b.price || 0;
                return priceB - priceA;
            });
            break;
        case 'name-asc':
            filteredProducts.sort((a, b) => a.name.localeCompare(b.name));
            break;
        case 'name-desc':
            filteredProducts.sort((a, b) => b.name.localeCompare(a.name));
            break;
    }
    
    // Update URL
    updateURL();
    
    // Display products
    displayProducts(filteredProducts);
}

function displayProducts(products) {
    const gridEl = document.getElementById('products-grid');
    const emptyState = document.getElementById('empty-state');
    const productCount = document.getElementById('product-count');
    
    if (!gridEl) return;
    
    // Update count
    if (productCount) {
        productCount.textContent = `${products.length} sản phẩm`;
    }
    
    if (products.length === 0) {
        if (emptyState) emptyState.classList.remove('d-none');
        gridEl.innerHTML = '';
        return;
    }
    
    if (emptyState) emptyState.classList.add('d-none');
    
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

function updateURL() {
    const params = new URLSearchParams();
    if (currentType) params.set('type', currentType);
    
    if (selectedBrands.size > 0) {
        params.set('brand', Array.from(selectedBrands).join(','));
    }
    
    if (currentPriceRange !== 'all') {
        params.set('priceRange', currentPriceRange);
    }
    
    if (currentSort !== 'default') {
        params.set('sort', currentSort);
    }
    
    const newURL = `/products.html?${params.toString()}`;
    window.history.pushState({}, '', newURL);
}

function updatePageTitle() {
    const titleMap = {
        'LAPTOP': 'Laptop',
        'PC': 'PC Gaming, Streaming',
        'KEYBOARD': 'Bàn phím',
        'MOUSE': 'Chuột',
        'MONITOR': 'Màn hình PC Gaming',
        'HEADPHONE': 'Tai nghe',
        'ACCESSORY': 'Phụ kiện'
    };
    
    const titleEl = document.getElementById('page-title');
    if (titleEl) {
        titleEl.textContent = titleMap[currentType] || 'Sản phẩm';
    }
    
    // Update page title
    document.title = `${titleMap[currentType] || 'Sản phẩm'} - E-Commerce`;
}

function addToCart(productId) {
    if (!apiClient.isAuthenticated()) {
        showAlert('Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng', 'warning');
        setTimeout(() => showLoginModal(), 1000);
        return;
    }
    showAlert('Sản phẩm đã được thêm vào giỏ hàng', 'success');
    // TODO: Implement cart functionality
}

function showError(message) {
    const gridEl = document.getElementById('products-grid');
    if (gridEl) {
        gridEl.innerHTML = `
            <div class="alert alert-danger" role="alert">
                <i class="bi bi-exclamation-triangle"></i> ${message}
            </div>
        `;
    }
}


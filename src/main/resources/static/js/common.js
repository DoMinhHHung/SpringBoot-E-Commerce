// Common functions to load header and footer
function loadHeader() {
  fetch("/fragments/header.html")
    .then((response) => response.text())
    .then((html) => {
      const headerContainer = document.getElementById("header-container");
      if (headerContainer) {
        headerContainer.innerHTML = html;
        initializeHeader();
        // load notifications script after header is inserted
        if (!window.__notificationsScriptLoaded) {
          const s = document.createElement("script");
          s.src = "/js/notifications.js";
          s.onload = () => {
            window.__notificationsScriptLoaded = true;
            if (typeof initializeNotifications === "function") {
              initializeNotifications();
            }
          };
          s.onerror = () => {
            console.warn("Failed to load notifications.js");
          };
          document.body.appendChild(s);
        } else if (typeof initializeNotifications === "function") {
          initializeNotifications();
        }
      }
    })
    .catch((error) => {
      console.error("Error loading header:", error);
      createSimpleHeader();
    });
}

function loadFooter() {
  fetch("/fragments/footer.html")
    .then((response) => response.text())
    .then((html) => {
      const footerContainer = document.getElementById("footer-container");
      if (footerContainer) {
        footerContainer.innerHTML = html;
      }
    })
    .catch((error) => {
      console.error("Error loading footer:", error);
      createSimpleFooter();
    });
}

function createSimpleHeader() {
  const headerContainer = document.getElementById("header-container");
  if (!headerContainer) return;

  headerContainer.innerHTML = `
        <!-- Header Top -->
        <div class="header-top">
            <div class="container">
                <div class="row align-items-center">
                    <div class="col-md-6">
                        <a href="tel:19005301"><i class="bi bi-telephone"></i> Hotline: 1900.5301</a>
                        <a href="/"><i class="bi bi-shop"></i> Hệ thống Showroom</a>
                    </div>
                    <div class="col-md-6 text-end">
                        <a href="#" onclick="showLoginModal(); return false;" id="login-link">Đăng nhập</a>
                        <span class="mx-2">|</span>
                        <a href="#" onclick="showRegisterModal(); return false;" id="register-link">Đăng ký</a>
                    </div>
                </div>
            </div>
        </div>

        <!-- Navbar Main -->
        <nav class="navbar navbar-expand-lg navbar-main">
            <div class="container">
                <a class="navbar-brand fw-bold" href="/index.html"><img src="/logo/logo_main.png" alt="Logo" class="logo-img"></a>
                <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                    <span class="navbar-toggler-icon"></span>
                </button>
                <div class="collapse navbar-collapse" id="navbarNav">
                    <ul class="navbar-nav me-auto">
                        <li class="nav-item">
                            <a class="nav-link" href="/index.html">Trang chủ</a>
                        </li>
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
                                Sản phẩm
                            </a>
                            <ul class="dropdown-menu">
                                <li><a class="dropdown-item" href="/index.html?type=PC">PC Gaming</a></li>
                                <li><a class="dropdown-item" href="/index.html?type=LAPTOP">Laptop</a></li>
                                <li><a class="dropdown-item" href="/index.html?type=KEYBOARD">Bàn phím</a></li>
                                <li><a class="dropdown-item" href="/index.html?type=MOUSE">Chuột</a></li>
                                <li><a class="dropdown-item" href="/index.html?type=MONITOR">Màn hình</a></li>
                                <li><a class="dropdown-item" href="/index.html?type=HEADPHONE">Tai nghe</a></li>
                                <li><a class="dropdown-item" href="/index.html?type=ACCESSORY">Phụ kiện</a></li>
                            </ul>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="/promotions.html">Khuyến mãi</a>
                        </li>
                    </ul>
                    <div class="search-box me-3">
                        <input type="text" class="form-control" id="search-input" placeholder="Tìm kiếm sản phẩm...">
                        <button type="button" onclick="handleSearch()"><i class="bi bi-search"></i></button>
                    </div>
                    <a href="#" class="cart-icon me-3" onclick="showCart()">
                        <i class="bi bi-cart3"></i>
                        <span class="badge" id="cart-badge">0</span>
                    </a>
                    <!-- User Avatar Dropdown -->
                    <div id="user-avatar-dropdown" class="user-avatar-dropdown d-none">
                        <div class="user-avatar-wrapper" id="user-avatar-wrapper">
                            <img src="" alt="User Avatar" id="user-avatar-img" class="user-avatar-img">
                        </div>
                        <div class="user-dropdown-menu" id="user-dropdown-menu">
                            <a href="/profile.html" class="dropdown-item">
                                <i class="bi bi-person"></i> Thông tin người dùng
                            </a>
                            <a href="/admin/dashboard.html" class="dropdown-item d-none" id="admin-menu-item">
                                <i class="bi bi-gear"></i> Quản lý
                            </a>
                            <a href="/admin/dashboard.html" class="dropdown-item d-none" id="editor-menu-item">
                                <i class="bi bi-pencil-square"></i> Editor
                            </a>
                            <a href="#" class="dropdown-item" onclick="showOrders(); return false;">
                                <i class="bi bi-box-seam"></i> Tra cứu đơn hàng
                            </a>
                            <div class="dropdown-divider"></div>
                            <a href="#" class="dropdown-item" onclick="logout(); return false;">
                                <i class="bi bi-box-arrow-right"></i> Đăng xuất
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </nav>

        <!-- Fixed Category Menu Button -->
        <button class="category-menu-btn" id="categoryMenuBtn" onclick="toggleCategoryMenu()">
            <i class="bi bi-list"></i>
            <span>Danh mục</span>
            <i class="bi bi-chevron-down ms-2"></i>
        </button>

        <!-- Category Dropdown Menu -->
        <div class="category-dropdown-menu" id="categoryDropdownMenu">
            <div class="category-menu-wrapper">
                <!-- Left Panel: Categories List -->
                <div class="category-menu-left">
                    <div class="category-menu-item" data-category="LAPTOP" onmouseenter="showCategorySubMenu('LAPTOP')" onclick="navigateToCategory('LAPTOP')">
                        <i class="bi bi-laptop category-icon"></i>
                        <span>Laptop</span>
                        <i class="bi bi-chevron-right category-arrow"></i>
                    </div>
                    <div class="category-menu-item" data-category="PC" onmouseenter="showCategorySubMenu('PC')" onclick="navigateToCategory('PC')">
                        <i class="bi bi-cpu category-icon"></i>
                        <span>PC Gaming, Streaming</span>
                        <i class="bi bi-chevron-right category-arrow"></i>
                    </div>
                    <div class="category-menu-item" data-category="KEYBOARD" onmouseenter="showCategorySubMenu('KEYBOARD')" onclick="navigateToCategory('KEYBOARD')">
                        <i class="bi bi-keyboard category-icon"></i>
                        <span>Bàn phím</span>
                        <i class="bi bi-chevron-right category-arrow"></i>
                    </div>
                    <div class="category-menu-item" data-category="MOUSE" onmouseenter="showCategorySubMenu('MOUSE')" onclick="navigateToCategory('MOUSE')">
                        <i class="bi bi-mouse category-icon"></i>
                        <span>Chuột</span>
                        <i class="bi bi-chevron-right category-arrow"></i>
                    </div>
                    <div class="category-menu-item" data-category="MONITOR" onmouseenter="showCategorySubMenu('MONITOR')" onclick="navigateToCategory('MONITOR')">
                        <i class="bi bi-display category-icon"></i>
                        <span>Màn hình PC Gaming</span>
                        <i class="bi bi-chevron-right category-arrow"></i>
                    </div>
                    <div class="category-menu-item" data-category="HEADPHONE" onmouseenter="showCategorySubMenu('HEADPHONE')" onclick="navigateToCategory('HEADPHONE')">
                        <i class="bi bi-headphones category-icon"></i>
                        <span>Tai nghe</span>
                        <i class="bi bi-chevron-right category-arrow"></i>
                    </div>
                    <div class="category-menu-item" data-category="ACCESSORY" onmouseenter="showCategorySubMenu('ACCESSORY')" onclick="navigateToCategory('ACCESSORY')">
                        <i class="bi bi-puzzle category-icon"></i>
                        <span>Phụ kiện</span>
                        <i class="bi bi-chevron-right category-arrow"></i>
                    </div>
                </div>
                
                <!-- Right Panel: Sub-menu Content -->
                <div class="category-menu-right" id="categorySubMenu">
                    <!-- Sub-menu content will be loaded here -->
                </div>
            </div>
        </div>
    `;
  initializeHeader();
  // Initialize category menu hover after header is created
  setTimeout(function () {
    initCategoryMenuHover();
  }, 100);
}

function createSimpleFooter() {
  const footerContainer = document.getElementById("footer-container");
  if (!footerContainer) return;

  footerContainer.innerHTML = `
        <footer class="footer">
            <div class="container">
                <div class="row">
                    <div class="col-md-3">
                        <h5>Về chúng tôi</h5>
                        <a href="#">Giới thiệu</a>
                        <a href="#">Tuyển dụng</a>
                        <a href="#">Liên hệ</a>
                    </div>
                    <div class="col-md-3">
                        <h5>Chính sách</h5>
                        <a href="#">Chính sách bảo hành</a>
                        <a href="#">Chính sách giao hàng</a>
                        <a href="#">Chính sách bảo mật</a>
                    </div>
                    <div class="col-md-3">
                        <h5>Thông tin</h5>
                        <a href="#">Hệ thống cửa hàng</a>
                        <a href="#">Hướng dẫn mua hàng</a>
                        <a href="#">Hướng dẫn thanh toán</a>
                        <a href="#">Hướng dẫn trả góp</a>
                    </div>
                    <div class="col-md-3">
                        <h5>Tổng đài hỗ trợ</h5>
                        <p><strong>Mua hàng:</strong> 1900.5301</p>
                        <p><strong>Bảo hành:</strong> 1900.5325</p>
                        <p><strong>Email:</strong> cskh@example.com</p>
                    </div>
                </div>
                <div class="footer-bottom">
                    <p>&copy; 2025 E-Commerce. All rights reserved.</p>
                </div>
            </div>
        </footer>
    `;
}

function initializeHeader() {
  // Check authentication status
  if (apiClient && apiClient.isAuthenticated()) {
    updateUserMenu();
    updateCartBadge();
  } else {
    // Nếu chưa đăng nhập, vẫn reset cart badge về 0
    const badge = document.getElementById("cart-badge");
    if (badge) {
      badge.textContent = "0";
    }
  }
  
  // Initialize search autocomplete
  initSearchAutocomplete();
}

function updateUserMenu() {
  if (!apiClient) return;

  // Use request directly with noAuthRedirect to avoid automatic redirect to login when token invalid
  apiClient
    .request("/users/profile", { method: "GET", noAuthRedirect: true })
    .then((user) => {
      const loginLink = document.getElementById("login-link");
      const registerLink = document.getElementById("register-link");
      const userAvatarDropdown = document.getElementById(
        "user-avatar-dropdown"
      );
      const userAvatarImg = document.getElementById("user-avatar-img");
      const adminMenuItem = document.getElementById("admin-menu-item");

      if (loginLink) loginLink.classList.add("d-none");
      if (registerLink) registerLink.classList.add("d-none");
      if (userAvatarDropdown) {
        userAvatarDropdown.classList.remove("d-none");

        // Set avatar image
        const wrapper = document.getElementById("user-avatar-wrapper");
        if (userAvatarImg && wrapper) {
          const icon = wrapper.querySelector(".bi-person-fill");
          if (icon) icon.remove();

          if (user.avatar && user.avatar.trim() !== "") {
            const avatarUrl = user.avatar.trim();

            if (!wrapper.contains(userAvatarImg)) {
              wrapper.appendChild(userAvatarImg);
            }

            userAvatarImg.src = avatarUrl;
            userAvatarImg.alt = user.fullName || user.email || "User";
            userAvatarImg.style.display = "block";
            userAvatarImg.style.visibility = "visible";

            userAvatarImg.onerror = function (e) {
              userAvatarImg.style.display = "none";
              if (!wrapper.querySelector(".bi-person-fill")) {
                const errorIcon = document.createElement("i");
                errorIcon.className = "bi bi-person-fill";
                errorIcon.style.cssText = "font-size: 24px; color: #666;";
                wrapper.appendChild(errorIcon);
              }
            };
          } else {
            userAvatarImg.style.display = "none";
            userAvatarImg.src = "";
            if (!wrapper.querySelector(".bi-person-fill")) {
              const icon = document.createElement("i");
              icon.className = "bi bi-person-fill";
              icon.style.cssText = "font-size: 24px; color: #666;";
              wrapper.appendChild(icon);
            }
          }
        }
      }

        // Show admin menu item if role === 'ADMIN'
        if (adminMenuItem) {
            const roles = Array.isArray(user.role) ? user.role : (typeof user.role === 'string' ? [user.role] : []);
            if (roles.indexOf('ADMIN') !== -1 || roles.indexOf('ROLE_ADMIN') !== -1 || user.role === 'ADMIN') {
                adminMenuItem.classList.remove('d-none');
            } else {
                adminMenuItem.classList.add('d-none');
            }
        }

        // Show editor menu item if role === 'EDITOR'
        const editorMenuItem = document.getElementById('editor-menu-item');
        if (editorMenuItem) {
            const roles = Array.isArray(user.role) ? user.role : (typeof user.role === 'string' ? [user.role] : []);
            const isEditor = roles.indexOf('EDITOR') !== -1 || roles.indexOf('ROLE_EDITOR') !== -1 || user.role === 'EDITOR';
            if (isEditor) {
                editorMenuItem.classList.remove('d-none');
            } else {
                editorMenuItem.classList.add('d-none');
            }
        }
        
        // Sau khi update user menu thành công, cũng update cart badge
        updateCartBadge();
    }).catch(() => {
        if (apiClient) apiClient.clearAuth();
        // Hide avatar dropdown on error
        const userAvatarDropdown = document.getElementById('user-avatar-dropdown');
        if (userAvatarDropdown) userAvatarDropdown.classList.add('d-none');
        
        // Reset cart badge về 0 khi logout hoặc lỗi
        const badge = document.getElementById('cart-badge');
        if (badge) {
            badge.textContent = '0';
        }
    });
}

// Function để update cart badge
async function updateCartBadge() {
  if (!apiClient || !apiClient.isAuthenticated()) {
    const badge = document.getElementById("cart-badge");
    if (badge) {
      badge.textContent = "0";
    }
    return;
  }

  try {
    // Lấy user ID
    let user = apiClient.getUser();
    if (!user || !user.id) {
      try {
        user = await apiClient.request("/users/profile", {
          method: "GET",
          noAuthRedirect: true,
        });
        if (user && user.id) {
          apiClient.setUser(user);
        } else {
          return;
        }
      } catch (err) {
        // Không thể lấy profile, có thể chưa đăng nhập
        const badge = document.getElementById("cart-badge");
        if (badge) {
          badge.textContent = "0";
        }
        return;
      }
    }

    const userId = user.id;

    // Fetch cart để lấy tổng số lượng
    const cart = await apiClient.request(`/cart/${userId}`, {
      method: "GET",
      noAuthRedirect: true,
    });

    const totalQuantity =
      cart && Array.isArray(cart.items)
        ? cart.items.reduce((sum, item) => sum + (item.quantity || 0), 0)
        : 0;

    const badge = document.getElementById("cart-badge");
    if (badge) {
      badge.textContent = totalQuantity || "0";
    }
  } catch (error) {
    // Nếu lỗi (401, 404, etc.), set badge về 0
    console.warn("updateCartBadge error:", error);
    const badge = document.getElementById("cart-badge");
    if (badge) {
      badge.textContent = "0";
    }
  }
}

function logout() {
  if (confirm("Bạn có chắc muốn đăng xuất?")) {
    if (apiClient) {
      apiClient.logout();
    }
  }
}

async function showCart() {
  try {
    if (!apiClient || !apiClient.isAuthenticated()) {
      showAlert("Vui lòng đăng nhập để xem giỏ hàng", "warning");
      showLoginModal();
      return;
    }

    try {
      const profile = await apiClient.getProfile();
      if (!profile || !profile.id) {
        showLoginModal();
        return;
      }
      // token OK -> go to cart
      window.location.href = "/cart.html";
    } catch (err) {
      console.warn("showCart: profile check failed", err);
      showLoginModal();
    }
  } catch (e) {
    console.error("showCart error", e);
    window.location.href = "/cart.html";
  }
}

function showOrders() {
  if (!apiClient || !apiClient.isAuthenticated()) {
    showLoginModal();
    return;
  }
  window.location.href = "/orders.html";
}

function handleSearch() {
  const searchInput = document.getElementById("search-input");
  if (searchInput) {
    const query = searchInput.value.trim();
    if (query) {
      // Hide dropdown if open
      hideSearchDropdown();
      // Navigate to search results page
      window.location.href = `/search-results.html?q=${encodeURIComponent(query)}`;
    }
  }
}

// Search Autocomplete Functions
let searchTimeout = null;
let searchDropdown = null;

function initSearchAutocomplete() {
    const searchInput = document.getElementById('search-input');
    if (!searchInput) return;
    
    // Create dropdown container
    createSearchDropdown();
    
    // Event listeners
    searchInput.addEventListener('input', handleSearchInput);
    searchInput.addEventListener('focus', handleSearchFocus);
    searchInput.addEventListener('keydown', handleSearchKeydown);
    
    // Click outside to close dropdown
    document.addEventListener('click', handleClickOutside);
}

function createSearchDropdown() {
    const searchBox = document.querySelector('.search-box');
    if (!searchBox || document.getElementById('search-dropdown')) return;
    
    searchDropdown = document.createElement('div');
    searchDropdown.id = 'search-dropdown';
    searchDropdown.className = 'search-dropdown';
    searchBox.style.position = 'relative';
    searchBox.appendChild(searchDropdown);
}

async function handleSearchInput(e) {
    const query = e.target.value.trim();
    
    clearTimeout(searchTimeout);
    
    if (query.length < 2) {
        hideSearchDropdown();
        return;
    }
    
    searchTimeout = setTimeout(async () => {
        await loadSearchAutocomplete(query);
    }, 300);
}

function handleSearchFocus(e) {
    const query = e.target.value.trim();
    if (query.length >= 2) {
        loadSearchAutocomplete(query);
    }
}

function handleSearchKeydown(e) {
    if (e.key === 'Enter') {
        e.preventDefault();
        handleSearch();
    } else if (e.key === 'Escape') {
        hideSearchDropdown();
    }
}

function handleClickOutside(e) {
    const searchBox = document.querySelector('.search-box');
    if (searchBox && !searchBox.contains(e.target)) {
        hideSearchDropdown();
    }
}

async function loadSearchAutocomplete(query) {
    if (!searchDropdown) return;
    
    try {
        const response = await apiClient.searchAutocomplete(query, 5);
        const { products, totalCount } = response;
        
        displaySearchDropdown(products, totalCount, query);
    } catch (error) {
        console.error('Search autocomplete error:', error);
        hideSearchDropdown();
    }
}

function displaySearchDropdown(products, totalCount, query) {
    if (!searchDropdown) return;
    
    if (products.length === 0 && totalCount === 0) {
        searchDropdown.innerHTML = `
            <div class="search-dropdown-empty">
                <p class="text-muted p-3 mb-0">Không tìm thấy sản phẩm nào</p>
            </div>
        `;
        searchDropdown.classList.add('show');
        return;
    }
    
    let html = '';
    
    // Display products (max 5)
    products.forEach(product => {
        const discountPercent = product.priceAfterDiscount && product.priceAfterDiscount < product.price
            ? Math.round((1 - product.priceAfterDiscount / product.price) * 100)
            : 0;
        
        // Get short description (first 80 characters)
        const shortDesc = product.description 
            ? (product.description.length > 80 ? product.description.substring(0, 80) + '...' : product.description)
            : '';
        
        html += `
            <div class="search-dropdown-item" onclick="navigateToProduct(${product.id})">
                <img src="${product.mainImage}" alt="${product.name}" class="search-dropdown-item-image">
                <div class="search-dropdown-item-content">
                    ${product.brand ? `<div class="search-dropdown-item-brand">${product.brand}</div>` : ''}
                    <div class="search-dropdown-item-name">${product.name}</div>
                    ${shortDesc ? `<div class="search-dropdown-item-desc text-muted" style="font-size: 0.8rem; line-height: 1.3;">${shortDesc}</div>` : ''}
                    <div class="search-dropdown-item-price">
                        ${discountPercent > 0 ? `<span class="search-dropdown-item-discount">-${discountPercent}%</span>` : ''}
                        ${product.priceAfterDiscount && product.priceAfterDiscount < product.price
                            ? `<span class="price-old">${formatPrice(product.price)}</span>
                               <span class="price-new">${formatPrice(product.priceAfterDiscount)}</span>`
                            : `<span class="price-new">${formatPrice(product.price)}</span>`
                        }
                    </div>
                </div>
            </div>
        `;
    });
    
    // Show "View more" button if there are more products
    const remainingCount = totalCount - products.length;
    if (remainingCount > 0) {
        html += `
            <div class="search-dropdown-view-more" onclick="navigateToSearchResults('${query.replace(/'/g, "\\'")}')">
                Xem thêm ${remainingCount} sản phẩm
            </div>
        `;
    }
    
    searchDropdown.innerHTML = html;
    searchDropdown.classList.add('show');
}

function hideSearchDropdown() {
    if (searchDropdown) {
        searchDropdown.classList.remove('show');
    }
}

function navigateToProduct(productId) {
    hideSearchDropdown();
    window.location.href = `/product-detail.html?id=${productId}`;
}

function navigateToSearchResults(query) {
    hideSearchDropdown();
    window.location.href = `/search-results.html?q=${encodeURIComponent(query)}`;
}

function showLoginModal() {
  const modal = document.getElementById("loginModal");
  if (modal) {
    const bsModal = new bootstrap.Modal(modal);
    bsModal.show();
    return;
  }

  // If modal not present, try to load it and wait for it to be added to DOM.
  // This avoids redirecting the user away when the modal fragment is still loading.
  loadAuthModal();

  const maxRetries = 200; // ~20 seconds (200 * 100ms) to allow modal fragment to load on slow connections
  let attempts = 0;
  const interval = setInterval(() => {
    const m = document.getElementById("loginModal");
    attempts++;
    if (m) {
      clearInterval(interval);
      const bsModal = new bootstrap.Modal(m);
      bsModal.show();
    } else if (attempts >= maxRetries) {
      clearInterval(interval);
      // Modal couldn't be loaded in time; show a helpful in-page alert and let user click Login in header
      showAlert(
        'Không thể mở modal đăng nhập ngay bây giờ. Vui lòng bấm "Đăng nhập" ở góc trên để mở trang đăng nhập.',
        "warning"
      );
    }
  }, 100);
}

function showRegisterModal() {
  const modal = document.getElementById("registerModal");
  if (modal) {
    const bsModal = new bootstrap.Modal(modal);
    bsModal.show();
  } else {
    window.location.href = "/register.html";
  }
}

// Modal Functions
function switchToRegisterModal() {
  const loginModal = bootstrap.Modal.getInstance(
    document.getElementById("loginModal")
  );
  if (loginModal) {
    loginModal.hide();
  }
  // Open register modal after a short delay
  setTimeout(() => {
    const registerModal = new bootstrap.Modal(
      document.getElementById("registerModal")
    );
    registerModal.show();
  }, 300);
}

function switchToLoginModal() {
  // Close register modal
  const registerModal = bootstrap.Modal.getInstance(
    document.getElementById("registerModal")
  );
  if (registerModal) {
    registerModal.hide();
  }
  // Open login modal after a short delay
  setTimeout(() => {
    const loginModal = new bootstrap.Modal(
      document.getElementById("loginModal")
    );
    loginModal.show();
  }, 300);
}

function switchToPhoneLogin() {
  const phoneSection = document.getElementById("phone-login-section");
  const emailForm = document.getElementById("modal-login-form");
  if (phoneSection) phoneSection.style.display = "block";
  if (emailForm) emailForm.style.display = "none";
}

function switchToEmailLogin() {
  const phoneSection = document.getElementById("phone-login-section");
  const emailForm = document.getElementById("modal-login-form");
  if (phoneSection) phoneSection.style.display = "none";
  if (emailForm) emailForm.style.display = "block";
}

function togglePasswordVisibility(inputId) {
  const input = document.getElementById(inputId);
  if (!input) return;

  let iconId = "";
  if (inputId === "modal-password") {
    iconId = "toggle-password-icon";
  } else if (inputId === "modal-register-password") {
    iconId = "toggle-register-password-icon";
  } else if (inputId === "modal-register-confirm-password") {
    iconId = "toggle-confirm-password-icon";
  }

  const icon = document.getElementById(iconId);
  if (!icon) return;

  if (input.type === "password") {
    input.type = "text";
    icon.classList.remove("bi-eye");
    icon.classList.add("bi-eye-slash");
  } else {
    input.type = "password";
    icon.classList.remove("bi-eye-slash");
    icon.classList.add("bi-eye");
  }
}

function showForgotPassword() {
  const modal = bootstrap.Modal.getInstance(
    document.getElementById("loginModal")
  );
  if (modal) modal.hide();
  window.location.href = "/forgot-password.html";
}

function showFacebookLogin() {
  alert("Tính năng đăng nhập bằng Facebook đang được phát triển");
}

function initializeAuthModal() {
  // Login Form Handler
  const loginForm = document.getElementById("modal-login-form");
  if (loginForm) {
    // Remove existing listener if any by cloning
    const newLoginForm = loginForm.cloneNode(true);
    loginForm.parentNode.replaceChild(newLoginForm, loginForm);

    newLoginForm.addEventListener("submit", async function (e) {
      e.preventDefault();

      // Get values directly from the form element (not by ID to avoid stale references)
      const formData = new FormData(e.target);
      const emailInput = e.target.querySelector("#modal-email");
      const passwordInput = e.target.querySelector("#modal-password");

      if (!emailInput || !passwordInput) {
        showAlert("Không tìm thấy các trường đăng nhập", "error");
        return;
      }

      const email = emailInput.value.trim();
      const password = passwordInput.value;

      if (!email || !password) {
        showAlert("Vui lòng nhập đầy đủ email và mật khẩu", "error");
        return;
      }

      const submitBtn = e.target.querySelector('button[type="submit"]');
      const originalText = submitBtn.innerHTML;
      submitBtn.disabled = true;
      submitBtn.innerHTML =
        '<span class="spinner-border spinner-border-sm"></span> Đang đăng nhập...';

      try {
        await apiClient.login(email, password);
        const user = await apiClient.getProfile();
        apiClient.setUser(user);

        const modal = bootstrap.Modal.getInstance(
          document.getElementById("loginModal")
        );
        if (modal) modal.hide();

        showAlert("Đăng nhập thành công!", "success");
        setTimeout(() => location.reload(), 500);
      } catch (error) {
        showAlert("Đăng nhập thất bại: " + error.message, "error");
      } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalText;
      }
    });
  }

  // Register Form Handler
  const registerForm = document.getElementById("modal-register-form");
  if (registerForm) {
    // Remove existing listener if any
    const newRegisterForm = registerForm.cloneNode(true);
    registerForm.parentNode.replaceChild(newRegisterForm, registerForm);

    newRegisterForm.addEventListener("submit", async function (e) {
      e.preventDefault();

      const password = document.getElementById("modal-register-password").value;
      const confirmPassword = document.getElementById(
        "modal-register-confirm-password"
      ).value;

      if (password !== confirmPassword) {
        showAlert("Mật khẩu xác nhận không khớp", "error");
        return;
      }

      if (password.length < 6) {
        showAlert("Mật khẩu phải có ít nhất 6 ký tự", "error");
        return;
      }

      const registerData = {
        email: document.getElementById("modal-register-email").value,
        password: password,
        fullName: document.getElementById("modal-register-fullname").value,
        phone: document.getElementById("modal-register-phone").value,
        gender: document.getElementById("modal-register-gender").value || null,
        dob: document.getElementById("modal-register-dob").value || null,
      };

      const submitBtn = e.target.querySelector('button[type="submit"]');
      const originalText = submitBtn.innerHTML;
      submitBtn.disabled = true;
      submitBtn.innerHTML =
        '<span class="spinner-border spinner-border-sm"></span> Đang đăng ký...';

      try {
        await apiClient.register(registerData);
        const modal = bootstrap.Modal.getInstance(
          document.getElementById("registerModal")
        );
        if (modal) modal.hide();

        showAlert(
          "Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.",
          "success"
        );
      } catch (error) {
        showAlert("Đăng ký thất bại: " + error.message, "error");
      } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalText;
      }
    });
  }
}

function loadAuthModal() {
  fetch("/fragments/auth-modal.html")
    .then((response) => response.text())
    .then((html) => {
      // Check if modal already exists
      if (document.getElementById("loginModal")) {
        // Re-initialize handlers in case modal was already loaded
        initializeAuthModal();
        return;
      }
      document.body.insertAdjacentHTML("beforeend", html);
      // Initialize form handlers after modal is loaded
      initializeAuthModal();
    })
    .catch((error) => {
      console.error("Error loading auth modal:", error);
    });
}

// Category Menu Functions
let categoryMenuTimeout = null;

function toggleCategoryMenu() {
  const menu = document.getElementById("categoryDropdownMenu");
  const btn = document.getElementById("categoryMenuBtn");

  if (menu && menu.classList.contains("show")) {
    closeCategoryMenu();
  } else {
    openCategoryMenu();
  }
}

function openCategoryMenu() {
  const menu = document.getElementById("categoryDropdownMenu");
  const btn = document.getElementById("categoryMenuBtn");

  if (menu && btn) {
    clearTimeout(categoryMenuTimeout);
    menu.classList.add("show");
    btn.classList.add("active");

    // Show default sub-menu (LAPTOP) when menu first opens
    const subMenuContainer = document.getElementById("categorySubMenu");
    if (subMenuContainer && !subMenuContainer.innerHTML.trim()) {
      showCategorySubMenu("LAPTOP");
    }
  }
}

function closeCategoryMenu() {
  const menu = document.getElementById("categoryDropdownMenu");
  const btn = document.getElementById("categoryMenuBtn");

  if (menu) {
    // Add closing animation class
    menu.classList.add("closing");
    menu.classList.remove("show");

    // Remove closing class after animation completes
    setTimeout(function () {
      if (menu) {
        menu.classList.remove("closing");
      }
    }, 300);
  }
  if (btn) btn.classList.remove("active");
}

// Map categorySubMenu section types to API filter parameter names
const filterTypeMap = {
  'brand': 'brand',
  'chip': 'cpu',
  'screen': 'screenSize',
  'switch': 'switchType',
  'connection': 'connection',
  'dpi': 'dpi',
  'resolution': 'resolution',
  'refresh': 'refreshRate',
  'usage': 'usage',
  'pc-usage': 'usage',
  'monitor-usage': 'usage',
  'type': 'typeFilter',
  'size': 'size',
  'components': null, // PC components - handled differently
  'monitor-brand': 'brand'
};

// Extract filter value from item name (e.g., "Laptop Core i5" -> "Core i5")
function extractFilterValue(itemName, filterType) {
  if (filterType === 'chip' || filterType === 'cpu') {
    // Remove "Laptop" prefix if exists
    return itemName.replace(/^Laptop\s+/i, '').trim();
  }
  if (filterType === 'screen') {
    // Extract size (e.g., "Laptop 15.6 inch" -> "15.6 inch")
    const match = itemName.match(/(\d+(?:\.\d+)?\s*inch)/i);
    return match ? match[1] : itemName.replace(/^Laptop\s+/i, '').trim();
  }
  if (filterType === 'dpi') {
    // Extract DPI range (e.g., "Dưới 8000 DPI" -> "8000")
    const match = itemName.match(/(\d+)/);
    return match ? match[1] : itemName;
  }
  if (filterType === 'refresh') {
    // Extract Hz (e.g., "144Hz" -> "144Hz")
    const match = itemName.match(/(\d+Hz)/i);
    return match ? match[1] : itemName;
  }
  if (filterType === 'resolution') {
    // Extract resolution (e.g., "Full HD (1920x1080)" -> "1920x1080")
    const match = itemName.match(/\((\d+x\d+)\)/);
    return match ? match[1] : itemName;
  }
  if (filterType === 'size') {
    // Extract size (e.g., "27 inch" -> "27 inch")
    const match = itemName.match(/(\d+(?:\.\d+)?\s*inch)/i);
    return match ? match[1] : itemName;
  }
  // For other types, return as is
  return itemName;
}

function navigateToCategory(type, filterValue = null, filterType = 'brand') {
  closeCategoryMenu();
  let url = `/products.html?type=${type}`;
  
  if (filterValue) {
    // Map filterType from categorySubMenu to API parameter name
    const apiParamName = filterTypeMap[filterType] || filterType;
    
    // Extract actual filter value from item name
    const extractedValue = extractFilterValue(filterValue, filterType);
    
    if (apiParamName === 'brand') {
      url += `&brand=${encodeURIComponent(extractedValue)}`;
    } else if (apiParamName) {
      url += `&${apiParamName}=${encodeURIComponent(extractedValue)}`;
    }
  }
  
  window.location.href = url;
}

// Category Sub-menu Data
const categorySubMenus = {
  LAPTOP: {
    sections: [
      {
        title: "Thương hiệu",
        type: "brand",
        items: [
          { name: "MacBook", icon: "bi-apple" },
          { name: "ASUS", icon: "bi-laptop" },
          { name: "Lenovo", icon: "bi-laptop" },
          { name: "DELL", icon: "bi-laptop" },
          { name: "hp", icon: "bi-laptop" },
          { name: "acer", icon: "bi-laptop" },
          { name: "LG", icon: "bi-laptop" },
          { name: "msi", icon: "bi-laptop" },
          { name: "GIGABYTE", icon: "bi-laptop" },
          { name: "Masstel", icon: "bi-laptop" },
          { name: "Samsung", icon: "bi-laptop" },
          { name: "Microsoft", icon: "bi-laptop" },
        ],
      },
      {
        title: "Dòng chip",
        type: "chip",
        items: [
          { name: "Laptop Core i3", icon: "bi-cpu" },
          { name: "Laptop Core i5", icon: "bi-cpu" },
          { name: "Laptop Core i7", icon: "bi-cpu" },
          { name: "Laptop Core i9", icon: "bi-cpu" },
          { name: "Laptop Core U5", icon: "bi-cpu" },
          { name: "Laptop Core U7", icon: "bi-cpu" },
          { name: "Laptop Core U9", icon: "bi-cpu" },
          { name: "Apple M3 Series", icon: "bi-apple" },
          { name: "Apple M4 Series", icon: "bi-apple" },
          { name: "Apple M5 Series", icon: "bi-apple", badge: "Mới" },
          { name: "AMD Ryzen", icon: "bi-cpu" },
          { name: "Intel Core Ultra", icon: "bi-cpu", badge: "Hot" },
        ],
      },
      {
        title: "Phân khúc giá",
        type: "price",
        items: [
          { name: "Dưới 10 triệu", icon: "bi-currency-dollar" },
          { name: "Từ 10 - 15 triệu", icon: "bi-currency-dollar" },
          { name: "Từ 15 - 20 triệu", icon: "bi-currency-dollar" },
          { name: "Từ 20 - 25 triệu", icon: "bi-currency-dollar" },
          { name: "Từ 25 - 30 triệu", icon: "bi-currency-dollar" },
        ],
      },
      {
        title: "Kích thước màn hình",
        type: "screen",
        items: [
          { name: "Laptop 13 inch", icon: "bi-display" },
          { name: "Laptop 14 inch", icon: "bi-display" },
          { name: "Laptop 15.6 inch", icon: "bi-display" },
          { name: "Laptop 16 inch", icon: "bi-display" },
        ],
      },
    ],
  },
  PC: {
    sections: [
      {
        title: "Chọn PC theo nhu cầu",
        type: "pc-usage",
        items: [
          { name: "Gaming", icon: "bi-controller" },
          { name: "Đồ họa", icon: "bi-palette" },
          { name: "Văn phòng", icon: "bi-briefcase" },
        ],
      },
      {
        title: "Linh kiện máy tính",
        type: "components",
        items: [
          { name: "CPU", icon: "bi-cpu" },
          { name: "Main", icon: "bi-cpu" },
          { name: "RAM", icon: "bi-cpu" },
          { name: "Ổ cứng", icon: "bi-hdd" },
          { name: "Nguồn", icon: "bi-lightning-charge" },
          { name: "VGA", icon: "bi-cpu" },
          { name: "Tản nhiệt", icon: "bi-snow" },
          { name: "Case", icon: "bi-box" },
        ],
      },
      {
        title: "Chọn màn hình theo hãng",
        type: "monitor-brand",
        items: [
          { name: "ASUS", icon: "bi-display" },
          { name: "SAMSUNG", icon: "bi-display" },
          { name: "DELL", icon: "bi-display" },
          { name: "LG", icon: "bi-display" },
          { name: "msi", icon: "bi-display" },
          { name: "acer", icon: "bi-display" },
          { name: "XIAOMI", icon: "bi-display" },
          { name: "ViewSonic", icon: "bi-display" },
          { name: "PHILIPS", icon: "bi-display" },
          { name: "AOC", icon: "bi-display" },
          { name: "alhua", icon: "bi-display" },
          { name: "KOORUI", icon: "bi-display" },
        ],
      },
      {
        title: "Chọn màn hình theo nhu cầu",
        type: "monitor-usage",
        items: [
          { name: "Gaming", icon: "bi-controller" },
          { name: "Văn phòng", icon: "bi-briefcase" },
          { name: "Đồ họa", icon: "bi-palette" },
          { name: "Lập trình", icon: "bi-code-square" },
          { name: "Màn hình di động", icon: "bi-display" },
          { name: "Arm màn hình", icon: "bi-display" },
        ],
      },
    ],
  },
  KEYBOARD: {
    sections: [
      {
        title: "Thương hiệu",
        type: "brand",
        items: [
          { name: "Logitech", icon: "bi-keyboard" },
          { name: "Corsair", icon: "bi-keyboard" },
          { name: "Razer", icon: "bi-keyboard" },
          { name: "SteelSeries", icon: "bi-keyboard" },
          { name: "HyperX", icon: "bi-keyboard" },
          { name: "ASUS ROG", icon: "bi-keyboard" },
          { name: "Cooler Master", icon: "bi-keyboard" },
          { name: "Ducky", icon: "bi-keyboard" },
        ],
      },
      {
        title: "Loại switch",
        type: "switch",
        items: [
          { name: "Mechanical", icon: "bi-keyboard" },
          { name: "Membrane", icon: "bi-keyboard" },
          { name: "Optical", icon: "bi-keyboard" },
          { name: "Hybrid", icon: "bi-keyboard" },
        ],
      },
      {
        title: "Kết nối",
        type: "connection",
        items: [
          { name: "Có dây", icon: "bi-usb" },
          { name: "Không dây", icon: "bi-bluetooth" },
          { name: "Cả hai", icon: "bi-wifi" },
        ],
      },
      {
        title: "Kích thước",
        type: "size",
        items: [
          { name: "Full-size", icon: "bi-keyboard" },
          { name: "Tenkeyless", icon: "bi-keyboard" },
          { name: "Compact", icon: "bi-keyboard" },
        ],
      },
      {
        title: "Phân khúc giá",
        type: "price",
        items: [
          { name: "Dưới 1 triệu", icon: "bi-currency-dollar" },
          { name: "Từ 1 - 2 triệu", icon: "bi-currency-dollar" },
          { name: "Từ 2 - 3 triệu", icon: "bi-currency-dollar" },
          { name: "Trên 3 triệu", icon: "bi-currency-dollar" },
        ],
      },
    ],
  },
  MOUSE: {
    sections: [
      {
        title: "Thương hiệu",
        type: "brand",
        items: [
          { name: "Logitech", icon: "bi-mouse" },
          { name: "Razer", icon: "bi-mouse" },
          { name: "Corsair", icon: "bi-mouse" },
          { name: "SteelSeries", icon: "bi-mouse" },
          { name: "ASUS ROG", icon: "bi-mouse" },
          { name: "HyperX", icon: "bi-mouse" },
          { name: "Zowie", icon: "bi-mouse" },
          { name: "Glorious", icon: "bi-mouse" },
        ],
      },
      {
        title: "Kết nối",
        type: "connection",
        items: [
          { name: "Có dây", icon: "bi-usb" },
          { name: "Không dây", icon: "bi-bluetooth" },
          { name: "Cả hai", icon: "bi-wifi" },
        ],
      },
      {
        title: "DPI",
        type: "dpi",
        items: [
          { name: "Dưới 8000 DPI", icon: "bi-mouse" },
          { name: "8000 - 12000 DPI", icon: "bi-mouse" },
          { name: "12000 - 16000 DPI", icon: "bi-mouse" },
          { name: "Trên 16000 DPI", icon: "bi-mouse" },
        ],
      },
      {
        title: "Loại",
        type: "type",
        items: [
          { name: "Gaming", icon: "bi-controller" },
          { name: "Văn phòng", icon: "bi-briefcase" },
        ],
      },
      {
        title: "Phân khúc giá",
        type: "price",
        items: [
          { name: "Dưới 500k", icon: "bi-currency-dollar" },
          { name: "Từ 500k - 1 triệu", icon: "bi-currency-dollar" },
          { name: "Từ 1 - 2 triệu", icon: "bi-currency-dollar" },
          { name: "Trên 2 triệu", icon: "bi-currency-dollar" },
        ],
      },
    ],
  },
  MONITOR: {
    sections: [
      {
        title: "Thương hiệu",
        type: "brand",
        items: [
          { name: "ASUS", icon: "bi-display" },
          { name: "SAMSUNG", icon: "bi-display" },
          { name: "DELL", icon: "bi-display" },
          { name: "LG", icon: "bi-display" },
          { name: "msi", icon: "bi-display" },
          { name: "acer", icon: "bi-display" },
          { name: "XIAOMI", icon: "bi-display" },
          { name: "ViewSonic", icon: "bi-display" },
          { name: "PHILIPS", icon: "bi-display" },
          { name: "AOC", icon: "bi-display" },
        ],
      },
      {
        title: "Kích thước",
        type: "size",
        items: [
          { name: "24 inch", icon: "bi-display" },
          { name: "27 inch", icon: "bi-display" },
          { name: "32 inch", icon: "bi-display" },
          { name: "34 inch", icon: "bi-display" },
          { name: "Trên 34 inch", icon: "bi-display" },
        ],
      },
      {
        title: "Độ phân giải",
        type: "resolution",
        items: [
          { name: "Full HD (1920x1080)", icon: "bi-display" },
          { name: "2K (2560x1440)", icon: "bi-display" },
          { name: "4K (3840x2160)", icon: "bi-display" },
          { name: "Ultrawide", icon: "bi-display" },
        ],
      },
      {
        title: "Tần số quét",
        type: "refresh",
        items: [
          { name: "60Hz", icon: "bi-display" },
          { name: "144Hz", icon: "bi-display" },
          { name: "165Hz", icon: "bi-display" },
          { name: "240Hz", icon: "bi-display" },
          { name: "Trên 240Hz", icon: "bi-display" },
        ],
      },
      {
        title: "Loại",
        type: "type",
        items: [
          { name: "Gaming", icon: "bi-controller" },
          { name: "Văn phòng", icon: "bi-briefcase" },
          { name: "Đồ họa", icon: "bi-palette" },
          { name: "Lập trình", icon: "bi-code-square" },
        ],
      },
      {
        title: "Phân khúc giá",
        type: "price",
        items: [
          { name: "Dưới 5 triệu", icon: "bi-currency-dollar" },
          { name: "Từ 5 - 10 triệu", icon: "bi-currency-dollar" },
          { name: "Từ 10 - 15 triệu", icon: "bi-currency-dollar" },
          { name: "Trên 15 triệu", icon: "bi-currency-dollar" },
        ],
      },
    ],
  },
  HEADPHONE: {
    sections: [
      {
        title: "Thương hiệu",
        type: "brand",
        items: [
          { name: "Sony", icon: "bi-headphones" },
          { name: "Bose", icon: "bi-headphones" },
          { name: "Sennheiser", icon: "bi-headphones" },
          { name: "Audio-Technica", icon: "bi-headphones" },
          { name: "HyperX", icon: "bi-headphones" },
          { name: "SteelSeries", icon: "bi-headphones" },
          { name: "Razer", icon: "bi-headphones" },
          { name: "Logitech", icon: "bi-headphones" },
        ],
      },
      {
        title: "Loại",
        type: "type",
        items: [
          { name: "Over-ear", icon: "bi-headphones" },
          { name: "On-ear", icon: "bi-headphones" },
          { name: "In-ear", icon: "bi-headphones" },
          { name: "True Wireless", icon: "bi-headphones" },
        ],
      },
      {
        title: "Kết nối",
        type: "connection",
        items: [
          { name: "Có dây", icon: "bi-usb" },
          { name: "Bluetooth", icon: "bi-bluetooth" },
          { name: "Cả hai", icon: "bi-wifi" },
        ],
      },
      {
        title: "Nhu cầu sử dụng",
        type: "usage",
        items: [
          { name: "Gaming", icon: "bi-controller" },
          { name: "Nghe nhạc", icon: "bi-music-note" },
          { name: "Văn phòng", icon: "bi-briefcase" },
          { name: "Thể thao", icon: "bi-activity" },
        ],
      },
      {
        title: "Phân khúc giá",
        type: "price",
        items: [
          { name: "Dưới 1 triệu", icon: "bi-currency-dollar" },
          { name: "Từ 1 - 3 triệu", icon: "bi-currency-dollar" },
          { name: "Từ 3 - 5 triệu", icon: "bi-currency-dollar" },
          { name: "Trên 5 triệu", icon: "bi-currency-dollar" },
        ],
      },
    ],
  },
  ACCESSORY: {
    sections: [
      {
        title: "Loại phụ kiện",
        type: "type",
        items: [
          { name: "USB Drive", icon: "bi-usb" },
          { name: "Webcam", icon: "bi-camera-video" },
          { name: "Microphone", icon: "bi-mic" },
          { name: "Speaker", icon: "bi-speaker" },
          { name: "Hub USB", icon: "bi-usb" },
          { name: "Adapter", icon: "bi-lightning-charge" },
          { name: "Cable", icon: "bi-usb" },
          { name: "Stand", icon: "bi-display" },
        ],
      },
      {
        title: "Thương hiệu",
        type: "brand",
        items: [
          { name: "Logitech", icon: "bi-puzzle" },
          { name: "Anker", icon: "bi-puzzle" },
          { name: "Belkin", icon: "bi-puzzle" },
          { name: "Samsung", icon: "bi-puzzle" },
          { name: "SanDisk", icon: "bi-puzzle" },
          { name: "Kingston", icon: "bi-puzzle" },
        ],
      },
      {
        title: "Phân khúc giá",
        type: "price",
        items: [
          { name: "Dưới 500k", icon: "bi-currency-dollar" },
          { name: "Từ 500k - 1 triệu", icon: "bi-currency-dollar" },
          { name: "Từ 1 - 2 triệu", icon: "bi-currency-dollar" },
          { name: "Trên 2 triệu", icon: "bi-currency-dollar" },
        ],
      },
    ],
  },
};

// Show category sub-menu
function showCategorySubMenu(category) {
  const subMenuContainer = document.getElementById("categorySubMenu");
  const menuItems = document.querySelectorAll(".category-menu-item");

  if (!subMenuContainer) return;

  // Remove active class from all items
  menuItems.forEach((item) => item.classList.remove("active"));

  // Add active class to current item
  const currentItem = document.querySelector(`[data-category="${category}"]`);
  if (currentItem) {
    currentItem.classList.add("active");
  }

  // Get sub-menu data
  const subMenuData = categorySubMenus[category];
  if (!subMenuData) {
    subMenuContainer.innerHTML = '<p class="text-muted">Đang tải...</p>';
    return;
  }

  // Render sub-menu
  let html = "";
  subMenuData.sections.forEach((section) => {
    const filterType = section.type || 'brand';
    html += `
            <div class="sub-menu-section">
                <h6 class="sub-menu-title">${section.title}</h6>
                <div class="sub-menu-grid">
                    ${section.items
                      .map(
                        (item) => `
                        <div class="sub-menu-item" onclick="navigateToCategory('${category}', '${
                          item.name
                        }', '${filterType}')">
                            <i class="bi ${item.icon} sub-menu-icon"></i>
                            <span class="sub-menu-item-text">
                                ${item.name}
                                ${
                                  item.badge
                                    ? `<span class="sub-menu-badge">${item.badge}</span>`
                                    : ""
                                }
                            </span>
                        </div>
                    `
                      )
                      .join("")}
                </div>
            </div>
        `;
  });

  subMenuContainer.innerHTML = html;
}

// Initialize hover events for category menu
function initCategoryMenuHover() {
  const btn = document.getElementById("categoryMenuBtn");
  const menu = document.getElementById("categoryDropdownMenu");

  if (!btn || !menu) return;

  // Open menu on button hover
  btn.addEventListener("mouseenter", function () {
    clearTimeout(categoryMenuTimeout);
    openCategoryMenu();
  });

  // Keep menu open when hovering over menu
  menu.addEventListener("mouseenter", function () {
    clearTimeout(categoryMenuTimeout);
  });

  // Close menu when mouse leaves button and menu
  btn.addEventListener("mouseleave", function () {
    categoryMenuTimeout = setTimeout(function () {
      // Check if mouse is not over menu
      if (!menu.matches(":hover")) {
        closeCategoryMenu();
      }
    }, 200);
  });

  menu.addEventListener("mouseleave", function () {
    categoryMenuTimeout = setTimeout(function () {
      // Check if mouse is not over button
      if (!btn.matches(":hover")) {
        closeCategoryMenu();
      }
    }, 200);
  });
}

// Close menu when clicking outside (optional, for click behavior)
document.addEventListener("click", function (event) {
  const menu = document.getElementById("categoryDropdownMenu");
  const btn = document.getElementById("categoryMenuBtn");

  if (
    menu &&
    btn &&
    !menu.contains(event.target) &&
    !btn.contains(event.target)
  ) {
    closeCategoryMenu();
  }
});

// Load header and footer when DOM is ready
document.addEventListener("DOMContentLoaded", function () {
  loadHeader();
  loadFooter();
  loadAuthModal();

  // Initialize category menu hover after header is loaded
  setTimeout(function () {
    initCategoryMenuHover();
  }, 500);
});

// Common functions to load header and footer
function loadHeader() {
    fetch('/fragments/header.html')
        .then(response => response.text())
        .then(html => {
            const headerContainer = document.getElementById('header-container');
            if (headerContainer) {
                headerContainer.innerHTML = html;
                // Initialize header scripts after loading
                initializeHeader();
            }
        })
        .catch(error => {
            console.error('Error loading header:', error);
            // Fallback: create simple header
            createSimpleHeader();
        });
}

function loadFooter() {
    fetch('/fragments/footer.html')
        .then(response => response.text())
        .then(html => {
            const footerContainer = document.getElementById('footer-container');
            if (footerContainer) {
                footerContainer.innerHTML = html;
            }
        })
        .catch(error => {
            console.error('Error loading footer:', error);
            // Fallback: create simple footer
            createSimpleFooter();
        });
}

function createSimpleHeader() {
    const headerContainer = document.getElementById('header-container');
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
                        <span id="user-menu" class="d-none">
                            <a href="/profile.html" id="profile-link"></a>
                            <span class="mx-2">|</span>
                            <a href="#" onclick="logout()">Đăng xuất</a>
                        </span>
                    </div>
                </div>
            </div>
        </div>

        <!-- Navbar Main -->
        <nav class="navbar navbar-expand-lg navbar-main">
            <div class="container">
                <a class="navbar-brand fw-bold" href="/index.html">E-COMMERCE</a>
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
                        <li class="nav-item">
                            <a class="nav-link" href="/profile.html">Tài khoản</a>
                        </li>
                    </ul>
                    <div class="search-box me-3">
                        <input type="text" class="form-control" id="search-input" placeholder="Tìm kiếm sản phẩm...">
                        <button type="button" onclick="handleSearch()"><i class="bi bi-search"></i></button>
                    </div>
                    <a href="#" class="cart-icon" onclick="showCart()">
                        <i class="bi bi-cart3"></i>
                        <span class="badge" id="cart-badge">0</span>
                    </a>
                </div>
            </div>
        </nav>

        <!-- Category Navigation -->
        <nav class="category-nav">
            <div class="container">
                <ul class="nav">
                    <li class="nav-item">
                        <a class="nav-link active" href="/index.html">Tất cả</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="/index.html?type=PC">PC Gaming</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="/index.html?type=LAPTOP">Laptop</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="/index.html?type=KEYBOARD">Bàn phím</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="/index.html?type=MOUSE">Chuột</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="/index.html?type=MONITOR">Màn hình</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="/index.html?type=HEADPHONE">Tai nghe</a>
                    </li>
                </ul>
            </div>
        </nav>
    `;
    initializeHeader();
}

function createSimpleFooter() {
    const footerContainer = document.getElementById('footer-container');
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
    }
}

function updateUserMenu() {
    if (!apiClient) return;
    
    apiClient.getProfile().then(user => {
        const loginLink = document.getElementById('login-link');
        const registerLink = document.getElementById('register-link');
        const userMenu = document.getElementById('user-menu');
        const profileLink = document.getElementById('profile-link');
        const adminMenu = document.getElementById('admin-menu');
        
        if (loginLink) loginLink.classList.add('d-none');
        if (registerLink) registerLink.classList.add('d-none');
        if (userMenu) userMenu.classList.remove('d-none');
        if (profileLink) profileLink.textContent = user.fullName || user.email;

        // Show admin menu if role === 'ADMIN'
        if (adminMenu) {
            // user.role might be an array or string depending on backend; normalize
            const roles = Array.isArray(user.role) ? user.role : (typeof user.role === 'string' ? [user.role] : []);
            if (roles.indexOf('ADMIN') !== -1 || roles.indexOf('ROLE_ADMIN') !== -1) {
                adminMenu.classList.remove('d-none');
            } else {
                adminMenu.classList.add('d-none');
            }
        }
    }).catch(() => {
        if (apiClient) apiClient.clearAuth();
    });
}

function logout() {
    if (confirm('Bạn có chắc muốn đăng xuất?')) {
        if (apiClient) {
            apiClient.logout();
        }
    }
}

function showCart() {
    window.location.href = "/cart.html"
}

function handleSearch() {
    const searchInput = document.getElementById('search-input');
    if (searchInput) {
        const query = searchInput.value;
        if (query.trim()) {
            window.location.href = `/index.html?search=${encodeURIComponent(query)}`;
        }
    }
}

function showLoginModal() {
    const modal = document.getElementById('loginModal');
    if (modal) {
        const bsModal = new bootstrap.Modal(modal);
        bsModal.show();
    } else {
        // Fallback if modal not loaded yet
        window.location.href = '/login.html';
    }
}

function showRegisterModal() {
    const modal = document.getElementById('registerModal');
    if (modal) {
        const bsModal = new bootstrap.Modal(modal);
        bsModal.show();
    } else {
        // Fallback if modal not loaded yet
        window.location.href = '/register.html';
    }
}

// Modal Functions
function switchToRegisterModal() {
    // Close login modal
    const loginModal = bootstrap.Modal.getInstance(document.getElementById('loginModal'));
    if (loginModal) {
        loginModal.hide();
    }
    // Open register modal after a short delay
    setTimeout(() => {
        const registerModal = new bootstrap.Modal(document.getElementById('registerModal'));
        registerModal.show();
    }, 300);
}

function switchToLoginModal() {
    // Close register modal
    const registerModal = bootstrap.Modal.getInstance(document.getElementById('registerModal'));
    if (registerModal) {
        registerModal.hide();
    }
    // Open login modal after a short delay
    setTimeout(() => {
        const loginModal = new bootstrap.Modal(document.getElementById('loginModal'));
        loginModal.show();
    }, 300);
}

function switchToPhoneLogin() {
    const phoneSection = document.getElementById('phone-login-section');
    const emailForm = document.getElementById('modal-login-form');
    if (phoneSection) phoneSection.style.display = 'block';
    if (emailForm) emailForm.style.display = 'none';
}

function switchToEmailLogin() {
    const phoneSection = document.getElementById('phone-login-section');
    const emailForm = document.getElementById('modal-login-form');
    if (phoneSection) phoneSection.style.display = 'none';
    if (emailForm) emailForm.style.display = 'block';
}

function togglePasswordVisibility(inputId) {
    const input = document.getElementById(inputId);
    if (!input) return;
    
    let iconId = '';
    if (inputId === 'modal-password') {
        iconId = 'toggle-password-icon';
    } else if (inputId === 'modal-register-password') {
        iconId = 'toggle-register-password-icon';
    } else if (inputId === 'modal-register-confirm-password') {
        iconId = 'toggle-confirm-password-icon';
    }
    
    const icon = document.getElementById(iconId);
    if (!icon) return;
    
    if (input.type === 'password') {
        input.type = 'text';
        icon.classList.remove('bi-eye');
        icon.classList.add('bi-eye-slash');
    } else {
        input.type = 'password';
        icon.classList.remove('bi-eye-slash');
        icon.classList.add('bi-eye');
    }
}

function showForgotPassword() {
    const modal = bootstrap.Modal.getInstance(document.getElementById('loginModal'));
    if (modal) modal.hide();
    window.location.href = '/forgot-password.html';
}

function showFacebookLogin() {
    alert('Tính năng đăng nhập bằng Facebook đang được phát triển');
}

function initializeAuthModal() {
    // Login Form Handler
    const loginForm = document.getElementById('modal-login-form');
    if (loginForm) {
        // Remove existing listener if any by cloning
        const newLoginForm = loginForm.cloneNode(true);
        loginForm.parentNode.replaceChild(newLoginForm, loginForm);
        
        newLoginForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            // Get values directly from the form element (not by ID to avoid stale references)
            const formData = new FormData(e.target);
            const emailInput = e.target.querySelector('#modal-email');
            const passwordInput = e.target.querySelector('#modal-password');
            
            if (!emailInput || !passwordInput) {
                showAlert('Không tìm thấy các trường đăng nhập', 'error');
                return;
            }
            
            const email = emailInput.value.trim();
            const password = passwordInput.value;
            
            if (!email || !password) {
                showAlert('Vui lòng nhập đầy đủ email và mật khẩu', 'error');
                return;
            }
            
            const submitBtn = e.target.querySelector('button[type="submit"]');
            const originalText = submitBtn.innerHTML;
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Đang đăng nhập...';
            
            try {
                await apiClient.login(email, password);
                const user = await apiClient.getProfile();
                apiClient.setUser(user);
                
                const modal = bootstrap.Modal.getInstance(document.getElementById('loginModal'));
                if (modal) modal.hide();
                
                showAlert('Đăng nhập thành công!', 'success');
                setTimeout(() => location.reload(), 500);
            } catch (error) {
                showAlert('Đăng nhập thất bại: ' + error.message, 'error');
            } finally {
                submitBtn.disabled = false;
                submitBtn.innerHTML = originalText;
            }
        });
    }

    // Register Form Handler
    const registerForm = document.getElementById('modal-register-form');
    if (registerForm) {
        // Remove existing listener if any
        const newRegisterForm = registerForm.cloneNode(true);
        registerForm.parentNode.replaceChild(newRegisterForm, registerForm);
        
        newRegisterForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const password = document.getElementById('modal-register-password').value;
            const confirmPassword = document.getElementById('modal-register-confirm-password').value;
            
            if (password !== confirmPassword) {
                showAlert('Mật khẩu xác nhận không khớp', 'error');
                return;
            }
            
            if (password.length < 6) {
                showAlert('Mật khẩu phải có ít nhất 6 ký tự', 'error');
                return;
            }
            
            const registerData = {
                email: document.getElementById('modal-register-email').value,
                password: password,
                fullName: document.getElementById('modal-register-fullname').value,
                phone: document.getElementById('modal-register-phone').value,
                gender: document.getElementById('modal-register-gender').value || null,
                dob: document.getElementById('modal-register-dob').value || null
            };
            
            const submitBtn = e.target.querySelector('button[type="submit"]');
            const originalText = submitBtn.innerHTML;
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Đang đăng ký...';
            
            try {
                await apiClient.register(registerData);
                const modal = bootstrap.Modal.getInstance(document.getElementById('registerModal'));
                if (modal) modal.hide();
                
                showAlert('Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.', 'success');
            } catch (error) {
                showAlert('Đăng ký thất bại: ' + error.message, 'error');
            } finally {
                submitBtn.disabled = false;
                submitBtn.innerHTML = originalText;
            }
        });
    }
}

function loadAuthModal() {
    fetch('/fragments/auth-modal.html')
        .then(response => response.text())
        .then(html => {
            // Check if modal already exists
            if (document.getElementById('loginModal')) {
                // Re-initialize handlers in case modal was already loaded
                initializeAuthModal();
                return;
            }
            document.body.insertAdjacentHTML('beforeend', html);
            // Initialize form handlers after modal is loaded
            initializeAuthModal();
        })
        .catch(error => {
            console.error('Error loading auth modal:', error);
        });
}

// Load header and footer when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    loadHeader();
    loadFooter();
    loadAuthModal();
});


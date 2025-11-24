// admin-common.js - Common functionality for admin pages

let adminUser = null;
let userPermissions = new Set(); // Store user permissions

document.addEventListener('DOMContentLoaded', async function() {
    // Check authentication
    if (!apiClient || !apiClient.isAuthenticated()) {
        window.location.href = '/login.html?redirect=' + encodeURIComponent(window.location.pathname);
        return;
    }

    try {
        adminUser = await apiClient.getProfile();
        if (!adminUser) {
            window.location.href = '/login.html';
            return;
        }
        
        const roles = Array.isArray(adminUser.role) ? adminUser.role : (typeof adminUser.role === 'string' ? [adminUser.role] : []);
        const isAdmin = roles.indexOf('ADMIN') !== -1 || roles.indexOf('ROLE_ADMIN') !== -1 || adminUser.role === 'ADMIN';
        const isEditor = roles.indexOf('EDITOR') !== -1 || roles.indexOf('ROLE_EDITOR') !== -1 || adminUser.role === 'EDITOR';
            
        if (!isAdmin && !isEditor) {
            alert('Không có quyền truy cập');
            window.location.href = '/index.html';
            return;
        }

        // Load permissions của current user
        await loadUserPermissions();
        
        // Ẩn menu items theo permission/role
        hideMenuItemsByPermission();

        // Set user name
        const userNameEl = document.getElementById('admin-user-name');
        if (userNameEl) {
            userNameEl.textContent = adminUser.fullName || adminUser.email;
        }

        // Set active menu item
        setActiveMenuItem();
        
        // Setup sidebar toggle
        setupSidebarToggle();
    } catch (err) {
        console.error('Error initializing admin:', err);
        alert('Lỗi tải dữ liệu: ' + (err.message || err));
    }
});

// Function để load permissions của current user
async function loadUserPermissions() {
    try {
        const permissions = await apiClient.getMyPermissions();
        userPermissions = new Set(permissions.map(p => p.code));
        console.log('Loaded user permissions:', Array.from(userPermissions));
    } catch (err) {
        console.error('Error loading permissions:', err);
        // Fallback: nếu không load được permissions, dùng role-based
        userPermissions = new Set();
    }
}

// Function để ẩn menu items theo permission/role
function hideMenuItemsByPermission() {
    const roles = Array.isArray(adminUser.role) ? adminUser.role : (typeof adminUser.role === 'string' ? [adminUser.role] : []);
    const isAdmin = roles.indexOf('ADMIN') !== -1 || roles.indexOf('ROLE_ADMIN') !== -1 || adminUser.role === 'ADMIN';
    const isEditor = roles.indexOf('EDITOR') !== -1 || roles.indexOf('ROLE_EDITOR') !== -1 || adminUser.role === 'EDITOR';
    
    // Mapping menu items với permission/role requirements
    const menuRules = [
        {
            page: 'dashboard',
            show: isAdmin || isEditor  // Luôn hiển thị cho ADMIN và EDITOR
        },
        {
            page: 'products',
            show: isAdmin || isEditor || userPermissions.has('PRODUCT_VIEW') || 
                  userPermissions.has('PRODUCT_CREATE') || userPermissions.has('PRODUCT_UPDATE') || userPermissions.has('PRODUCT_DELETE')
        },
        {
            page: 'promotions',
            show: isAdmin || userPermissions.has('PROMOTION_CREATE') || 
                  userPermissions.has('PROMOTION_UPDATE') || userPermissions.has('PROMOTION_DELETE')
        },
        {
            page: 'users',
            show: isAdmin  // Chỉ ADMIN
        },
        {
            page: 'permissions',
            show: isAdmin  // Chỉ ADMIN
        },
        {
            page: 'orders',
            show: isAdmin || userPermissions.has('ORDER_VIEW')
        },
        {
            page: 'transactions',
            show: isAdmin || userPermissions.has('TRANSACTION_VIEW') || userPermissions.has('TRANSACTION_SUMMARY')
        },
        {
            page: 'support',
            show: isAdmin || userPermissions.has('SUPPORT_VIEW_PENDING')
        }
    ];
    
    // Ẩn menu items không có quyền
    menuRules.forEach(rule => {
        const menuItem = document.querySelector(`[data-page="${rule.page}"]`);
        if (menuItem) {
            if (!rule.show) {
                menuItem.style.display = 'none';
                console.log(`Hidden menu item: ${rule.page}`);
            } else {
                menuItem.style.display = ''; // Hiển thị
            }
        }
    });
}

function setActiveMenuItem() {
    const currentPath = window.location.pathname;
    const pageName = currentPath.split('/').pop().replace('.html', '');
    
    document.querySelectorAll('.admin-sidebar-item').forEach(item => {
        item.classList.remove('active');
        if (item.dataset.page === pageName) {
            item.classList.add('active');
        }
    });
}

function setupSidebarToggle() {
    const toggle = document.getElementById('sidebar-toggle');
    const sidebar = document.getElementById('admin-sidebar');
    const mobileMenuBtn = document.getElementById('mobile-menu-btn');
    
    if (toggle && sidebar) {
        toggle.addEventListener('click', () => {
            sidebar.classList.toggle('show');
        });
    }
    
    // Mobile menu button (if exists)
    if (mobileMenuBtn && sidebar) {
        mobileMenuBtn.addEventListener('click', () => {
            sidebar.classList.toggle('show');
        });
    }
    
    // Close sidebar when clicking outside on mobile
    document.addEventListener('click', (e) => {
        if (window.innerWidth < 768 && sidebar) {
            if (!sidebar.contains(e.target) && 
                !e.target.closest('.admin-sidebar-toggle') && 
                !e.target.closest('#mobile-menu-btn')) {
                sidebar.classList.remove('show');
            }
        }
    });
    
    // Handle window resize
    window.addEventListener('resize', () => {
        if (window.innerWidth >= 768 && sidebar) {
            sidebar.classList.remove('show');
        }
    });
}

function adminLogout() {
    if (confirm('Bạn có chắc muốn đăng xuất?')) {
        if (apiClient) {
            apiClient.logout();
        }
        window.location.href = '/index.html';
    }
}

// Make adminLogout available globally
window.adminLogout = adminLogout;

// Helper function to set admin page content
function setAdminContent(html) {
    const contentWrapper = document.getElementById('admin-content-wrapper');
    if (contentWrapper) {
        contentWrapper.innerHTML = html;
    }
}


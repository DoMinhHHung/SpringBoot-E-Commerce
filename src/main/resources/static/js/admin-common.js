// admin-common.js - Common functionality for admin pages

let adminUser = null;

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
        
        const isAdmin = Array.isArray(adminUser.role) 
            ? adminUser.role.indexOf('ADMIN') !== -1 || adminUser.role.indexOf('ROLE_ADMIN') !== -1
            : adminUser.role === 'ADMIN' || adminUser.role === 'ROLE_ADMIN';
            
        if (!isAdmin) {
            alert('Không có quyền truy cập');
            window.location.href = '/index.html';
            return;
        }

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


// admin-permissions.js - Quản lý phân quyền

let allPermissions = [];
let allUsers = [];
let currentSelectedRole = null;
let currentSelectedPermission = null;
let currentSelectedUser = null;

document.addEventListener('DOMContentLoaded', async function() {
    if (!apiClient || !apiClient.isAuthenticated()) {
        window.location.href = '/login.html?redirect=' + encodeURIComponent(window.location.pathname);
        return;
    }

    try {
        const user = await apiClient.getProfile();
        if (!user) {
            window.location.href = '/login.html';
            return;
        }
        
        const isAdmin = Array.isArray(user.role) 
            ? user.role.indexOf('ADMIN') !== -1 || user.role.indexOf('ROLE_ADMIN') !== -1
            : user.role === 'ADMIN' || user.role === 'ROLE_ADMIN';
            
        if (!isAdmin) {
            alert('Không có quyền truy cập');
            window.location.href = '/index.html';
            return;
        }

        setupEventListeners();
        await loadInitialData();
    } catch (err) {
        console.error('Error initializing:', err);
        showAlert('Lỗi tải dữ liệu: ' + (err.message || err), 'error');
    }
});

async function loadInitialData() {
    try {
        // Load all permissions
        allPermissions = await apiClient.request('/admin/permissions', { method: 'GET' });
        
        // Load all users (cần tạo API endpoint này hoặc lấy từ users page)
        // Tạm thời để trống, sẽ load khi cần
        
        // Populate permission dropdown
        populatePermissionDropdown();
        populateUserDropdown();
    } catch (error) {
        console.error('Error loading initial data:', error);
        showAlert('Lỗi tải dữ liệu ban đầu', 'error');
    }
}

function populatePermissionDropdown() {
    const select = document.getElementById('select-permission');
    select.innerHTML = '<option value="">-- Chọn quyền hạn --</option>';
    allPermissions.forEach(perm => {
        const option = document.createElement('option');
        option.value = perm.code;
        option.textContent = `${perm.code} - ${perm.name}`;
        select.appendChild(option);
    });
}

async function populateUserDropdown() {
    try {
        allUsers = await apiClient.request('/admin/permissions/users', { method: 'GET' });
        const select = document.getElementById('select-user');
        select.innerHTML = '<option value="">-- Chọn người dùng --</option>';
        allUsers.forEach(user => {
            const option = document.createElement('option');
            option.value = user.id;
            option.textContent = `${user.fullName || '-'} (${user.email})`;
            select.appendChild(option);
        });
    } catch (error) {
        console.error('Error loading users:', error);
        showAlert('Lỗi tải danh sách người dùng', 'error');
    }
}

function setupEventListeners() {
    // Tab 1: Roles
    document.getElementById('select-role').addEventListener('change', async (e) => {
        currentSelectedRole = e.target.value;
        document.getElementById('btn-add-permission-to-role').disabled = !currentSelectedRole;
        if (currentSelectedRole) {
            await loadRoleData(currentSelectedRole);
        } else {
            clearRoleData();
        }
    });

    document.getElementById('btn-add-permission-to-role').addEventListener('click', () => {
        if (currentSelectedRole) {
            openAddPermissionModal(currentSelectedRole);
        }
    });

    // Tab 2: Permissions
    document.getElementById('select-permission').addEventListener('change', async (e) => {
        currentSelectedPermission = e.target.value;
        document.getElementById('btn-add-role-to-permission').disabled = !currentSelectedPermission;
        if (currentSelectedPermission) {
            await loadPermissionData(currentSelectedPermission);
        } else {
            clearPermissionData();
        }
    });

    document.getElementById('btn-add-role-to-permission').addEventListener('click', () => {
        if (currentSelectedPermission) {
            openAddRoleToPermissionModal(currentSelectedPermission);
        }
    });

    // Tab 3: Users
    document.getElementById('select-user').addEventListener('change', async (e) => {
        currentSelectedUser = e.target.value;
        document.getElementById('btn-add-role-to-user').disabled = !currentSelectedUser;
        if (currentSelectedUser) {
            await loadUserData(currentSelectedUser);
        } else {
            clearUserData();
        }
    });

    document.getElementById('btn-add-role-to-user').addEventListener('click', () => {
        if (currentSelectedUser) {
            openAddRoleToUserModal(currentSelectedUser);
        }
    });

    // Modal confirm buttons
    document.getElementById('btn-confirm-add-permission').addEventListener('click', async () => {
        await confirmAddPermission();
    });

    document.getElementById('btn-confirm-add-role-user').addEventListener('click', async () => {
        await confirmAddRoleToUser();
    });

    document.getElementById('btn-confirm-add-role-permission').addEventListener('click', async () => {
        await confirmAddRoleToPermission();
    });
}

// ========== TAB 1: ROLES ==========

async function loadRoleData(roleName) {
    try {
        // Load users with this role
        const users = await apiClient.request(`/admin/permissions/roles/${roleName}/users`, { method: 'GET' });
        renderRoleUsers(users);

        // Load permissions for this role
        const permissions = await apiClient.request(`/admin/permissions/roles/${roleName}/permissions`, { method: 'GET' });
        renderRolePermissions(permissions, roleName);
    } catch (error) {
        console.error('Error loading role data:', error);
        showAlert('Lỗi tải dữ liệu vai trò', 'error');
    }
}

function renderRoleUsers(users) {
    const container = document.getElementById('role-users-list');
    
    if (!users || users.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
                </svg>
                <p>No data</p>
            </div>
        `;
        return;
    }

    const table = `
        <table class="table table-hover">
            <thead>
                <tr>
                    <th>Tên</th>
                    <th>Email</th>
                    <th>Thao tác</th>
                </tr>
            </thead>
            <tbody>
                ${users.map(user => `
                    <tr>
                        <td>${user.fullName || '-'}</td>
                        <td>${user.email || '-'}</td>
                        <td>
                            <button class="btn btn-sm btn-outline-primary" onclick="viewUserDetails(${user.id})">
                                Xem
                            </button>
                        </td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
    container.innerHTML = table;
}

function renderRolePermissions(permissions, roleName) {
    const container = document.getElementById('role-permissions-list');
    
    if (!permissions || permissions.size === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
                </svg>
                <p>No data</p>
            </div>
        `;
        return;
    }

    const permissionsArray = Array.from(permissions);
    const table = `
        <table class="table table-hover">
            <thead>
                <tr>
                    <th>Mã</th>
                    <th>Mô tả</th>
                    <th>Thao tác</th>
                </tr>
            </thead>
            <tbody>
                ${permissionsArray.map(perm => `
                    <tr>
                        <td><code>${perm.code}</code></td>
                        <td>${perm.description || perm.name || '-'}</td>
                        <td>
                            <button class="btn btn-sm btn-outline-danger" onclick="removePermissionFromRole('${roleName}', '${perm.code}')">
                                Xóa
                            </button>
                        </td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
    container.innerHTML = table;
}

function clearRoleData() {
    document.getElementById('role-users-list').innerHTML = `
        <div class="empty-state">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
            </svg>
            <p>No data</p>
        </div>
    `;
    document.getElementById('role-permissions-list').innerHTML = `
        <div class="empty-state">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
            </svg>
            <p>No data</p>
        </div>
    `;
}

function openAddPermissionModal(roleName) {
    const modal = new bootstrap.Modal(document.getElementById('modal-add-permission'));
    const select = document.getElementById('modal-permission-select');
    
    // Get current permissions for this role
    apiClient.request(`/admin/permissions/roles/${roleName}/permissions`, { method: 'GET' })
        .then(currentPermissions => {
            const currentCodes = Array.from(currentPermissions).map(p => p.code);
            
            select.innerHTML = '<option value="">-- Chọn quyền hạn --</option>';
            allPermissions.forEach(perm => {
                if (!currentCodes.includes(perm.code)) {
                    const option = document.createElement('option');
                    option.value = perm.code;
                    option.textContent = `${perm.code} - ${perm.name}`;
                    select.appendChild(option);
                }
            });
            
            modal.show();
        })
        .catch(error => {
            console.error('Error loading permissions:', error);
            showAlert('Lỗi tải danh sách quyền hạn', 'error');
        });
}

async function confirmAddPermission() {
    const permissionCode = document.getElementById('modal-permission-select').value;
    if (!permissionCode || !currentSelectedRole) {
        showAlert('Vui lòng chọn quyền hạn', 'error');
        return;
    }

    try {
        await apiClient.request(`/admin/permissions/roles/${currentSelectedRole}/permissions`, {
            method: 'POST',
            body: JSON.stringify({ permissionCode })
        });
        
        bootstrap.Modal.getInstance(document.getElementById('modal-add-permission')).hide();
        showAlert('Thêm quyền hạn thành công', 'success');
        await loadRoleData(currentSelectedRole);
    } catch (error) {
        console.error('Error adding permission:', error);
        showAlert('Lỗi thêm quyền hạn: ' + (error.message || error), 'error');
    }
}

async function removePermissionFromRole(roleName, permissionCode) {
    if (!confirm(`Bạn có chắc muốn xóa quyền ${permissionCode} khỏi vai trò ${roleName}?`)) {
        return;
    }

    try {
        await apiClient.request(`/admin/permissions/roles/${roleName}/permissions/${permissionCode}`, {
            method: 'DELETE'
        });
        showAlert('Xóa quyền hạn thành công', 'success');
        await loadRoleData(roleName);
    } catch (error) {
        console.error('Error removing permission:', error);
        showAlert('Lỗi xóa quyền hạn: ' + (error.message || error), 'error');
    }
}

// ========== TAB 2: PERMISSIONS ==========

async function loadPermissionData(permissionCode) {
    try {
        // Load roles with this permission
        const roles = await apiClient.request(`/admin/permissions/permissions/${permissionCode}/roles`, { method: 'GET' });
        renderPermissionRoles(roles, permissionCode);

        // Load users with this permission
        const users = await apiClient.request(`/admin/permissions/permissions/${permissionCode}/users`, { method: 'GET' });
        renderPermissionUsers(users);
    } catch (error) {
        console.error('Error loading permission data:', error);
        showAlert('Lỗi tải dữ liệu quyền hạn', 'error');
    }
}

function renderPermissionRoles(roles, permissionCode) {
    const container = document.getElementById('permission-roles-list');
    
    if (!roles || roles.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
                </svg>
                <p>No data</p>
            </div>
        `;
        return;
    }

    const table = `
        <table class="table table-hover">
            <thead>
                <tr>
                    <th>Tên vai trò</th>
                    <th>Mã vai trò</th>
                    <th>Thao tác</th>
                </tr>
            </thead>
            <tbody>
                ${roles.map(role => `
                    <tr>
                        <td>${role.roleName}</td>
                        <td><code>${role.roleCode}</code></td>
                        <td>
                            <button class="btn btn-sm btn-outline-danger" onclick="removeRoleFromPermission('${permissionCode}', '${role.roleName}')">
                                Xóa
                            </button>
                        </td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
    container.innerHTML = table;
}

function renderPermissionUsers(users) {
    const container = document.getElementById('permission-users-list');
    
    if (!users || users.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
                </svg>
                <p>No data</p>
            </div>
        `;
        return;
    }

    const table = `
        <table class="table table-hover">
            <thead>
                <tr>
                    <th>Tên</th>
                    <th>Email</th>
                </tr>
            </thead>
            <tbody>
                ${users.map(user => `
                    <tr>
                        <td>${user.fullName || '-'}</td>
                        <td>${user.email || '-'}</td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
    container.innerHTML = table;
}

function clearPermissionData() {
    document.getElementById('permission-roles-list').innerHTML = `
        <div class="empty-state">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
            </svg>
            <p>No data</p>
        </div>
    `;
    document.getElementById('permission-users-list').innerHTML = `
        <div class="empty-state">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
            </svg>
            <p>No data</p>
        </div>
    `;
}

function openAddRoleToPermissionModal(permissionCode) {
    const modal = new bootstrap.Modal(document.getElementById('modal-add-role-permission'));
    const select = document.getElementById('modal-role-permission-select');
    
    // Get current roles for this permission
    apiClient.request(`/admin/permissions/permissions/${permissionCode}/roles`, { method: 'GET' })
        .then(currentRoles => {
            const currentRoleNames = currentRoles.map(r => r.roleName);
            
            select.innerHTML = '<option value="">-- Chọn vai trò --</option>';
            ['ADMIN', 'USER', 'EDITOR'].forEach(role => {
                if (!currentRoleNames.includes(role)) {
                    const option = document.createElement('option');
                    option.value = role;
                    option.textContent = role;
                    select.appendChild(option);
                }
            });
            
            modal.show();
        })
        .catch(error => {
            console.error('Error loading roles:', error);
            showAlert('Lỗi tải danh sách vai trò', 'error');
        });
}

async function confirmAddRoleToPermission() {
    const roleName = document.getElementById('modal-role-permission-select').value;
    if (!roleName || !currentSelectedPermission) {
        showAlert('Vui lòng chọn vai trò', 'error');
        return;
    }

    try {
        await apiClient.request(`/admin/permissions/roles/${roleName}/permissions`, {
            method: 'POST',
            body: JSON.stringify({ permissionCode: currentSelectedPermission })
        });
        
        bootstrap.Modal.getInstance(document.getElementById('modal-add-role-permission')).hide();
        showAlert('Thêm vai trò thành công', 'success');
        await loadPermissionData(currentSelectedPermission);
    } catch (error) {
        console.error('Error adding role:', error);
        showAlert('Lỗi thêm vai trò: ' + (error.message || error), 'error');
    }
}

async function removeRoleFromPermission(permissionCode, roleName) {
    if (!confirm(`Bạn có chắc muốn xóa vai trò ${roleName} khỏi quyền ${permissionCode}?`)) {
        return;
    }

    try {
        await apiClient.request(`/admin/permissions/roles/${roleName}/permissions/${permissionCode}`, {
            method: 'DELETE'
        });
        showAlert('Xóa vai trò thành công', 'success');
        await loadPermissionData(permissionCode);
    } catch (error) {
        console.error('Error removing role:', error);
        showAlert('Lỗi xóa vai trò: ' + (error.message || error), 'error');
    }
}

// ========== TAB 3: USERS ==========

async function loadUserData(userId) {
    try {
        // Load roles for this user
        const roles = await apiClient.request(`/admin/permissions/users/${userId}/roles`, { method: 'GET' });
        renderUserRoles(roles, userId);

        // Load permissions for this user
        const permissions = await apiClient.request(`/admin/permissions/users/${userId}/permissions`, { method: 'GET' });
        renderUserPermissions(permissions);
    } catch (error) {
        console.error('Error loading user data:', error);
        showAlert('Lỗi tải dữ liệu người dùng', 'error');
    }
}

function renderUserRoles(roles, userId) {
    const container = document.getElementById('user-roles-list');
    
    if (!roles || roles.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
                </svg>
                <p>No data</p>
            </div>
        `;
        return;
    }

    const table = `
        <table class="table table-hover">
            <thead>
                <tr>
                    <th>Tên</th>
                    <th>Mã</th>
                    <th>Thao tác</th>
                </tr>
            </thead>
            <tbody>
                ${roles.map(role => `
                    <tr>
                        <td>${role.roleName}</td>
                        <td><code>${role.roleCode}</code></td>
                        <td>
                            <button class="btn btn-sm btn-outline-danger" onclick="removeRoleFromUser(${userId}, '${role.roleName}')">
                                Xóa
                            </button>
                        </td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
    container.innerHTML = table;
}

function renderUserPermissions(permissions) {
    const container = document.getElementById('user-permissions-list');
    
    if (!permissions || permissions.size === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
                </svg>
                <p>No data</p>
            </div>
        `;
        return;
    }

    const permissionsArray = Array.from(permissions);
    const table = `
        <table class="table table-hover">
            <thead>
                <tr>
                    <th>Mã</th>
                    <th>Mô tả</th>
                </tr>
            </thead>
            <tbody>
                ${permissionsArray.map(perm => `
                    <tr>
                        <td><code>${perm.code}</code></td>
                        <td>${perm.description || perm.name || '-'}</td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
    container.innerHTML = table;
}

function clearUserData() {
    document.getElementById('user-roles-list').innerHTML = `
        <div class="empty-state">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
            </svg>
            <p>No data</p>
        </div>
    `;
    document.getElementById('user-permissions-list').innerHTML = `
        <div class="empty-state">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
            </svg>
            <p>No data</p>
        </div>
    `;
}

function openAddRoleToUserModal(userId) {
    const modal = new bootstrap.Modal(document.getElementById('modal-add-role-user'));
    const select = document.getElementById('modal-role-select');
    
    // Get current roles for this user
    apiClient.request(`/admin/permissions/users/${userId}/roles`, { method: 'GET' })
        .then(currentRoles => {
            const currentRoleNames = currentRoles.map(r => r.roleName);
            
            select.innerHTML = '<option value="">-- Chọn vai trò --</option>';
            ['ADMIN', 'USER', 'EDITOR'].forEach(role => {
                if (!currentRoleNames.includes(role)) {
                    const option = document.createElement('option');
                    option.value = role;
                    option.textContent = role;
                    select.appendChild(option);
                }
            });
            
            modal.show();
        })
        .catch(error => {
            console.error('Error loading roles:', error);
            showAlert('Lỗi tải danh sách vai trò', 'error');
        });
}

async function confirmAddRoleToUser() {
    const roleName = document.getElementById('modal-role-select').value;
    if (!roleName || !currentSelectedUser) {
        showAlert('Vui lòng chọn vai trò', 'error');
        return;
    }

    try {
        await apiClient.request(`/admin/permissions/users/${currentSelectedUser}/role`, {
            method: 'PUT',
            body: JSON.stringify({ roleName })
        });
        
        bootstrap.Modal.getInstance(document.getElementById('modal-add-role-user')).hide();
        showAlert('Thêm vai trò thành công', 'success');
        await loadUserData(currentSelectedUser);
    } catch (error) {
        console.error('Error adding role:', error);
        showAlert('Lỗi thêm vai trò: ' + (error.message || error), 'error');
    }
}

async function removeRoleFromUser(userId, roleName) {
    if (!confirm(`Bạn có chắc muốn xóa vai trò ${roleName} khỏi người dùng này?`)) {
        return;
    }

    try {
        // Assign USER role as default
        await apiClient.request(`/admin/permissions/users/${userId}/role`, {
            method: 'PUT',
            body: JSON.stringify({ roleName: 'USER' })
        });
        showAlert('Xóa vai trò thành công', 'success');
        await loadUserData(userId);
    } catch (error) {
        console.error('Error removing role:', error);
        showAlert('Lỗi xóa vai trò: ' + (error.message || error), 'error');
    }
}

function viewUserDetails(userId) {
    // Switch to users tab and select this user
    document.getElementById('users-tab').click();
    setTimeout(() => {
        document.getElementById('select-user').value = userId;
        document.getElementById('select-user').dispatchEvent(new Event('change'));
    }, 100);
}

function showAlert(message, type) {
    // Simple alert - có thể thay bằng toast notification
    if (type === 'success') {
        alert('✓ ' + message);
    } else {
        alert('✗ ' + message);
    }
}


// API Configuration
const API_BASE_URL = window.location.origin + '/api';
const AUTH_TOKEN_KEY = 'auth_token';
const REFRESH_TOKEN_KEY = 'refresh_token';
const USER_KEY = 'user_info';

class ApiClient {
    constructor() {
        this.baseURL = API_BASE_URL;
    }

    getAuthToken() {
        return localStorage.getItem(AUTH_TOKEN_KEY);
    }

    setAuthToken(token) {
        localStorage.setItem(AUTH_TOKEN_KEY, token);
    }

    getRefreshToken() {
        return localStorage.getItem(REFRESH_TOKEN_KEY);
    }

    setRefreshToken(token) {
        localStorage.setItem(REFRESH_TOKEN_KEY, token);
    }

    getUser() {
        const userStr = localStorage.getItem(USER_KEY);
        return userStr ? JSON.parse(userStr) : null;
    }

    setUser(user) {
        localStorage.setItem(USER_KEY, JSON.stringify(user));
    }

    clearAuth() {
        localStorage.removeItem(AUTH_TOKEN_KEY);
        localStorage.removeItem(REFRESH_TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
        console.warn('apiClient.clearAuth() called - auth tokens removed from localStorage');
    }

    getHeaders(includeAuth = true) {
        const headers = {
            'Content-Type': 'application/json'
        };
        
        if (includeAuth) {
            const token = this.getAuthToken();
            if (token) {
                headers['Authorization'] = `Bearer ${token}`;
            }
        }
        
        return headers;
    }

    async handleResponse(response) {
        if (!response.ok) {
            try {
                if (response.status === 403) {
                    try { this.clearAuth(); } catch (e) {}
                    window.location.href = '/banned.html';
                    return null;
                }
            } catch (e) {}

            let errorObj = { message: response.statusText || 'An error occurred' };
            try {
                const text = await response.text();
                if (text) {
                    try { errorObj = JSON.parse(text); } catch (e) { errorObj.message = text; }
                }
            } catch (e) {
            }

            try {
                if (response.status === 403) {
                    const msg = (errorObj && errorObj.message) ? errorObj.message.toString().toLowerCase() : '';
                    if (msg.includes('bị chặn') || msg.includes('blocked') || msg.includes('banned')) {
                        this.clearAuth();
                        window.location.href = '/banned.html';
                        return null;
                    }
                }
            } catch (e) {}

            const err = new Error(errorObj.message || `HTTP error! status: ${response.status}`);
            err.status = response.status;
            throw err;
        }

        if (response.status === 204) return null;
        const txt = await response.text();
        if (!txt) return null;
        try {
            return JSON.parse(txt);
        } catch (e) {
            return txt;
        }
    }

    async request(endpoint, options = {}) {
        const url = `${this.baseURL}${endpoint}`;
        const config = {
            headers: this.getHeaders(!options.skipAuth),
            credentials: 'same-origin',
            ...options
        };

        try {
            const response = await fetch(url, config);
            return await this.handleResponse(response);
        } catch (error) {
            if (error && error.status === 401 && options && options.noAuthRedirect) {
                console.warn('apiClient.request: received 401 for', url, 'noAuthRedirect=true');
                throw error;
            }

            if (error && error.status === 401) {
                console.warn('apiClient.request: received 401 for', url, 'attempting refresh if possible');
                 const refreshToken = this.getRefreshToken();
                 if (refreshToken) {
                     try {
                         const refreshResponse = await fetch(`${this.baseURL}/auth/refresh-token?token=${refreshToken}`, {
                             method: 'POST',
                             headers: this.getHeaders(false)
                         });
                         const data = await this.handleResponse(refreshResponse);
                         this.setAuthToken(data.accessToken);
                         this.setRefreshToken(data.refreshToken);

                        config.headers = this.getHeaders(true);
                        const retryResponse = await fetch(url, config);
                        return await this.handleResponse(retryResponse);
                     } catch (refreshError) {
                        this.clearAuth();
                        throw refreshError;
                     }
                 } else {
                    this.clearAuth();
                    throw error;
                 }
             }
             console.warn('apiClient.request: throwing error for', url, error);
             throw error;
         }
     }

    async login(email, password) {
        const response = await this.request('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email, password })
        });
        this.setAuthToken(response.accessToken);
        this.setRefreshToken(response.refreshToken);
        return response;
    }

    async register(userData) {
        return this.request('/auth', {
            method: 'POST',
            body: JSON.stringify(userData)
        });
    }

    async verifyAccount(token) {
        return this.request(`/auth/verify?token=${token}`, {
            method: 'GET',
            skipAuth: true
        });
    }

    async forgotPassword(email) {
        return this.request('/auth/forgot-password', {
            method: 'POST',
            body: JSON.stringify({ email }),
            skipAuth: true
        });
    }

    async resetPassword(email, otp, newPassword) {
        return this.request('/auth/reset-password', {
            method: 'POST',
            body: JSON.stringify({ email, otp, newPassword }),
            skipAuth: true
        });
    }

    // Product APIs
    async getAllProducts() {
        return this.request('/products', {
            method: 'GET',
            skipAuth: true
        });
    }

    async getProductById(id) {
        return this.request(`/products/${id}`, {
            method: 'GET',
            skipAuth: true
        });
    }

    async getProductsByType(type) {
        return this.request(`/products/type/${type}`, {
            method: 'GET',
            skipAuth: true
        });
    }

    async getHotSaleProducts(limit = 6) {
        return this.request(`/products/hot-sale?limit=${limit}`, {
            method: 'GET',
            skipAuth: true
        });
    }

    // Admin/Product modification APIs (use FormData because backend expects @ModelAttribute with files)
    async adminCreateProduct(formData) {
        const url = `${this.baseURL}/products`;
        const token = this.getAuthToken();
        const headers = {};
        if (token) headers['Authorization'] = `Bearer ${token}`;

        const response = await fetch(url, {
            method: 'POST',
            headers: headers,
            credentials: 'same-origin',
            body: formData
        });
        return this.handleResponse(response);
    }

    async adminUpdateProduct(id, formData) {
        const url = `${this.baseURL}/products/${id}`;
        const token = this.getAuthToken();
        const headers = {};
        if (token) headers['Authorization'] = `Bearer ${token}`;

        const response = await fetch(url, {
            method: 'PUT',
            headers: headers,
            credentials: 'same-origin',
            body: formData
        });
        return this.handleResponse(response);
    }

    // Promotion APIs
    async getActivePromotions() {
        return this.request('/promotions/active', {
            method: 'GET',
            skipAuth: true
        });
    }

    async getAllPromotions() {
        return this.request('/promotions', {
            method: 'GET',
            skipAuth: true
        });
    }

    // Admin Promotion APIs
    async adminCreatePromotion(payload) {
        return this.request('/promotions', {
            method: 'POST',
            body: JSON.stringify(payload)
        });
    }

    async adminUpdatePromotion(id, payload) {
        return this.request(`/promotions/${id}`, {
            method: 'PUT',
            body: JSON.stringify(payload)
        });
    }

    async adminDeletePromotion(id) {
        return this.request(`/promotions/${id}`, {
            method: 'DELETE'
        });
    }

    async getProductsByPromotion(promotionId) {
        return this.request(`/promotions/${promotionId}/products`, {
            method: 'GET',
            skipAuth: true
        });
    }

    // Admin assign products to promotion
    async adminAssignProductsToPromotion(promotionId, productIds) {
        return this.request(`/promotions/${promotionId}/products`, {
            method: 'POST',
            body: JSON.stringify(productIds)
        });
    }

    async adminAssignAllProductsToPromotion(promotionId) {
        return this.request(`/promotions/${promotionId}/products/assign-all`, {
            method: 'POST'
        });
    }

    // User APIs
    async getProfile() {
        const resp = await this.request('/users/profile', {
            method: 'GET',
            noAuthRedirect: true
        });
        // if server returned HTML (e.g., login page) or a string, treat as unauthenticated or banned
        try {
            if (!resp) return null;
            if (typeof resp === 'string') {
                const s = resp.trim().toLowerCase();
                if (s.startsWith('<!doctype') || s.startsWith('<html')) {
                    // clear auth and redirect to login
                    try { this.clearAuth(); } catch (e) {}
                    window.location.href = '/login.html';
                    return null;
                }
            }
        } catch (e) {}
        return resp;
    }

    async updateProfile(userData) {
        return this.request('/users/profile', {
            method: 'PUT',
            body: JSON.stringify(userData)
        });
    }

    async changePassword(oldPassword, newPassword) {
        return this.request('/users/change-password', {
            method: 'PUT',
            body: JSON.stringify({ oldPassword, newPassword })
        });
    }

    logout() {
        this.clearAuth();
        window.location.href = '/index.html';
    }

    isAuthenticated() {
        return !!this.getAuthToken();
    }

    // Payment APIs
    async createPayment(paymentRequest) {
        return this.request('/payments/create', {
            method: 'POST',
            body: JSON.stringify(paymentRequest)
        });
    }

    async getPaymentStatus(orderCode) {
        return this.request(`/payments/status/${orderCode}`, {
            method: 'GET'
        });
    }
}

// Global API client instance
const apiClient = new ApiClient();

// Utility functions
function showAlert(message, type = 'info') {
    // Get or create alert container
    let alertContainer = document.getElementById('alert-container');
    if (!alertContainer) {
        alertContainer = document.createElement('div');
        alertContainer.id = 'alert-container';
        document.body.appendChild(alertContainer);
    }
    
    // Create alert element
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type === 'error' ? 'danger' : type} alert-custom alert-dismissible fade show`;
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;
    
    // Append to alert container (shows from top to bottom)
    alertContainer.appendChild(alertDiv);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        if (alertDiv.parentNode) {
            alertDiv.classList.remove('show');
            alertDiv.classList.add('fade');
            setTimeout(() => {
                if (alertDiv.parentNode) {
                    alertDiv.remove();
                }
            }, 150); // Wait for fade animation
        }
    }, 5000);
}

function formatPrice(price) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(price);
}

function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN');
}

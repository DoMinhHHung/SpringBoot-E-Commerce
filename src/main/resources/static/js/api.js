// API Configuration
const API_BASE_URL = window.location.origin + '/api';
const AUTH_TOKEN_KEY = 'auth_token';
const REFRESH_TOKEN_KEY = 'refresh_token';
const USER_KEY = 'user_info';

// API Helper Functions
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
            const error = await response.json().catch(() => ({ message: 'An error occurred' }));
            throw new Error(error.message || `HTTP error! status: ${response.status}`);
        }
        return response.json();
    }

    async request(endpoint, options = {}) {
        const url = `${this.baseURL}${endpoint}`;
        const config = {
            headers: this.getHeaders(!options.skipAuth),
            ...options
        };

        try {
            const response = await fetch(url, config);
            return await this.handleResponse(response);
        } catch (error) {
            if (error.message.includes('401')) {
                // Try to refresh token
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
                        
                        // Retry original request
                        config.headers = this.getHeaders(true);
                        const retryResponse = await fetch(url, config);
                        return await this.handleResponse(retryResponse);
                    } catch (refreshError) {
                        this.clearAuth();
                        window.location.href = '/login.html';
                        throw refreshError;
                    }
                } else {
                    this.clearAuth();
                    window.location.href = '/login.html';
                    throw error;
                }
            }
            throw error;
        }
    }

    // Auth APIs
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

    async getProductsByPromotion(promotionId) {
        return this.request(`/promotions/${promotionId}/products`, {
            method: 'GET',
            skipAuth: true
        });
    }

    // User APIs
    async getProfile() {
        return this.request('/users/profile', {
            method: 'GET'
        });
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


// Chat Widget with STOMP over SockJS
class ChatWidget {
    constructor() {
        this.stompClient = null;
        this.sessionId = this.generateSessionId();
        this.connected = false;
        this.productId = null;
    }

    generateSessionId() {
        // Use crypto.randomUUID() if available, otherwise fall back to timestamp-based ID
        if (typeof crypto !== 'undefined' && crypto.randomUUID) {
            return 'session-' + crypto.randomUUID();
        }
        // Fallback for older browsers: use timestamp and random values from crypto.getRandomValues
        if (typeof crypto !== 'undefined' && crypto.getRandomValues) {
            const array = new Uint32Array(2);
            crypto.getRandomValues(array);
            return 'session-' + array[0].toString(36) + array[1].toString(36) + '-' + Date.now();
        }
        return 'session-' + Date.now() + '-' + performance.now().toString(36).replace('.', '');
    }

    init(productId) {
        this.productId = productId;
        this.createWidget();
        this.attachEventListeners();
    }

    createWidget() {
        const widgetHTML = `
            <div id="chat-widget" class="chat-widget">
                <div id="chat-widget-button" class="chat-widget-button">
                    <i class="bi bi-chat-dots"></i>
                    <span>Tư vấn</span>
                </div>
                <div id="chat-widget-container" class="chat-widget-container" style="display: none;">
                    <div class="chat-widget-header">
                        <div>
                            <i class="bi bi-robot"></i>
                            <span>Trợ lý sản phẩm</span>
                        </div>
                        <button id="chat-widget-close" class="chat-widget-close">
                            <i class="bi bi-x"></i>
                        </button>
                    </div>
                    <div id="chat-active-filters" class="chat-active-filters" style="display:none;padding:8px 12px;background:#f8f9fa;border-bottom:1px solid #e9ecef;">
                        <!-- Active filters will appear here -->
                    </div>
                    <div id="chat-search-spinner" class="chat-search-spinner" style="display:none;padding:8px 12px;border-bottom:1px solid #e9ecef;color:#6c757d;">
                        <i class="bi bi-arrow-repeat" style="margin-right:6px;" aria-hidden="true"></i>
                        Đang tìm...
                    </div>
                    <div id="chat-widget-messages" class="chat-widget-messages">
                        <div class="chat-message bot-message">
                            <div class="message-content">
                                Xin chào! Tôi là trợ lý ảo. Tôi có thể giúp bạn tìm sản phẩm phù hợp. Hãy nhập từ khóa bạn đang tìm kiếm.
                            </div>
                        </div>
                    </div>
                    <div class="chat-widget-input">
                        <input type="text" id="chat-input" placeholder="Nhập tin nhắn..." disabled>
                        <button id="chat-send" class="chat-send-btn" disabled>
                            <i class="bi bi-send"></i>
                        </button>
                    </div>
                    <div id="chat-connection-status" class="chat-connection-status">
                        <span class="status-indicator"></span>
                        <span class="status-text">Đang kết nối...</span>
                    </div>
                </div>
            </div>
        `;
        document.body.insertAdjacentHTML('beforeend', widgetHTML);
    }

    attachEventListeners() {
        const button = document.getElementById('chat-widget-button');
        const closeBtn = document.getElementById('chat-widget-close');
        const sendBtn = document.getElementById('chat-send');
        const input = document.getElementById('chat-input');

        button.addEventListener('click', () => this.toggleWidget());
        closeBtn.addEventListener('click', () => this.toggleWidget());
        sendBtn.addEventListener('click', () => this.sendMessage());
        input.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                this.sendMessage();
            }
        });
    }

    toggleWidget() {
        const container = document.getElementById('chat-widget-container');
        const button = document.getElementById('chat-widget-button');
        
        if (container.style.display === 'none') {
            container.style.display = 'flex';
            button.style.display = 'none';
            if (!this.connected) {
                this.connect();
            }
        } else {
            container.style.display = 'none';
            button.style.display = 'flex';
        }
    }

    connect() {
        const socket = new SockJS('/ws');
        this.stompClient = Stomp.over(socket);
        
        // Disable debug logging
        this.stompClient.debug = null;

        this.stompClient.connect({}, 
            () => {
                console.log('Connected to WebSocket');
                this.connected = true;
                this.updateConnectionStatus(true);
                
                // Subscribe to replies for this session
                this.stompClient.subscribe('/topic/replies.' + this.sessionId, (message) => {
                    const response = JSON.parse(message.body);
                    this.displayBotMessage(response);
                });

                // Enable input after connection
                document.getElementById('chat-input').disabled = false;
                document.getElementById('chat-send').disabled = false;

                try {
                    this.stompClient.send('/app/chat', {}, JSON.stringify({
                        sessionId: this.sessionId,
                        text: ''
                    }));
                } catch (err) {
                    console.warn('Failed to request initial greeting:', err);
                }
            },
            (error) => {
                console.error('WebSocket connection error:', error);
                this.connected = false;
                this.updateConnectionStatus(false);
                this.displayErrorMessage('Không thể kết nối. Vui lòng thử lại sau.');
            }
        );
    }

    updateConnectionStatus(connected) {
        const statusText = document.querySelector('.status-text');
        const statusIndicator = document.querySelector('.status-indicator');
        
        if (connected) {
            statusText.textContent = 'Đã kết nối';
            statusIndicator.className = 'status-indicator connected';
        } else {
            statusText.textContent = 'Mất kết nối';
            statusIndicator.className = 'status-indicator disconnected';
        }
    }

    sendMessage() {
        const input = document.getElementById('chat-input');
        const text = input.value.trim();
        
        if (!text || !this.connected) {
            return;
        }

        // Display user message
        this.displayUserMessage(text);
        
        // Show spinner and active filters
        this.showSearchSpinner(true);
        this.setActiveFiltersFromQuery(text);

        // Send message via STOMP
        const chatMessage = {
            sessionId: this.sessionId,
            text: text,
            productId: this.productId
        };

        this.stompClient.send('/app/chat', {}, JSON.stringify(chatMessage));
        
        // Clear input
        input.value = '';
    }

    // send suggestion text as if the user typed it
    sendSuggestion(text) {
        if (!this.connected) return;
        this.displayUserMessage(text);
        const chatMessage = {
            sessionId: this.sessionId,
            text: text,
            productId: this.productId
        };
        this.stompClient.send('/app/chat', {}, JSON.stringify(chatMessage));
    }

    displayUserMessage(text) {
        const messagesDiv = document.getElementById('chat-widget-messages');
        const messageHTML = `
            <div class="chat-message user-message">
                <div class="message-content">${this.escapeHtml(text)}</div>
            </div>
        `;
        messagesDiv.insertAdjacentHTML('beforeend', messageHTML);
        this.scrollToBottom();
    }

    displayBotMessage(response) {
        this.showSearchSpinner(false);
        const messagesDiv = document.getElementById('chat-widget-messages');
        
        let productsHTML = '';
        if (response.products && response.products.length > 0) {
            productsHTML = '<div class="product-suggestions">';
            response.products.forEach(product => {
                const price = this.formatPrice(product.price);
                productsHTML += `
                    <div class="product-suggestion">
                        <img src="${this.escapeHtml(product.imageUrl)}" 
                             alt="${this.escapeHtml(product.name)}"
                             onerror="this.src='https://via.placeholder.com/80x80'">
                        <div class="product-info">
                            <div class="product-name">${this.escapeHtml(product.name)}</div>
                            <div class="product-price">${price}</div>
                            <a href="/product-detail.html?id=${product.id}" target="_blank" class="view-product">
                                Xem chi tiết
                            </a>
                        </div>
                    </div>
                `;
            });
            productsHTML += '</div>';
        }

        // render suggestions as clickable buttons
        let suggestionsHTML = '';
        if (response.suggestions && response.suggestions.length > 0) {
            suggestionsHTML = '<div class="chat-suggestions">';
            response.suggestions.forEach(s => {
                // Support both legacy string suggestions and new {label, query} objects
                let label = '';
                let query = '';
                if (s && typeof s === 'object' && s.label !== undefined) {
                    label = s.label;
                    query = s.query || '';
                } else {
                    label = String(s);
                    query = String(s);
                }
                const safeLabel = this.escapeHtml(label);
                const safeQuery = this.escapeHtml(query);
                suggestionsHTML += `<button class="suggestion-btn" data-query="${safeQuery}" data-label="${safeLabel}">${safeLabel}</button>`;
            });
            suggestionsHTML += '</div>';
        }

        const messageHTML = `
            <div class="chat-message bot-message">
                <div class="message-content">
                    ${this.escapeHtml(response.text)}
                    ${productsHTML}
                    ${suggestionsHTML}
                </div>
            </div>
        `;
        messagesDiv.insertAdjacentHTML('beforeend', messageHTML);

        // Attach click handlers to suggestion buttons inside the newly inserted message
        const lastMsg = messagesDiv.lastElementChild;
        if (lastMsg) {
            const buttons = lastMsg.querySelectorAll('.suggestion-btn');
            buttons.forEach(btn => {
                btn.addEventListener('click', (e) => {
                    const query = e.currentTarget.dataset.query;
                    const label = e.currentTarget.dataset.label;
                    // If query is empty, prefill input with the label so user can edit and send
                    if (!query || query.trim() === '') {
                        const input = document.getElementById('chat-input');
                        input.value = label || '';
                        input.focus();
                    } else {
                        // Display a friendly user message using the label (or query if no label)
                        this.displayUserMessage(label || query);
                        // Send the structured query to server
                        const chatMessage = {
                            sessionId: this.sessionId,
                            text: query,
                            productId: this.productId
                        };
                        this.stompClient.send('/app/chat', {}, JSON.stringify(chatMessage));
                    }
                    // Show spinner and set active filters based on the query
                    this.showSearchSpinner(true);
                    this.setActiveFiltersFromQuery(query);
                });
            });
        }

        this.scrollToBottom();
    }

    displayErrorMessage(text) {
+        // hide spinner on error
+        this.showSearchSpinner(false);
        const messagesDiv = document.getElementById('chat-widget-messages');
        const messageHTML = `
            <div class="chat-message bot-message error-message">
                <div class="message-content">
                    <i class="bi bi-exclamation-triangle"></i>
                    ${this.escapeHtml(text)}
                </div>
            </div>
        `;
        messagesDiv.insertAdjacentHTML('beforeend', messageHTML);
        this.scrollToBottom();
    }

    scrollToBottom() {
        const messagesDiv = document.getElementById('chat-widget-messages');
        messagesDiv.scrollTop = messagesDiv.scrollHeight;
    }

    formatPrice(price) {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(price);
    }

    escapeHtml(text) {
        const map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        };
        return String(text).replace(/[&<>"']/g, m => map[m]);
    }

    disconnect() {
        if (this.stompClient !== null) {
            this.stompClient.disconnect();
        }
        this.connected = false;
        console.log('Disconnected from WebSocket');
    }

// add helper to render active filters
    setActiveFiltersFromQuery(query) {
        try {
            if (!query) {
                this.clearActiveFilters();
                return;
            }
            const el = document.getElementById('chat-active-filters');
            const parts = [];
            const q = String(query).trim();
            const lower = q.toLowerCase();
            if (q.startsWith('brand:')) {
                parts.push('Hãng: ' + q.substring('brand:'.length));
            } else if (q.startsWith('type:')) {
                parts.push('Loại: ' + q.substring('type:'.length));
            } else if (q.startsWith('price:')) {
                const r = q.substring('price:'.length);
                parts.push('Tầm giá: ' + r);
            } else if (q.startsWith('spec:')) {
                parts.push('Thông số: ' + q.substring('spec:'.length));
            } else {
                // try to parse common pieces
                // brand words
                const brands = ['dell','hp','asus','acer','lenovo','apple','msi','lg'];
                for (const b of brands) {
                    if (lower.includes(b)) {
                        parts.push('Hãng: ' + b);
                        break;
                    }
                }
                if (lower.includes('gaming')) parts.push('Loại: Gaming');
                if (lower.includes('ultrabook')) parts.push('Loại: Ultrabook');
                // price patterns
                const mRange = lower.match(/(\d+[\.,]?\d*)\s*-\s*(\d+[\.,]?\d*)/);
                if (mRange) parts.push('Tầm giá: ' + mRange[1] + '-' + mRange[2]);
                const mSingle = lower.match(/(\d+[\.,]?\d*)\s*(triệu|m|vnđ|vnd)/);
                if (mSingle) parts.push('Tầm giá: ' + mSingle[1] + ' ' + (mSingle[2] || ''));
                // specs
                if (lower.includes('ram')) parts.push('Spec: RAM');
                if (lower.match(/\d+\s*gb/)) parts.push('Spec: ' + lower.match(/\d+\s*gb/)[0]);
            }

            if (parts.length === 0) {
                this.clearActiveFilters();
                return;
            }

            el.innerHTML = parts.map(p => `<span class="badge bg-secondary me-2">${this.escapeHtml(p)}</span>`).join('') + '<button id="clear-filters" class="btn btn-sm btn-link">Xóa</button>';
            el.style.display = 'block';
            const clearBtn = document.getElementById('clear-filters');
            if (clearBtn) {
                clearBtn.addEventListener('click', () => this.clearActiveFilters());
            }
        } catch (err) {
            console.warn('setActiveFiltersFromQuery error', err);
        }
    }

    clearActiveFilters() {
        const el = document.getElementById('chat-active-filters');
        if (el) {
            el.style.display = 'none';
            el.innerHTML = '';
        }
    }

    showSearchSpinner(show) {
        const sp = document.getElementById('chat-search-spinner');
        const input = document.getElementById('chat-input');
        const sendBtn = document.getElementById('chat-send');
        if (sp) sp.style.display = show ? 'block' : 'none';
        if (input) input.disabled = !!show ? true : false;
        if (sendBtn) sendBtn.disabled = !!show ? true : false;
    }
}

// Initialize widget when DOM is ready
let chatWidget = null;

function initChatWidget(productId) {
    if (!chatWidget) {
        chatWidget = new ChatWidget();
        chatWidget.init(productId);
    }
}

// Cleanup on page unload
window.addEventListener('beforeunload', () => {
    if (chatWidget) {
        chatWidget.disconnect();
    }
});

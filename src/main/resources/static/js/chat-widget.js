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
        const array = new Uint32Array(2);
        crypto.getRandomValues(array);
        return 'session-' + array[0].toString(36) + array[1].toString(36) + '-' + Date.now();
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
            (frame) => {
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

        const messageHTML = `
            <div class="chat-message bot-message">
                <div class="message-content">
                    ${this.escapeHtml(response.text)}
                    ${productsHTML}
                </div>
            </div>
        `;
        messagesDiv.insertAdjacentHTML('beforeend', messageHTML);
        this.scrollToBottom();
    }

    displayErrorMessage(text) {
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
        return text.replace(/[&<>"']/g, m => map[m]);
    }

    disconnect() {
        if (this.stompClient !== null) {
            this.stompClient.disconnect();
        }
        this.connected = false;
        console.log('Disconnected from WebSocket');
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

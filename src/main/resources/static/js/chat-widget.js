// Chat Widget with STOMP over SockJS
class ChatWidget {
    constructor() {
        this.stompClient = null;
        this.sessionId = this.generateSessionId();
        this.connected = false;
        this.productId = null;
        this._retries = 0;
        this._maxRetries = 10;
        this._contactRequested = false;
        this._pendingContactTech = false;
        this.assigned = false;
    }

    generateSessionId() {
        if (typeof crypto !== 'undefined' && crypto.randomUUID) {
            return 'session-' + crypto.randomUUID();
        }
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

        // Attempt to restore session status from server so assigned/pending state persists across reloads
        try {
            setTimeout(() => {
                fetch('/api/support/session/' + encodeURIComponent(this.sessionId), { credentials: 'same-origin' })
                    .then(r => r.ok ? r.json() : null)
                    .then(data => {
                        if (!data) return;
                        // server stores status as PENDING | ASSIGNED | CLOSED
                        if (data.status === 'ASSIGNED') {
                            this.setAssigned(true);
                            this._contactRequested = true;
                        } else if (data.status === 'PENDING') {
                            this._contactRequested = true;
                        } else {
                            this.setAssigned(false);
                        }
                    }).catch(() => {});
            }, 300);
        } catch (e) {}

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
                        <div class="chat-header-actions">
                            <button id="chat-contact-tech" class="chat-contact-tech" title="Liên hệ kỹ thuật">Liên hệ kỹ thuật</button>
                            <span id="chat-assigned-indicator" class="badge bg-info text-white ms-2" style="display:none; font-size:0.75rem; align-self:center;">Kỹ thuật viên đang trả lời...</span>
                            <button id="chat-widget-close" class="chat-widget-close">
                                <i class="bi bi-x"></i>
                            </button>
                        </div>
                    </div>
                    <div id="chat-active-filters" class="chat-active-filters" style="display:none;padding:8px 12px;background:#f8f9fa;border-bottom:1px solid #e9ecef;">
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
        const contactTechBtn = document.getElementById('chat-contact-tech');

        button.addEventListener('click', () => this.toggleWidget());
        closeBtn.addEventListener('click', () => this.toggleWidget());
        sendBtn.addEventListener('click', () => this.sendMessage());
        input.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                this.sendMessage();
            }
        });
        if (contactTechBtn) {
            // use a named handler that can be swapped when an admin joins
            contactTechBtn.addEventListener('click', (e) => {
                e.preventDefault();
                if (this.assigned) {
                    // Send structured close request to dedicated destination /app/support/close
                    try {
                        const payload = { adminId: null, sessionId: this.sessionId };
                        if (this.stompClient && this.connected && typeof this.stompClient.send === 'function') {
                            this.stompClient.send('/app/support/close', {}, JSON.stringify(payload));
                        } else {
                            // if sockjs/stomp not ready, try fallback to /app/chat LEAVE_AGENT
                            if (this.stompClient && this.connected) {
                                this.stompClient.send('/app/chat', {}, JSON.stringify({ sessionId: this.sessionId, text: 'LEAVE_AGENT' }));
                            }
                        }
                    } catch (err) {
                        console.warn('Failed to send support close', err);
                    }
                    // update UI optimistically
                    this.setAssigned(false);
                } else {
                    this.requestContactTech();
                }
            });
        }
    }

    setAssigned(flag) {
        this.assigned = !!flag;
        const btn = document.getElementById('chat-contact-tech');
        const indicator = document.getElementById('chat-assigned-indicator');
        if (!btn) return;
        if (this.assigned) {
            btn.textContent = 'Rời khỏi cuộc trò chuyện';
            btn.classList.remove('chat-contact-tech');
            btn.classList.add('btn-danger');
            if (indicator) indicator.style.display = 'inline-block';
        } else {
            btn.textContent = 'Liên hệ kỹ thuật';
            btn.classList.remove('btn-danger');
            btn.classList.add('chat-contact-tech');
            if (indicator) indicator.style.display = 'none';
        }
    }

    // Request contact with technical staff. Sends a special CALL_HUMAN message via /app/chat
    requestContactTech() {
        // Prevent duplicate requests
        if (this._contactRequested) return;
        this._contactRequested = true;

        const doSend = () => {
            try {
                if (this.stompClient && this.connected) {
                    const payload = {
                        sessionId: this.sessionId,
                        text: 'CALL_HUMAN',
                        productId: this.productId || null
                    };
                    this.stompClient.send('/app/chat', {}, JSON.stringify(payload));

                    // Show confirmation system message
                    const messagesDiv = document.getElementById('chat-widget-messages');
                    const msg = `Yêu cầu liên hệ kỹ thuật đã được gửi. Vui lòng chờ kỹ thuật viên liên hệ.`;
                    const messageHTML = `\n                        <div class="chat-message bot-message">\n                            <div class="message-content">${this.escapeHtml(msg)}</div>\n                        </div>\n                    `;
                    messagesDiv.insertAdjacentHTML('beforeend', messageHTML);
                    this.scrollToBottom();
                }
            } catch (err) {
                console.warn('requestContactTech error', err);
                this.displayErrorMessage('Không thể gửi yêu cầu liên hệ kỹ thuật lúc này. Vui lòng thử lại.');
                this._contactRequested = false;
            }
        };

        if (this.connected) {
            doSend();
        } else {
            // If not connected, connect and send when ready
            this._pendingContactTech = true;
            this.connect();
            // ensure we send after connect success (connect sets this.connected and sends greeting)
            // hook into a small poll to wait for connection
            const maxWait = 10000; // 10s
            const start = Date.now();
            const interval = setInterval(() => {
                if (this.connected) {
                    clearInterval(interval);
                    doSend();
                } else if (Date.now() - start > maxWait) {
                    clearInterval(interval);
                    this.displayErrorMessage('Không thể kết nối tới dịch vụ chat. Vui lòng thử lại sau.');
                    this._contactRequested = false;
                }
            }, 300);
        }
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
        this._retries = 0;

        this.stompClient.connect({},
            () => {
                console.log('Connected to WebSocket');
                this.connected = true;
                this.updateConnectionStatus(true);
                this._retries = 0;

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

                // If user requested contact before connection completed, send it now
                if (this._pendingContactTech) {
                    this._pendingContactTech = false;
                    try {
                        this.stompClient.send('/app/chat', {}, JSON.stringify({ sessionId: this.sessionId, text: 'CALL_HUMAN', productId: this.productId || null }));
                        const messagesDiv = document.getElementById('chat-widget-messages');
                        const msg = `Yêu cầu liên hệ kỹ thuật đã được gửi. Vui lòng chờ kỹ thuật viên liên hệ.`;
                        const messageHTML = `\n                            <div class="chat-message bot-message">\n                                <div class="message-content">${this.escapeHtml(msg)}</div>\n                            </div>\n                        `;
                        messagesDiv.insertAdjacentHTML('beforeend', messageHTML);
                        this.scrollToBottom();
                        this._contactRequested = true;
                    } catch (err) {
                        console.warn('Failed to send pending contact tech request', err);
                        this.displayErrorMessage('Không thể gửi yêu cầu liên hệ kỹ thuật sau khi kết nối.');
                        this._contactRequested = false;
                    }
                }
            },
            (error) => {
                console.error('WebSocket connection error:', error);
                this.connected = false;
                this.updateConnectionStatus(false);

                // Retry logic
                if (this._retries < this._maxRetries) {
                    this._retries++;
                    console.log(`Retrying connection in 2s (Attempt ${this._retries}/${this._maxRetries})`);
                    setTimeout(() => this.connect(), 2000);
                } else {
                    this.displayErrorMessage('Không thể kết nối. Vui lòng thử lại sau.');
                }
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
        // If user already requested human support or an admin assigned, do not show the bot-search spinner
        // so the user can continue typing multiple messages while waiting for admin.
        if (!this._contactRequested && !this.assigned) {
            this.showSearchSpinner(true);
        }
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
                             onerror="this.src='data:image/svg+xml;utf8,<svg xmlns=\\'http://www.w3.org/2000/svg\\' width=\\'80\\' height=\\'80\\'><rect width=\\'100%\\' height=\\'100%\\' fill=\\'%23f3f3f3\\' /><text x=\\'50%\\' y=\\'50%\\' dominant-baseline=\\'middle\\' text-anchor=\\'middle\\' fill=\\'%23888\\' font-family=\\'Arial, Helvetica, sans-serif\\' font-size=\\'10\\'>No Image</text></svg>'">
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
        // detect special LEAVE_AGENT suggestion and handle it via contact button instead of rendering repeatedly
        let suggestions = Array.isArray(response.suggestions) ? response.suggestions.slice() : [];
        let hasLeave = false;
        suggestions = suggestions.filter(s => {
            try {
                let label = '';
                let query = '';
                if (s && typeof s === 'object' && s.label !== undefined) {
                    label = String(s.label || '');
                    query = String(s.query || '');
                } else {
                    label = String(s || '');
                    query = String(s || '');
                }
                if (/LEAVE_AGENT/i.test(query) || /rời/i.test(label)) {
                    hasLeave = true;
                    return false; // remove from suggestions to avoid repeated rendering
                }
            } catch (e) {}
            return true;
        });

        if (hasLeave) {
            // set contact button to 'leave' state
            try { this.setAssigned(true); } catch (e) {}
        }

        // Detect system-like messages and apply special styling + state toggles
        let isSystem = false;
        try {
            const txt = (response.text || '').toLowerCase();
            // keywords: 'tư vấn' (advisor), 'tham gia' (joined), 'rời' (leave), 'kết thúc' (ended)
            if (txt.includes('tư vấn') || txt.includes('tham gia') || txt.includes('rời') || txt.includes('kết th')) {
                isSystem = true;
            }
            // toggle assigned state based on content
            if (isSystem) {
                if (txt.includes('tham gia') || txt.includes('đã tham gia')) {
                    try { this.setAssigned(true); } catch (e) {}
                }
                if (txt.includes('kết th') || txt.includes('rời') || txt.includes('đã kết thúc')) {
                    try { this.setAssigned(false); } catch (e) {}
                }
            }
        } catch (e) {}

        if (suggestions && suggestions.length > 0) {
            suggestionsHTML = '<div class="chat-suggestions">';
            suggestions.forEach(s => {
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
            <div class="chat-message bot-message${isSystem ? ' system' : ''}">
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
                    // Only show bot-search spinner if user hasn't asked for human support and no admin assigned
                    if (!this._contactRequested && !this.assigned) {
                        this.showSearchSpinner(true);
                    }
                    this.setActiveFiltersFromQuery(query);
                });
            });
        }

        this.scrollToBottom();
    }

    displayErrorMessage(text) {
        // hide spinner on error
        this.showSearchSpinner(false);
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
            let q = String(query).trim();
            const lower = q.toLowerCase();

            // Nếu là QUERY: (từ AI trả về), ta trích xuất phần sau QUERY:
            if (lower.startsWith('query:')) {
                q = q.substring('query:'.length).trim();
            }

            // 1. Structured filters - Dùng regex để bắt các cặp key:value
            const regexMap = {
                'brand:': { label: 'Hãng' },
                'type:': { label: 'Loại' },
                'promotion:': { label: 'Khuyến mãi' },
                'spec:': { label: 'Thông số' },
                // Xử lý giá tiền đặc biệt để format lại VND cho đẹp
                'price:': {
                    label: 'Tầm giá',
                    handler: (val) => {
                        // Định dạng lại giá: 15000000-20000000 -> 15 Triệu - 20 Triệu
                        const [min, max] = val.split('-').map(s => s.trim());
                        const format = (price) => {
                            const num = parseInt(price, 10);
                            if (isNaN(num)) return price;
                            if (num >= 1000000) {
                                // Sử dụng Intl.NumberFormat để định dạng số triệu
                                return new Intl.NumberFormat('vi-VN', {
                                    minimumFractionDigits: 0,
                                    maximumFractionDigits: 1 // Giữ 1 số lẻ cho 15.5 Triệu
                                }).format(num / 1000000) + ' Triệu';
                            }
                            // Nếu nhỏ hơn 1 triệu, hiển thị VND
                            return new Intl.NumberFormat('vi-VN').format(num) + ' đ';
                        };

                        let displayMin = min ? format(min) : '';
                        let displayMax = max ? format(max) : '';

                        if (!min && max) {
                            return `Tối đa ${displayMax}`;
                        } else if (min && !max) {
                            return `Tối thiểu ${displayMin}`;
                        }
                        return (displayMin) + (displayMax ? ' - ' + displayMax : '');
                    }
                }
            };

            let remainingQuery = ' ' + q; // Thêm khoảng trắng để regex dễ match

            for (const key in regexMap) {
                // Regex: Tìm khoảng trắng + key + : + value (value là non-whitespace)
                // Ta tìm kiếm các pattern theo cú pháp ` key:value `
                const pattern = new RegExp(`\\s*${key}\\s*([^\\s]+)`, 'gi');
                let match;
                // Dùng .exec liên tục để tìm tất cả matches
                while ((match = pattern.exec(remainingQuery)) !== null) {
                    const value = match[1].trim();
                    if (value) {
                        const { label, handler } = regexMap[key];
                        const displayValue = handler ? handler(value) : value;
                        parts.push(`${label}: ${displayValue}`);
                        // Xóa phần đã match (cả key:value) khỏi chuỗi để chuẩn bị cho free-text
                        remainingQuery = remainingQuery.replace(match[0], ' ');
                    }
                }
            }

            // 2. Fallback: Nếu vẫn còn text, coi là free-text search
            const freeText = remainingQuery.trim();
            if (freeText && freeText !== 'query:') {
                parts.push(`Tìm kiếm: ${freeText}`);
            }

            if (parts.length === 0) {
                this.clearActiveFilters();
                return;
            }

            el.innerHTML = parts.map(p => `<span class="badge bg-secondary me-2">${this.escapeHtml(p)}</span>`).join('') + '<button id="clear-filters" class="btn btn-sm btn-link">Xóa</button>';
            el.style.display = 'block';
            const clearBtn = document.getElementById('clear-filters');
            if (clearBtn) {
                clearBtn.addEventListener('click', () => {
                    this.clearActiveFilters();
                });
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

// notifications.js - connects to /ws and subscribes to /topic/notifications/{userId}
// expects SockJS and Stomp loaded in page

let stompClient = null;
let currentUserId = null;
let socketConnected = false;

function initNotifications(userId) {
    currentUserId = userId;
    connectSocket(userId);
    if (userId) {
        fetchNotifications(userId);
        fetchUnreadCount(userId);
    } else {
        try { fetchSiteNotifications(); } catch(e) {}
    }
}

window.initNotifications = initNotifications;

window.initializeNotifications = function () {
    let userId = currentUserId || window.currentUserId || (document.body && document.body.dataset && document.body.dataset.userId);
    try {
        if ((!userId || userId === 'undefined') && window.apiClient && typeof window.apiClient.getUser === 'function') {
            const user = window.apiClient.getUser();
            if (user && user.id) userId = user.id;
        }
    } catch (e) {
    }

    connectSocket(userId || null);

    if (userId) {
        initNotifications(userId);
    }

    // GÁN SỰ KIỆN CHO NÚT MARK ALL READ (Fixing Mark All button)
    const markAllBtn = document.getElementById('mark-all-read');
    if (markAllBtn) {
        markAllBtn.addEventListener('click', function(event) {
            event.preventDefault();
            markAllNotifications();
        });
    }
};

window.toggleNotificationDropdown = function (event) {
    if (event && event.preventDefault) event.preventDefault();
    const dropdown = document.getElementById('notification-dropdown') || document.getElementById('notif-dropdown');
    if (!dropdown) return;

    const isVisible = dropdown.style.display === 'block' || dropdown.classList.contains('show');
    if (isVisible) {
        dropdown.style.display = 'none';
        dropdown.classList.remove('show');
    } else {
        dropdown.style.display = 'block';
        dropdown.classList.add('show');
        const userId = currentUserId || window.currentUserId || (document.body && document.body.dataset && document.body.dataset.userId);
        if (userId) {
            fetchNotifications(userId);
            fetchUnreadCount(userId);
        } else {
            fetchSiteNotifications();
        }
    }
};

function connectSocket(userId) {
    if (socketConnected) {
        if (stompClient && userId) {
            try { stompClient.subscribe('/topic/notifications/' + userId, function(message) { let dto; try{dto=JSON.parse(message.body);}catch(e){return;} pushNotificationToUI(dto); }); } catch(e){}
        }
        return;
    }

    try {
        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

        // Disable debug logging
        this.stompClient.debug = null;

        stompClient.connect({}, function () {
            socketConnected = true;
            stompClient.subscribe('/topic/site.notifications', function (message) {
                let dto;
                try {
                    dto = JSON.parse(message.body);
                } catch (e) {
                    console.warn('Received non-JSON site notification', e, message.body);
                    return;
                }
                pushSiteNotificationToUI(dto);
            });

            if (userId) {
                stompClient.subscribe('/topic/notifications/' + userId, function (message) {
                    let dto;
                    try {
                        dto = JSON.parse(message.body);
                    } catch (e) {
                        console.warn('Received non-JSON STOMP message', e, message.body);
                        return;
                    }
                    pushNotificationToUI(dto);
                });
            }
        }, function (err) {
            console.error('STOMP error', err);
        });
    } catch (e) {
        console.error('Websocket init error', e);
    }
}

function fetchSiteNotifications() {
    fetch('/api/site-notifications')
        .then(async res => {
            if (!res.ok) throw new Error('Site notifications fetch failed: ' + res.status);
            const contentType = res.headers.get('content-type') || '';
            if (contentType.indexOf('application/json') === -1) {
                console.warn('Non-JSON from /api/site-notifications');
                return [];
            }
            return res.json();
        })
        .then(list => {
            const listEl = document.getElementById('notif-list') || document.getElementById('notification-list');
            if (!listEl) return;
            list.forEach(n => {
                const normalized = normalizeNotification(n);
                const item = buildNotificationItem(normalized);
                listEl.prepend(item);
            });
        })
        .catch(err => console.warn('Failed to load site notifications', err));
}

function fetchNotifications(userId) {
    fetch('/api/notifications/' + userId)
        .then(async res => {
            const contentType = res.headers.get('content-type') || '';
            if (!res.ok) {
                throw new Error('Network response was not ok: ' + res.status);
            }
            if (contentType.indexOf('application/json') === -1) {
                const text = await res.text();
                console.warn('Non-JSON response from /api/notifications', text.slice(0, 200));
                throw new Error('Non-JSON response');
            }
            return res.json();
        })
        .then(list => {
            renderNotificationList(list);
        })
        .catch(err => console.error('Failed to load notifications', err));
}

function fetchUnreadCount(userId) {
    fetch('/api/notifications/' + userId + '/unreadCount')
        .then(res => {
            if (!res.ok) throw new Error('Network response was not ok');
            return res.json();
        })
        .then(count => updateBellCount(count.unreadCount || 0))
        .catch(err => console.error('Failed to load unread count', err));
}

function pushNotificationToUI(dto) {
    const countEl = document.getElementById('notif-count') || document.getElementById('notification-badge');
    const current = parseInt((countEl && countEl.innerText) || '0') || 0;
    updateBellCount(current + 1);

    const listEl = document.getElementById('notif-list') || document.getElementById('notification-list');
    if (listEl) {
        const item = buildNotificationItem(dto);
        listEl.prepend(item);
    }
}

function pushSiteNotificationToUI(dto) {
    const listEl = document.getElementById('notif-list') || document.getElementById('notification-list');
    if (listEl) {
        const normalized = normalizeNotification(dto);
        const item = buildNotificationItem(normalized);
        listEl.prepend(item);
    }
}

function renderNotificationList(list) {
    const listEl = document.getElementById('notif-list') || document.getElementById('notification-list');
    if (!listEl) return;
    listEl.innerHTML = '';
    list.forEach(n => {
        const normalized = normalizeNotification(n);
        const item = buildNotificationItem(normalized);
        listEl.appendChild(item);
    });
}

function normalizeNotification(n) {
    // ... (logic normalization giữ nguyên)
    const out = {};
    out.id = n.id || n.id;
    out.title = n.title || n.type || 'Thông báo';
    out.message = n.message || n.message || '';
    out.read = (typeof n.read !== 'undefined') ? n.read : (n.readFlag || false);
    out.type = n.type || (n.url ? 'SITE' : null) || n.type;
    out.refId = n.refId || n.productId || n.refId;
    if (n.createdAt) out.createdAt = n.createdAt;
    else if (n.timestamp) out.createdAt = new Date(n.timestamp).toISOString();
    else out.createdAt = new Date().toISOString();
    return out;
}

function buildNotificationItem(n) {
    const li = document.createElement('li');
    li.className = 'notification-item dropdown-item d-flex justify-content-between align-items-start';
    li.dataset.id = n.id || '';

    // Thêm class 'unread'/'is-read'
    if (!n.read) {
        li.classList.add('unread');
    } else {
        li.classList.add('is-read');
    }

    const iconClass = getNotificationIcon(n.type);
    const targetUrl = getNotificationUrl(n); // **LẤY URL CHI TIẾT**

    // LEFT COLUMN (Nội dung)
    const left = document.createElement('div');
    left.className = 'ms-2 me-auto notif-content-wrapper';

    // ... (HTML structure for title and message remains the same)
    left.innerHTML = `
        <div class="notif-item-title d-flex align-items-center">
            <i class="${iconClass} notif-type-icon"></i>
            <span class="fw-bold">${escapeHtml(n.title || 'Thông báo')}</span>
        </div>
        <div class="small text-muted notif-item-message">
            ${escapeHtml(n.message || '')}
        </div>
    `;
    li.appendChild(left);

    // RIGHT COLUMN (Timestamp)
    const right = document.createElement('div');
    right.className = 'text-end small text-muted text-nowrap ms-2';
    right.innerText = formatTimestamp(n.createdAt);
    li.appendChild(right);

    // 2. Xử lý sự kiện click: ĐIỀU HƯỚNG BẮT BUỘC VÀ MARK READ
    li.addEventListener('click', function () {
        // Gọi API Mark Read
        if (n.id) {
            fetch('/api/notifications/markRead/' + n.id, { method: 'POST' }).catch(()=>{});
        }

        // Cập nhật trạng thái đọc trên UI ngay lập tức
        li.classList.remove('unread');
        li.classList.add('is-read');

        // *** ĐIỀU HƯỚNG CHÍNH XÁC ***
        if (targetUrl) {
            window.location.href = targetUrl; // Chuyển đến trang chi tiết (Product/Order/Promotion)
        } else {
            // Nếu không có link chi tiết, nhảy đến trang List tổng hợp (như nút "Xem tất cả" muốn)
            window.location.href = '/notifications.html';
        }
    });

    return li;
}

function getNotificationUrl(n) {
    if (n.type === 'ORDER' && n.refId) {
        return '/order-detail.html?orderCode=' + n.refId;
    }
    if (n.type === 'PRODUCT' && n.refId) {
        return '/product-detail.html?id=' + n.refId;
    }
    if (n.type === 'PROMOTION' && n.refId) {
        return '/promotions.html';
    }
    if (n.url) {
        return n.url;
    }
    return null;
}

// Mark All Read Logic
async function markAllNotifications() {
    const listEl = document.getElementById('notif-list') || document.getElementById('notification-list');
    const items = listEl ? Array.from(listEl.querySelectorAll('.notification-item.unread')) : [];

    if (items.length === 0) return;

    const idsToMark = items.map(li => li.dataset.id).filter(Boolean);

    try {
        await Promise.all(idsToMark.map(id =>
            fetch('/api/notifications/markRead/' + id, { method: 'POST' }).catch(console.warn)
        ));

        items.forEach(li => {
            li.classList.remove('unread');
            li.classList.add('is-read');
        });

        updateBellCount(0);
        showAlert('Đã đánh dấu tất cả thông báo là đã đọc', 'success');
    } catch (e) {
        showAlert('Lỗi khi đánh dấu đã đọc', 'error');
    }
}
window.markAllNotifications = markAllNotifications;


// ... (Các hàm helper khác không đổi: updateBellCount, getNotificationIcon, formatTimestamp, escapeHtml)
function getNotificationIcon(type) {
    switch ((type || '').toUpperCase()) {
        case 'PRODUCT':
            return 'bi bi-box-seam';
        case 'PROMOTION':
            return 'bi bi-tag';
        case 'ORDER':
            return 'bi bi-cart-check';
        case 'SITE':
            return 'bi bi-megaphone';
        default:
            return 'bi bi-info-circle';
    }
}

function updateBellCount(count) {
    const countEl = document.getElementById('notif-count') || document.getElementById('notification-badge');
    if (!countEl) return;
    if (count > 0) {
        countEl.innerText = count;
        countEl.style.display = 'inline-block';
    } else {
        countEl.innerText = '0';
        countEl.style.display = 'none';
    }
}

function formatTimestamp(ts) {
    if (!ts) return '';
    try {
        const d = new Date(ts);
        return d.toLocaleString('vi-VN');
    } catch (e) { return ts; }
}

function escapeHtml(text) {
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return String(text).replace(/[&<>"']/g, m => map[m]);
}
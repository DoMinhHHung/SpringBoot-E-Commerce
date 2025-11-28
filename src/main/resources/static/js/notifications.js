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
        // still fetch site notifications on notifications page if present
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

    // always connect socket (to receive site-wide notifications)
    connectSocket(userId || null);

    if (userId) {
        initNotifications(userId);
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
        // refresh content when opening
        const userId = currentUserId || window.currentUserId || (document.body && document.body.dataset && document.body.dataset.userId);
        if (userId) {
            fetchNotifications(userId);
            fetchUnreadCount(userId);
        } else {
            // fetch site notifications as fallback
            fetchSiteNotifications();
        }
    }
};

function connectSocket(userId) {
    if (socketConnected) {
        // already connected; if userId changed and is now present, subscribe to user topic
        if (stompClient && userId) {
            try { stompClient.subscribe('/topic/notifications/' + userId, function(message) { let dto; try{dto=JSON.parse(message.body);}catch(e){return;} pushNotificationToUI(dto); }); } catch(e){}
        }
        return;
    }

    try {
        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, function () {
            socketConnected = true;
            // always subscribe to site notifications
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

            // subscribe to user-specific if provided
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
    // public site notifications list
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
            // render into notifications dropdown if present
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
        .then(count => updateBellCount(count))
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
    // site notifications don't increment user unread count, but we show them in list
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
    // Accept either {id,title,message,read,createdAt,type,refId}
    // or {id,type,title,message,url,productId,timestamp}
    const out = {};
    out.id = n.id || n.id;
    out.title = n.title || n.title || '';
    out.message = n.message || n.message || '';
    out.read = (typeof n.read !== 'undefined') ? n.read : (n.readFlag || false);
    out.type = n.type || (n.url ? 'SITE' : null) || n.type;
    out.refId = n.refId || n.productId || n.refId;
    // createdAt: prefer ISO, else epoch millis
    if (n.createdAt) out.createdAt = n.createdAt;
    else if (n.timestamp) out.createdAt = new Date(n.timestamp).toISOString();
    else out.createdAt = new Date().toISOString();
    return out;
}

function buildNotificationItem(n) {
    const li = document.createElement('li');
    li.className = 'notification-item dropdown-item d-flex justify-content-between align-items-start';
    const left = document.createElement('div');
    left.innerHTML = `<div class="fw-bold">${escapeHtml(n.title || '')}</div><div class="small text-muted">${escapeHtml(n.message || '')}</div>`;
    li.appendChild(left);

    const right = document.createElement('div');
    right.className = 'text-end small text-muted';
    right.innerText = formatTimestamp(n.createdAt);
    li.appendChild(right);

    // click behavior: if type == ORDER and has refId, open order detail
    li.addEventListener('click', function () {
        if (n.type === 'ORDER' && n.refId) {
            window.location.href = '/order/order-detail.html?id=' + n.refId;
        } else if (n.type === 'PRODUCT' && n.refId) {
            window.location.href = '/product/product-detail.html?id=' + n.refId;
        } else if (n.type === 'SITE' && n.url) {
            window.location.href = n.url;
        }
        // mark read
        if (n.id) fetch('/api/notifications/markRead/' + n.id, { method: 'POST' }).catch(()=>{});
        li.classList.remove('fw-bold');
    });

    return li;
}

function updateBellCount(count) {
    const countEl = document.getElementById('notif-count') || document.getElementById('notification-badge');
    if (!countEl) return;
    if (count > 0) {
        countEl.innerText = count;
        countEl.style.display = 'inline-block';
    } else {
        countEl.innerText = '';
        countEl.style.display = 'none';
    }
}

function formatTimestamp(ts) {
    if (!ts) return '';
    try {
        const d = new Date(ts);
        return d.toLocaleString();
    } catch (e) { return ts; }
}

function escapeHtml(s) {
    return String(s || '').replace(/[&<>"']/g, function (c) { return {'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":"&#39;"}[c]; });
}

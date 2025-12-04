
let notifStomp = null;
let notifConnected = false;
let notifUnread = 0;
let notifList = [];

function initializeNotifications() {
  let bell = document.getElementById("notification-bell");
  let badge = document.getElementById("notification-badge");
  let listEl = document.getElementById("notification-list");
  let markAllBtn = document.getElementById("mark-all-read");

  if (!badge || !bell || !listEl) {
    const container = document.getElementById("notification-bell-container") || document.getElementById("header-container");
    if (container) {
      try {
        if (!document.getElementById("notification-bell-container")) {
          const wrapper = document.createElement("div");
          wrapper.className = "notification-bell-container me-3";
          wrapper.id = "notification-bell-container";
          wrapper.style.position = 'relative';

          wrapper.innerHTML = `
            <a href="#" id="notification-bell" class="notification-icon">
              <i class="bi bi-bell"></i>
              <span class="badge" id="notification-badge">0</span>
            </a>
            <div id="notification-dropdown" class="notification-dropdown" style="display:none; position:absolute; right:10px; top:40px; z-index:1050; width:320px;">
              <div class="card">
                <div class="card-header d-flex justify-content-between align-items-center">
                  <strong>Thông báo</strong>
                  <button class="btn btn-sm btn-link" id="mark-all-read">Đánh dấu đã đọc</button>
                </div>
                <ul class="list-group list-group-flush" id="notification-list" style="max-height:320px; overflow:auto"></ul>
                <div class="card-footer text-center"><a href="/notifications.html">Xem tất cả</a></div>
              </div>
            </div>
          `;
          const headerParent = document.getElementById('header-container') || document.querySelector('.navbar-main .container') || container;
          if (headerParent) {
            // insert before user avatar area if exists
            const avatar = headerParent.querySelector('#user-avatar-dropdown');
            if (avatar && avatar.parentNode) avatar.parentNode.insertBefore(wrapper, avatar);
            else headerParent.appendChild(wrapper);
          } else {
            container.appendChild(wrapper);
          }
        }
      } catch (e) {
        // ignore creation errors
        console.warn('Failed to auto-create notification UI:', e);
      }

      // re-query after creation attempt
      badge = document.getElementById("notification-badge");
      listEl = document.getElementById("notification-list");
      // also re-query bell and markAllBtn so we can bind handlers
      bell = document.getElementById("notification-bell");
      markAllBtn = document.getElementById("mark-all-read");
    }
  }

  // If there is still no badge, create an in-memory badge (so script doesn't bail) but don't return
  if (!badge) {
    // create a dummy badge that does nothing visually
    badge = document.createElement('span');
    badge.id = 'notification-badge';
    badge.style.display = 'none';
    document.body.appendChild(badge);
  }

  // restore unread from localStorage
  try {
    const stored = localStorage.getItem("site_notif_unread");
    if (stored) {
      notifUnread = parseInt(stored, 10) || 0;
      updateBadge();
    }
  } catch (e) {}

  if (markAllBtn) {
    markAllBtn.addEventListener("click", (e) => {
      e.preventDefault();
      markAllRead();
    });
  }

  if (bell) {
    bell.addEventListener("click", (e) => {
      e.preventDefault();
      toggleNotificationDropdown(e);
    });
  }

  // connect socket even if some DOM elements were missing; rendering will create nodes as needed
  connectNotificationSocket();
}

function connectNotificationSocket() {
  if (notifConnected || !window.SockJS || !window.Stomp) {
    return;
  }
  const socket = new SockJS("/ws");
  notifStomp = Stomp.over(socket);
  notifStomp.debug = null;
  notifStomp.connect(
    {},
    function () {
      notifConnected = true;
      try {
        notifStomp.subscribe("/topic/site.notifications", function (frame) {
          try {
            const payload = JSON.parse(frame.body);
            pushNotification(payload);
          } catch (e) {
            console.warn("Invalid notification payload", e);
          }
        });
      } catch (e) {
        console.error("Subscribe error", e);
      }
    },
    function (err) {
      notifConnected = false;
      // retry after delay
      setTimeout(connectNotificationSocket, 5000);
    }
  );
}

function pushNotification(payload) {
  // normalize payload: { type, title, message, url }
  const item = {
    id: payload.id || "notif-" + Date.now(),
    type: payload.type || "info",
    title:
      payload.title ||
      (payload.type === "product" ? "Sản phẩm mới" : "Thông báo"),
    message: payload.message || "",
    url:
      payload.url ||
      (payload.productId
        ? "/product-detail.html?id=" + payload.productId
        : null),
    ts: payload.timestamp || Date.now(),
    productId: payload.productId || null
  };
  notifList.unshift(item);

  // increment unread
  notifUnread = (notifUnread || 0) + 1;
  try {
    localStorage.setItem("site_notif_unread", String(notifUnread));
  } catch (e) {}
  updateBadge();

  // ensure notification list exists and render item
  renderNotificationItem(item);

  // also show a small in-page toast so user sees content immediately
  try {
    showInPageToast(item);
  } catch (e) {}

  // browser native notification
  try {
    if ("Notification" in window && Notification.permission === "granted") {
      const n = new Notification(item.title, { body: item.message });
      n.onclick = function () {
        if (item.url) window.location.href = item.url;
      };
      setTimeout(() => n.close(), 4000);
    }
  } catch (e) {}
}

function renderNotificationItem(item) {
  let listEl = document.getElementById("notification-list");
  // If list not present, try to create dropdown markup under header
  if (!listEl) {
    const header = document.getElementById('header-container') || document.querySelector('nav.navbar-main .container');
    try {
      const wrapper = document.getElementById('notification-bell-container');
      if (wrapper) {
        const dropdown = wrapper.querySelector('#notification-dropdown');
        if (!dropdown) {
          const dd = document.createElement('div');
          dd.id = 'notification-dropdown';
          dd.className = 'notification-dropdown';
          dd.style.cssText = 'display:none; position:absolute; right:10px; top:40px; z-index:1050; width:320px;';
          dd.innerHTML = `
            <div class="card">
              <div class="card-header d-flex justify-content-between align-items-center">
                <strong>Thông báo</strong>
                <button class="btn btn-sm btn-link" id="mark-all-read">Đánh dấu đã đọc</button>
              </div>
              <ul class="list-group list-group-flush" id="notification-list" style="max-height:320px; overflow:auto"></ul>
              <div class="card-footer text-center"><a href="/notifications.html">Xem tất cả</a></div>
            </div>
          `;
          wrapper.appendChild(dd);
        }
      }
    } catch (e) {
      console.warn('Failed to create notification dropdown dynamically', e);
    }
    listEl = document.getElementById('notification-list');
  }

  if (!listEl) {
    // last resort: create a floating list in body
    const floatContainerId = 'floating-notif-list-container';
    let float = document.getElementById(floatContainerId);
    if (!float) {
      float = document.createElement('div');
      float.id = floatContainerId;
      float.style.cssText = 'position:fixed; right:10px; top:80px; z-index:2000; width:320px; max-height:400px; overflow:auto;';
      document.body.appendChild(float);
    }
    const li = document.createElement('div');
    li.className = 'list-group-item';
    li.dataset.id = item.id;
    const time = new Date(item.ts).toLocaleString();
    li.innerHTML = `<div class="notif-item"><div class="d-flex justify-content-between"><div><strong>${escapeHtml(item.title)}</strong><div class="small text-muted">${escapeHtml(item.message)}</div></div><div class="small text-muted">${escapeHtml(time)}</div></div></div>`;
    li.addEventListener('click', () => {
      if (item.url) window.location.href = item.url;
      else markItemRead(item.id);
    });
    if (item.read) li.classList.add('list-group-item-secondary');
    float.prepend(li);
    return;
  }

  const li = document.createElement("li");
  li.className = "list-group-item d-flex justify-content-between align-items-start";
  li.dataset.id = item.id;
  const time = formatNotifTime(item.ts);
  li.innerHTML = `<div class="ms-2 me-auto"><div class="fw-bold">${escapeHtml(item.title)}</div><div class="small text-muted">${escapeHtml(item.message)}</div></div><div class="small text-muted text-nowrap ms-2">${escapeHtml(time)}</div>`;
  // click opens url if exists
  li.addEventListener("click", () => {
    if (item.url) {
      window.location.href = item.url;
    } else {
      // mark this item read: remove badge count
      markItemRead(item.id);
      li.classList.add('list-group-item-secondary');
    }
  });
  if (item.read) li.classList.add('list-group-item-secondary');
  listEl.prepend(li);
}

function showInPageToast(item) {
  // create a small toast in top-right corner that disappears after a few seconds
  const toastId = 'site-notif-toast';
  let container = document.getElementById(toastId + '-container');
  if (!container) {
    container = document.createElement('div');
    container.id = toastId + '-container';
    container.style.cssText = 'position:fixed; right:12px; top:70px; z-index:2500; display:flex; flex-direction:column; gap:8px;';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.className = 'card shadow-sm';
  toast.style.cssText = 'width:320px; cursor:pointer;';
  toast.innerHTML = `
    <div class="card-body p-2">
      <div class="d-flex justify-content-between">
        <div><strong>${escapeHtml(item.title)}</strong><div class="small text-muted">${escapeHtml(item.message)}</div></div>
        <div class="small text-muted">${new Date(item.ts).toLocaleTimeString()}</div>
      </div>
    </div>
  `;
  toast.addEventListener('click', () => {
    if (item.url) window.location.href = item.url;
  });
  container.prepend(toast);
  setTimeout(() => {
    try { toast.remove(); } catch(e) {}
  }, 6000);
}

function updateBadge() {
  const badge = document.getElementById("notification-badge");
  if (!badge) return;
  if (!notifUnread || notifUnread <= 0) {
    badge.style.display = "none";
    badge.textContent = "0";
  } else {
    badge.style.display = "inline-block";
    badge.textContent = notifUnread > 99 ? "99+" : String(notifUnread);
  }
}

function markAllRead() {
  notifUnread = 0;
  try {
    localStorage.setItem("site_notif_unread", "0");
  } catch (e) {}
  updateBadge();
  // persist to server
  try {
    fetch('/api/notifications/read-all', { method: 'PUT', credentials: 'same-origin' }).catch(() => {});
  } catch (e) {}
}

function markItemRead(id) {
  // optional implementation: remove that item or mark visually
  try {
    // server expects numeric id
    const numericId = parseInt(String(id).replace(/^notif-/, ''), 10);
    if (!isNaN(numericId)) {
      fetch('/api/notifications/' + numericId + '/read', { method: 'PUT', credentials: 'same-origin' })
        .then(() => {
          // mark visually
          const el = document.querySelector('#notification-list [data-id="' + id + '"]');
          if (el) el.classList.add('list-group-item-secondary');
        })
        .catch(() => {});
    } else {
      // fallback: mark badge as zero
      markAllRead();
    }
  } catch (e) {
    markAllRead();
  }
}

// helper: format timestamp nicely
function formatNotifTime(ts) {
  try {
    const d = new Date(ts);
    return d.toLocaleString('vi-VN');
  } catch (e) { return '' + ts; }
}

// Safe fetch for /api/notifications: returns { items: Array, authRequired: boolean, error: string }
function fetchNotificationsSafe() {
  const headers = {};
  try {
    if (window.apiClient && typeof apiClient.getAuthToken === 'function') {
      const t = apiClient.getAuthToken();
      if (t) headers['Authorization'] = 'Bearer ' + t;
    }
  } catch (e) {}

  return fetch('/api/notifications', { credentials: 'same-origin', headers })
    .then(async (r) => {
      if (!r.ok) {
        let txt = null;
        try { txt = await r.text(); } catch (e) {}
        console.warn('Notifications endpoint returned non-OK status', r.status, txt);
        // If server returned HTML login page (redirect), detect by looking for '<!DOCTYPE' or '<html'
        const lower = (txt || '').toLowerCase();
        if (lower.includes('<!doctype') || lower.includes('<html') || lower.includes('đăng nhập') || lower.includes('login')) {
          return { items: [], authRequired: true, error: 'auth' };
        }
        return { items: [], authRequired: false, error: 'status-' + r.status };
      }
      const ct = (r.headers.get('content-type') || '').toLowerCase();
      if (ct.includes('application/json')) {
        try {
          const data = await r.json();
          return { items: Array.isArray(data) ? data : [], authRequired: false };
        } catch (e) {
          // invalid json
          let txt = null;
          try { txt = await r.text(); } catch (ex) {}
          console.warn('Invalid JSON from /api/notifications', e, txt);
          return { items: [], authRequired: false, error: 'invalid-json' };
        }
      }
      // not JSON (HTML or text) — log for debugging and detect login page
      let txt = null;
      try { txt = await r.text(); } catch (e) {}
      const lower = (txt || '').toLowerCase();
      console.warn('Non-JSON response from /api/notifications', ct, txt);
      if (lower.includes('<!doctype') || lower.includes('<html') || lower.includes('đăng nhập') || lower.includes('login')) {
        return { items: [], authRequired: true, error: 'html-login' };
      }
      return { items: [], authRequired: false, error: 'non-json' };
    })
    .catch((err) => {
      console.warn('Failed to fetch /api/notifications', err);
      return { items: [], authRequired: false, error: err && err.message ? String(err.message) : 'network' };
    });
}

// Add load more support: pageSize and lastTimestamp as cursor
let notifPageSize = 20;
let notifLoadCursor = null; // timestamp cursor for pagination (oldest loaded)

function buildFloatingListItem(item) {
  const li = document.createElement('li');
  li.className = 'list-group-item d-flex justify-content-between align-items-start';
  li.dataset.id = item.id;
  const time = formatNotifTime(item.ts);
  li.innerHTML = `<div class="ms-2 me-auto"><div class="fw-bold">${escapeHtml(item.title)}</div><div class="small text-muted">${escapeHtml(item.message)}</div></div><div class="small text-muted text-nowrap ms-2">${escapeHtml(time)}</div>`;
  li.addEventListener('click', (ev) => {
    ev.stopPropagation();
    if (item.url) {
      window.location.href = item.url;
    } else {
      markItemRead(item.id);
      li.classList.add('list-group-item-secondary');
    }
  });
  if (item.read) li.classList.add('list-group-item-secondary');
  return li;
}

function toggleNotificationDropdown() {
  console.debug && console.debug('toggleNotificationDropdown called');
  // If floating panel exists, remove it (toggle off)
  const existing = document.getElementById('notification-floating-panel');
  if (existing) {
    existing.remove();
    return;
  }

  const bell = document.getElementById('notification-bell') || document.querySelector('.notification-bell');
  const rect = bell ? bell.getBoundingClientRect() : { top: 60, left: window.innerWidth - 360, bottom: 60 };

  // create floating panel
  const panel = document.createElement('div');
  panel.id = 'notification-floating-panel';
  panel.style.position = 'fixed';
  panel.style.zIndex = 9999;
  panel.style.background = '#fff';
  const top = (rect.bottom || 60) + 8 + window.scrollY;
  let left = (rect.left || (window.innerWidth - 360)) + window.scrollX;
  if (left + 340 > window.innerWidth) left = window.innerWidth - 350;
  panel.style.top = top + 'px';
  panel.style.left = Math.max(8, left) + 'px';
  panel.style.width = '340px';
  panel.style.boxShadow = '0 6px 18px rgba(0,0,0,0.15)';
  panel.style.borderRadius = '4px';

  panel.innerHTML = `
    <div class="card">
      <div class="card-header d-flex justify-content-between align-items-center">
        <strong>Thông báo</strong>
        <div>
          <button id="floating-mark-all" class="btn btn-sm btn-link">Đánh dấu đã đọc</button>
          <button id="floating-close" class="btn btn-sm btn-link">Đóng</button>
        </div>
      </div>
      <ul id="notification-list-floating" class="list-group list-group-flush" style="max-height:360px; overflow:auto"></ul>
      <div class="card-footer text-center"><a href="/notifications.html">Xem tất cả</a></div>
    </div>
  `;

  document.body.appendChild(panel);

  const closeBtn = document.getElementById('floating-close');
  if (closeBtn) closeBtn.addEventListener('click', () => panel.remove());
  const markBtn = document.getElementById('floating-mark-all');
  if (markBtn) markBtn.addEventListener('click', () => { markAllRead(); panel.remove(); });

  const listEl = document.getElementById('notification-list-floating');
  if (!listEl) return;
  listEl.innerHTML = '<li class="list-group-item text-muted">Đang tải...</li>';

  // use safe fetch helper to handle HTML or non-JSON responses gracefully
  fetchNotificationsSafe()
    .then(res => {
      listEl.innerHTML = '';
      if (!res) {
        listEl.innerHTML = '<li class="list-group-item text-danger">Không thể tải thông báo</li>';
        return;
      }
      if (res.authRequired) {
        // if we already have realtime notifications in memory, show them as fallback
        if (Array.isArray(notifList) && notifList.length > 0) {
          const items = notifList.slice().map(it => ({
            id: it.id || ('notif-' + Date.now()),
            type: it.type,
            title: it.title,
            message: it.message,
            url: it.url || (it.productId ? '/product-detail.html?id=' + it.productId : null),
            productId: it.productId || null,
            ts: it.ts || it.timestamp || Date.now(),
            read: it.read || false
          })).reverse();
          items.forEach(it => listEl.appendChild(buildFloatingListItem(it)));
          return;
        }
        // otherwise show login prompt
        const redirect = encodeURIComponent(window.location.pathname + window.location.search);
        listEl.innerHTML = `<li class="list-group-item text-warning">Bạn cần <a href="/login.html?redirect=${redirect}">đăng nhập</a> để xem lịch sử thông báo</li>`;
        return;
      }
       const serverItems = Array.isArray(res.items) ? res.items : [];
       let items = serverItems.slice().reverse();
       if ((!items || items.length === 0) && Array.isArray(notifList) && notifList.length > 0) {
         // fallback to in-memory websocket notifications so dropdown matches badge count
         items = notifList.slice().reverse();
       }
       if (!items || items.length === 0) {
         listEl.innerHTML = '<li class="list-group-item text-muted">Không có thông báo</li>';
         return;
       }
       items.forEach(it => {
         const item = {
           id: it.id || ('notif-' + (it.timestamp || it.ts || Date.now())),
           type: it.type,
           title: it.title,
           message: it.message,
           url: it.url || (it.productId ? '/product-detail.html?id=' + it.productId : null),
           productId: it.productId || null,
           ts: it.timestamp || it.ts || Date.now(),
           read: it.read || false
         };
         const li = buildFloatingListItem(item);
         listEl.appendChild(li);
       });
     })
     .catch(err => {
       console.warn('Failed to load notifications', err);
       listEl.innerHTML = '<li class="list-group-item text-danger">Không thể tải thông báo</li>';
     });

  // close panel when clicking outside
  function onDocClick(ev) {
    if (!panel.contains(ev.target) && ev.target !== bell && !bell.contains(ev.target)) {
      panel.remove();
      document.removeEventListener('click', onDocClick);
    }
  }
  setTimeout(() => document.addEventListener('click', onDocClick), 50);
}

// Ensure dropdown exists and return the list element
function ensureNotificationDropdown() {
  let listEl = document.getElementById('notification-list');
  let wrapper = document.getElementById('notification-bell-container');
  if (!wrapper) {
    // try to find header container to insert into
    const container = document.getElementById('header-container') || document.querySelector('.navbar-main .container');
    wrapper = document.createElement('div');
    wrapper.id = 'notification-bell-container';
    wrapper.className = 'notification-bell-container me-3';
    wrapper.style.position = 'relative';
    wrapper.innerHTML = `
      <a href="#" id="notification-bell" class="notification-icon">
        <i class="bi bi-bell"></i>
        <span class="badge" id="notification-badge">0</span>
      </a>
      <div id="notification-dropdown" class="notification-dropdown" style="display:none; position:absolute; right:10px; top:40px; z-index:1050; width:320px;">
        <div class="card">
          <div class="card-header d-flex justify-content-between align-items-center">
            <strong>Thông báo</strong>
            <button class="btn btn-sm btn-link" id="mark-all-read">Đánh dấu đã đọc</button>
          </div>
          <ul class="list-group list-group-flush" id="notification-list" style="max-height:320px; overflow:auto"></ul>
          <div class="card-footer text-center"><a href="/notifications.html">Xem tất cả</a></div>
        </div>
      </div>
    `;
    try {
      if (container) container.appendChild(wrapper);
      else document.body.appendChild(wrapper);
    } catch (e) {
      document.body.appendChild(wrapper);
    }
  }

  // ensure event binding for bell
  const bell = document.getElementById('notification-bell');
  if (bell && !bell._notifBound) {
    bell.addEventListener('click', (ev) => { ev.preventDefault(); toggleNotificationDropdown(); });
    bell._notifBound = true;
  }

  // bind mark-all-read button
  const markBtn = document.getElementById('mark-all-read');
  if (markBtn && !markBtn._notifBound) {
    markBtn.addEventListener('click', (ev) => { ev.preventDefault(); markAllRead(); });
    markBtn._notifBound = true;
  }

  listEl = document.getElementById('notification-list');
  return listEl;
}

// Ensure there's always a delegated click handler so bell clicks work even if script loaded before header
if (!window.__notifDelegatedClickBound) {
  document.addEventListener('click', function (ev) {
    try {
      const target = ev.target;
      // find bell element by id or wrapper class
      const bellEl = target.closest ? target.closest('#notification-bell, .notification-bell, [data-notification-bell]') : null;
      if (bellEl) {
        ev.preventDefault();
        toggleNotificationDropdown();
      }
    } catch (e) {}
  }, true);
  window.__notifDelegatedClickBound = true;
}

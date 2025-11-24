// notifications.js
// Manage site-wide realtime notifications (new products, promotions)
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
          wrapper.className = "notification-bell me-3";
          wrapper.id = "notification-bell-container";
          wrapper.style.position = 'relative';

          wrapper.innerHTML = `
            <a href="#" id="notification-bell" class="me-2">
              <i class="bi bi-bell" style="font-size: 1.25rem; position: relative"></i>
              <span class="badge bg-danger" id="notification-badge" style="position: relative; top: -10px; left: -8px">0</span>
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
          // Prefer inserting into header container if available
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
    float.prepend(li);
    return;
  }

  const li = document.createElement("li");
  li.className = "list-group-item";
  li.dataset.id = item.id;
  const time = new Date(item.ts).toLocaleString();
  li.innerHTML = `<div class="notif-item"><div class="d-flex justify-content-between"><div><strong>${escapeHtml(item.title)}</strong><div class="small text-muted">${escapeHtml(item.message)}</div></div><div class="small text-muted">${escapeHtml(time)}</div></div></div>`;
  // click opens url if exists
  li.addEventListener("click", () => {
    if (item.url) {
      window.location.href = item.url;
    } else {
      // mark this item read: remove badge count
      markItemRead(item.id);
    }
  });
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
}

function markItemRead(id) {
  // optional implementation: remove that item or mark visually
  markAllRead();
}

function toggleNotificationDropdown(e) {
  const drop = document.getElementById("notification-dropdown");
  if (!drop) return;
  if (drop.style.display === "none" || drop.style.display === "") {
    drop.style.display = "block";
    // open: clear unread
    markAllRead();
  } else {
    drop.style.display = "none";
  }
}

function escapeHtml(text) {
  if (text === null || text === undefined) return "";
  return String(text).replace(/[&<>\"'\n]/g, function (m) {
    return {
      "&": "&amp;",
      "<": "&lt;",
      ">": "&gt;",
      '"': "&quot;",
      "'": "&#039;",
      "\n": " ",
    }[m];
  });
}

// expose for debug
window.initializeNotifications = initializeNotifications;
// Expose pushNotification so you can trigger notifications from the browser console while testing
window.pushNotification = pushNotification;

// Optional helper for quick product notification testing from console:
window.testProductNotification = function(productId = 1) {
  pushNotification({
    id: 'test-' + Date.now(),
    type: 'product',
    title: 'Test: Sản phẩm mới',
    message: 'Click để xem chi tiết sản phẩm ' + productId,
    productId: productId,
    timestamp: Date.now()
  });
};

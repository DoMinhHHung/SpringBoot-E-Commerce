// notifications.js
// Manage site-wide realtime notifications (new products, promotions)
let notifStomp = null;
let notifConnected = false;
let notifUnread = 0;
let notifList = [];

function initializeNotifications() {
  // attach UI handlers
  const bell = document.getElementById("notification-bell");
  const badge = document.getElementById("notification-badge");
  const listEl = document.getElementById("notification-list");
  const markAllBtn = document.getElementById("mark-all-read");

  if (!badge) return;

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
  };
  notifList.unshift(item);

  // increment unread
  notifUnread = (notifUnread || 0) + 1;
  try {
    localStorage.setItem("site_notif_unread", String(notifUnread));
  } catch (e) {}
  updateBadge();
  renderNotificationItem(item);

  // browser native notification
  try {
    if ("Notification" in window && Notification.permission === "granted") {
      const n = new Notification(item.title, { body: item.message });
      setTimeout(() => n.close(), 4000);
    }
  } catch (e) {}
}

function renderNotificationItem(item) {
  const listEl = document.getElementById("notification-list");
  if (!listEl) return;
  const li = document.createElement("li");
  li.className = "list-group-item";
  li.dataset.id = item.id;
  const time = new Date(item.ts).toLocaleString();
  li.innerHTML = `<div class="notif-item"><div class="d-flex justify-content-between"><div><strong>${escapeHtml(
    item.title
  )}</strong><div class="small text-muted">${escapeHtml(
    item.message
  )}</div></div><div class="small text-muted">${escapeHtml(
    time
  )}</div></div></div>`;
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

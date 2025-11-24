class AdminSupportApp {
  constructor(adminId) {
    this.adminId = adminId || "admin";
    this.stomp = null;
    this.connected = false;
    this.activeSessionId = null;
    this.sessions = {}; // sessionId -> messages[]
    this._retries = 0;
    this._maxRetries = 10;
  }

  connect() {
    const socket = new SockJS("/ws");
    this.stomp = Stomp.over(socket);
    this.stomp.debug = null;
    this.stomp.connect(
      {},
      () => {
        this.connected = true;
        this.subscribeIncoming();
        this.renderStatus("Đã kết nối");
        this._retries = 0;
        // request notification permission
        if ("Notification" in window && Notification.permission === "default") {
          Notification.requestPermission().catch(() => {});
        }
        // fetch pending requests on load
        this.fetchPending();
      },
      (err) => {
        console.error("Admin WS error", err);
        this.connected = false;
        this.renderStatus("Mất kết nối - Đang thử kết nối lại...");
        this.scheduleReconnect();
      }
    );

    if (this.stomp && this.stomp.ws) {
      this.stomp.ws.onclose = () => {
        if (this.connected) return;
        this.renderStatus("Mất kết nối - Đang thử kết nối lại...");
        this.scheduleReconnect();
      };
    }
  }

  scheduleReconnect() {
    const attempt = Math.min(this._retries + 1, this._maxRetries);
    this._retries = attempt;
    const delay = Math.min(30000, 1000 * Math.pow(2, attempt - 1));
    setTimeout(() => this.connect(), delay);
  }

  async fetchPending() {
    try {
      const res = await fetch("/api/support/pending", {
        credentials: "same-origin",
      });
      if (!res.ok) return;
      const list = await res.json();
      if (Array.isArray(list)) {
        if (list.length === 0) this.showEmptyPending();
        list.forEach((item) => this.addPendingRequest(item));
        // notify if there are pending items at load
        if (
          list.length > 0 &&
          "Notification" in window &&
          Notification.permission === "granted"
        ) {
          const n = new Notification("Bạn có yêu cầu tư vấn đang chờ", {
            body: `Tổng: ${list.length}`,
            tag: "pending-initial",
          });
          setTimeout(() => n.close(), 4000);
        }
      }
    } catch (e) {
      // ignore
    }
  }

  showEmptyPending() {
    const list = document.getElementById("pending-list");
    if (!list) return;
    if (!list.querySelector(".empty-item")) {
      const li = document.createElement("li");
      li.className = "list-group-item text-muted empty-item";
      li.textContent = "Đang chờ yêu cầu mới...";
      list.appendChild(li);
    }
  }

  subscribeIncoming() {
    this.stomp.subscribe("/topic/admin.incoming", (frame) => {
      const data = JSON.parse(frame.body);
      const listEl = document.getElementById("pending-list");
      // support both single object and array payloads
      const handleItem = (item) => {
        if (!item || !item.sessionId) return;
        // remove empty marker if exists
        const empty = listEl && listEl.querySelector(".empty-item");
        if (empty) empty.remove();
        // normalize question field: server may send lastQuestion
        if (!item.question && item.lastQuestion)
          item.question = item.lastQuestion;
        this.addPendingRequest(item);
        // push notification for each item
        try {
          if (
            "Notification" in window &&
            Notification.permission === "granted"
          ) {
            const n = new Notification("Yêu cầu tư vấn mới", {
              body:
                (item.question || "Khách hàng cần tư vấn") +
                "\nPhiên: " +
                item.sessionId,
              tag: item.sessionId,
            });
            setTimeout(() => n.close(), 5000);
          }
        } catch (e) {}
      };

      if (Array.isArray(data)) {
        data.forEach((d) => handleItem(d));
      } else {
        handleItem(data);
      }
    });
  }

  joinSession(sessionId) {
    this.activeSessionId = sessionId;
    this.stomp.send(
      "/app/support/join",
      {},
      JSON.stringify({ adminId: this.adminId, sessionId })
    );
    this.stomp.subscribe("/topic/admin.session." + sessionId, (frame) => {
      const msg = JSON.parse(frame.body);
      this.appendMessage(sessionId, msg);
    });
    this.clearPending(sessionId);
    this.renderActiveSession(sessionId);
    const endBtn = document.getElementById("end-session");
    if (endBtn) {
      endBtn.onclick = () => this.closeSession();
      endBtn.disabled = false;
    }
  }

  sendMessage(text) {
    if (!this.activeSessionId || !text) return;
    const payload = {
      sessionId: this.activeSessionId,
      adminId: this.adminId,
      text: text,
      timestamp: Date.now(),
    };
    this.stomp.send("/app/support/adminSend", {}, JSON.stringify(payload));
  }

  closeSession() {
    if (!this.activeSessionId) return;
    const payload = { sessionId: this.activeSessionId, adminId: this.adminId };
    this.stomp.send("/app/support/close", {}, JSON.stringify(payload));
    const endBtn = document.getElementById("end-session");
    if (endBtn) endBtn.disabled = true;
  }

  // UI helpers below (vanilla, minimal)
  addPendingRequest(req) {
    const list = document.getElementById("pending-list");
    const li = document.createElement("li");
    li.dataset.sessionId = req.sessionId;
    li.className =
      "list-group-item d-flex justify-content-between align-items-center";
    li.innerHTML = `
            <div>
                <div><strong>Phiên:</strong> ${this.escape(req.sessionId)}</div>
                <div><small>${this.escape(req.question || "")}</small></div>
            </div>
            <button class="btn btn-sm btn-primary">Nhận</button>
        `;
    li.querySelector("button").addEventListener("click", () =>
      this.joinSession(req.sessionId)
    );
    list.prepend(li);
  }

  clearPending(sessionId) {
    const list = document.getElementById("pending-list");
    const item = list.querySelector(
      `li[data-session-id="${CSS.escape(sessionId)}"]`
    );
    if (item) item.remove();
  }

  appendMessage(sessionId, msg) {
    if (!this.sessions[sessionId]) this.sessions[sessionId] = [];
    this.sessions[sessionId].push(msg);
    if (this.activeSessionId === sessionId) {
      const box = document.getElementById("chat-box");
      const isAdmin = !!msg.adminId;
      const who = isAdmin ? "Admin" : "KH";
      const div = document.createElement("div");
      div.className = isAdmin ? "text-end mb-2" : "text-start mb-2";
      div.innerHTML =
        `<span class="badge ${
          isAdmin ? "bg-primary" : "bg-secondary"
        }">${who}</span> ` + this.escape(msg.text || "");
      box.appendChild(div);
      box.scrollTop = box.scrollHeight;
    }
  }

  renderActiveSession(sessionId) {
    document.getElementById("active-session").textContent = sessionId;
    const box = document.getElementById("chat-box");
    box.innerHTML = "";
    (this.sessions[sessionId] || []).forEach((m) =>
      this.appendMessage(sessionId, m)
    );
  }

  renderStatus(text) {
    const el = document.getElementById("conn-status");
    if (el) el.textContent = text;
  }

  escape(t) {
    return String(t).replace(
      /[&<>"']/g,
      (m) =>
        ({
          "&": "&amp;",
          "<": "&lt;",
          ">": "&gt;",
          '"': "&quot;",
          "'": "&#039;",
        }[m])
    );
  }
}

window.addEventListener("DOMContentLoaded", () => {
  const app = new AdminSupportApp("admin");
  app.connect();
  const form = document.getElementById("send-form");
  if (form) {
    form.addEventListener("submit", (e) => {
      e.preventDefault();
      const input = document.getElementById("send-input");
      const txt = (input.value || "").trim();
      if (txt) {
        app.sendMessage(txt);
        input.value = "";
        input.focus();
      }
    });
  }
});

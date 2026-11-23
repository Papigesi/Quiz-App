// Ortak UI yardımcıları: navbar render etme, küçük utilites.

function escapeHtml(str) {
  if (str === null || str === undefined) return "";
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}

function formatDate(iso) {
  if (!iso) return "-";
  const d = new Date(iso);
  return d.toLocaleString("tr-TR", {
    year: "numeric", month: "2-digit", day: "2-digit",
    hour: "2-digit", minute: "2-digit",
  });
}

function renderNavbar(user) {
  const root = document.getElementById("navbar-root");
  if (!root) return;
  const isAdmin = user && user.role === "ADMIN";

  root.innerHTML = `
    <nav class="navbar">
      <a href="${isAdmin ? "/admin.html" : "/index.html"}" class="navbar-brand">QuizApp</a>
      <div class="navbar-links">
        ${isAdmin
          ? `<a href="/admin.html">Admin Paneli</a>`
          : `<a href="/index.html">Anasayfa</a>
             <a href="/history.html">Geçmişim</a>`}
        <a href="/leaderboard.html">Liderlik</a>
        <span class="navbar-user">
          ${escapeHtml(user.name)}
          ${isAdmin ? `<span class="badge badge-admin" style="margin-left:.35rem">ADMIN</span>` : ""}
        </span>
        <a href="#" id="logout-link">Çıkış</a>
      </div>
    </nav>
  `;

  document.getElementById("logout-link").addEventListener("click", (e) => {
    e.preventDefault();
    auth.logout();
  });
}

function showAlert(elementId, message, type = "danger") {
  const el = document.getElementById(elementId);
  if (!el) return;
  el.innerHTML = `<div class="alert alert-${type}">${escapeHtml(message)}</div>`;
}

function clearAlert(elementId) {
  const el = document.getElementById(elementId);
  if (el) el.innerHTML = "";
}

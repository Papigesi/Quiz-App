// Basit istemci tarafı oturum yönetimi: kullanıcı bilgisi localStorage'da tutulur.
// Bu projenin amacı homework olduğundan JWT/Session karmaşası kullanılmamıştır.

const SESSION_KEY = "quizapp_user";

const auth = {
  getUser() {
    const raw = localStorage.getItem(SESSION_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw);
    } catch (_) {
      return null;
    }
  },

  setUser(user) {
    localStorage.setItem(SESSION_KEY, JSON.stringify(user));
  },

  async logout() {
    try {
      await api.logout();
    } catch (_) {
      // oturum zaten kapalı olabilir
    }
    localStorage.removeItem(SESSION_KEY);
    window.location.href = "/login.html";
  },

  isAdmin() {
    const u = this.getUser();
    return !!u && u.role === "ADMIN";
  },

  isSuperAdmin() {
    const u = this.getUser();
    return !!u && u.superAdmin === true;
  },

  requireLogin({ adminOnly = false } = {}) {
    const user = this.getUser();
    if (!user) {
      window.location.href = "/login.html";
      return null;
    }
    if (adminOnly && user.role !== "ADMIN") {
      window.location.href = "/index.html";
      return null;
    }
    return user;
  },
};

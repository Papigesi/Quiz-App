// REST istemcisi: tüm /api çağrılarını tek noktadan yönetir.
// Hata durumunda backend'in döndürdüğü JSON'u Promise.reject ile aktarır.

const API_BASE = "/api";

async function apiRequest(path, options = {}) {
  const config = {
    credentials: "include",
    headers: { "Content-Type": "application/json", ...(options.headers || {}) },
    ...options,
  };
  if (config.body && typeof config.body !== "string") {
    config.body = JSON.stringify(config.body);
  }
  const res = await fetch(API_BASE + path, config);

  if (res.status === 204) return null;

  let data = null;
  try {
    data = await res.json();
  } catch (_) {
    // boş yanıt
  }

  if (!res.ok) {
    const err = new Error((data && data.message) || `HTTP ${res.status}`);
    err.status = res.status;
    err.body = data;
    throw err;
  }
  return data;
}

const api = {
  get: (path) => apiRequest(path),
  post: (path, body) => apiRequest(path, { method: "POST", body }),
  put: (path, body) => apiRequest(path, { method: "PUT", body }),
  delete: (path) => apiRequest(path, { method: "DELETE" }),

  // Auth
  login: (email, password) => api.post("/auth/login", { email, password }),
  register: (name, email, password) => api.post("/auth/register", { name, email, password }),
  logout: () => api.post("/auth/logout"),

  // Categories
  getCategories: () => api.get("/categories"),
  createCategory: (c) => api.post("/categories", c),
  updateCategory: (id, c) => api.put(`/categories/${id}`, c),
  deleteCategory: (id) => api.delete(`/categories/${id}`),

  // Quizzes
  getAllQuizzes: () => api.get("/quizzes"),
  getQuiz: (id) => api.get(`/quizzes/${id}`),
  createQuiz: (q) => api.post("/quizzes", q),
  updateQuiz: (id, q) => api.put(`/quizzes/${id}`, q),
  deleteQuiz: (id) => api.delete(`/quizzes/${id}`),
  getQuizQuestions: (id) => api.get(`/quizzes/${id}/questions`),
  getQuizQuestionsAdmin: (id) => api.get(`/quizzes/${id}/questions/admin`),
  getAvailableQuestions: (quizId) => api.get(`/quizzes/${quizId}/available-questions`),
  addQuestionToQuiz: (quizId, questionId) => api.post(`/quizzes/${quizId}/questions/${questionId}`),
  removeQuestionFromQuiz: (quizId, questionId) => api.delete(`/quizzes/${quizId}/questions/${questionId}`),

  // Questions (admin pool)
  getAllQuestionsForAdmin: (categoryId) =>
    api.get(`/questions/admin${categoryId ? `?categoryId=${categoryId}` : ""}`),
  createQuestion: (q) => api.post("/questions", q),
  updateQuestion: (id, q) => api.put(`/questions/${id}`, q),
  deleteQuestion: (id) => api.delete(`/questions/${id}`),

  // Results
  submitQuiz: (userId, quizId, answers) =>
    api.post("/results/submit", { userId, quizId, answers }),
  getUserHistory: (userId) => api.get(`/results/user/${userId}`),
  getLeaderboard: () => api.get("/results/leaderboard"),
  getAllResults: () => api.get("/results"),

  // Users
  getAllUsers: () => api.get("/users"),
  deleteUser: (id) => api.delete(`/users/${id}`),
  promoteUser: (targetId) => api.post(`/users/${targetId}/promote`),
  demoteUser: (targetId) => api.post(`/users/${targetId}/demote`),
};

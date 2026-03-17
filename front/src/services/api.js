import axios from 'axios';

const API_BASE_URL = 'http://localhost:8081/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const postApi = {
  getPosts: (access_token, page = 0, size = 20) => api.get('/posts', {
    params: { page, size },
    headers: { 'Authorization': `Bearer ${access_token}` }
  }),
  getPost: (id, access_token) => api.get(`/posts/${id}`, {
    headers: { 'Authorization': `Bearer ${access_token}` }
  }),
  createPost: (postData, access_token) => api.post('/posts', postData, {
    headers: { 'Authorization': `Bearer ${access_token}` }
  }),
  updatePost: (id, postData, access_token) => api.put(`/posts/${id}`, postData, {
    headers: { 'Authorization': `Bearer ${access_token}` }
  }),
  deletePost: (id, access_token) => api.delete(`/posts/${id}`, {
    headers: { 'Authorization': `Bearer ${access_token}` }
  }),

  getCategories: (access_token) => api.get('/categories', {
    headers: { 'Authorization': `Bearer ${access_token}` }
  }),
  getCategory: (id, access_token) => api.get(`/categories/${id}`, {
    headers: { 'Authorization': `Bearer ${access_token}` }
  }),
  createCategory: (categoryData, access_token) => api.post('/categories', categoryData, {
    headers: { 'Authorization': `Bearer ${access_token}` }
  }),
  updateCategory: (id, categoryData, access_token) => api.put(`/categories/${id}`, categoryData, {
    headers: { 'Authorization': `Bearer ${access_token}` }
  }),
  deleteCategory: (id, access_token) => api.delete(`/categories/${id}`, {
    headers: { 'Authorization': `Bearer ${access_token}` }
  }),

  getComments: (access_token) => api.get('/comments', {
    headers: { 'Authorization': `Bearer ${access_token}` }
  }),
  getPostComments: (postId, access_token) => api.get(`/comments/post/${postId}`, {
    headers: { 'Authorization': `Bearer ${access_token}` }
  }),
  createComment: (commentData, access_token) => api.post('/comments', commentData, {
    headers: { 'Authorization': `Bearer ${access_token}` }
  }),
  updateComment: (id, commentData, access_token) => api.put(`/comments/${id}`, commentData, {
    headers: { 'Authorization': `Bearer ${access_token}` }
  }),
  deleteComment: (id, access_token) => api.delete(`/comments/${id}`, {
    headers: { 'Authorization': `Bearer ${access_token}` }
  }),

  getUsers: (access_token) => api.get('/users', {
    headers: { 'Authorization': `Bearer ${access_token}` }
  }),
  getUser: (id, access_token) => api.get(`/users/${id}`, {
    headers: { 'Authorization': `Bearer ${access_token}` }
  }),
  updateUser: (id, userData, access_token) => api.put(`/users/${id}`, userData, {
    headers: { 'Authorization': `Bearer ${access_token}` }
  }),
  deleteUser: (id, access_token) => api.delete(`/users/${id}`, {
    headers: { 'Authorization': `Bearer ${access_token}` }
  }),

  login: (credentials) => api.post('/auth/login', credentials),
  register: (userData) => api.post('/auth/register', userData),
};

export default api;

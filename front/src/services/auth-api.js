import axios from 'axios';

const API_BASE_URL = 'http://localhost:8081/api';

class AuthApiService {
  constructor(getAccessToken) {
    this.getAccessToken = getAccessToken;
    this.setupInterceptors();
  }

  setupInterceptors() {
    // Request interceptor
    this.client = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.client.interceptors.request.use(
      async (config) => {
        // Skip for auth endpoints
        if (config.url?.includes('/auth/')) {
          return config;
        }

        try {
          const accessToken = await this.getAccessToken();
          if (accessToken) {
            config.headers.Authorization = `Bearer ${accessToken}`;
          }
        } catch (error) {
          console.error('Failed to get access token:', error);
        }

        return config;
      },
      (error) => Promise.reject(error)
    );

    // Response interceptor for 401 handling
    this.client.interceptors.response.use(
      (response) => response,
      async (error) => {
        const originalRequest = error.config;

        if (error.response?.status === 401 && !originalRequest._retry) {
          originalRequest._retry = true;

          try {
            const newAccessToken = await this.getAccessToken();
            if (newAccessToken) {
              originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
              return this.client(originalRequest);
            }
          } catch (refreshError) {
            console.error('Failed to refresh token:', refreshError);
            // Redirect to login
            window.location.href = '/';
            return Promise.reject(refreshError);
          }
        }

        return Promise.reject(error);
      }
    );
  }

  getPosts(page = 0, size = 20) {
    return this.client.get('/posts', { params: { page, size } });
  }

  getPost(id) {
    return this.client.get(`/posts/${id}`);
  }

  createPost(postData) {
    return this.client.post('/posts', postData);
  }

  updatePost(id, postData) {
    return this.client.put(`/posts/${id}`, postData);
  }

  deletePost(id) {
    return this.client.delete(`/posts/${id}`);
  }

  getCategories() {
    return this.client.get('/categories');
  }

  getCategory(id) {
    return this.client.get(`/categories/${id}`);
  }

  createCategory(categoryData) {
    return this.client.post('/categories', categoryData);
  }

  updateCategory(id, categoryData) {
    return this.client.put(`/categories/${id}`, categoryData);
  }

  deleteCategory(id) {
    return this.client.delete(`/categories/${id}`);
  }

  getComments() {
    return this.client.get('/comments');
  }

  getPostComments(postId) {
    return this.client.get(`/comments/post/${postId}`);
  }

  createComment(commentData) {
    return this.client.post('/comments', commentData);
  }

  updateComment(id, commentData) {
    return this.client.put(`/comments/${id}`, commentData);
  }

  deleteComment(id) {
    return this.client.delete(`/comments/${id}`);
  }

  getUsers() {
    return this.client.get('/users');
  }

  getUser(id) {
    return this.client.get(`/users/${id}`);
  }

  updateUser(id, userData) {
    return this.client.put(`/users/${id}`, userData);
  }

  deleteUser(id) {
    return this.client.delete(`/users/${id}`);
  }

  login(credentials) {
    return this.client.post('/auth/login', credentials);
  }

  register(userData) {
    return this.client.post('/auth/register', userData);
  }

  getRoles() {
    return this.client.get('/auth/roles');
  }
}

export default AuthApiService;

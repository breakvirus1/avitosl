import React from 'react';
import { useAuth } from '../hooks/useAuth';
import PostList from './PostList.jsx';
import { useNavigate } from 'react-router-dom';
import './Home.css';
import './PostList.css';

function Home() {
  const navigate = useNavigate();
  const { isAuthenticated, apiService, user, logout } = useAuth();
  const [posts, setPosts] = React.useState([]);
  const [loading, setLoading] = React.useState(true);
  const [error, setError] = React.useState(null);
  const [pagination, setPagination] = React.useState({
    currentPage: 0,
    pageSize: 20,
    totalElements: 0,
    totalPages: 0
  });
  const [userRoles, setUserRoles] = React.useState([]);

  const fetchPosts = async (page = 0, size = 20) => {
    try {
      setLoading(true);
      const response = await apiService.getPosts(page, size);
      setPosts(response.data.content);
      setPagination({
        currentPage: page,
        pageSize: size,
        totalElements: response.data.totalElements,
        totalPages: response.data.totalPages
      });
      setError(null);
    } catch (err) {
      console.error('Error fetching posts:', err);
      setError(err.response?.data?.message || err.message || 'Failed to load posts');
    } finally {
      setLoading(false);
    }
  };

  const fetchRoles = async () => {
    try {
      const response = await apiService.getRoles();
      setUserRoles(response.data);
    } catch (err) {
      console.error('Error fetching roles:', err);
    }
  };

  React.useEffect(() => {
    if (isAuthenticated) {
      fetchPosts(pagination.currentPage, pagination.pageSize);
      fetchRoles();
    }
  }, [isAuthenticated, pagination.currentPage, pagination.pageSize]);

  const handleDeletePost = async (postId) => {
    try {
      await apiService.deletePost(postId);
      await fetchPosts(pagination.currentPage, pagination.pageSize);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to delete post');
    }
  };

  const handlePageChange = (newPage) => {
    fetchPosts(newPage, pagination.pageSize);
  };

  const hasRole = (roleName) => {
    return userRoles.includes(roleName);
  };

  const isAdmin = hasRole('ADMIN');
  const isUser = hasRole('USER');

  const handleLogout = async () => {
    try {
      await logout();
    } catch (err) {
      console.error('Logout failed:', err);
    }
    navigate('/');
  };

  const handleUserClick = () => {
    navigate('/profile');
  };

  return (
    <div className="home-container">
      <header className="home-header">
        <div className="home-header-inner">
          <h1 className="home-header-logo">Avito</h1>
          <div className="home-header-user">
            <button
              onClick={handleUserClick}
              className="home-header-username-btn"
            >
              {user?.firstName || user?.email || 'Пользователь'}
            </button>
            <button
              onClick={handleLogout}
              className="home-header-logout"
            >
              Выйти
            </button>
          </div>
        </div>
      </header>
      <main className="home-main">
        <div className="home-content-wrapper">
          {error && (
            <div className="home-error">
              <strong>Ошибка</strong>
              <p>{error}</p>
              <button
                onClick={() => setError(null)}
                className="home-error-close"
              >
                ×
              </button>
            </div>
          )}

          <div className="home-create-section">
            <button
              onClick={() => navigate('/create-post')}
              className="home-create-btn"
            >
              <span>+</span> Создать объявление
            </button>
          </div>

          <div className="home-layout">
            <div className="home-sidebar">
              {isUser ? (
                <div className="home-info-card user">
                  <strong>Быстрая форма</strong>
                  <p>
                    Используйте форму справа для быстрого создания объявления. Для расширенных опций перейдите на страницу создания.
                  </p>
                </div>
              ) : (
                <div className="home-info-card non-user">
                  <strong>Доступ ограничен</strong>
                  <p>Только авторизованные пользователи могут создавать объявления</p>
                </div>
              )}
            </div>
            <div className="home-main-content">
              <PostList
                posts={posts}
                onDelete={handleDeletePost}
                loading={loading}
                isAdmin={isAdmin}
                pagination={pagination}
                onPageChange={handlePageChange}
              />
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}

export default Home;

import React from 'react';
import { useAuth } from '../hooks/useAuth';
import PostList from './PostList.jsx';
import { useNavigate } from 'react-router-dom';

function Home() {
  const navigate = useNavigate();
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
  const { isAuthenticated, apiService } = useAuth();

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
  }, [isAuthenticated]);

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

  return (
    <div>
      <header style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        background: '#001529',
        padding: '0 20px',
        height: '64px'
      }}>
        <h1 style={{ color: 'white', margin: 0, fontSize: '24px' }}>Avito</h1>
      </header>
      <main style={{ padding: '24px', minHeight: 'calc(100vh - 64px)' }}>
        {error && (
          <div style={{
            background: '#fff2f0',
            border: '1px solid #ffccc7',
            borderRadius: '6px',
            padding: '16px',
            marginBottom: '16px',
            color: '#ff4d4f',
            position: 'relative'
          }}>
            <strong>Ошибка</strong>
            <p style={{ margin: '8px 0 0 0' }}>{error}</p>
            <button
              onClick={() => setError(null)}
              style={{
                position: 'absolute',
                right: '16px',
                top: '16px',
                background: 'transparent',
                border: 'none',
                cursor: 'pointer',
                fontSize: '16px'
              }}
            >
              ×
            </button>
          </div>
        )}

        <div style={{ marginBottom: '24px' }}>
          <button
            onClick={() => navigate('/create-post')}
            style={{
              padding: '10px 20px',
              borderRadius: '4px',
              border: 'none',
              background: isAuthenticated ? '#1890ff' : '#d9d9d9',
              color: isAuthenticated ? 'white' : '#000000d9',
              cursor: isAuthenticated ? 'pointer' : 'not-allowed',
              fontSize: '14px',
              fontWeight: 500,
              display: 'flex',
              alignItems: 'center',
              gap: '8px'
            }}
            disabled={!isAuthenticated}
          >
            <span>+</span> Создать объявление
          </button>
        </div>

        <div style={{ display: 'flex', gap: '24px' }}>
          <div style={{ flex: '0 0 25%' }}>
            {isUser ? (
              <div style={{
                background: '#e6f7ff',
                border: '1px solid #91d5ff',
                borderRadius: '6px',
                padding: '16px',
                color: '#1890ff'
              }}>
                <strong>Быстрая форма</strong>
                <p style={{ margin: '8px 0 0 0', fontSize: '12px' }}>
                  Используйте форму справа для быстрого создания объявления. Для расширенных опций перейдите на страницу создания.
                </p>
              </div>
            ) : (
              <div style={{
                background: '#fffbe6',
                border: '1px solid #ffe58f',
                borderRadius: '6px',
                padding: '16px',
                color: '#faad14'
              }}>
                <strong>Доступ ограничен</strong>
                <p style={{ margin: '8px 0 0 0' }}>Только авторизованные пользователи могут создавать объявления</p>
              </div>
            )}
          </div>
          <div style={{ flex: '1' }}>
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
      </main>
    </div>
  );
}

export default Home;

import React from 'react';
import { useAuth } from '../hooks/useAuth';
import PostList from './PostList.jsx';
import './Home.css';
import './PostList.css';

function HomePublic() {
  const { login } = useAuth();
  const [posts, setPosts] = React.useState([]);
  const [loading, setLoading] = React.useState(true);
  const [error, setError] = React.useState(null);
  const [pagination, setPagination] = React.useState({
    currentPage: 0,
    pageSize: 20,
    totalElements: 0,
    totalPages: 0
  });

  const fetchPosts = async (page = 0, size = 20) => {
    try {
      setLoading(true);
      const response = await fetch(`http://localhost:1291/api/posts?page=${page}&size=${size}`);
      if (!response.ok) {
        throw new Error('Failed to fetch posts');
      }
      const data = await response.json();
      setPosts(data.content);
      setPagination({
        currentPage: page,
        pageSize: size,
        totalElements: data.totalElements,
        totalPages: data.totalPages
      });
      setError(null);
    } catch (err) {
      console.error('Error fetching posts:', err);
      setError(err.message || 'Failed to load posts');
    } finally {
      setLoading(false);
    }
  };

  React.useEffect(() => {
    fetchPosts(pagination.currentPage, pagination.pageSize);
  }, [pagination.currentPage, pagination.pageSize]);

  const handlePageChange = (newPage) => {
    fetchPosts(newPage, pagination.pageSize);
  };

  const handleLogin = () => {
    login();
  };

  return (
    <div className="home-container">
      <header className="home-header">
        <div className="home-header-inner">
          <h1 className="home-header-logo">Avito</h1>
          <button
            onClick={handleLogin}
            className="home-header-login-btn"
          >
            Войти
          </button>
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

          <div className="home-main-content" style={{ width: '100%' }}>
            <PostList
              posts={posts}
              onDelete={() => {}}
              loading={loading}
              isAdmin={false}
              pagination={pagination}
              onPageChange={handlePageChange}
            />
          </div>
        </div>
      </main>
    </div>
  );
}

export default HomePublic;

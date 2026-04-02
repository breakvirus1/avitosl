import { useEffect, useState, useContext } from 'react';
import { AuthContext } from '../contexts/AuthContext';
import AuthApiService from '../services/auth-api';
import AuthBar from './AuthBar.jsx';
import PostList from './PostList.jsx';
import './HomePublic.css';

function HomePublic() {
  const { apiService: contextApiService } = useContext(AuthContext);
  // Используем apiService из контекста, если доступен, иначе создаем новый экземпляр
  const apiService = contextApiService || new AuthApiService(() => Promise.resolve(null));
  
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [pagination, setPagination] = useState({
    totalElements: 0,
    totalPages: 0,
    currentPage: 0
  });

  useEffect(() => {
    fetchPosts(0);
  }, []);

  const fetchPosts = async (page = 0) => {
    try {
      setLoading(true);
      const response = await apiService.getPosts(page, 20);
      setPosts(response.data.content);
      setPagination({
        totalElements: response.data.totalElements,
        totalPages: response.data.totalPages,
        currentPage: response.data.number
      });
      setError(null);
    } catch (err) {
      setError('Ошибка загрузки объявлений');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handlePageChange = (page) => {
    fetchPosts(page);
  };

  if (loading) {
    return (
      <div className="home-container">
        <div className="home-loading">
          <div className="spinner"></div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="home-container">
        <div className="home-error">
          <strong>Ошибка</strong>
          {error}
        </div>
        <button className="retry-btn" onClick={fetchPosts}>Попробовать снова</button>
      </div>
    );
  }

  return (
    <div className="home-container">
      <AuthBar />

      <main className="home-main">
        <div className="home-content-wrapper">
          <section className="posts-section">
            {error && (
              <div className="home-error">
                <strong>Ошибка</strong>
                {error}
              </div>
            )}
            <PostList
              posts={posts}
              loading={loading}
              isAdmin={false}
              pagination={pagination}
              onPageChange={handlePageChange}
              variant="grid"
            />
          </section>
        </div>
      </main>
    </div>
  );
}

export default HomePublic;

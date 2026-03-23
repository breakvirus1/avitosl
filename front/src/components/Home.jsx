import React, { useEffect, useState, useContext } from 'react';
import { AuthContext } from '../contexts/AuthContext';
import AuthBar from './AuthBar.jsx';
import PostList from './PostList.jsx';
import './Home.css';

function Home() {
  const { apiService } = useContext(AuthContext);
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

  if (loading) {
    return (
      <div className="home-container">
        <div className="home-loading">
          <div className="spinner"></div>
        </div>
      </div>
    );
  }

  return (
    <div className="home-container">
      <AuthBar />

      <main className="home-main">
        <div className="home-content-wrapper">
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
            onPageChange={fetchPosts}
            variant="list"
          />
        </div>
      </main>
    </div>
  );
}

export default Home;

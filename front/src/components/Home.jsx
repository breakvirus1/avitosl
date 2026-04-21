import { useEffect, useState, useContext } from 'react';
import { AuthContext } from '../contexts/AuthContext';
import AuthBar from './AuthBar.jsx';
import PostList from './PostList.jsx';
import Chat from './Chat.jsx';
import './Home.css';

function Home() {
  const { apiService: contextApiService, isAuthenticated } = useContext(AuthContext);
  const apiService = contextApiService;
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [pagination, setPagination] = useState({
    totalElements: 0,
    totalPages: 0,
    currentPage: 0
  });
  // Chat state
  const [showChat, setShowChat] = useState(false);
  const [chatReceiverId, setChatReceiverId] = useState(null);
  const [chatReceiverName, setChatReceiverName] = useState('');

  useEffect(() => {
    fetchPosts(0);
  }, []);

  // Check for pending chat from notification click
  useEffect(() => {
    if (isAuthenticated) {
      const pendingChat = sessionStorage.getItem('pendingChat');
      if (pendingChat) {
        try {
          const { receiverId, receiverName } = JSON.parse(pendingChat);
          if (receiverId && receiverName) {
            setChatReceiverId(receiverId);
            setChatReceiverName(receiverName);
            setShowChat(true);
          }
        } catch (err) {
          console.error('Error parsing pending chat:', err);
        } finally {
          sessionStorage.removeItem('pendingChat');
        }
      }
    }
  }, [isAuthenticated]);

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

  const handleCloseChat = () => {
    setShowChat(false);
    setChatReceiverId(null);
    setChatReceiverName('');
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
      <main className="home-main" style={{ maxWidth: '800px', margin: '0 auto', width: '100%' }}>
        <AuthBar />
        {showChat && (
          <Chat
            receiverId={chatReceiverId}
            receiverName={chatReceiverName}
            onClose={handleCloseChat}
          />
        )}

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

export default Home;

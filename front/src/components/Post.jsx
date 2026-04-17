import { useAuth } from "react-oidc-context";
import { useEffect, useState } from "react";
import axios from 'axios';
import { API_BASE_URL } from '../config';

const Posts = () => {
  const auth = useAuth();
  const [posts, setPosts] = useState([]);
  const [fetchFailed, setFetchFailed] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const getPosts = async () => {
    try {
      if (auth.isAuthenticated && auth.user) {
        setIsLoading(true);
        const accessToken = auth.user.access_token;

        const response = await axios.get(`${API_BASE_URL}/posts`, {
          headers: {
            'Authorization': `Bearer ${accessToken}`
          }
        });

        setPosts(response.data);
        setFetchFailed(false);
      }
    } catch (e) {
      console.error("ERROR fetching posts:", e);
      setFetchFailed(true);
      setErrorMessage(e.response?.data?.message || "Не удалось загрузить посты");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (auth.isAuthenticated) {
      getPosts();
    }
  }, [auth.isAuthenticated]);

  if (!auth.isAuthenticated) {
    return (
      <div style={{ padding: '24px' }}>
        <div style={{
          background: '#fffbe6',
          border: '1px solid #ffe58f',
          borderRadius: '6px',
          padding: '16px',
          color: '#faad14',
          display: 'flex',
          alignItems: 'center',
          gap: '12px'
        }}>
          <span style={{ fontSize: '24px' }}>⚠️</span>
          <div>
            <strong>Требуется авторизация</strong>
            <p style={{ margin: '4px 0 0 0' }}>Пожалуйста, войдите в систему для просмотра постов</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div style={{ padding: '24px' }}>
      <h2 style={{ marginBottom: '24px' }}>Посты</h2>

      {isLoading ? (
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <div style={{
            display: 'inline-block',
            width: '40px',
            height: '40px',
            border: '4px solid #f3f3f3',
            borderTop: '4px solid #1890ff',
            borderRadius: '50%',
            animation: 'spin 1s linear infinite'
          }}></div>
          <div style={{ marginTop: '16px' }}>Загрузка постов...</div>
        </div>
      ) : fetchFailed ? (
        <div style={{
          background: '#fff2f0',
          border: '1px solid #ffccc7',
          borderRadius: '6px',
          padding: '16px',
          color: '#ff4d4f',
          display: 'flex',
          alignItems: 'center',
          gap: '12px'
        }}>
          <span style={{ fontSize: '24px' }}>❌</span>
          <div>
            <strong>Ошибка загрузки</strong>
            <p style={{ margin: '4px 0 0 0' }}>{errorMessage}</p>
          </div>
        </div>
      ) : posts.length === 0 ? (
        <div style={{
          textAlign: 'center',
          padding: '50px',
          background: '#fafafa',
          borderRadius: '8px',
          border: '1px dashed #d9d9d9'
        }}>
          <h3 style={{ marginBottom: '16px' }}>Пока нет постов</h3>
          <p style={{ color: '#666' }}>Создайте свой первый пост!</p>
        </div>
      ) : (
        <div>
          {posts.map((post) => (
            <div
              key={post.id}
              style={{
                padding: "16px",
                marginBottom: "16px",
                border: "1px solid #d9d9d9",
                borderRadius: "6px",
                background: "#fafafa"
              }}
            >
              <h3 style={{ margin: '0 0 12px 0', fontSize: '20px' }}>{post.name}</h3>
              <p style={{ marginBottom: '12px', color: '#666' }}>{post.description}</p>
              <div style={{ display: 'flex', gap: '16px', flexWrap: 'wrap' }}>
                <div>
                  <strong>Цена:</strong> {post.price?.toLocaleString()} ₽
                </div>
                {post.category && (
                  <div>
                    <strong>Категория:</strong> {post.category.name}
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Posts;

import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import AuthBar from './AuthBar';
import './PostView.css';

function PostViewPublic() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { apiService } = useAuth();
  const [post, setPost] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentImageIndex, setCurrentImageIndex] = useState(0);

  useEffect(() => {
    if (id) {
      fetchPost();
    }
  }, [id]);

  const fetchPost = async () => {
    try {
      setLoading(true);
      const response = await apiService.getPost(id);
      setPost(response.data);
      setError(null);
    } catch (err) {
      console.error('Error fetching post:', err);
      setError(err.response?.data?.message || err.message || 'Failed to load post');
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '';
    return new Date(dateString).toLocaleDateString('ru-RU', {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const formatPrice = (price) => {
    if (!price) return 'Цена не указана';
    return `${price.toLocaleString()} ₽`;
  };

  const getPhotoUrl = (photoId) => {
    return `http://localhost:8081/api/photos/${photoId}/file`;
  };

  const handleNextImage = (e) => {
    e.stopPropagation();
    if (!post || !post.photos || post.photos.length <= 1) return;
    setCurrentImageIndex(prev =>
      prev === post.photos.length - 1 ? 0 : prev + 1
    );
  };

  const handlePrevImage = (e) => {
    e.stopPropagation();
    if (!post || !post.photos || post.photos.length <= 1) return;
    setCurrentImageIndex(prev =>
      prev === 0 ? post.photos.length - 1 : prev - 1
    );
  };

  const handleDotClick = (index, e) => {
    e.stopPropagation();
    setCurrentImageIndex(index);
  };

  // Автоматическая смена изображений каждые 3 секунды
  useEffect(() => {
    if (!post || !post.photos || post.photos.length <= 1) return;

    const interval = setInterval(() => {
      setCurrentImageIndex(prev =>
        prev === post.photos.length - 1 ? 0 : prev + 1
      );
    }, 3000);

    return () => clearInterval(interval);
  }, [post]);

  if (loading) {
    return (
      <div className="post-view-loading">
        <div className="post-view-spinner"></div>
        <p>Загрузка объявления...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="post-view-error">
        <strong>Ошибка</strong>
        <p>{error}</p>
        <button onClick={() => navigate('/')} className="post-view-back-btn">
          Назад к объявлениям
        </button>
      </div>
    );
  }

  if (!post) {
    return (
      <div className="post-view-not-found">
        <p>Объявление не найдено</p>
        <button onClick={() => navigate('/')} className="post-view-back-btn">
          Назад к объявлениям
        </button>
      </div>
    );
  }

  const photos = post.photos || [];
  const currentIndex = currentImageIndex;
  const currentPhoto = photos[currentIndex];
  const photoUrl = currentPhoto ? getPhotoUrl(currentPhoto.id) : null;

  return (
    <div className="post-view-wrapper">
      <div style={{ maxWidth: '800px', margin: '0 auto', width: '100%' }}>
        <AuthBar />
        <div className="post-view-content">
        <div className="post-view-main">
          <div className="post-view-header">
            <h1 className="post-view-title">{post.title}</h1>
            <div className="post-view-meta">
              <span className="post-view-date">
                {formatDate(post.createdAt)}
              </span>
              {post.category && (
                <span className="post-view-category">
                  {post.category.name}
                </span>
              )}
              {post.subcategory && (
                <span className="post-view-subcategory">
                  {post.subcategory.name}
                </span>
              )}
            </div>
          </div>

          {photos.length > 0 && (
            <div className="post-view-image-container">
              <img
                src={photoUrl}
                alt={`${post.title} - фото ${currentIndex + 1}`}
                className="post-view-image"
              />
              {photos.length > 1 && (
                <>
                  <button
                    className="post-view-image-carousel-nav prev"
                    onClick={handlePrevImage}
                  >
                    ‹
                  </button>
                  <button
                    className="post-view-image-carousel-nav next"
                    onClick={handleNextImage}
                  >
                    ›
                  </button>
                  <div className="post-view-image-carousel-dots">
                    {photos.map((_, idx) => (
                      <div
                        key={idx}
                        className={`post-view-image-carousel-dot ${idx === currentIndex ? 'active' : ''}`}
                        onClick={(e) => handleDotClick(idx, e)}
                      />
                    ))}
                  </div>
                </>
              )}
            </div>
          )}

          <div className="post-view-description">
            <h3>Описание</h3>
            <p>{post.description || 'Без описания'}</p>
          </div>
        </div>

        <div className="post-view-sidebar">
          <div className="post-view-price-card">
            <div className="post-view-price">
              {formatPrice(post.price)}
            </div>
            <div className="post-view-status">
              {post.active ? (
                <span className="post-view-status-active">Активно</span>
              ) : (
                <span className="post-view-status-inactive">Неактивно</span>
              )}
            </div>
          </div>

          <div className="post-view-author-card">
            <h4>Продавец</h4>
            {post.author && (
              <>
                <p className="post-view-author-name">
                  {post.author.firstName} {post.author.lastName || ''}
                </p>
                <p className="post-view-author-email">
                  {post.author.email}
                </p>
              </>
            )}
          </div>

          {/* Для неавторизованных пользователей показываем кнопки с редиректом на вход */}
          <div className="post-view-actions">
            <button
              className="post-view-contact-btn"
              onClick={() => navigate('/')}
            >
              Написать продавцу
            </button>
            {post.active && (
              <button
                className="post-view-purchase-btn"
                onClick={() => navigate('/')}
              >
                Купить
              </button>
            )}
          </div>

          <div className="post-view-public-notice">
            <p>Для связи с продавцом или покупки товара необходимо войти в систему</p>
          </div>

          <div className="post-view-dates">
            <p>Создано: {formatDate(post.createdAt)}</p>
            {post.updatedAt && (
              <p>Обновлено: {formatDate(post.updatedAt)}</p>
            )}
          </div>
        </div>
        </div>
      </div>
    </div>
  );
}

export default PostViewPublic;
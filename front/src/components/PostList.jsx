import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './PostList.css';

function PostList({ posts, onDelete, loading, isAdmin, pagination, onPageChange, variant = 'list' }) {
  const navigate = useNavigate();
  const isGrid = variant === 'grid';

  // Состояние для слайдера изображений: { [postId]: currentIndex }
  const [imageIndices, setImageIndices] = useState({});

  // Автоматическая смена изображений каждые 3 секунды
  useEffect(() => {
    const intervals = [];
    
    posts.forEach((post) => {
      if (post.photos && post.photos.length > 1) {
        const interval = setInterval(() => {
          setImageIndices(prev => ({
            ...prev,
            [post.id]: (prev[post.id] || 0) === post.photos.length - 1 ? 0 : (prev[post.id] || 0) + 1
          }));
        }, 3000);
        intervals.push(interval);
      }
    });

    return () => {
      intervals.forEach(clearInterval);
    };
  }, [posts]);

  const handleNextImage = (postId, e) => {
    e.stopPropagation();
    const post = posts.find(p => p.id === postId);
    if (!post || !post.photos || post.photos.length <= 1) return;
    
    setImageIndices(prev => ({
      ...prev,
      [postId]: (prev[postId] || 0) === post.photos.length - 1 ? 0 : (prev[postId] || 0) + 1
    }));
  };

  const handlePrevImage = (postId, e) => {
    e.stopPropagation();
    const post = posts.find(p => p.id === postId);
    if (!post || !post.photos || post.photos.length <= 1) return;
    
    setImageIndices(prev => ({
      ...prev,
      [postId]: (prev[postId] || 0) === 0 ? post.photos.length - 1 : (prev[postId] || 0) - 1
    }));
  };

  const handleDotClick = (postId, index, e) => {
    e.stopPropagation();
    setImageIndices(prev => ({
      ...prev,
      [postId]: index
    }));
  };

  const getPhotoUrl = (photoId) => {
    return `http://localhost:8081/api/photos/${photoId}/file`;
  };

  if (loading) {
    return (
      <div className="post-list-loading">
        <div className="spinner"></div>
        <div>Загрузка объявлений...</div>
      </div>
    );
  }

  if (!posts || posts.length === 0) {
    return (
      <div className="post-list-empty">
        <div className="post-list-empty-icon">📭</div>
        <p className="post-list-empty-text">Нет объявлений</p>
        <button className="post-list-empty-btn">Создать первое объявление</button>
      </div>
    );
  }

  return (
    <div className="post-list">
      <div className="post-list-header">
        <h2 className="post-list-title">Объявления</h2>
        <span className="post-list-count">({pagination.totalElements})</span>
      </div>

      <div className={isGrid ? 'post-list-grid' : 'post-list-list'}>
        {posts.map((post) => {
          const photos = post.photos || [];
          const currentIndex = imageIndices[post.id] || 0;
          const currentPhoto = photos[currentIndex];
          const photoUrl = currentPhoto ? getPhotoUrl(currentPhoto.id) : null;

          if (isGrid) {
            return (
              <div
                key={post.id}
                className="post-card"
                onClick={() => navigate(`/post/${post.id}`)}
              >
                <div className="post-card-image-container">
                  {photoUrl ? (
                    <>
                      <img
                        src={photoUrl}
                        alt={post.title}
                        className="post-card-image"
                      />
                      {photos.length > 1 && (
                        <>
                          <button
                            className="post-card-image-carousel-nav prev"
                            onClick={(e) => handlePrevImage(post.id, e)}
                          >
                            ‹
                          </button>
                          <button
                            className="post-card-image-carousel-nav next"
                            onClick={(e) => handleNextImage(post.id, e)}
                          >
                            ›
                          </button>
                          <div className="post-card-image-carousel-dots">
                            {photos.map((_, idx) => (
                              <div
                                key={idx}
                                className={`post-card-image-carousel-dot ${idx === currentIndex ? 'active' : ''}`}
                                onClick={(e) => handleDotClick(post.id, idx, e)}
                              />
                            ))}
                          </div>
                        </>
                      )}
                    </>
                  ) : (
                    <div className="post-card-image-placeholder">
                      Нет изображений
                    </div>
                  )}
                </div>
                <div className="post-card-content">
                  <h3 className="post-card-title">{post.title}</h3>
                  <p className="post-card-description">
                    {post.description && post.description.length > 100
                      ? post.description.substring(0, 100) + '...'
                      : post.description || 'Без описания'}
                  </p>
                  <div className="post-card-meta">
                    <strong className="post-card-price">
                      {post.price ? `${post.price.toLocaleString()} ₽` : 'Договорная'}
                    </strong>
                    <span className="post-card-date">
                      {new Date(post.createdAt).toLocaleDateString('ru-RU')}
                    </span>
                  </div>
                  <div className="post-card-footer">
                    <div className="post-card-actions">
                      <button className="post-card-btn post-card-btn-details">
                        Подробнее
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            );
          }

          // List variant
          return (
            <div
              key={post.id}
              className="post-card-list"
            >
              <div className="post-card-list-image-container">
                {photoUrl ? (
                  <>
                    <img
                      src={photoUrl}
                      alt={post.title}
                      className="post-card-list-image"
                    />
                    {photos.length > 1 && (
                      <>
                        <button
                          className="post-card-image-carousel-nav prev"
                          onClick={(e) => handlePrevImage(post.id, e)}
                        >
                          ‹
                        </button>
                        <button
                          className="post-card-image-carousel-nav next"
                          onClick={(e) => handleNextImage(post.id, e)}
                        >
                          ›
                        </button>
                        <div className="post-card-image-carousel-dots">
                          {photos.map((_, idx) => (
                            <div
                              key={idx}
                              className={`post-card-image-carousel-dot ${idx === currentIndex ? 'active' : ''}`}
                              onClick={(e) => handleDotClick(post.id, idx, e)}
                            />
                          ))}
                        </div>
                      </>
                    )}
                  </>
                ) : (
                  <div className="post-card-image-placeholder">
                    Нет изображений
                  </div>
                )}
              </div>
              <div className="post-card-list-content">
                <h3 className="post-card-list-title">{post.title}</h3>
                <div className="post-card-list-tags">
                  {post.category && (
                    <span className="post-card-tag post-card-tag-category">
                      {post.category.name}
                    </span>
                  )}
                  {post.subcategory && (
                    <span className="post-card-tag post-card-tag-subcategory">
                      {post.subcategory.name}
                    </span>
                  )}
                </div>
                <p className="post-card-list-description">
                  {post.description || 'Без описания'}
                </p>
                <div className="post-card-list-meta">
                  <span>Автор: {post.author?.firstName} {post.author?.lastName || ''}</span>
                  <span>{new Date(post.createdAt).toLocaleDateString('ru-RU')}</span>
                </div>
                <div className="post-card-list-actions">
                  <button
                    className="post-card-list-btn post-card-list-btn-details"
                    onClick={(e) => {
                      e.stopPropagation();
                      navigate(`/post/${post.id}`);
                    }}
                  >
                    Подробнее
                  </button>
                  {isAdmin && (
                    <button
                      className="post-card-list-btn post-card-list-btn-delete"
                      onClick={(e) => {
                        e.stopPropagation();
                        onDelete(post.id);
                      }}
                    >
                      Удалить
                    </button>
                  )}
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {pagination.totalPages > 1 && (
        <div className="post-list-pagination">
          <button
            className="post-list-pagination-btn"
            onClick={() => onPageChange(0)}
            disabled={pagination.currentPage === 0}
          >
            Первая
          </button>
          <button
            className="post-list-pagination-btn"
            onClick={() => onPageChange(pagination.currentPage - 1)}
            disabled={pagination.currentPage === 0}
          >
            Назад
          </button>
          <span className="post-list-pagination-info">
            Страница {pagination.currentPage + 1} из {pagination.totalPages}
          </span>
          <button
            className="post-list-pagination-btn"
            onClick={() => onPageChange(pagination.currentPage + 1)}
            disabled={pagination.currentPage >= pagination.totalPages - 1}
          >
            Вперед
          </button>
          <button
            className="post-list-pagination-btn"
            onClick={() => onPageChange(pagination.totalPages - 1)}
            disabled={pagination.currentPage >= pagination.totalPages - 1}
          >
            Последняя
          </button>
        </div>
      )}
    </div>
  );
}

export default PostList;

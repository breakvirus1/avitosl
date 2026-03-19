import React from 'react';
import { useNavigate } from 'react-router-dom';
import './PostList.css';

function PostList({ posts, onDelete, loading, isAdmin, pagination, onPageChange }) {
  const navigate = useNavigate();

  if (loading) {
    return (
      <div className="post-list-loading">
        <div className="post-list-spinner"></div>
        <p className="post-list-loading-text">Загрузка объявлений...</p>
      </div>
    );
  }

  if (!posts || posts.length === 0) {
    return (
      <div className="post-list-empty">
        <div className="post-list-empty-icon">📭</div>
        <p className="post-list-empty-text">Нет объявлений</p>
        <button className="post-list-empty-btn">
          Создать первое объявление
        </button>
      </div>
    );
  }

  const handleCardClick = (postId) => {
    navigate(`/post/${postId}`);
  };

  const handleMoreClick = (e, postId) => {
    e.stopPropagation();
    navigate(`/post/${postId}`);
  };

  const handleDeleteClick = (e, postId) => {
    e.stopPropagation();
    if (window.confirm('Вы уверены, что хотите удалить это объявление?')) {
      onDelete(postId);
    }
  };

  return (
    <div className="post-list">
      <div className="post-list-header">
        <h2 className="post-list-title">Объявления</h2>
        <span className="post-list-count">{pagination.totalElements} объявлений</span>
      </div>

      <div className="post-list-grid">
        {posts.map((post) => (
          <div
            key={post.id}
            className="post-card"
            onClick={() => handleCardClick(post.id)}
          >
            {post.photos && post.photos.length > 0 ? (
              <img
                src={`http://localhost:1291/api/photos/${post.photos[0].id}/content`}
                alt={post.title}
                className="post-card-image"
                onError={(e) => {
                  e.target.style.display = 'none';
                  e.target.nextSibling.style.display = 'flex';
                }}
              />
            ) : (
              <div className="post-card-image-placeholder">
                📷
              </div>
            )}

            <div className="post-card-content">
              <h3 className="post-card-title">{post.title}</h3>

              <div className="post-card-tags">
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

              <div className="post-card-price">
                {post.price?.toLocaleString()} ₽
              </div>

              <div className="post-card-date">
                {new Date(post.createdAt).toLocaleDateString('ru-RU')}
              </div>

              <div className="post-card-actions">
                <button
                  className="post-card-btn post-card-btn-primary"
                  onClick={(e) => handleMoreClick(e, post.id)}
                >
                  Подробнее
                </button>
                {isAdmin && (
                  <button
                    className="post-card-btn post-card-btn-danger"
                    onClick={(e) => handleDeleteClick(e, post.id)}
                  >
                    🗑️ Удалить
                  </button>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>

      {pagination.totalPages > 1 && (
        <div className="post-list-pagination">
          <button
            onClick={() => onPageChange(0)}
            disabled={pagination.currentPage === 0}
          >
            Первая
          </button>
          <button
            onClick={() => onPageChange(pagination.currentPage - 1)}
            disabled={pagination.currentPage === 0}
          >
            Назад
          </button>
          <span>
            Страница {pagination.currentPage + 1} из {pagination.totalPages}
          </span>
          <button
            onClick={() => onPageChange(pagination.currentPage + 1)}
            disabled={pagination.currentPage >= pagination.totalPages - 1}
          >
            Вперед
          </button>
          <button
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

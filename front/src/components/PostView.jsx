import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Chat from './Chat';
import './PostView.css';

function PostViewBuyer({ post, user, comments, onCreateComment, onUpdateComment, onDeleteComment, onPurchasePost, login }) {
  const navigate = useNavigate();
  const isAuthenticated = !!user;
  const [currentImageIndex, setCurrentImageIndex] = useState(0);
  const [showChat, setShowChat] = useState(false);
  const [chatReceiverId, setChatReceiverId] = useState(null);
  const [chatReceiverName, setChatReceiverName] = useState('');
  const [editingCommentId, setEditingCommentId] = useState(null);
  const [editingText, setEditingText] = useState('');
  const [commentText, setCommentText] = useState('');

  // Check for pending chat from notification click
  useEffect(() => {
    const pendingChat = sessionStorage.getItem('pendingChat');
    if (pendingChat && user) {
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
  }, [user]);

  const handleCreateComment = async (e) => {
    e.preventDefault();
    if (!commentText.trim()) return;

    try {
      await onCreateComment({ content: commentText.trim(), postId: post.id });
      setCommentText('');
    } catch (err) {
      console.error('Error creating comment:', err);
      alert(err.response?.data?.message || 'Ошибка при создании комментария');
    }
  };

  const handleUpdateComment = async (commentId) => {
    if (!editingText.trim()) return;

    try {
      await onUpdateComment(commentId, { content: editingText.trim(), postId: post.id });
      setEditingCommentId(null);
      setEditingText('');
    } catch (err) {
      console.error('Error updating comment:', err);
      alert(err.response?.data?.message || 'Ошибка при обновлении комментария');
    }
  };

  const handleDeleteComment = async (commentId) => {
    if (!window.confirm('Вы уверены, что хотите удалить этот комментарий?')) {
      return;
    }

    try {
      await onDeleteComment(commentId);
    } catch (err) {
      console.error('Error deleting comment:', err);
      alert(err.response?.data?.message || 'Ошибка при удалении комментария');
    }
  };

  const startEditing = (comment) => {
    setEditingCommentId(comment.id);
    setEditingText(comment.content);
  };

  const cancelEditing = () => {
    setEditingCommentId(null);
    setEditingText('');
  };

  const isCommentOwner = (comment) => {
    return comment.userId === user?.id;
  };

  const isOwner = post.author?.id === user?.id || post.author?.keycloakId === user?.sub;

  const handleOpenChat = () => {
    if (post?.author?.keycloakId) {
      setChatReceiverId(post.author.keycloakId);
      setChatReceiverName(post.author.firstName || 'Продавец');
      setShowChat(true);
    }
  };

  const handleCloseChat = () => {
    setShowChat(false);
    setChatReceiverId(null);
    setChatReceiverName('');
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
    return `/api/photos/${photoId}/file`;
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

  // Automatic image slider every 3 seconds
  useEffect(() => {
    if (!post || !post.photos || post.photos.length <= 1) return;

    const interval = setInterval(() => {
      setCurrentImageIndex(prev =>
        prev === post.photos.length - 1 ? 0 : prev + 1
      );
    }, 3000);

    return () => clearInterval(interval);
  }, [post]);

  const photos = post.photos || [];
  const currentIndex = currentImageIndex;
  const currentPhoto = photos[currentIndex];
  const photoUrl = currentPhoto ? getPhotoUrl(currentPhoto.id) : null;

  const renderPost = () => (
    <>
      <button onClick={() => navigate(-1)} className="post-view-back">
        ← Назад к списку
      </button>

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
                    aria-label="Previous image"
                  >
                    ‹
                  </button>
                  <button
                    className="post-view-image-carousel-nav next"
                    onClick={handleNextImage}
                    aria-label="Next image"
                  >
                    ›
                  </button>
                  <div className="post-view-image-carousel-dots">
                    {photos.map((photo, index) => (
                      <button
                        key={photo.id}
                        className={`post-view-image-carousel-dot ${index === currentIndex ? 'active' : ''}`}
                        onClick={(e) => handleDotClick(index, e)}
                        aria-label={`Go to image ${index + 1}`}
                      />
                    ))}
                  </div>
                </>
              )}
            </div>
          )}

          <div className="post-view-description">
            {post.description}
          </div>

          <div className="post-view-price-card">
            <div className="post-view-price">
              {formatPrice(post.price)}
            </div>
            <div className="post-view-status">
              {post.isActive ? (
                <span className="post-view-status-active">Активно</span>
              ) : (
                <span className="post-view-status-inactive">Неактивно</span>
              )}
              {post.purchased && (
                <span className="post-view-status-purchased">Куплено</span>
              )}
            </div>
          </div>

          <div className="post-view-author-card">
            <h4>Продавец</h4>
            <p className="post-view-author-name">
              {post.author?.firstName} {post.author?.lastName || ''}
            </p>
            <p className="post-view-author-email">
              {post.author?.email}
            </p>
            {isAuthenticated && !isOwner && post.isActive && !post.purchased && (
              <button
                className="post-view-contact-btn"
                onClick={handleOpenChat}
                style={{ marginTop: '8px' }}
              >
                Написать продавцу
              </button>
            )}
          </div>

          {isAuthenticated && isOwner && (
            <div className="post-view-actions">
              <button
                className="post-view-edit-btn"
                onClick={() => navigate(`/edit-post/${post.id}`)}
              >
                Редактировать
              </button>
              <button
                className="post-view-delete-btn"
                onClick={handleDeletePost}
              >
                Удалить
              </button>
            </div>
          )}

          {isAuthenticated && !isOwner && post.isActive && !post.purchased && (
            <div className="post-view-actions">
              <button
                className="post-view-purchase-btn"
                onClick={onPurchasePost}
              >
                Купить за {formatPrice(post.price)}
              </button>
            </div>
          )}

          {post.purchased && (
            <div className="post-view-purchase-info">
              <h4>Вы купили это объявление</h4>
              <p>Дата покупки: {formatDate(post.purchaseDate)}</p>
            </div>
          )}

          <div className="post-view-dates">
            <p>Создано: {formatDate(post.createdAt)}</p>
            {post.updatedAt && post.updatedAt !== post.createdAt && (
              <p>Обновлено: {formatDate(post.updatedAt)}</p>
            )}
          </div>
        </div>
      </div>

      {showChat && chatReceiverId && (
        <Chat
          receiverId={chatReceiverId}
          receiverName={chatReceiverName}
          onClose={handleCloseChat}
        />
      )}
    </>
  );


  const renderComments = () => {
    const renderComment = (comment) => (
      <div key={comment.id} className="post-view-comment-item">
        <div className="post-view-comment-header">
          <div className="post-view-comment-author">
            <span className="post-view-comment-author-name">
              {comment.authorFirstName || 'Пользователь'} {comment.authorLastName || ''}
            </span>
            <span className="post-view-comment-date">
              {formatDate(comment.createdAt)}
            </span>
            {comment.updatedAt && comment.updatedAt !== comment.createdAt && (
              <span className="post-view-comment-edited">(отредактировано)</span>
            )}
          </div>

          {isCommentOwner(comment) && (
            <div className="post-view-comment-actions">
              {editingCommentId === comment.id ? (
                <>
                  <button
                    onClick={() => handleUpdateComment(comment.id)}
                    className="post-view-comment-save-btn"
                  >
                    Сохранить
                  </button>
                  <button
                    type="button"
                    onClick={cancelEditing}
                    className="post-view-comment-cancel-btn"
                  >
                    Отмена
                  </button>
                </>
              ) : (
                <>
                  <button
                    type="button"
                    onClick={() => startEditing(comment)}
                    className="post-view-comment-edit-btn"
                  >
                    Редактировать
                  </button>
                  <button
                    type="button"
                    onClick={() => handleDeleteComment(comment.id)}
                    className="post-view-comment-delete-btn"
                  >
                    Удалить
                  </button>
                </>
              )}
            </div>
          )}
        </div>

        <div className="post-view-comment-content">
          {editingCommentId === comment.id ? (
            <textarea
              value={editingText}
              onChange={(e) => setEditingText(e.target.value)}
              className="post-view-comment-edit-textarea"
              rows="3"
              maxLength="2000"
            />
          ) : (
            <p>{comment.content}</p>
          )}
        </div>
      </div>
    );

    return (
      <div className="post-view-comments-section">
        <div className="post-view-comments-header">
          <h3>Комментарии ({comments.length})</h3>
        </div>

        {isAuthenticated && user ? (
          <form onSubmit={handleCreateComment} className="post-view-comment-form">
            <textarea
              value={commentText}
              onChange={(e) => setCommentText(e.target.value)}
              placeholder="Напишите комментарий..."
              className="post-view-comment-input"
              rows="3"
              maxLength="2000"
            />
            <div className="post-view-comment-form-actions">
              <button type="submit" className="post-view-comment-submit">
                Отправить
              </button>
            </div>
          </form>
        ) : (
          <div className="post-view-comments-login-prompt">
            <p>Для добавления комментария необходимо<a href="/" onClick={(e) => { e.preventDefault(); login(); }}> войти</a> в систему.</p>
          </div>
        )}

        <div className="post-view-comments-list">
          {comments.length === 0 ? (
            <p className="post-view-no-comments">Пока нет комментариев</p>
          ) : (
            comments.map(renderComment)
          )}
        </div>
      </div>
    );
  };
  return (
    <>
      {renderPost()}
      {renderComments()}
      {showChat && chatReceiverId && (
        <Chat
          receiverId={chatReceiverId}
          receiverName={chatReceiverName}
          onClose={handleCloseChat}
        />
      )}
    </>
  );
}

export default PostViewBuyer;

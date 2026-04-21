import { useState, useEffect } from 'react';
<<<<<<< HEAD
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
=======
import { useNavigate } from 'react-router-dom';
>>>>>>> kafka
import Chat from './Chat';
import './PostView.css';

function PostViewBuyer({ post, user, comments, onCreateComment, onUpdateComment, onDeleteComment, onPurchasePost }) {
  const navigate = useNavigate();
<<<<<<< HEAD
  const { isAuthenticated, apiService, user } = useAuth();
  const [post, setPost] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
=======
  const isAuthenticated = !!user;
>>>>>>> kafka
  const [currentImageIndex, setCurrentImageIndex] = useState(0);
  const [showChat, setShowChat] = useState(false);
  const [chatReceiverId, setChatReceiverId] = useState(null);
  const [chatReceiverName, setChatReceiverName] = useState('');
<<<<<<< HEAD
  
  // Комментарии
  const [comments, setComments] = useState([]);
  const [loadingComments, setLoadingComments] = useState(false);
  const [commentText, setCommentText] = useState('');
  const [editingCommentId, setEditingCommentId] = useState(null);
  const [editingText, setEditingText] = useState('');

  useEffect(() => {
    if (isAuthenticated && id) {
      fetchPost();
      fetchComments();
    }
  }, [isAuthenticated, id]);
=======
  const [editingCommentId, setEditingCommentId] = useState(null);
  const [editingText, setEditingText] = useState('');
  const [commentText, setCommentText] = useState('');
>>>>>>> kafka

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

<<<<<<< HEAD
  const fetchComments = async () => {
    try {
      setLoadingComments(true);
      const response = await apiService.getPostComments(id);
      setComments(response.data);
    } catch (err) {
      console.error('Error fetching comments:', err);
    } finally {
      setLoadingComments(false);
    }
  };

  const handleCreateComment = async (e) => {
    e.preventDefault();
    if (!commentText.trim()) return;
    
    try {
      await apiService.createComment({
        text: commentText.trim(),
        postId: parseInt(id)
      });
      setCommentText('');
      fetchComments();
    } catch (err) {
      console.error('Error creating comment:', err);
      alert(err.response?.data?.message || 'Ошибка при создании комментария');
    }
  };

  const handleUpdateComment = async (commentId) => {
    if (!editingText.trim()) return;
    
    try {
      await apiService.updateComment(commentId, {
        text: editingText.trim(),
        postId: parseInt(id)
      });
      setEditingCommentId(null);
      setEditingText('');
      fetchComments();
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
      await apiService.deleteComment(commentId);
      fetchComments();
    } catch (err) {
      console.error('Error deleting comment:', err);
      alert(err.response?.data?.message || 'Ошибка при удалении комментария');
    }
  };

  const startEditing = (comment) => {
    setEditingCommentId(comment.id);
    setEditingText(comment.text);
  };

  const cancelEditing = () => {
    setEditingCommentId(null);
    setEditingText('');
  };

  const isCommentOwner = (commentAuthor) => {
    return commentAuthor?.id === user?.id || commentAuthor?.keycloakId === user?.sub;
  };

  const handleDeletePost = async () => {
=======
  const handleUpdateComment = async (commentId) => {
    if (!editingText.trim()) return;

>>>>>>> kafka
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

<<<<<<< HEAD
  // Рендер поста
  const renderPost = () => (
    <>
      <button onClick={() => navigate('/')} className="post-view-back">
=======
  const renderPost = () => (
    <>
      <button onClick={() => navigate(-1)} className="post-view-back">
>>>>>>> kafka
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
              {post.isActive ? (
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

          <div className="post-view-actions">
            {!isOwner && (
              <button
                className="post-view-contact-btn"
                onClick={handleOpenChat}
              >
                Написать продавцу
              </button>
            )}
            {!isOwner && post.isActive && (
                <button
                  className="post-view-purchase-btn"
                  onClick={onPurchasePost}
                >
                  Купить
                </button>
              )}
          </div>

          <div className="post-view-dates">
            <p>Создано: {formatDate(post.createdAt)}</p>
            {post.updatedAt && (
              <p>Обновлено: {formatDate(post.updatedAt)}</p>
            )}
          </div>
        </div>
      </div>
    </>
  );

<<<<<<< HEAD
  // Рендер секции комментариев
  const renderComments = () => (
    <div className="post-view-comments-section">
      <div className="post-view-comments-header">
        <h3>Комментарии ({comments.length})</h3>
      </div>
      
      {isAuthenticated ? (
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
          <p>Чтобы оставить комментарий, необходимо <button onClick={() => navigate('/')} className="post-view-login-link">войти</button> в систему</p>
        </div>
      )}
      
      <div className="post-view-comments-list">
        {loadingComments ? (
          <div className="post-view-comments-loading">Загрузка комментариев...</div>
        ) : comments.length === 0 ? (
          <div className="post-view-comments-empty">Пока нет комментариев. Будьте первым!</div>
        ) : (
          comments.map((comment) => (
            <div key={comment.id} className="post-view-comment-item">
              <div className="post-view-comment-header">
                <div className="post-view-comment-author">
                  <span className="post-view-comment-author-name">
                    {comment.author?.firstName} {comment.author?.lastName || ''}
                  </span>
                  <span className="post-view-comment-date">
                    {formatDate(comment.createdAt)}
                  </span>
                  {comment.updatedAt && comment.updatedAt !== comment.createdAt && (
                    <span className="post-view-comment-edited">(отредактировано)</span>
                  )}
                </div>
                
                {isAuthenticated && isCommentOwner(comment.author) && (
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
                  <p>{comment.text}</p>
                )}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );

  return (
    <div className="post-view-wrapper">
=======
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

        {isAuthenticated ? (
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
            <p>Чтобы оставить комментарий, необходимо <button onClick={() => navigate('/')} className="post-view-login-link">войти</button> в систему</p>
          </div>
        )}

        <div className="post-view-comments-list">
          {comments.map(renderComment)}
        </div>
      </div>
    );
  };

  return (
    <>
>>>>>>> kafka
      {renderPost()}
      {renderComments()}
      {showChat && (
        <Chat
          receiverId={chatReceiverId}
          receiverName={chatReceiverName}
          postId={post?.id}
          onClose={handleCloseChat}
        />
      )}
<<<<<<< HEAD
    </div>
=======
    </>
>>>>>>> kafka
  );
}

export default PostViewBuyer;


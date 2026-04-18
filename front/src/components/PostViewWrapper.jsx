import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import AuthBar from './AuthBar';
import PostViewBuyer from './PostView';
import PostViewByAuthor from './PostViewByAuthor';
import PostViewPublic from './PostViewPublic';

function PostViewWrapper() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated, apiService, user } = useAuth();

  const [post, setPost] = useState(null);
  const [comments, setComments] = useState([]);
  const [loadingPost, setLoadingPost] = useState(true);
  const [error, setError] = useState(null);

  const fetchPost = useCallback(async () => {
    if (!id) return;
    try {
      setLoadingPost(true);
      const response = await apiService.getPost(id);
      setPost(response.data);
      setError(null);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load post');
    } finally {
      setLoadingPost(false);
    }
  }, [id, apiService]);

  const fetchComments = useCallback(async () => {
    if (!id) return;
    try {
      const response = await apiService.getPostComments(id);
      setComments(response.data);
    } catch (err) {
      console.error('Error fetching comments:', err);
    }
  }, [id, apiService]);

  useEffect(() => {
    if (isAuthenticated && id) {
      fetchPost();
      fetchComments();
    }
  }, [isAuthenticated, id, fetchPost, fetchComments]);

  const isOwner = post && (post.author?.id === user?.id || post.author?.keycloakId === user?.sub);

  // Обработчики для комментариев
  const handleCreateComment = async ({ content, postId }) => {
    await apiService.createComment({ content, postId: parseInt(postId) });
    fetchComments();
  };

  const handleUpdateComment = async (commentId, { content, postId }) => {
    await apiService.updateComment(commentId, { content, postId: parseInt(postId) });
    fetchComments();
  };

  const handleDeleteComment = async (commentId) => {
    await apiService.deleteComment(commentId);
    fetchComments();
  };

  // Обработчики для автора
  const handleDeletePost = async () => {
    try {
      await apiService.deletePost(id);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to delete post');
    }
  };

  const handleEditPost = () => {
    navigate(`/edit-post/${post.id}`);
  };

  // Обработчик для покупки
  const handlePurchasePost = async () => {
    if (!window.confirm(`Вы уверены, что хотите купить "${post.title}" за ${formatPrice(post.price)}?`)) {
      return;
    }
    try {
      await apiService.purchasePost(id);
      alert('Покупка успешно совершена!');
      navigate('/profile');
    } catch (err) {
      console.error('Error purchasing post:', err);
      alert(err.response?.data?.message || 'Ошибка при покупке объявления');
    }
  };

  const formatPrice = (price) => {
    if (!price) return 'Цена не указана';
    return `${price.toLocaleString()} ₽`;
  };

  // Рендерим состояния загрузки и ошибки только для авторизованных
  if (isAuthenticated) {
    if (loadingPost) {
      return (
        <div className="post-view-wrapper">
          <div style={{ maxWidth: '800px', margin: '0 auto', width: '100%' }}>
            <AuthBar />
            <div className="post-view-loading">
              <div className="post-view-spinner"></div>
              <p>Загрузка объявления...</p>
            </div>
          </div>
        </div>
      );
    }

    if (error) {
      return (
        <div className="post-view-wrapper">
          <div style={{ maxWidth: '800px', margin: '0 auto', width: '100%' }}>
            <AuthBar />
            <div className="post-view-error">
              <strong>Ошибка</strong>
              <p>{error}</p>
              <button onClick={() => navigate('/')} className="post-view-back-btn">
                Назад к объявлениям
              </button>
            </div>
          </div>
        </div>
      );
    }

    if (!post) {
      return (
        <div className="post-view-wrapper">
          <div style={{ maxWidth: '800px', margin: '0 auto', width: '100%' }}>
            <AuthBar />
            <div className="post-view-not-found">
              <p>Объявление не найдено</p>
              <button onClick={() => navigate('/')} className="post-view-back-btn">
                Назад к объявлениям
              </button>
            </div>
          </div>
        </div>
      );
    }

    // Авторизован: определяем, автор ли пользователь
    return (
      <div className="post-view-wrapper">
        <div style={{ maxWidth: '800px', margin: '0 auto', width: '100%' }}>
          <AuthBar />
          {isOwner ? (
          <PostViewByAuthor
            post={post}
            user={user}
            comments={comments}
            onCreateComment={handleCreateComment}
            onUpdateComment={handleUpdateComment}
            onDeleteComment={handleDeleteComment}
            onDeletePost={handleDeletePost}
            onEditPost={handleEditPost}
          />
          ) : (
            <PostViewBuyer
              post={post}
              user={user}
              comments={comments}
              onCreateComment={handleCreateComment}
              onUpdateComment={handleUpdateComment}
              onDeleteComment={handleDeleteComment}
              onPurchasePost={handlePurchasePost}
            />
          )}
        </div>
      </div>
    );
  }

  // Не авторизован: публичный вид
  return (
    <div className="post-view-wrapper">
      <div style={{ maxWidth: '800px', margin: '0 auto', width: '100%' }}>
        <AuthBar />
        <PostViewPublic />
      </div>
    </div>
  );
}

export default PostViewWrapper;
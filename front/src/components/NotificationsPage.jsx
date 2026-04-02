import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import './Notifications.css';

function NotificationsPage() {
  const navigate = useNavigate();
  const { apiService, fetchUnreadCount } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchNotifications();
  }, []);

  const fetchNotifications = async () => {
    try {
      setLoading(true);
      const response = await apiService.getUnreadMessages();
      const notificationsData = response.data?.content || response.data || [];
      setNotifications(Array.isArray(notificationsData) ? notificationsData : []);
      setError(null);
    } catch (err) {
      console.error('Error fetching notifications:', err);
      setError(err.response?.data?.message || 'Не удалось загрузить уведомления');
    } finally {
      setLoading(false);
    }
  };

  const handleNotificationClick = async (notification) => {
    try {
      await apiService.markAsRead(notification.id);
      if (fetchUnreadCount) {
        await fetchUnreadCount();
      }
      // Navigate to post page if postId exists, otherwise to home
      if (notification.postId) {
        navigate(`/post/${notification.postId}`);
      } else {
        navigate('/');
      }
      // Store chat data to open chat after navigation
      sessionStorage.setItem('pendingChat', JSON.stringify({
        receiverId: notification.senderId,
        receiverName: notification.senderFirstName,
        postId: notification.postId
      }));
    } catch (err) {
      console.error('Error marking notification as read:', err);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);

    if (date.toDateString() === today.toDateString()) {
      return `Сегодня, ${date.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' })}`;
    } else if (date.toDateString() === yesterday.toDateString()) {
      return `Вчера, ${date.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' })}`;
    } else {
      return date.toLocaleDateString('ru-RU', {
        day: 'numeric',
        month: 'long',
        hour: '2-digit',
        minute: '2-digit'
      });
    }
  };

  const getNotificationText = (notification) => {
    const senderName = notification.senderFirstName || 'Пользователь';
    const messagePreview = notification.message.length > 50
      ? notification.message.substring(0, 50) + '...'
      : notification.message;
    return `${senderName}: ${messagePreview}`;
  };

  return (
    <div className="notifications-page">
      <div className="notifications-page-container">
        <div className="notifications-page-header">
          <h1>Уведомления</h1>
          <button className="notifications-back-btn" onClick={() => navigate(-1)}>
            ← Назад
          </button>
        </div>

        <div className="notifications-page-content">
          {loading && (
            <div className="notifications-loading">Загрузка...</div>
          )}

          {error && !loading && (
            <div className="notifications-error">{error}</div>
          )}

          {!loading && notifications.length === 0 && (
            <div className="notifications-empty">Нет новых уведомлений</div>
          )}

          {!loading && notifications.length > 0 && (
            <ul className="notifications-list">
              {notifications.map((notification) => (
                <li
                  key={notification.id}
                  className="notification-item"
                  onClick={() => handleNotificationClick(notification)}
                >
                  <div className="notification-message">
                    {getNotificationText(notification)}
                  </div>
                  <div className="notification-time">
                    {formatDate(notification.createdAt)}
                  </div>
                  {!notification.read && (
                    <div className="notification-unread-indicator"></div>
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
}

export default NotificationsPage;

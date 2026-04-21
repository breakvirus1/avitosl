<<<<<<< HEAD
=======
import { useState, useEffect, useCallback } from 'react';
>>>>>>> kafka
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import AuthBar from './AuthBar';
import './UserProfile.css';

function UserProfile() {
  const navigate = useNavigate();
  const { user, isAuthenticated, logout, apiService, fetchUnreadCount } = useAuth();
  const [balance, setBalance] = useState(0);
  const [showAddFundsModal, setShowAddFundsModal] = useState(false);
  const [fundsAmount, setFundsAmount] = useState('');
  const [isAddingFunds, setIsAddingFunds] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [notificationsLoading, setNotificationsLoading] = useState(true);

  // Callbacks first to avoid TDZ in dependencies
  const fetchBalance = useCallback(async () => {
    try {
      const response = await apiService.getWalletBalance();
      setBalance(response.data.balance ?? response.data);
    } catch (error) {
      console.error('Failed to fetch balance:', error);
    }
  }, [apiService]);

  const fetchNotifications = useCallback(async () => {
    try {
      setNotificationsLoading(true);
      const response = await apiService.getUnreadMessages();
      const messages = response.data || [];
      
      const notificationsWithSender = await Promise.all(
        messages.map(async (msg) => {
          try {
            const sender = await apiService.getUserByKeycloakId(msg.senderKeycloakId);
            return {
              id: msg.id,
              type: 'chat',
              message: msg.message,
              createdAt: msg.createdAt,
              read: msg.isRead,
              senderId: msg.senderKeycloakId,
              senderFirstName: sender.data?.firstName || 'Пользователь',
              senderLastName: sender.data?.lastName || '',
              postId: null
            };
          } catch {
            return {
              id: msg.id,
              type: 'chat',
              message: msg.message,
              createdAt: msg.createdAt,
              read: msg.isRead,
              senderId: msg.senderKeycloakId,
              senderFirstName: 'Пользователь',
              senderLastName: '',
              postId: null
            };
          }
        })
      );
      
      setNotifications(notificationsWithSender);
    } catch (error) {
      console.error('Failed to fetch notifications:', error);
    } finally {
      setNotificationsLoading(false);
    }
  }, [apiService]);

  useEffect(() => {
    if (isAuthenticated) {
      fetchBalance();
      fetchNotifications();
    }
  }, [isAuthenticated, fetchBalance, fetchNotifications]);

  const handleLogout = async () => {
    try {
      await logout();
    } catch (err) {
      console.error('Logout failed:', err);
    }
    navigate('/');
  };

  const handleAddFunds = () => {
    setShowAddFundsModal(true);
  };

  const handleConfirmAddFunds = async () => {
    const amount = parseFloat(fundsAmount);
    if (amount && amount > 0) {
      setIsAddingFunds(true);
      try {
        await apiService.addFundsToWallet(amount);
        fetchBalance();
        setShowAddFundsModal(false);
        setFundsAmount('');
      } catch (error) {
        console.error('Failed to add funds:', error);
        alert(error.response?.data?.message || 'Ошибка при пополнении баланса');
      } finally {
        setIsAddingFunds(false);
      }
    }
  };

   const handleCloseAddFundsModal = () => {
     setShowAddFundsModal(false);
     setFundsAmount('');
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
       // Store chat data to open chat after navigation (only for chat messages)
       if (notification.type === 'chat') {
         sessionStorage.setItem('pendingChat', JSON.stringify({
           receiverId: notification.senderId,
           receiverName: notification.senderFirstName
         }));
       }
     } catch (err) {
       console.error('Error marking notification as read:', err);
     }
   };

  if (!isAuthenticated || !user) {
    return (
      <div className="user-profile-container">
        <div className="user-profile-not-found">
          <p>Пользователь не найден</p>
          <button onClick={() => navigate('/')} className="user-profile-back-btn">
            На главную
          </button>
        </div>
      </div>
    );
  }

  // Получаем данные из JWT token (поле profile)
  const profile = user.profile || {};
  const firstName = profile.given_name || profile.name?.split(' ')[0] || 'Не указано';
  const lastName = profile.family_name || profile.name?.split(' ').slice(1).join(' ') || 'Не указано';
  const email = profile.email || 'Не указано';
  const userId = user.id || profile.sub || 'Не указано';

  return (
    <div className="user-profile-container">
      <div style={{ maxWidth: '800px', margin: '0 auto', width: '100%' }}>
        <AuthBar />
        <div className="user-profile-content">
        <button onClick={() => navigate(-1)} className="user-profile-back">
          ← Назад
        </button>

        <div className="user-profile-card">
          <div className="user-profile-avatar">
            {firstName?.charAt(0) || email?.charAt(0) || 'U'}
          </div>

          <div className="user-profile-info">
            <h1 className="user-profile-name">
              {firstName} {lastName}
            </h1>
            <p className="user-profile-email">{email}</p>

            <div className="user-profile-details">
              <div className="user-profile-detail-item">
                <span className="user-profile-label">Имя:</span>
                <span className="user-profile-value">{firstName}</span>
              </div>
              <div className="user-profile-detail-item">
                <span className="user-profile-label">Фамилия:</span>
                <span className="user-profile-value">{lastName}</span>
              </div>
              <div className="user-profile-detail-item">
                <span className="user-profile-label">Email:</span>
                <span className="user-profile-value">{email}</span>
              </div>
              <div className="user-profile-detail-item">
                <span className="user-profile-label">ID:</span>
                <span className="user-profile-value">{userId}</span>
              </div>
              <div className="user-profile-detail-item">
                <span className="user-profile-label">Баланс:</span>
                <span className="user-profile-value">{balance} ₽</span>
                <button onClick={handleAddFunds} className="user-profile-add-funds-btn">
                  Пополнить
                </button>
              </div>
            </div>
          </div>

          <div className="user-profile-actions">
            <button onClick={handleLogout} className="user-profile-logout-btn">
              Выйти из системы
            </button>
          </div>
        </div>

        {/* Purchases CTA */}
        <div className="user-profile-purchases-cta">
          <button
            className="user-profile-btn"
            onClick={() => navigate('/purchases')}
          >
            Мои покупки
          </button>
        </div>

        {/* Notifications Section */}
        <div className="user-profile-notifications">
          <h2>Уведомления</h2>
          {notificationsLoading ? (
            <p className="user-profile-notifications-loading">Загрузка...</p>
          ) : notifications.length === 0 ? (
            <p className="user-profile-notifications-empty">Нет уведомлений</p>
          ) : (
            <ul className="user-profile-notifications-list">
              {notifications.slice(0, 5).map((notification) => (
                <li
                  key={notification.id}
                  className="user-profile-notification-item"
                  onClick={() => handleNotificationClick(notification)}
                >
                  <span className="user-profile-notification-message">
                    {notification.senderFirstName}: {notification.message.length > 30 ? notification.message.substring(0, 30) + '...' : notification.message}
                  </span>
                  <span className="user-profile-notification-time">
                    {new Date(notification.createdAt).toLocaleDateString('ru-RU')}
                  </span>
                  {!notification.read && (
                    <span className="user-profile-notification-unread"></span>
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>

        {/* Add Funds Modal */}
        {showAddFundsModal && (
          <div className="user-profile-modal-overlay" onClick={handleCloseAddFundsModal}>
            <div className="user-profile-modal" onClick={(e) => e.stopPropagation()}>
              <div className="user-profile-modal-header">
                <h3>Пополнить баланс</h3>
                <button
                  className="user-profile-modal-close"
                  onClick={handleCloseAddFundsModal}
                >
                  ×
                </button>
              </div>
              <div className="user-profile-modal-body">
                <div className="user-profile-modal-field">
                  <label htmlFor="funds-amount">Сумма пополнения (₽):</label>
                  <input
                    id="funds-amount"
                    type="number"
                    min="0.01"
                    step="0.01"
                    value={fundsAmount}
                    onChange={(e) => setFundsAmount(e.target.value)}
                    placeholder="0.00"
                    className="user-profile-modal-input"
                    disabled={isAddingFunds}
                  />
                </div>
              </div>
              <div className="user-profile-modal-footer">
                <button
                  className="user-profile-modal-cancel-btn"
                  onClick={handleCloseAddFundsModal}
                  disabled={isAddingFunds}
                >
                  Отмена
                </button>
                <button
                  className="user-profile-modal-confirm-btn"
                  onClick={handleConfirmAddFunds}
                  disabled={!fundsAmount || parseFloat(fundsAmount) <= 0 || isAddingFunds}
                >
                  {isAddingFunds ? 'Пополнение...' : 'Пополнить'}
                </button>
              </div>
            </div>
          </div>
        )}
        </div>
      </div>
    </div>
  );
}

export default UserProfile;

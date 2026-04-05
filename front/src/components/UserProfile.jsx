import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import AuthBar from './AuthBar';
import './UserProfile.css';

const API_BASE_URL = 'http://localhost:8081/api';

function UserProfile() {
  const navigate = useNavigate();
  const { user, isAuthenticated, logout, apiService } = useAuth();
  const [balance, setBalance] = useState(0);
  const [purchases, setPurchases] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showAddFundsModal, setShowAddFundsModal] = useState(false);
  const [fundsAmount, setFundsAmount] = useState('');
  const [isAddingFunds, setIsAddingFunds] = useState(false);

  useEffect(() => {
    if (isAuthenticated) {
      fetchBalance();
      fetchPurchases();
    }
  }, [isAuthenticated]);

  const fetchBalance = async () => {
    try {
      const response = await apiService.getWalletBalance();
      setBalance(response.data);
    } catch (error) {
      console.error('Failed to fetch balance:', error);
    }
  };

  const fetchPurchases = async () => {
    try {
      const response = await apiService.getUserPurchases();
      setPurchases(response.data.content || []);
    } catch (error) {
      console.error('Failed to fetch purchases:', error);
    } finally {
      setLoading(false);
    }
  };

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

        <div className="user-profile-purchases">
          <h2>Мои покупки</h2>
          {loading ? (
            <p>Загрузка...</p>
          ) : purchases.length === 0 ? (
            <p>У вас пока нет покупок</p>
          ) : (
            <div className="user-profile-purchases-list">
              {purchases.map((purchase) => (
                <div key={purchase.id} className="user-profile-purchase-item">
                  <div className="user-profile-purchase-info">
                    <h3>{purchase.post?.title || 'Объявление'}</h3>
                    <p>Цена: {purchase.purchasePrice} ₽</p>
                    <p>Дата покупки: {new Date(purchase.createdAt).toLocaleDateString('ru-RU')}</p>
                  </div>
                  <button
                    onClick={() => navigate(`/bought/${purchase.post?.id}`)}
                    className="user-profile-view-post-btn"
                  >
                    Просмотреть
                  </button>
                </div>
              ))}
            </div>
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

import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import './UserProfile.css';

function UserProfile() {
  const navigate = useNavigate();
  const { user, isAuthenticated, logout } = useAuth();

  const handleLogout = async () => {
    try {
      await logout();
    } catch (err) {
      console.error('Logout failed:', err);
    }
    navigate('/');
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

  return (
    <div className="user-profile-container">
      <div className="user-profile-content">
        <button onClick={() => navigate(-1)} className="user-profile-back">
          ← Назад
        </button>

        <div className="user-profile-card">
          <div className="user-profile-avatar">
            {user.firstName?.charAt(0) || user.email?.charAt(0) || 'U'}
          </div>
          
          <div className="user-profile-info">
            <h1 className="user-profile-name">
              {user.firstName} {user.lastName || ''}
            </h1>
            <p className="user-profile-email">{user.email}</p>
            
            <div className="user-profile-details">
              <div className="user-profile-detail-item">
                <span className="user-profile-label">Имя:</span>
                <span className="user-profile-value">{user.firstName || 'Не указано'}</span>
              </div>
              <div className="user-profile-detail-item">
                <span className="user-profile-label">Фамилия:</span>
                <span className="user-profile-value">{user.lastName || 'Не указано'}</span>
              </div>
              <div className="user-profile-detail-item">
                <span className="user-profile-label">Email:</span>
                <span className="user-profile-value">{user.email || 'Не указано'}</span>
              </div>
              <div className="user-profile-detail-item">
                <span className="user-profile-label">ID:</span>
                <span className="user-profile-value">{user.id || 'Не указано'}</span>
              </div>
            </div>
          </div>

          <div className="user-profile-actions">
            <button onClick={handleLogout} className="user-profile-logout-btn">
              Выйти из системы
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default UserProfile;

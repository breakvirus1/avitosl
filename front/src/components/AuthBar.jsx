import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import './AuthBar.css';

function AuthBar() {
  const navigate = useNavigate();
  const { user, isAuthenticated, login, logout, unreadCount } = useAuth();

  const handleClick = () => {
    if (isAuthenticated) {
      logout();
    } else {
      login();
    }
  };

  const handleUserNameClick = () => {
    if (isAuthenticated) {
      navigate('/profile');
    }
  };

  const handleNotificationsClick = () => {
    navigate('/notifications');
  };

  const getUserName = () => {
    if (!user) return '';
    const profile = user.profile || {};
    return profile.given_name || profile.name || profile.email?.split('@')[0] || '';
  };

  return (
    <div className="auth-bar">
      {isAuthenticated && user?.profile && (
        <button
          className="auth-bar-username-btn"
          onClick={handleUserNameClick}
        >
          {getUserName()}
        </button>
      )}
      {isAuthenticated && (
        <button
          className="auth-bar-create-btn"
          onClick={() => navigate('/create-post')}
        >
          Создать объявление
        </button>
      )}
      {isAuthenticated && (
        <button
          className="auth-bar-notification-btn"
          onClick={handleNotificationsClick}
        >
          🔔
          {unreadCount > 0 && (
            <span className="auth-bar-notification-count">{unreadCount}</span>
          )}
        </button>
      )}
      <button
        className={`auth-bar-btn ${isAuthenticated ? 'auth-bar-btn-logout' : 'auth-bar-btn-login'}`}
        onClick={handleClick}
      >
        {isAuthenticated ? 'Выйти' : 'Войти'}
      </button>
    </div>
  );
}

export default AuthBar;

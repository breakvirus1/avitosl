import React from 'react';
import { useAuth } from '../hooks/useAuth';

function AuthBar() {
  const { user, isAuthenticated, login, logout } = useAuth();

  const containerStyle = {
    display: 'flex',
    alignItems: 'center',
    gap: '12px'
  };

  const textStyle = {
    color: 'rgba(255, 255, 255, 0.85)',
    fontSize: '14px'
  };

  const buttonStyle = {
    background: isAuthenticated ? '#ff4d4f' : '#1890ff',
    color: 'white',
    border: 'none',
    padding: '8px 16px',
    borderRadius: '4px',
    cursor: 'pointer',
    fontSize: '14px',
    fontWeight: '500'
  };

  const handleClick = () => {
    if (isAuthenticated) {
      logout();
    } else {
      login();
    }
  };

  return (
    <div style={containerStyle}>
      {isAuthenticated && user?.profile && (
        <span style={textStyle}>
          {user.profile.name || user.profile.preferred_username || user.profile.email}
        </span>
      )}
      <button style={buttonStyle} onClick={handleClick}>
        {isAuthenticated ? 'Выйти' : 'Войти'}
      </button>
    </div>
  );
}

export default AuthBar;

import React, { useEffect } from 'react';
import { useAuth } from '../hooks/useAuth';

function PrivateRoute({ children }) {
  const { isAuthenticated, loading, login } = useAuth();

  const containerStyle = {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: '100vh',
    textAlign: 'center'
  };

  const titleStyle = {
    fontSize: '32px',
    fontWeight: 600,
    marginBottom: '16px',
    color: '#000000d9'
  };

  const subtitleStyle = {
    fontSize: '20px',
    color: '#00000073',
    marginBottom: '24px'
  };

  const spinStyle = {
    display: 'inline-block',
    width: '40px',
    height: '40px',
    border: '4px solid #f3f3f3',
    borderTop: '4px solid #1890ff',
    borderRadius: '50%',
    animation: 'spin 1s linear infinite'
  };

  useEffect(() => {
    if (!loading && !isAuthenticated) {
      login();
    }
  }, [loading, isAuthenticated, login]);

  if (loading) {
    return (
      <div style={containerStyle}>
        <h1 style={titleStyle}>Загрузка</h1>
        <h2 style={subtitleStyle}>Проверка авторизации...</h2>
        <div style={spinStyle}></div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return (
      <div style={containerStyle}>
        <h1 style={titleStyle}>Требуется авторизация</h1>
        <h2 style={subtitleStyle}>Перенаправление на страницу входа...</h2>
        <div style={spinStyle}></div>
      </div>
    );
  }

  return children;
}

export default PrivateRoute;

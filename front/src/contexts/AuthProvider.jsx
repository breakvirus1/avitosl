import React, { useEffect, useState, useMemo, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  login,
  logout,
  completeLogin,
  completeLogout,
  getUser,
  removeUser
} from '../oidc';
import AuthApiService from '../services/auth-api';
import { AuthContext } from './AuthContext';

export const AuthProvider = ({ children }) => {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    // Проверяем, есть ли сохраненный пользователь при загрузке
    const checkAuth = async () => {
      try {
        const currentUser = await getUser();
        if (currentUser) {
          setUser(currentUser);
          setIsAuthenticated(true);
        }
      } catch (err) {
        console.error('Auth check failed:', err);
        await removeUser();
      } finally {
        setLoading(false);
      }
    };

    checkAuth();

    // Обработка callback после логина/логаута
    const handleCallback = async () => {
      if (window.location.pathname === '/callback') {
        try {
          const user = await completeLogin();
          setUser(user);
          setIsAuthenticated(true);
          navigate('/', { replace: true });
        } catch (err) {
          console.error('Login callback failed:', err);
          setError('Ошибка авторизации');
          navigate('/', { replace: true });
        }
      }

      if (window.location.pathname === '/post-logout-callback') {
        try {
          await completeLogout();
        } catch (err) {
          console.error('Logout callback failed:', err);
        } finally {
          navigate('/', { replace: true });
        }
      }
    };

    handleCallback();
  }, [navigate]);

  // Обработка 401 ошибок и автоматическое обновление токена
  useEffect(() => {
    const handleAccessTokenExpired = () => {
      console.log('Access token expired, attempting silent renew...');
      // silent renew будет автоматически выполнен oidc-client
    };

    // Слушаем события от oidc-client
    window.addEventListener('access_token_expired', handleAccessTokenExpired);

    return () => {
      window.removeEventListener('access_token_expired', handleAccessTokenExpired);
    };
  }, []);

  const handleLogin = async () => {
    try {
      setError(null);
      await login();
    } catch (err) {
      console.error('Login failed:', err);
      setError('Не удалось выполнить вход');
    }
  };

  const handleLogout = async () => {
    try {
      await removeUser();
      setUser(null);
      setIsAuthenticated(false);
      await logout();
    } catch (err) {
      console.error('Logout failed:', err);
      // При ошибке все равно очищаем локальное состояние
      setUser(null);
      setIsAuthenticated(false);
      navigate('/');
    }
  };

  const getAccessToken = async () => {
    if (!user) return null;
    try {
      // Проверяем, не истек ли токен
      if (user.expired) {
        console.log('Token expired, getting new user...');
        const newUser = await getUser();
        if (newUser) {
          setUser(newUser);
          return newUser.access_token;
        }
        return null;
      }
      return user.access_token;
    } catch (err) {
      console.error('Get access token failed:', err);
      return null;
    }
  };

  const apiService = useMemo(() => {
    return new AuthApiService(getAccessToken);
  }, [getAccessToken]);

  const value = {
    user,
    isAuthenticated,
    loading,
    error,
    login: handleLogin,
    logout: handleLogout,
    getAccessToken,
    apiService,
    clearError: () => setError(null)
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

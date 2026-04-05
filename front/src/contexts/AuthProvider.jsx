import React, { useEffect, useState, useMemo, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  login,
  logout,
  completeLogin,
  completeLogout,
  getUser,
  removeUser,
  userManager
} from '../oidc';
import AuthApiService from '../services/auth-api';
import { AuthContext } from './AuthContext';

export const AuthProvider = ({ children }) => {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [unreadCount, setUnreadCount] = useState(0);
  
  // Используем ref для гарантии однократной обработки callback в рамках сессии
  const callbackProcessedRef = useRef(sessionStorage.getItem('callbackProcessed') === 'true');
  
  const setCallbackProcessed = (value) => {
    callbackProcessedRef.current = value;
    if (value) {
      sessionStorage.setItem('callbackProcessed', 'true');
    } else {
      sessionStorage.removeItem('callbackProcessed');
    }
  };
  
  // Мьютекс для предотвращения параллельных вызовов signinSilent
  const silentRenewInProgress = useRef(false);

  useEffect(() => {
    // Сбрасываем флаг обработки callback при монтировании, если не на callback странице
    if (window.location.pathname !== '/callback' && window.location.pathname !== '/post-logout-callback') {
      setCallbackProcessed(false);
      callbackProcessedRef.current = false;
    }

    // Слушаем изменения sessionStorage из других вкладок
    const handleStorageChange = (event) => {
      if (event.key === 'callbackProcessed') {
        callbackProcessedRef.current = event.newValue === 'true';
      }
    };
    window.addEventListener('storage', handleStorageChange);
    
    return () => {
      window.removeEventListener('storage', handleStorageChange);
    };
  }, []);

  useEffect(() => {
    // Проверяем, есть ли сохраненный пользователь при загрузке
    const checkAuth = async () => {
      try {
        const currentUser = await getUser();
        if (currentUser) {
          // Формируем правильный объект user с id и sub из profile
          const enrichedUser = {
            ...currentUser,
            id: currentUser.profile?.sub, // Используем sub как id
            sub: currentUser.profile?.sub
          };
          setUser(enrichedUser);
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
      const isCallbackPage = window.location.pathname === '/callback';
      const isLogoutCallbackPage = window.location.pathname === '/post-logout-callback';
      
      // Проверяем флаг в ref и sessionStorage для защиты от повторной обработки
      if ((isCallbackPage || isLogoutCallbackPage) && !callbackProcessedRef.current) {
        try {
          // Устанавливаем флаг в sessionStorage и ref ДО обработки
          setCallbackProcessed(true);
          callbackProcessedRef.current = true;
          
          if (isCallbackPage) {
            const user = await completeLogin();
            console.log('User logged in:', user);
            console.log('User profile:', user.profile);
            console.log('Realm access:', user.profile?.realm_access);
            console.log('Resource access:', user.profile?.resource_access);

            // Формируем правильный объект user с id и sub из profile
            const enrichedUser = {
              ...user,
              id: user.profile?.sub, // Используем sub как id
              sub: user.profile?.sub
            };

            setUser(enrichedUser);
            setIsAuthenticated(true);
            navigate('/', { replace: true });
          } else {
            await completeLogout();
            navigate('/', { replace: true });
          }
        } catch (err) {
          console.error(isCallbackPage ? 'Login callback failed:' : 'Logout callback failed:', err);
          if (isCallbackPage) {
            setError('Ошибка авторизации');
            navigate('/', { replace: true });
          } else {
            navigate('/', { replace: true });
          }
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
      // Сбрасываем флаг обработки callback для следующего входа
      setCallbackProcessed(false);
      await removeUser();
      setUser(null);
      setIsAuthenticated(false);
      await logout();
    } catch (err) {
      console.error('Logout failed:', err);
      // При ошибке все равно очищаем локальное состояние
      setUser(null);
      setIsAuthenticated(false);
      setCallbackProcessed(false);
      navigate('/');
    }
  };

  const getAccessToken = useCallback(async () => {
    const currentUser = user;
    if (!currentUser) return null;
    try {
      // Проверяем, не истек ли токен
      if (currentUser.expired) {
        console.log('Token expired, attempting silent renew...');
        
        // Если уже идет процесс обновления, ждем его завершения
        if (silentRenewInProgress.current) {
          console.log('Silent renew already in progress, waiting...');
          return new Promise((resolve) => {
            const checkInterval = setInterval(() => {
              if (!silentRenewInProgress.current) {
                clearInterval(checkInterval);
                if (currentUser && !currentUser.expired) {
                  resolve(currentUser.access_token);
                } else {
                  resolve(null);
                }
              }
            }, 50);
          });
        }
        
        try {
          silentRenewInProgress.current = true;
          // Пытаемся обновить токен через silent renew
          const newUser = await userManager.signinSilent();
          setUser(newUser);
          silentRenewInProgress.current = false;
          return newUser.access_token;
        } catch (silentError) {
          silentRenewInProgress.current = false;
          console.error('Silent renew failed:', silentError);
          // Если silent renew не удался, очищаем пользователя и перенаправляем на логин
          await removeUser();
          setUser(null);
          setIsAuthenticated(false);
          return null;
        }
      }
      return currentUser.access_token;
    } catch (err) {
      console.error('Get access token failed:', err);
      return null;
    }
  }, [user]);

  const apiService = useMemo(() => {
    return new AuthApiService(() => getAccessToken());
  }, [getAccessToken]);

  const fetchUnreadCount = useCallback(async () => {
    if (!isAuthenticated) return;
    try {
      const response = await apiService.getUnreadCount();
      setUnreadCount(response.data);
    } catch (err) {
      console.error('Failed to fetch unread count:', err);
    }
  }, [isAuthenticated, apiService]);

  useEffect(() => {
    if (isAuthenticated) {
      fetchUnreadCount();
      // Обновляем счетчик каждые 30 секунд
      const interval = setInterval(fetchUnreadCount, 30000);
      return () => clearInterval(interval);
    }
  }, [isAuthenticated, fetchUnreadCount]);

  const value = {
    user,
    isAuthenticated,
    loading,
    error,
    login: handleLogin,
    logout: handleLogout,
    getAccessToken,
    apiService,
    clearError: () => setError(null),
    unreadCount,
    fetchUnreadCount
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

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

  const handleAdminPanelClick = () => {
    navigate('/admin-panel');
  };

  const handleHomeClick = () => {
    navigate('/');
  };

  const getUserRoles = () => {
    if (!user || !user.access_token) return [];

    // Декодируем access_token для получения claims
    const decodeJwt = (token) => {
      try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
          return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
        return JSON.parse(jsonPayload);
      } catch (e) {
        console.error('Failed to decode JWT:', e);
        return null;
      }
    };

    const claims = decodeJwt(user.access_token);
    if (!claims) return [];

    const roles = new Set();

    // Проверяем realm roles
    const realmAccess = claims.realm_access;
    if (realmAccess && realmAccess.roles) {
      realmAccess.roles.forEach(role => roles.add(role));
    }

    // Проверяем client roles (для клиента avitofrontend)
    const resourceAccess = claims.resource_access;
    if (resourceAccess && resourceAccess.avitofrontend && resourceAccess.avitofrontend.roles) {
      resourceAccess.avitofrontend.roles.forEach(role => roles.add(role));
    }

    // Проверяем groups
    const groups = claims.groups;
    if (groups && Array.isArray(groups)) {
      groups.forEach(group => roles.add(group));
    }

    return Array.from(roles);
  };

  const hasAdminRole = () => {
    const roles = getUserRoles();
    return roles.includes('ADMIN') || roles.includes('admin');
  };

  const getUserName = () => {
    if (!user) return '';
    const profile = user.profile || {};
    return profile.given_name || profile.name || profile.email?.split('@')[0] || '';
  };

  return (
    <div className="auth-bar">
      {isAuthenticated && user?.profile && (
        <div className="auth-bar-user-section">
          <button
            className="auth-bar-username-btn"
            onClick={handleUserNameClick}
          >
            {getUserName()}
          </button>
          <div className="auth-bar-roles">
            Роли: {getUserRoles().join(', ')}
          </div>
        </div>
      )}
      {isAuthenticated && hasAdminRole() && (
        <button
          className="auth-bar-admin-btn"
          onClick={handleAdminPanelClick}
        >
          Admin Panel
        </button>
      )}
      {isAuthenticated && (
        <button
          className="auth-bar-home-btn"
          onClick={handleHomeClick}
        >
          Объявления
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

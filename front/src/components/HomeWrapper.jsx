import { useContext } from 'react';
import { AuthContext } from '../contexts/AuthContext';
import Home from './Home';
import HomePublic from './HomePublic';
import './HomeWrapper.css';

function HomeWrapper() {
  const { isAuthenticated, loading } = useContext(AuthContext);

  if (loading) {
    return (
      <div className="home-wrapper-loading">
        <div className="spinner"></div>
      </div>
    );
  }

  if (isAuthenticated) {
    return <Home />;
  }

  return <HomePublic />;
}

export default HomeWrapper;

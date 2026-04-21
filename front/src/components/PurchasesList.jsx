import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import AuthBar from './AuthBar';
import './PurchasesList.css';

function PurchasesList() {
  const navigate = useNavigate();
  const { apiService } = useAuth();
  const [purchases, setPurchases] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalItems, setTotalItems] = useState(0);
  const pageSize = 10;

  const fetchPurchases = useCallback(async (page) => {
    try {
      setLoading(true);
      const response = await apiService.getUserPurchases(page, pageSize);
      setPurchases(response.data.content || []);
      setTotalPages(response.data.totalPages || 0);
      setTotalItems(response.data.totalElements || 0);
      setError(null);
    } catch (err) {
      console.error('Failed to fetch purchases:', err);
      setError(err.response?.data?.message || 'Не удалось загрузить покупки');
    } finally {
      setLoading(false);
    }
  }, [apiService]);

  useEffect(() => {
    fetchPurchases(currentPage);
  }, [currentPage, fetchPurchases]);

  const formatDate = (dateString) => {
    if (!dateString) return '';
    return new Date(dateString).toLocaleDateString('ru-RU', {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const formatPrice = (price) => {
    if (!price) return 'Цена не указана';
    return `${price.toLocaleString()} ₽`;
  };

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < totalPages) {
      setCurrentPage(newPage);
    }
  };

  if (loading) {
    return (
      <div className="purchases-list-wrapper">
        <div style={{ maxWidth: '800px', margin: '0 auto', width: '100%' }}>
          <AuthBar />
          <div className="purchases-list-loading">
            <div className="purchases-list-spinner"></div>
            <p>Загрузка покупок...</p>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="purchases-list-wrapper">
        <div style={{ maxWidth: '800px', margin: '0 auto', width: '100%' }}>
          <AuthBar />
          <div className="purchases-list-error">
            <strong>Ошибка</strong>
            <p>{error}</p>
            <button onClick={() => navigate('/profile')} className="purchases-list-back-btn">
              Назад в профиль
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="purchases-list-wrapper">
      <div style={{ maxWidth: '800px', margin: '0 auto', width: '100%' }}>
        <AuthBar />
        <div className="purchases-list-container">
          <div className="purchases-list-header">
            <h1>Мои покупки</h1>
            <button className="purchases-list-back-btn" onClick={() => navigate(-1)}>
              ← Назад
            </button>
          </div>

          <div className="purchases-list-content">
            {loading ? (
              <p>Загрузка...</p>
            ) : purchases.length === 0 ? (
              <div className="purchases-list-empty">
                <p>У вас пока нет покупок</p>
                <button onClick={() => navigate('/')} className="purchases-list-home-btn">
                  Перейти к объявлениям
                </button>
              </div>
            ) : (
              <>
                <div className="purchases-list-info">
                  <span>Всего покупок: {totalItems}</span>
                </div>
                <div className="purchases-list-items">
                  {purchases.map((purchase) => (
                    <div key={purchase.id} className="purchases-list-item">
                      <div className="purchases-list-item-info">
                        <h3>{purchase.post?.title || 'Объявление'}</h3>
                        <p className="purchases-list-item-price">
                          Цена: {formatPrice(purchase.purchasePrice)}
                        </p>
                        <p className="purchases-list-item-date">
                          Дата покупки: {formatDate(purchase.createdAt)}
                        </p>
                        {purchase.notes && (
                          <p className="purchases-list-item-notes">
                            {purchase.notes}
                          </p>
                        )}
                      </div>
                      <button
                        onClick={() => navigate(`/bought/${purchase.post?.id}`)}
                        className="purchases-list-view-btn"
                      >
                        Просмотреть
                      </button>
                    </div>
                  ))}
                </div>

                {totalPages > 1 && (
                  <div className="purchases-list-pagination">
                    <button
                      className="purchases-list-page-btn"
                      onClick={() => handlePageChange(currentPage - 1)}
                      disabled={currentPage === 0}
                    >
                      ‹ Назад
                    </button>
                    <span className="purchases-list-page-info">
                      Страница {currentPage + 1} из {totalPages}
                    </span>
                    <button
                      className="purchases-list-page-btn"
                      onClick={() => handlePageChange(currentPage + 1)}
                      disabled={currentPage >= totalPages - 1}
                    >
                      Вперед ›
                    </button>
                  </div>
                )}
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default PurchasesList;

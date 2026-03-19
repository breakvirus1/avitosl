import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

function CreatePostPage() {
  const navigate = useNavigate();
  const { isAuthenticated, apiService } = useAuth();
  const [categories, setCategories] = React.useState([]);
  const [loading, setLoading] = React.useState(true);
  const [submitting, setSubmitting] = React.useState(false);
  const [error, setError] = React.useState(null);
  const [success, setSuccess] = React.useState(false);

  const [formData, setFormData] = React.useState({
    title: '',
    description: '',
    price: '',
    categoryId: '',
    subcategoryId: ''
  });

  React.useEffect(() => {
    if (isAuthenticated) {
      fetchCategories();
    }
  }, [isAuthenticated]);

  const fetchCategories = async () => {
    try {
      setLoading(true);
      const response = await apiService.getCategories();
      setCategories(response.data);
      setError(null);
    } catch (err) {
      console.error('Error fetching categories:', err);
      setError(err.response?.data?.message || err.message || 'Failed to load categories');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    if (!formData.title.trim()) {
      setError('Please enter a title');
      return;
    }
    if (!formData.price || parseFloat(formData.price) < 0) {
      setError('Please enter a valid price');
      return;
    }
    if (!formData.categoryId) {
      setError('Please select a category');
      return;
    }
    if (!formData.subcategoryId) {
      setError('Please select a subcategory');
      return;
    }

    try {
      setSubmitting(true);
      const postData = {
        title: formData.title.trim(),
        description: formData.description || '',
        price: parseFloat(formData.price),
        categoryId: parseInt(formData.categoryId),
        subcategoryId: parseInt(formData.subcategoryId)
      };

      await apiService.createPost(postData);
      setSuccess(true);
      setFormData({
        title: '',
        description: '',
        price: '',
        categoryId: '',
        subcategoryId: ''
      });

      setTimeout(() => {
        navigate('/');
      }, 1500);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to create post');
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancel = () => {
    navigate('/');
  };

  const containerStyle = {
    maxWidth: '800px',
    margin: '0 auto',
    padding: '24px'
  };

  const headerStyle = {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: '24px'
  };

  const titleStyle = {
    fontSize: '24px',
    fontWeight: 600,
    margin: 0,
    color: '#000000d9'
  };

  const formStyle = {
    background: '#fff',
    padding: '24px',
    borderRadius: '8px',
    boxShadow: '0 1px 2px 0 rgba(0, 0, 0, 0.03), 0 1px 6px -1px rgba(0, 0, 0, 0.02), 0 2px 4px 0 rgba(0, 0, 0, 0.02)'
  };

  const formGroupStyle = {
    marginBottom: '16px'
  };

  const labelStyle = {
    display: 'block',
    marginBottom: '8px',
    fontWeight: 500,
    fontSize: '14px',
    color: '#000000d9'
  };

  const inputStyle = {
    width: '100%',
    padding: '8px 12px',
    border: '1px solid #d9d9d9',
    borderRadius: '4px',
    fontSize: '14px',
    boxSizing: 'border-box'
  };

  const textareaStyle = {
    ...inputStyle,
    minHeight: '100px',
    resize: 'vertical'
  };

  const buttonStyle = {
    padding: '8px 16px',
    borderRadius: '4px',
    border: 'none',
    cursor: 'pointer',
    fontSize: '14px',
    fontWeight: 500,
    transition: 'all 0.2s'
  };

  const primaryButtonStyle = {
    ...buttonStyle,
    background: '#1890ff',
    color: 'white'
  };

  const cancelButtonStyle = {
    ...buttonStyle,
    background: '#fff',
    color: '#000000d9',
    border: '1px solid #d9d9d9',
    marginRight: '8px'
  };

  const errorStyle = {
    background: '#fff2f0',
    border: '1px solid #ffccc7',
    borderRadius: '6px',
    padding: '12px 16px',
    marginBottom: '16px',
    color: '#ff4d4f'
  };

  const successStyle = {
    background: '#f6ffed',
    border: '1px solid #b7eb8f',
    borderRadius: '6px',
    padding: '12px 16px',
    marginBottom: '16px',
    color: '#52c41a'
  };

  if (!isAuthenticated) {
    return (
      <div style={containerStyle}>
        <div style={errorStyle}>
          <strong>Доступ запрещен</strong>
          <p style={{ margin: '8px 0 0 0' }}>Пожалуйста, авторизуйтесь для создания объявлений</p>
        </div>
      </div>
    );
  }

  return (
    <div style={containerStyle}>
      <div style={headerStyle}>
        <h1 style={titleStyle}>Создать объявление</h1>
        <button
          onClick={handleCancel}
          style={cancelButtonStyle}
        >
          Назад
        </button>
      </div>

      {error && (
        <div style={errorStyle}>
          <strong>Ошибка</strong>
          <p style={{ margin: '8px 0 0 0' }}>{error}</p>
        </div>
      )}

      {success && (
        <div style={successStyle}>
          <strong>Успех!</strong>
          <p style={{ margin: '8px 0 0 0' }}>Объявление создано. Перенаправление на главную страницу...</p>
        </div>
      )}

      <div style={formStyle}>
        <form onSubmit={handleSubmit}>
          <div style={formGroupStyle}>
            <label style={labelStyle} htmlFor="title">Название *</label>
            <input
              type="text"
              id="title"
              name="title"
              value={formData.title}
              onChange={handleChange}
              style={inputStyle}
              placeholder="Введите название объявления"
              disabled={submitting}
            />
          </div>

          <div style={formGroupStyle}>
            <label style={labelStyle} htmlFor="description">Описание</label>
            <textarea
              id="description"
              name="description"
              value={formData.description}
              onChange={handleChange}
              style={textareaStyle}
              placeholder="Введите описание объявления"
              disabled={submitting}
            />
          </div>

          <div style={formGroupStyle}>
            <label style={labelStyle} htmlFor="price">Цена *</label>
            <input
              type="number"
              id="price"
              name="price"
              value={formData.price}
              onChange={handleChange}
              style={inputStyle}
              placeholder="0.00"
              min="0"
              step="0.01"
              disabled={submitting}
            />
          </div>

          <div style={formGroupStyle}>
            <label style={labelStyle} htmlFor="categoryId">Категория *</label>
            <select
              id="categoryId"
              name="categoryId"
              value={formData.categoryId}
              onChange={handleChange}
              style={inputStyle}
              disabled={submitting || loading}
            >
              <option value="">Выберите категорию</option>
              {categories.map(category => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
          </div>

          <div style={{ display: 'flex', gap: '16px' }}>
            <div style={{ flex: 1 }}>
              <div style={formGroupStyle}>
                <label style={labelStyle} htmlFor="subcategoryId">Подкатегория *</label>
                <select
                  id="subcategoryId"
                  name="subcategoryId"
                  value={formData.subcategoryId}
                  onChange={handleChange}
                  style={inputStyle}
                  disabled={submitting || !formData.categoryId}
                >
                  <option value="">Выберите подкатегорию</option>
                  {formData.categoryId && categories
                    .find(c => c.id === parseInt(formData.categoryId))
                    ?.subcategories?.map(sub => (
                      <option key={sub.id} value={sub.id}>
                        {sub.name}
                      </option>
                    ))
                  }
                </select>
              </div>
            </div>
          </div>

          <div style={{ marginTop: '24px' }}>
            <button
              type="submit"
              style={primaryButtonStyle}
              disabled={submitting || loading}
            >
              {submitting ? 'Создание...' : 'Создать объявление'}
            </button>
            <button
              type="button"
              onClick={handleCancel}
              style={cancelButtonStyle}
              disabled={submitting}
            >
              Отмена
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default CreatePostPage;

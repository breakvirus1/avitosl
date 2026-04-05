import { useRef, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import AuthBar from './AuthBar';
import './CreatePostPage.css';

function CreatePostPage() {
  const navigate = useNavigate();
  const { isAuthenticated, apiService } = useAuth();
  const fileInputRef = useRef(null);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  const [uploadingFile, setUploadingFile] = useState(null);
  const [photos, setPhotos] = useState([]);
  const [createdPostId, setCreatedPostId] = useState(null);

  const [formData, setFormData] = useState({
    title: '',
    description: '',
    price: '',
    categoryId: '',
    subcategoryId: ''
  });

  useEffect(() => {
    if (isAuthenticated) {
      fetchCategories();
    }
  }, [isAuthenticated]);

  useEffect(() => {
    if (createdPostId) {
      fetchPhotos();
    }
  }, [createdPostId]);

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
    if (error) setError(null);
  };

  const handleFileSelect = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    try {
      setUploadingFile(file.name);
      // Если пост уже создан, загружаем фото для него
      if (createdPostId) {
        await apiService.uploadPhoto(createdPostId, file);
        setUploadingFile(null);
        if (fileInputRef.current) {
          fileInputRef.current.value = '';
        }
        await fetchPhotos();
        alert('Файл успешно загружен');
      } else {
        setError('Сначала создайте объявление, затем загружайте фото');
        setUploadingFile(null);
      }
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Ошибка загрузки файла');
      setUploadingFile(null);
    }
  };

  const fetchPhotos = async () => {
    try {
      const response = await apiService.getPhotosByPost(createdPostId);
      setPhotos(response.data);
    } catch (err) {
      console.error('Error fetching photos:', err);
      setPhotos([]);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    if (!formData.title.trim()) {
      setError('Введите название');
      return;
    }
    if (formData.price && parseFloat(formData.price) < 0) {
      setError('Цена не может быть отрицательной');
      return;
    }
    if (formData.price && parseFloat(formData.price) > 99999999.99) {
      setError('Цена не может превышать 99,999,999.99');
      return;
    }
    if (formData.price && parseFloat(formData.price) > 99999999.99) {
      setError('Цена не может превышать 99,999,999.99');
      return;
    }
    if (!formData.categoryId) {
      setError('Выберите категорию');
      return;
    }
    if (!formData.subcategoryId) {
      setError('Выберите подкатегорию');
      return;
    }

    try {
      setSubmitting(true);
      const postData = {
        title: formData.title.trim(),
        description: formData.description || '',
        price: formData.price ? parseFloat(formData.price) : null,
        categoryId: parseInt(formData.categoryId),
        subcategoryId: parseInt(formData.subcategoryId)
      };

      const response = await apiService.createPost(postData);
      const newPostId = response.data.id;
      setCreatedPostId(newPostId);
      setSuccess(true);
      
      // Не очищаем форму сразу, даем возможность загрузить фото
      setFormData({
        title: '',
        description: '',
        price: '',
        categoryId: '',
        subcategoryId: ''
      });

      // Перенаправление через 3 секунды, чтобы успел загрузить фото
      setTimeout(() => {
        navigate('/');
      }, 3000);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Ошибка создания объявления');
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancel = () => {
    navigate('/');
  };

  if (!isAuthenticated) {
    return (
      <div className="create-post-container">
        <div className="create-post-error">
          <strong>Доступ запрещен</strong>
          <p>Пожалуйста, авторизуйтесь для создания объявлений</p>
        </div>
      </div>
    );
  }

  return (
    <div className="create-post-container">
      <div style={{ maxWidth: '800px', margin: '0 auto', width: '100%' }}>
        <AuthBar />
        <div className="create-post-header">
        <h1>Создать объявление</h1>
        <button
          className="cancel-btn"
          onClick={handleCancel}
        >
          Назад
        </button>
      </div>

      {error && (
        <div className="create-post-error">
          <strong>Ошибка</strong>
          <p>{error}</p>
        </div>
      )}

      {success && (
        <div className="create-post-success">
          <strong>Успех!</strong>
          <p>Объявление создано. Перенаправление на главную страницу...</p>
        </div>
      )}

      <div className="create-post-form">
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="title">Название *</label>
            <input
              type="text"
              id="title"
              name="title"
              value={formData.title}
              onChange={handleChange}
              placeholder="Введите название объявления"
              disabled={submitting}
            />
          </div>

          <div className="form-group">
            <label htmlFor="description">Описание</label>
            <textarea
              id="description"
              name="description"
              value={formData.description}
              onChange={handleChange}
              placeholder="Введите описание объявления"
              disabled={submitting}
            />
          </div>

          <div className="form-group">
            <label htmlFor="price">Цена *</label>
            <input
              type="number"
              id="price"
              name="price"
              value={formData.price}
              onChange={handleChange}
              placeholder="0.00"
              min="0"
              max="99999999.99"
              step="0.01"
              disabled={submitting}
            />
          </div>

          <div className="form-group">
            <label htmlFor="categoryId">Категория *</label>
            <select
              id="categoryId"
              name="categoryId"
              value={formData.categoryId}
              onChange={handleChange}
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

          <div className="form-group">
            <label htmlFor="subcategoryId">Подкатегория *</label>
            <select
              id="subcategoryId"
              name="subcategoryId"
              value={formData.subcategoryId}
              onChange={handleChange}
              disabled={submitting || !formData.categoryId}
            >
              <option value="">Выберите подкатегорию</option>
              {formData.categoryId && categories
                .find(c => c.id === parseInt(formData.categoryId))
                ?.subcategories?.map(sub => (
                  <option key={sub.id} value={sub.id}>
                    {sub.name}
                  </option>
                ))}
            </select>
          </div>

          <div className="create-post-actions">
            <button
              type="submit"
              className="submit-btn"
              disabled={submitting || loading}
            >
              {submitting ? 'Создание...' : 'Создать объявление'}
            </button>
            <button
              type="button"
              onClick={handleCancel}
              className="cancel-btn"
              disabled={submitting}
            >
              Отмена
            </button>
          </div>
        </form>
      </div>

      <div className="create-post-photos-section">
        <h2>Фотографии (необязательно)</h2>
        <p className="help-text">Вы можете загрузить фотографии после создания объявления</p>
        
        <div className="file-upload-section">
          <input
            type="file"
            ref={fileInputRef}
            onChange={handleFileSelect}
            accept="image/*"
            style={{ display: 'none' }}
          />
          <button
            type="button"
            className="upload-file-btn"
            onClick={() => fileInputRef.current?.click()}
            disabled={!!uploadingFile || !createdPostId}
            title={!createdPostId ? "Сначала создайте объявление" : ""}
          >
            {uploadingFile ? `Загрузка ${uploadingFile}...` : 'Загрузить фото'}
          </button>
          {!createdPostId && (
            <span className="upload-hint"> (сначала создайте объявление)</span>
          )}
        </div>

        {photos.length > 0 && (
          <div className="photos-preview">
            <h3>Загруженные фотографии:</h3>
            <div className="photos-grid">
              {photos.map((photo) => (
                <div key={photo.id} className="photo-item">
                  <img
                    src={`http://localhost:8081/api/photos/${photo.id}/file`}
                    alt="Фото"
                    className="photo-preview-img"
                  />
                </div>
              ))}
            </div>
          </div>
        )}
        </div>
      </div>
    </div>
  );
}

export default CreatePostPage;

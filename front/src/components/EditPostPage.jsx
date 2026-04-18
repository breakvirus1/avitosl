import { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import AuthBar from './AuthBar';
import './CreatePostPage.css';

function EditPostPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated, apiService, user } = useAuth();
  const fileInputRef = useRef(null);
  
  const [post, setPost] = useState(null);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  const [uploadingFile, setUploadingFile] = useState(null);
  const [photos, setPhotos] = useState([]);

   const [formData, setFormData] = useState({
     title: '',
     description: '',
     price: '',
     categoryId: '',
     subcategoryId: '',
     active: false
   });

  useEffect(() => {
    if (isAuthenticated && id) {
      fetchPost();
      fetchCategories();
      fetchPhotos();
    }
  }, [isAuthenticated, id]);

  const fetchPost = async () => {
    try {
      setLoading(true);
      const response = await apiService.getPost(id);
      const postData = response.data;
      setPost(postData);
        setFormData({
          title: postData.title,
          description: postData.description || '',
          price: postData.price?.toString() || '',
          categoryId: postData.category?.id?.toString() || '',
          subcategoryId: postData.subcategory?.id?.toString() || '',
          active: postData.isActive
        });
      setError(null);
    } catch (err) {
      console.error('Error fetching post:', err);
      setError(err.response?.data?.message || err.message || 'Failed to load post');
    } finally {
      setLoading(false);
    }
  };

  const fetchCategories = async () => {
    try {
      const response = await apiService.getCategories();
      setCategories(response.data);
    } catch (err) {
      console.error('Error fetching categories:', err);
    }
  };

  const fetchPhotos = async () => {
    try {
      const response = await apiService.getPhotosByPost(id);
      setPhotos(response.data);
    } catch (err) {
      console.error('Error fetching photos:', err);
      setPhotos([]);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (error) setError(null);
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
         subcategoryId: parseInt(formData.subcategoryId),
         isActive: formData.active,
         keycloakId: user?.keycloakId || user?.sub
       };

      await apiService.updatePost(id, postData);
      setSuccess(true);
      
      setTimeout(() => {
        navigate(`/post/${id}`);
      }, 1500);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Ошибка обновления объявления');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeletePost = async () => {
    if (!window.confirm('Вы уверены, что хотите удалить это объявление?')) {
      return;
    }
    
    try {
      setDeleting(true);
      await apiService.deletePost(id);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Ошибка удаления объявления');
      setDeleting(false);
    }
  };

  const handleFileSelect = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    try {
      setUploadingFile(file.name);
      await apiService.uploadPhoto(id, file);
      setUploadingFile(null);
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
      // Перезагружаем фото
      await fetchPhotos();
      alert('Файл успешно загружен');
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Ошибка загрузки файла');
      setUploadingFile(null);
    }
  };

  const handleDeletePhoto = async (photoId) => {
    if (!window.confirm('Удалить это фото?')) {
      return;
    }
    
    try {
      await apiService.deletePhoto(photoId);
      await fetchPhotos();
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Ошибка удаления фото');
    }
  };

  if (!isAuthenticated) {
    return (
      <div className="create-post-container">
        <div className="create-post-error">
          <strong>Доступ запрещен</strong>
          <p>Пожалуйста, авторизуйтесь для редактирования объявлений</p>
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="create-post-container">
        <div className="create-post-loading">
          <div className="spinner"></div>
          <p>Загрузка объявления...</p>
        </div>
      </div>
    );
  }

  if (error && !post) {
    return (
      <div className="create-post-container">
        <div className="create-post-error">
          <strong>Ошибка</strong>
          <p>{error}</p>
          <button onClick={() => navigate('/')} className="back-btn">
            На главную
          </button>
        </div>
      </div>
    );
  }

  const selectedCategory = categories.find(cat => cat.id === parseInt(formData.categoryId));

  return (
    <div className="create-post-container">
      <div style={{ maxWidth: '800px', margin: '0 auto', width: '100%' }}>
        <AuthBar />
        <div className="create-post-header">
        <h1>Редактировать объявление</h1>
        <button
          className="cancel-btn"
          onClick={() => navigate(`/post/${id}`)}
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
          <p>Объявление обновлено. Перенаправление...</p>
        </div>
      )}

      <div className="edit-post-content">
        <div className="edit-post-form-section">
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
                rows="4"
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
                disabled={submitting || !selectedCategory || loading}
              >
                <option value="">Выберите подкатегорию</option>
                {selectedCategory?.subcategories?.map(sub => (
                  <option key={sub.id} value={sub.id}>{sub.name}</option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                <input
                  type="checkbox"
                  id="active"
                  name="active"
                  checked={formData.active}
                  onChange={(e) => setFormData(prev => ({ ...prev, active: e.target.checked }))}
                  disabled={submitting}
                  style={{ width: 'auto', margin: 0 }}
                />
                Активно
              </label>
            </div>

            <div className="form-actions">
              <button type="submit" className="submit-btn" disabled={submitting}>
                {submitting ? 'Сохранение...' : 'Сохранить изменения'}
              </button>
              <button 
                type="button" 
                className="delete-btn"
                onClick={handleDeletePost}
                disabled={deleting}
              >
                {deleting ? 'Удаление...' : 'Удалить объявление'}
              </button>
            </div>
          </form>
        </div>

        <div className="edit-post-photos-section">
          <h2>Фотографии объявления</h2>
          
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
              disabled={!!uploadingFile}
            >
              {uploadingFile ? `Загрузка ${uploadingFile}...` : 'Загрузить фото'}
            </button>
          </div>

          {photos.length > 0 ? (
            <div className="photos-list">
              {photos.map((photo) => (
                <div key={photo.id} className="photo-item">
                  <img
                    src={`/api/photos/${photo.id}/file`}
                    alt="Фото"
                    className="photo-preview"
                  />
                  <button
                    type="button"
                    className="delete-photo-btn"
                    onClick={() => handleDeletePhoto(photo.id)}
                  >
                    Удалить
                  </button>
                </div>
              ))}
            </div>
          ) : (
            <p className="no-photos">Нет загруженных фотографий</p>
          )}
        </div>
        </div>
      </div>
    </div>
  );
}

export default EditPostPage;

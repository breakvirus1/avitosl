import { useState, useEffect } from 'react';
import { useAuth } from '../hooks/useAuth';
import { useNavigate } from 'react-router-dom';
import AuthBar from './AuthBar';
import './AdminPanel.css';

const AdminPanel = () => {
  const navigate = useNavigate();
  const { user, isAuthenticated, apiService } = useAuth();
  
  // Все useState в самом начале
  const [activeTab, setActiveTab] = useState('users');
  const [users, setUsers] = useState([]);
  const [comments, setComments] = useState([]);
  const [posts, setPosts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [subcategories, setSubcategories] = useState([]);
  const [message, setMessage] = useState({ type: '', text: '' });

  // Users state
  const [userForm, setUserForm] = useState({ email: '', firstName: '', phoneNumber: '' });
  const [editingUserId, setEditingUserId] = useState(null);

  // Category state
  const [categoryForm, setCategoryForm] = useState({ name: '' });
  const [editingCategoryId, setEditingCategoryId] = useState(null);

  // Subcategory state
  const [subcategoryForm, setSubcategoryForm] = useState({ name: '', categoryId: null });
  const [editingSubcategoryId, setEditingSubcategoryId] = useState(null);

  // Post state
  const [postForm, setPostForm] = useState({ title: '', description: '', price: '', categoryId: '', subcategoryId: '' });
  const [editingPostId, setEditingPostId] = useState(null);

  // Comment state
  const [commentForm, setCommentForm] = useState({ text: '', postId: '' });
  const [editingCommentId, setEditingCommentId] = useState(null);

  // Fake Data state
  const [fakePostCount, setFakePostCount] = useState(10);

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
  
  // Первый useEffect - проверка роли
  useEffect(() => {
    if (isAuthenticated && !hasAdminRole()) {
      navigate('/');
    }
  }, [isAuthenticated, navigate]);
  
  // Второй useEffect - загрузка данных
  useEffect(() => {
    const loadData = async () => {
      try {
        switch (activeTab) {
          case 'users': {
            const res = await apiService.getUsers();
            setUsers(res.data);
            break;
          }
          case 'comments': {
            const res = await apiService.getComments();
            setComments(res.data);
            break;
          }
          case 'posts': {
            const res = await apiService.getPosts(0, 100);
            setPosts(res.data.content);
            break;
          }
          case 'categories': {
            const res = await apiService.getCategories();
            setCategories(res.data);
            break;
          }
          case 'subcategories': {
            const res = await apiService.getSubcategories();
            setSubcategories(res.data);
            break;
          }
          default:
            break;
        }
      } catch (_) {
        console.error('Error fetching data:', activeTab);
        setMessage({ type: 'error', text: 'Ошибка загрузки данных' });
      }
    };
    loadData();
  }, [activeTab, apiService]);
  
  // Если пользователь аутентифицирован, но не админ - показываем сообщение
  if (isAuthenticated && !hasAdminRole()) {
    return (
      <div className="admin-panel">
        <h1>Доступ запрещен</h1>
        <p>У вас нет прав для доступа к этой странице.</p>
      </div>
    );
  }

  const showMessage = (type, text) => {
    setMessage({ type, text });
    setTimeout(() => setMessage({ type: '', text: '' }), 3000);
  };

  // Users CRUD
  const handleCreateUser = async (e) => {
    e.preventDefault();
    try {
      await apiService.register({ ...userForm, password: 'temp123' });
      setUserForm({ email: '', firstName: '', phoneNumber: '' });
      showMessage('success', 'Пользователь создан');
      const res = await apiService.getUsers();
      setUsers(res.data);
    } catch (_) {
      showMessage('error', 'Ошибка создания пользователя');
    }
  };

  const handleUpdateUser = async (e) => {
    e.preventDefault();
    try {
      await apiService.updateUser(editingUserId, userForm);
      setEditingUserId(null);
      setUserForm({ email: '', firstName: '', phoneNumber: '' });
      showMessage('success', 'Пользователь обновлен');
      const res = await apiService.getUsers();
      setUsers(res.data);
    } catch (_) {
      showMessage('error', 'Ошибка обновления пользователя');
    }
  };

  const handleDeleteUser = async (id) => {
    if (!window.confirm('Заблокировать пользователя?')) return;
    try {
      await apiService.deleteUser(id);
      showMessage('success', 'Пользователь заблокирован');
      const res = await apiService.getUsers();
      setUsers(res.data);
    } catch (_) {
      showMessage('error', 'Ошибка блокировки пользователя');
    }
  };

  const handleEditUser = (user) => {
    setEditingUserId(user.id);
    setUserForm({
      email: user.email,
      firstName: user.firstName,
      phoneNumber: user.phoneNumber || ''
    });
  };

  // Comments CRUD
  const handleCreateComment = async (e) => {
    e.preventDefault();
    try {
      await apiService.createComment(commentForm);
      setCommentForm({ text: '', postId: '' });
      showMessage('success', 'Комментарий создан');
      const res = await apiService.getComments();
      setComments(res.data);
    } catch (_) {
      showMessage('error', 'Ошибка создания комментария');
    }
  };

  const handleUpdateComment = async (e) => {
    e.preventDefault();
    try {
      await apiService.updateComment(editingCommentId, { text: commentForm.text });
      setEditingCommentId(null);
      setCommentForm({ text: '', postId: '' });
      showMessage('success', 'Комментарий обновлен');
      const res = await apiService.getComments();
      setComments(res.data);
    } catch (_) {
      showMessage('error', 'Ошибка обновления комментария');
    }
  };

  const handleDeleteComment = async (id) => {
    if (!window.confirm('Удалить комментарий?')) return;
    try {
      await apiService.deleteComment(id);
      showMessage('success', 'Комментарий удален');
      const res = await apiService.getComments();
      setComments(res.data);
    } catch (_) {
      showMessage('error', 'Ошибка удаления комментария');
    }
  };

  const handleEditComment = (comment) => {
    setEditingCommentId(comment.id);
    setCommentForm({
      text: comment.text || comment.content,
      postId: comment.postId
    });
  };

  // Posts CRUD
  const handleCreatePost = async (e) => {
    e.preventDefault();
    try {
      await apiService.createPost({
        ...postForm,
        price: parseFloat(postForm.price) || 0
      });
      setPostForm({ title: '', description: '', price: '', categoryId: '', subcategoryId: '' });
      showMessage('success', 'Объявление создано');
      const res = await apiService.getPosts(0, 100);
      setPosts(res.data.content);
    } catch (_) {
      showMessage('error', 'Ошибка создания объявления');
    }
  };

  const handleUpdatePost = async (e) => {
    e.preventDefault();
    try {
      await apiService.updatePost(editingPostId, {
        ...postForm,
        price: parseFloat(postForm.price) || 0
      });
      setEditingPostId(null);
      setPostForm({ title: '', description: '', price: '', categoryId: '', subcategoryId: '' });
      showMessage('success', 'Объявление обновлен');
      const res = await apiService.getPosts(0, 100);
      setPosts(res.data.content);
    } catch (_) {
      showMessage('error', 'Ошибка обновления объявления');
    }
  };

  const handleDeletePost = async (id) => {
    if (!window.confirm('Удалить объявление?')) return;
    try {
      await apiService.deletePost(id);
      showMessage('success', 'Объявление удалено');
      const res = await apiService.getPosts(0, 100);
      setPosts(res.data.content);
    } catch (_) {
      showMessage('error', 'Ошибка удаления объявления');
    }
  };

  const handleEditPost = (post) => {
    setEditingPostId(post.id);
    setPostForm({
      title: post.title,
      description: post.description,
      price: post.price?.toString() || '',
      categoryId: post.categoryId?.toString() || '',
      subcategoryId: post.subcategoryId?.toString() || ''
    });
  };

  // Categories CRUD
  const handleCreateCategory = async (e) => {
    e.preventDefault();
    try {
      await apiService.createCategory(categoryForm);
      setCategoryForm({ name: '' });
      showMessage('success', 'Категория создана');
      const res = await apiService.getCategories();
      setCategories(res.data);
    } catch (_) {
      showMessage('error', 'Ошибка создания категории');
    }
  };

  const handleUpdateCategory = async (e) => {
    e.preventDefault();
    try {
      await apiService.updateCategory(editingCategoryId, categoryForm);
      setEditingCategoryId(null);
      setCategoryForm({ name: '' });
      showMessage('success', 'Категория обновлена');
      const res = await apiService.getCategories();
      setCategories(res.data);
    } catch (_) {
      showMessage('error', 'Ошибка обновления категории');
    }
  };

  const handleDeleteCategory = async (id) => {
    if (!window.confirm('Удалить категорию и все ее подкатегории?')) return;
    try {
      await apiService.deleteCategory(id);
      showMessage('success', 'Категория удалена');
      const res = await apiService.getCategories();
      setCategories(res.data);
    } catch (_) {
      showMessage('error', 'Ошибка удаления категории');
    }
  };

  const handleEditCategory = (category) => {
    setEditingCategoryId(category.id);
    setCategoryForm({
      name: category.name
    });
  };

  // Subcategories CRUD
  const handleCreateSubcategory = async (e) => {
    e.preventDefault();
    try {
      await apiService.createSubcategory(subcategoryForm);
      setSubcategoryForm({ name: '', categoryId: null });
      showMessage('success', 'Подкатегория создана');
      const res = await apiService.getSubcategories();
      setSubcategories(res.data);
    } catch (_) {
      showMessage('error', 'Ошибка создания подкатегории');
    }
  };

  const handleUpdateSubcategory = async (e) => {
    e.preventDefault();
    try {
      await apiService.updateSubcategory(editingSubcategoryId, subcategoryForm);
      setEditingSubcategoryId(null);
      setSubcategoryForm({ name: '', categoryId: null });
      showMessage('success', 'Подкатегория обновлена');
      const res = await apiService.getSubcategories();
      setSubcategories(res.data);
    } catch (_) {
      showMessage('error', 'Ошибка обновления подкатегории');
    }
  };

  const handleDeleteSubcategory = async (id) => {
    if (!window.confirm('Удалить подкатегорию?')) return;
    try {
      await apiService.deleteSubcategory(id);
      showMessage('success', 'Подкатегория удалена');
      const res = await apiService.getSubcategories();
      setSubcategories(res.data);
    } catch (_) {
      showMessage('error', 'Ошибка удаления подкатегории');
    }
  };

  const handleEditSubcategory = (subcategory) => {
    setEditingSubcategoryId(subcategory.id);
    setSubcategoryForm({
      name: subcategory.name,
      categoryId: subcategory.categoryId
    });
  };

  // Fake Data
  const handleGenerateFakePosts = async () => {
    try {
      await apiService.generateFakePosts(fakePostCount);
      showMessage('success', `Создано ${fakePostCount} фейковых объявлений`);
      const res = await apiService.getPosts(0, 100);
      setPosts(res.data.content);
    } catch (_) {
      showMessage('error', 'Ошибка генерации фейковых данных');
    }
  };

  const handleClearAllPosts = async () => {
    if (!window.confirm('Удалить все объявления и комментарии?')) return;
    try {
      await apiService.clearAllPosts();
      showMessage('success', 'Все объявления удалены');
      const res = await apiService.getPosts(0, 100);
      setPosts(res.data.content);
    } catch (_) {
      showMessage('error', 'Ошибка очистки данных');
    }
  };

  const tabs = [
    { id: 'users', label: 'Пользователи' },
    { id: 'posts', label: 'Объявления' },
    { id: 'categories', label: 'Категории' },
    { id: 'subcategories', label: 'Подкатегории' },
    { id: 'comments', label: 'Комментарии' },
    { id: 'fakedata', label: 'Фейковые данные' }
  ];

  return (
    <div className="admin-panel">
      <AuthBar />
      <h1>Admin Panel</h1>
      
      {message.text && (
        <div className={`admin-message admin-message-${message.type}`}>
          {message.text}
        </div>
      )}

      <div className="admin-tabs">
        {tabs.map(tab => (
          <button
            key={tab.id}
            className={`admin-tab ${activeTab === tab.id ? 'admin-tab-active' : ''}`}
            onClick={() => setActiveTab(tab.id)}
          >
            {tab.label}
          </button>
        ))}
      </div>

      <div className="admin-content">
        {activeTab === 'users' && (
          <div className="admin-section">
            <h2>Управление пользователями</h2>
            <form onSubmit={editingUserId ? handleUpdateUser : handleCreateUser} className="admin-form">
              <input
                type="email"
                placeholder="Email"
                value={userForm.email}
                onChange={(e) => setUserForm({ ...userForm, email: e.target.value })}
                required
              />
              <input
                type="text"
                placeholder="Имя"
                value={userForm.firstName}
                onChange={(e) => setUserForm({ ...userForm, firstName: e.target.value })}
                required
              />
              <input
                type="tel"
                placeholder="Телефон"
                value={userForm.phoneNumber}
                onChange={(e) => setUserForm({ ...userForm, phoneNumber: e.target.value })}
              />
              <button type="submit">{editingUserId ? 'Обновить' : 'Создать'}</button>
              {editingUserId && (
                <button type="button" onClick={() => { setEditingUserId(null); setUserForm({ email: '', firstName: '', phoneNumber: '' }); }}>
                  Отмена
                </button>
              )}
            </form>
            <table className="admin-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Email</th>
                  <th>Имя</th>
                  <th>Телефон</th>
                  <th>Активен</th>
                  <th>Действия</th>
                </tr>
              </thead>
              <tbody>
                {users.map(user => (
                  <tr key={user.id}>
                    <td>{user.id}</td>
                    <td>{user.email}</td>
                    <td>{user.firstName}</td>
                    <td>{user.phoneNumber || '-'}</td>
                    <td>{user.enabled ? 'Да' : 'Нет'}</td>
                    <td>
                      <button onClick={() => handleEditUser(user)}>Редактировать</button>
                      <button onClick={() => handleDeleteUser(user.id)}>Заблокировать</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {activeTab === 'comments' && (
          <div className="admin-section">
            <h2>Управление комментариями</h2>
            <form onSubmit={editingCommentId ? handleUpdateComment : handleCreateComment} className="admin-form">
              <textarea
                placeholder="Содержание"
                value={commentForm.text}
                onChange={(e) => setCommentForm({ ...commentForm, text: e.target.value })}
                required
              />
              <input
                type="number"
                placeholder="ID объявления"
                value={commentForm.postId}
                onChange={(e) => setCommentForm({ ...commentForm, postId: e.target.value })}
                required
              />
              <button type="submit">{editingCommentId ? 'Обновить' : 'Создать'}</button>
              {editingCommentId && (
                <button type="button" onClick={() => { setEditingCommentId(null); setCommentForm({ text: '', postId: '' }); }}>
                  Отмена
                </button>
              )}
            </form>
            <table className="admin-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Содержание</th>
                  <th>ID объявления</th>
                  <th>ID автора</th>
                  <th>Действия</th>
                </tr>
              </thead>
              <tbody>
                {comments.map(comment => (
                  <tr key={comment.id}>
                    <td>{comment.id}</td>
                    <td>{comment.text?.substring(0, 50) || comment.content?.substring(0, 50)}...</td>
                    <td>{comment.postId}</td>
                    <td>{comment.author?.id || comment.authorId || 'N/A'}</td>
                    <td>
                      <button onClick={() => handleEditComment(comment)}>Редактировать</button>
                      <button onClick={() => handleDeleteComment(comment.id)}>Удалить</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {activeTab === 'posts' && (
          <div className="admin-section">
            <h2>Управление объявлениями</h2>
            <form onSubmit={editingPostId ? handleUpdatePost : handleCreatePost} className="admin-form">
              <input
                type="text"
                placeholder="Заголовок"
                value={postForm.title}
                onChange={(e) => setPostForm({ ...postForm, title: e.target.value })}
                required
              />
              <textarea
                placeholder="Описание"
                value={postForm.description}
                onChange={(e) => setPostForm({ ...postForm, description: e.target.value })}
                required
              />
              <input
                type="number"
                placeholder="Цена"
                value={postForm.price}
                onChange={(e) => setPostForm({ ...postForm, price: e.target.value })}
              />
              <input
                type="number"
                placeholder="ID категории"
                value={postForm.categoryId}
                onChange={(e) => setPostForm({ ...postForm, categoryId: e.target.value })}
                required
              />
              <input
                type="number"
                placeholder="ID подкатегории"
                value={postForm.subcategoryId}
                onChange={(e) => setPostForm({ ...postForm, subcategoryId: e.target.value })}
              />
              <button type="submit">{editingPostId ? 'Обновить' : 'Создать'}</button>
              {editingPostId && (
                <button type="button" onClick={() => { setEditingPostId(null); setPostForm({ title: '', description: '', price: '', categoryId: '', subcategoryId: '' }); }}>
                  Отмена
                </button>
              )}
            </form>
            <table className="admin-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Заголовок</th>
                  <th>Цена</th>
                  <th>Категория</th>
                  <th>Действия</th>
                </tr>
              </thead>
              <tbody>
                {posts.map(post => (
                  <tr key={post.id}>
                    <td>{post.id}</td>
                    <td>{post.title}</td>
                    <td>{post.price} ₽</td>
                    <td>{post.categoryName || post.categoryId}</td>
                    <td>
                      <button onClick={() => handleEditPost(post)}>Редактировать</button>
                      <button onClick={() => handleDeletePost(post.id)}>Удалить</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {activeTab === 'categories' && (
          <div className="admin-section">
            <h2>Управление категориями</h2>
            <form onSubmit={editingCategoryId ? handleUpdateCategory : handleCreateCategory} className="admin-form">
              <input
                type="text"
                placeholder="Название категории"
                value={categoryForm.name}
                onChange={(e) => setCategoryForm({ name: e.target.value })}
                required
              />
              <button type="submit">{editingCategoryId ? 'Обновить' : 'Создать'}</button>
              {editingCategoryId && (
                <button type="button" onClick={() => { setEditingCategoryId(null); setCategoryForm({ name: '' }); }}>
                  Отмена
                </button>
              )}
            </form>
            <table className="admin-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Название</th>
                  <th>Подкатегорий</th>
                  <th>Действия</th>
                </tr>
              </thead>
              <tbody>
                {categories.map(category => (
                  <tr key={category.id}>
                    <td>{category.id}</td>
                    <td>{category.name}</td>
                    <td>{category.subcategories?.length || 0}</td>
                    <td>
                      <button onClick={() => handleEditCategory(category)}>Редактировать</button>
                      <button onClick={() => handleDeleteCategory(category.id)}>Удалить</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {activeTab === 'subcategories' && (
          <div className="admin-section">
            <h2>Управление подкатегориями</h2>
            <form onSubmit={editingSubcategoryId ? handleUpdateSubcategory : handleCreateSubcategory} className="admin-form">
              <input
                type="text"
                placeholder="Название подкатегории"
                value={subcategoryForm.name}
                onChange={(e) => setSubcategoryForm({ ...subcategoryForm, name: e.target.value })}
                required
              />
              <select
                value={subcategoryForm.categoryId || ''}
                onChange={(e) => setSubcategoryForm({ ...subcategoryForm, categoryId: e.target.value ? parseInt(e.target.value) : null })}
                required
              >
                <option value="">Выберите категорию</option>
                {categories.map(cat => (
                  <option key={cat.id} value={cat.id}>{cat.name}</option>
                ))}
              </select>
              <button type="submit">{editingSubcategoryId ? 'Обновить' : 'Создать'}</button>
              {editingSubcategoryId && (
                <button type="button" onClick={() => { setEditingSubcategoryId(null); setSubcategoryForm({ name: '', categoryId: null }); }}>
                  Отмена
                </button>
              )}
            </form>
            <table className="admin-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Название</th>
                  <th>Категория</th>
                  <th>Действия</th>
                </tr>
              </thead>
              <tbody>
                {subcategories.map(sub => (
                  <tr key={sub.id}>
                    <td>{sub.id}</td>
                    <td>{sub.name}</td>
                    <td>{categories.find(c => c.id === sub.categoryId)?.name || sub.categoryId}</td>
                    <td>
                      <button onClick={() => handleEditSubcategory(sub)}>Редактировать</button>
                      <button onClick={() => handleDeleteSubcategory(sub.id)}>Удалить</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {activeTab === 'fakedata' && (
          <div className="admin-section">
            <h2>Генерация фейковых данных</h2>
            <div className="fake-data-controls">
              <div className="fake-data-input">
                <label>Количество объявлений:</label>
                <input
                  type="number"
                  min="1"
                  max="1000"
                  value={fakePostCount}
                  onChange={(e) => setFakePostCount(parseInt(e.target.value) || 10)}
                />
              </div>
              <div className="fake-data-buttons">
                <button onClick={handleGenerateFakePosts} className="fake-generate-btn">
                  Сгенерировать объявления
                </button>
                <button onClick={handleClearAllPosts} className="fake-clear-btn">
                  Очистить все объявления
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminPanel;

import React from 'react'
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import PrivateRoute from './components/PrivateRoute.jsx'
import AuthBar from './components/AuthBar.jsx'
import { useAuth } from "react-oidc-context"
import { postApi } from './services/api.js'
import PostForm from './components/PostForm.jsx'
import PostList from './components/PostList.jsx'

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<PrivateRoute><Home /></PrivateRoute>} />
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </Router>
  )
}

function Home() {
  const [posts, setPosts] = React.useState([])
  const [categories, setCategories] = React.useState([])
  const [loading, setLoading] = React.useState(true)
  const [error, setError] = React.useState(null)
  const [pagination, setPagination] = React.useState({
    currentPage: 0,
    pageSize: 20,
    totalElements: 0,
    totalPages: 0
  })
  const auth = useAuth()

  const fetchPosts = async (page = 0, size = 20) => {
    try {
      setLoading(true)
      const accessToken = auth.user?.access_token
      if (!accessToken) {
        throw new Error('No access token available')
      }

      const response = await postApi.getPosts(accessToken, page, size)
      setPosts(response.data.content)
      setPagination({
        currentPage: page,
        pageSize: size,
        totalElements: response.data.totalElements,
        totalPages: response.data.totalPages
      })
      setError(null)
    } catch (err) {
      console.error('Error fetching posts:', err)
      setError(err.response?.data?.message || err.message || 'Failed to load posts')
    } finally {
      setLoading(false)
    }
  }

  const fetchCategories = async () => {
    try {
      const accessToken = auth.user?.access_token
      if (!accessToken) return

      const response = await postApi.getCategories(accessToken)
      setCategories(response.data)
    } catch (err) {
      console.error('Error fetching categories:', err)
    }
  }

  React.useEffect(() => {
    if (auth.isAuthenticated) {
      fetchPosts(pagination.currentPage, pagination.pageSize)
      fetchCategories()
    }
  }, [auth.isAuthenticated])

  const handleCreatePost = async (postData) => {
    try {
      const accessToken = auth.user?.access_token
      await postApi.createPost(postData, accessToken)
      await fetchPosts()
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to create post')
      throw err
    }
  }

  const handleDeletePost = async (postId) => {
    try {
      const accessToken = auth.user?.access_token
      await postApi.deletePost(postId, accessToken)
      await fetchPosts(pagination.currentPage, pagination.pageSize)
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to delete post')
    }
  }

  const handlePageChange = (newPage) => {
    fetchPosts(newPage, pagination.pageSize)
  }

  const hasRole = (roleName) => {
    if (!auth.user?.profile) return false
    const roles = auth.user.profile.realm_access?.roles || []
    return roles.includes(roleName)
  }

  const isAdmin = hasRole('ADMIN')
  const isUser = hasRole('USER')

  return (
    <div>
      <header style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        background: '#001529',
        padding: '0 20px',
        height: '64px'
      }}>
        <h1 style={{ color: 'white', margin: 0, fontSize: '24px' }}>Avito</h1>
        <AuthBar />
      </header>
      <main style={{ padding: '24px', minHeight: 'calc(100vh - 64px)' }}>
        {error && (
          <div style={{
            background: '#fff2f0',
            border: '1px solid #ffccc7',
            borderRadius: '6px',
            padding: '16px',
            marginBottom: '16px',
            color: '#ff4d4f'
          }}>
            <strong>Ошибка</strong>
            <p style={{ margin: '8px 0 0 0' }}>{error}</p>
            <button
              onClick={() => setError(null)}
              style={{
                position: 'absolute',
                right: '16px',
                top: '16px',
                background: 'transparent',
                border: 'none',
                cursor: 'pointer',
                fontSize: '16px'
              }}
            >
              ×
            </button>
          </div>
        )}

        <div style={{ display: 'flex', gap: '24px' }}>
          <div style={{ flex: '0 0 25%' }}>
            {isUser && (
              <PostForm
                onFinish={handleCreatePost}
                categories={categories}
              />
            )}
            {!isUser && (
              <div style={{
                background: '#fffbe6',
                border: '1px solid #ffe58f',
                borderRadius: '6px',
                padding: '16px',
                color: '#faad14'
              }}>
                <strong>Доступ ограничен</strong>
                <p style={{ margin: '8px 0 0 0' }}>Только авторизованные пользователи могут создавать объявления</p>
              </div>
            )}
          </div>
          <div style={{ flex: '1' }}>
            <PostList
              posts={posts}
              onDelete={handleDeletePost}
              loading={loading}
              isAdmin={isAdmin}
              pagination={pagination}
              onPageChange={handlePageChange}
            />
          </div>
        </div>
      </main>
    </div>
  )
}

export default App

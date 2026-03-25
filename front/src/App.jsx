import React from 'react'
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './contexts/AuthProvider'
import PrivateRoute from './components/PrivateRoute.jsx'
import HomeWrapper from './components/HomeWrapper.jsx'
import CreatePostPage from './components/CreatePostPage.jsx'
import EditPostPage from './components/EditPostPage.jsx'
import UserProfile from './components/UserProfile.jsx'
import Callback from './components/Callback.jsx'
import PostView from './components/PostView.jsx'

function App() {
  return (
    <Router>
      <AuthProvider>
        <Routes>
          <Route path="/" element={<HomeWrapper />} />
          <Route path="/create-post" element={<PrivateRoute><CreatePostPage /></PrivateRoute>} />
          <Route path="/edit-post/:id" element={<PrivateRoute><EditPostPage /></PrivateRoute>} />
          <Route path="/profile" element={<PrivateRoute><UserProfile /></PrivateRoute>} />
          <Route path="/post/:id" element={<PostView />} />
          <Route path="/callback" element={<Callback />} />
          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      </AuthProvider>
    </Router>
  )
}

export default App

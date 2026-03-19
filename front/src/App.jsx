import React from 'react'
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './contexts/AuthProvider'
import PrivateRoute from './components/PrivateRoute.jsx'
import AuthBar from './components/AuthBar.jsx'
import Home from './components/Home.jsx'
import HomePublic from './components/HomePublic.jsx'
import CreatePostPage from './components/CreatePostPage.jsx'
import Callback from './components/Callback.jsx'
import PostView from './components/PostView.jsx'
import UserProfile from './components/UserProfile.jsx'
import './App.css'

function App() {
  return (
    <Router>
      <AuthProvider>
        <Routes>
          <Route path="/" element={<HomePublic />} />
          <Route path="/home" element={<PrivateRoute><Home /></PrivateRoute>} />
          <Route path="/profile" element={<PrivateRoute><UserProfile /></PrivateRoute>} />
          <Route path="/create-post" element={<PrivateRoute><CreatePostPage /></PrivateRoute>} />
          <Route path="/post/:id" element={<PrivateRoute><PostView /></PrivateRoute>} />
          <Route path="/callback" element={<Callback />} />
          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      </AuthProvider>
    </Router>
  )
}

export default App

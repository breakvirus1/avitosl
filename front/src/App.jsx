import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './contexts/AuthProvider'
import PrivateRoute from './components/PrivateRoute.jsx'
import HomeWrapper from './components/HomeWrapper.jsx'
import CreatePostPage from './components/CreatePostPage.jsx'
import EditPostPage from './components/EditPostPage.jsx'
import UserProfile from './components/UserProfile.jsx'
import Callback from './components/Callback.jsx'
<<<<<<< HEAD
import PostView from './components/PostView.jsx'
=======
import PostViewWrapper from './components/PostViewWrapper.jsx'
import BoughtPost from './components/BoughtPost.jsx'
import PurchasesList from './components/PurchasesList.jsx'
>>>>>>> kafka
import NotificationsPage from './components/NotificationsPage.jsx'
import AdminPanel from './components/AdminPanel.jsx'

function App() {
  return (
    <Router>
      <AuthProvider>
        <Routes>
          <Route path="/" element={<HomeWrapper />} />
          <Route path="/create-post" element={<PrivateRoute><CreatePostPage /></PrivateRoute>} />
          <Route path="/edit-post/:id" element={<PrivateRoute><EditPostPage /></PrivateRoute>} />
          <Route path="/profile" element={<PrivateRoute><UserProfile /></PrivateRoute>} />
<<<<<<< HEAD
          <Route path="/post/:id" element={<PostView />} />
=======
          <Route path="/post/:id" element={<PostViewWrapper />} />
          <Route path="/bought/:id" element={<PrivateRoute><BoughtPost /></PrivateRoute>} />
          <Route path="/purchases" element={<PrivateRoute><PurchasesList /></PrivateRoute>} />
>>>>>>> kafka
          <Route path="/notifications" element={<PrivateRoute><NotificationsPage /></PrivateRoute>} />
          <Route path="/admin-panel" element={<PrivateRoute><AdminPanel /></PrivateRoute>} />
          <Route path="/callback" element={<Callback />} />
          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      </AuthProvider>
    </Router>
  )
}

export default App

import { useAuth } from '../hooks/useAuth';
import PostView from './PostView';
import PostViewPublic from './PostViewPublic';

function PostViewWrapper() {
  const { isAuthenticated } = useAuth();

  return isAuthenticated ? <PostView /> : <PostViewPublic />;
}

export default PostViewWrapper;
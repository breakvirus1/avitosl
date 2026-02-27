import "./App.css";
import Post from "./components/Post";
import keycloak from "./keycloak";
import { ReactKeycloakProvider, useKeycloak } from "@react-keycloak/web";
import { useEffect } from "react";

function App() {
  return (
    <ReactKeycloakProvider authClient={keycloak}>
      <SecuredContent />
    </ReactKeycloakProvider>
  );
}
const SecuredContent = () => {
  const { keycloak } = useKeycloak();
  const isLoggedIn = keycloak.authenticated;

  useEffect(() => {
    if (!isLoggedIn) {
      keycloak.login();
    }
  }, [isLoggedIn, keycloak]);

  if (!isLoggedIn) return null; // Return null or a loading spinner while redirecting

  return (
    <div>
      <h2>Springboot приложение с Keycloak</h2>
      пользователь: {keycloak.tokenParsed?.preferred_username}
      <button onClick={() => keycloak.logout()}>Выйти</button>
      <Post />
    </div>
  );
};
export default App;

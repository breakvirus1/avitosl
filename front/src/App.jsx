import "./App.css";
import Post from "./components/Post";
import keycloak from "./keycloak";
import { ReactKeycloakProvider, useKeycloak } from "@react-keycloak/web";

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


  if (!isLoggedIn) return (
    <div>
      <div>привет. это страница для авторизации</div>
      <button onClick={() => keycloak.login()}>Войти</button>
    </div>
  );
  return (
    <div>
      <h2>Spring Boot приложение с Keycloak</h2>
      <div>Пользователь: {keycloak.tokenParsed?.preferred_username}</div>
      <button onClick={() => keycloak.logout()}>Выйти</button>
      <Post />
    </div>
  );
};
export default App;

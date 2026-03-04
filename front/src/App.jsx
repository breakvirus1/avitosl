
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
  const { keycloak, initialized } = useKeycloak();

  useEffect(() => {
    if (!initialized) return;

    if (!keycloak.authenticated) {
      
      keycloak.login().catch((err) => {
        console.error("Ошибка :", err);
      });
    }
  }, [initialized, keycloak?.authenticated]);

  if (!initialized) {
    return <div>Инициализация Keycloak...</div>;
  }

  if (!keycloak.authenticated) {
    return <div>Перенаправление на страницу входа...</div>;
  }

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

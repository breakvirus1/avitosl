import Keycloak from "keycloak-js";

const keycloak = new Keycloak({
  url: "http://localhost:14082/",
  realm: "avitorealm",
  clientId: "avitofrontend",
});

export default keycloak;
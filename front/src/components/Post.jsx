import { useKeycloak } from "@react-keycloak/web";
import { useEffect, useState } from "react";

const Posts = () => {
  const { keycloak } = useKeycloak();
  const [posts, setPosts] = useState([]);
  const [fetchFailed, setFetchFailed] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const getData = async () => {
      try {
        if (keycloak && keycloak.authenticated) {
          setIsLoading(true);
          console.log("Keycloak authenticated:", keycloak.authenticated);
          await keycloak?.updateToken();
          console.log("Token updated");
          const req = await fetch("http://localhost:1291/api/posts", {
            headers: {
              ["Authorization"]: `Bearer ${keycloak.token}`,
            },
          });
          if (!req.ok) {
            setFetchFailed(true);
            setErrorMessage(`Failed to fetch posts: ${req.status} ${req.statusText}`);
            console.log("Fetch failed");
            return;
          }
          const data = await req.json();
          console.log("Fetched data:", data);
          setPosts(data);
        }
      } catch (e) {
        console.log("ERROR", e);
        setFetchFailed(true);
        setErrorMessage(`Нет постов`);
      } finally {
        setIsLoading(false);
      }
    };
    getData();
  }, [keycloak?.authenticated]);

  return (
    <>
      <div style={{ marginTop: "20px" }}>
        {isLoading ? (
          <div>Loading...</div>
        ) : fetchFailed ? (
          <div>{errorMessage}</div>
        ) : posts.length === 0 ? (
          <div>No posts available</div>
        ) : (
          posts.map((post) => (
            <div key={post.name} style={{ padding: "10px", marginBottom: "20px" }}>
              <span>
                {post.name} | {post.description} | price: {post.price}
              </span>
            </div>
          ))
        )}
      </div>
      
    </>
  );
};

export default Posts;
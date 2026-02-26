import { useKeycloak } from "@react-keycloak/web";
import { useEffect, useState } from "react";

type Post = {
  name: string;
  description: string;
  price: number;
};

const Posts = () => {
  const { keycloak } = useKeycloak();
  const [posts, setPosts] = useState<Post[]>([]);

  useEffect(() => {
    const getData = async () => {
      try {
        if (keycloak && keycloak.authenticated) {
          await keycloak?.updateToken();
          const req = await fetch("http://localhost:1291/api/posts", {
            headers: {
              ["Authorization"]: `Bearer ${keycloak.token}`,
            },
          });
          setPosts(await req.json());
        }
      } catch (e) {
        console.log("ERROR", e);
      }
    };
    getData();
  }, [keycloak?.authenticated]); 

  return (
    <>
      <div style={{ marginTop: "20px" }}>
        {posts.map((post) => (
          <div key={post.name} style={{ padding: "10px", marginBottom: "20px" }}>
            <span>
              {post.name} | {post.description} | price: {post.price}
            </span>
          </div>
        ))}
      </div>
      <button
        type="button"
        className="text-blue-800"
        onClick={() => keycloak.logout()}
      >
        Logout ({keycloak?.tokenParsed?.preferred_username})
      </button>
    </>
  );
};

export default Posts;

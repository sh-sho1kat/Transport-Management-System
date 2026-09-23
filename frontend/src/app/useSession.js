import { useEffect, useState } from "react";
import { api, csrf } from "@/shared/api/client";
export function useSession() {
  const [user, setUser] = useState(null),
    [ready, setReady] = useState(false),
    [sessionExpired, setSessionExpired] = useState(false),
    [error, setError] = useState(null);
  useEffect(() => {
    let active = true;
    csrf()
      .then(() => api("/me"))
      .then((u) => {
        if (active) setUser(u);
      })
      .catch((e) => {
        if (active && e.status !== 401) setError(e);
      })
      .finally(() => {
        if (active) setReady(true);
      });
    const expired = () => {
      setSessionExpired(true);
      setUser(null);
      setError(new Error("Your session expired. Please sign in again."));
    };
    window.addEventListener("wayline-session-expired", expired);
    return () => {
      active = false;
      window.removeEventListener("wayline-session-expired", expired);
    };
  }, []);
  async function logout() {
    try {
      await api("/auth/logout", { method: "POST" });
      sessionStorage.clear();
      setUser(null);
    } catch (e) {
      setError(e);
    }
  }
  const updateUser = (value) => {
    if (value) setSessionExpired(false);
    setUser(value);
  };
  return {
    user,
    setUser: updateUser,
    ready,
    error,
    setError,
    logout,
    sessionExpired,
  };
}

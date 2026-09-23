import { useEffect, useState } from "react";
import { BusFront, UserRound, LogOut, Menu } from "lucide-react";
import { ErrorBox, Loading } from "@/shared/ui";
import { roleLabel } from "@/shared/auth/access";
import { useSession } from "./useSession";
import { navigation, homePage, FeaturePage } from "./features";
export default function App() {
  const { user, setUser, ready, error, setError, logout, sessionExpired } =
    useSession();
  const [page, setPage] = useState(
      location.pathname === "/reset-password" ? "reset" : "journeys",
    ),
    [open, setOpen] = useState(false);
  useEffect(() => {
    if (ready)
      setPage((p) =>
        sessionExpired ? "auth" : p === "reset" ? p : homePage(user),
      );
  }, [user?.id, ready, sessionExpired]);
  const login = (u) => {
    setUser(u);
    setPage(homePage(u));
    setError(null);
  };
  const nav = (p) => {
    setPage(p);
    setOpen(false);
    setError(null);
  };
  if (!ready) return <Loading />;
  const menu = navigation(user);
  return (
    <div className="app">
      <aside className={"sidebar " + (open ? "open" : "")}>
        <a
          className="brand"
          href="#"
          onClick={(e) => {
            e.preventDefault();
            nav(homePage(user));
          }}
        >
          <span className="brand-icon">
            <BusFront size={23} />
          </span>
          wayline<span className="brand-dot">.</span>
        </a>
        <div className="workspace-label">
          {user ? roleLabel(user.role) + " workspace" : "PLAN YOUR JOURNEY"}
        </div>
        <nav aria-label="Main navigation">
          {menu.map(([id, label, Icon]) => (
            <button
              key={id}
              className={page === id ? "active" : ""}
              onClick={() => nav(id)}
            >
              <Icon size={19} />
              {label}
            </button>
          ))}
        </nav>
        <div className="sidebar-bottom">
          <div className="status-dot" /> One operator. Every journey.
          <p>Bus reservations & fleet operations</p>
        </div>
      </aside>
      <div className="main">
        <header className="topbar">
          <div className="topbar-title">
            <button
              className="mobile-menu secondary"
              aria-label="Toggle navigation"
              onClick={() => setOpen(!open)}
            >
              <Menu size={20} />
            </button>
            <span>Bus operations, connected.</span>
          </div>
          <div className="user-menu">
            {user ? (
              <>
                <button className="profile-link" onClick={() => nav("profile")}>
                  <span className="avatar">
                    {user.displayName.slice(0, 1).toUpperCase()}
                  </span>
                  <span>
                    {user.displayName}
                    <small>{roleLabel(user.role)}</small>
                  </span>
                </button>
                <button
                  className="icon-button"
                  aria-label="Sign out"
                  onClick={logout}
                >
                  <LogOut size={19} />
                </button>
              </>
            ) : (
              <button className="secondary" onClick={() => nav("auth")}>
                <UserRound size={16} /> Sign in
              </button>
            )}
          </div>
        </header>
        <main>
          <ErrorBox error={error} />
          <FeaturePage
            page={page}
            user={user}
            nav={nav}
            login={login}
            setUser={setUser}
          />
        </main>
        <footer>
          WAYLINE{" "}
          <span>Reservations made simple. Operations kept in sync.</span>
        </footer>
      </div>
    </div>
  );
}

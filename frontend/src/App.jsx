import { useEffect, useState } from "react";
import {
  BusFront,
  Search,
  Ticket,
  CalendarDays,
  MapPin,
  Route,
  Users,
  BarChart3,
  History,
  UserRound,
  LogOut,
  Menu,
} from "lucide-react";
import { api, csrf } from "./api";
import Auth from "./pages/Auth";
import Journeys from "./pages/Journeys";
import Bookings from "./pages/Bookings";
import Fleet from "./pages/Fleet";
import Operations from "./pages/Operations";
import Reports from "./pages/Reports";
import Profile from "./pages/Profile";
import { ErrorBox, Loading } from "./components/UI";
const menus = {
  PASSENGER: [
    ["journeys", "Find a journey", Search],
    ["bookings", "My reservations", Ticket],
  ],
  ADMIN: [
    ["operations", "Trip operations", CalendarDays],
    ["buses", "Fleet & seats", BusFront],
    ["routes", "Routes", Route],
    ["stops", "Stops & terminals", MapPin],
    ["staff", "Team & roles", Users],
    ["reports", "Reports", BarChart3],
    ["audit", "Activity log", History],
  ],
  DRIVER: [["operations", "Assigned trips", CalendarDays]],
};
export default function App() {
  const [user, setUser] = useState(null),
    [ready, setReady] = useState(false),
    [page, setPage] = useState(
      location.pathname === "/reset-password" ? "reset" : "journeys",
    ),
    [error, setError] = useState(null),
    [open, setOpen] = useState(false);
  useEffect(() => {
    csrf()
      .then(() => api("/me"))
      .then((u) => {
        setUser(u);
        if (page !== "reset")
          setPage(u.role === "PASSENGER" ? "journeys" : "operations");
      })
      .catch((e) => {
        if (e.status !== 401) setError(e);
      })
      .finally(() => setReady(true));
  }, []);
  useEffect(() => {
    const expired = () => {
      setUser(null);
      setPage("auth");
      setError(new Error("Your session expired. Please sign in again."));
    };
    window.addEventListener("wayline-session-expired", expired);
    return () => window.removeEventListener("wayline-session-expired", expired);
  }, []);
  const login = (u) => {
    setUser(u);
    setPage(u.role === "PASSENGER" ? "journeys" : "operations");
    setError(null);
  };
  const nav = (p) => {
    setPage(p);
    setOpen(false);
    setError(null);
  };
  async function logout() {
    try {
      await api("/auth/logout", { method: "POST" });
      sessionStorage.clear();
      setUser(null);
      setPage("journeys");
    } catch (e) {
      setError(e);
    }
  }
  if (!ready) return <Loading />;
  const menu = user
    ? menus[user.role]
    : [["journeys", "Find a journey", Search]];
  return (
    <div className="app">
      <aside className={"sidebar " + (open ? "open" : "")}>
        <a
          className="brand"
          href="#"
          onClick={(e) => {
            e.preventDefault();
            nav(user && user.role !== "PASSENGER" ? "operations" : "journeys");
          }}
        >
          <span className="brand-icon">
            <BusFront size={23} />
          </span>
          wayline<span className="brand-dot">.</span>
        </a>
        <div className="workspace-label">
          {user ? user.role.toLowerCase() + " workspace" : "PLAN YOUR JOURNEY"}
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
                    <small>{user.role.toLowerCase()}</small>
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
          {page === "auth" || page === "reset" ? (
            <Auth
              onLogin={login}
              initial={page === "reset" ? "reset" : "login"}
            />
          ) : page === "journeys" ? (
            <Journeys
              user={user}
              onSignIn={() => nav("auth")}
              onBooked={() => nav("bookings")}
            />
          ) : user && page === "profile" ? (
            <Profile user={user} onUpdate={setUser} />
          ) : user?.role === "PASSENGER" && page === "bookings" ? (
            <Bookings />
          ) : user && page === "operations" ? (
            <Operations driver={user.role === "DRIVER"} />
          ) : user?.role === "ADMIN" &&
            ["buses", "routes", "stops", "staff"].includes(page) ? (
            <Fleet key={page} kind={page} />
          ) : user?.role === "ADMIN" && ["reports", "audit"].includes(page) ? (
            <Reports key={page} audit={page === "audit"} />
          ) : null}
        </main>
        <footer>
          WAYLINE{" "}
          <span>Reservations made simple. Operations kept in sync.</span>
        </footer>
      </div>
    </div>
  );
}

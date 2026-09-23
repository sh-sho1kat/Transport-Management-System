import { useState } from "react";
import { api } from "@/shared/api/client";
import { Field, ErrorBox } from "@/shared/ui/index.jsx";
export default function Auth({ onLogin, initial = "login" }) {
  const [mode, setMode] = useState(initial),
    [error, setError] = useState(null),
    [message, setMessage] = useState(""),
    [busy, setBusy] = useState(false);
  async function submit(e) {
    e.preventDefault();
    setError(null);
    setMessage("");
    setBusy(true);
    const values = Object.fromEntries(new FormData(e.currentTarget));
    try {
      if (mode === "login")
        onLogin(await api("/auth/login", { method: "POST", body: values }));
      if (mode === "register") {
        await api("/auth/register", { method: "POST", body: values });
        setMode("login");
        setMessage("Account created. Sign in to reserve your seats.");
      }
      if (mode === "recovery") {
        const r = await api("/auth/password-reset-requests", {
          method: "POST",
          body: values,
        });
        setMessage(r.message);
      }
      if (mode === "reset") {
        const r = await api("/auth/password-resets", {
          method: "POST",
          body: {
            ...values,
            token: new URLSearchParams(location.search).get("token"),
          },
        });
        history.replaceState({}, "", "/");
        setMode("login");
        setMessage(r.message);
      }
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }
  const change = (m) => {
    setMode(m);
    setError(null);
    setMessage("");
  };
  return (
    <div className="auth-layout">
      <div className="auth-story">
        <span className="eyebrow">EVERY JOURNEY, WELL MANAGED</span>
        <h1>
          Your next stop.
          <br />A simpler journey.
        </h1>
        <p>
          Find a trip, choose your seat, and keep every reservation in one
          place.
        </p>
        <div className="route-art">
          <i />
          <span>Choose your route</span>
          <i />
          <span>Reserve your seat</span>
          <i />
          <span>Enjoy the journey</span>
        </div>
      </div>
      <section className="card auth-card">
        <span className="eyebrow">WELCOME TO WAYLINE</span>
        <h2>
          {
            {
              login: "Welcome back",
              register: "Create your account",
              recovery: "Recover your account",
              reset: "Set a new password",
            }[mode]
          }
        </h2>
        <p className="muted">
          {mode === "login"
            ? "Sign in to continue your journey."
            : "Your account keeps your journeys together."}
        </p>
        <ErrorBox error={error} />
        {message && (
          <div className="notice success" role="status">
            {message}
          </div>
        )}
        <form onSubmit={submit} key={mode}>
          {mode === "register" && (
            <>
              <Field
                label="Full name"
                name="displayName"
                required
                maxLength={100}
                autoComplete="name"
              />
              <Field
                label="Phone number"
                name="phone"
                required
                maxLength={30}
                autoComplete="tel"
              />
            </>
          )}
          {mode !== "reset" && (
            <Field
              label="Email address"
              name="email"
              type="email"
              required
              maxLength={254}
              autoComplete="email"
            />
          )}
          {mode !== "recovery" && (
            <Field
              label="Password"
              name="password"
              type="password"
              required
              minLength={mode === "login" ? 1 : 10}
              maxLength={72}
              autoComplete={
                mode === "login" ? "current-password" : "new-password"
              }
            />
          )}
          <button className="wide" disabled={busy}>
            {busy
              ? "Please wait…"
              : {
                  login: "Sign in",
                  register: "Create account",
                  recovery: "Send recovery link",
                  reset: "Update password",
                }[mode]}
          </button>
        </form>
        <div className="auth-links">
          {mode === "login" ? (
            <>
              <button
                className="text-button"
                onClick={() => change("register")}
              >
                Create an account
              </button>
              <button
                className="text-button"
                onClick={() => change("recovery")}
              >
                Forgot password?
              </button>
            </>
          ) : (
            <button className="text-button" onClick={() => change("login")}>
              Back to sign in
            </button>
          )}
        </div>
      </section>
    </div>
  );
}

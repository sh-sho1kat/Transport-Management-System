import { useState } from "react";
import { api } from "@/shared/api/client";
import { ErrorBox } from "@/shared/ui/index.jsx";
export default function DemoAccess({ onLogin }) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState(null);
  if (import.meta.env.VITE_PUBLIC_DEMO !== "true") return null;
  async function signIn(email, password) {
    setBusy(true);
    setError(null);
    try {
      onLogin(
        await api("/auth/login", { method: "POST", body: { email, password } }),
      );
    } catch (failure) {
      setError(failure);
    } finally {
      setBusy(false);
    }
  }
  return (
    <section className="card demo-access" aria-label="Public demo access">
      <span className="eyebrow">EXPLORE THE LIVE DEMO</span>
      <h2>Choose your demo account</h2>
      <p>
        Fictional journeys. No real payments. Passenger bookings are shared;
        admin access is read-only.
      </p>
      <div className="demo-account-grid">
        <div>
          <h3>Passenger</h3>
          <p>
            <code>passenger.demo@example.test</code>
            <br />
            Password: <code>DemoPass123!</code>
          </p>
          <button
            disabled={busy}
            onClick={() =>
              signIn("passenger.demo@example.test", "DemoPass123!")
            }
          >
            Try passenger demo
          </button>
        </div>
        <div>
          <h3>Administrator preview</h3>
          <p>
            <code>admin.demo@example.test</code>
            <br />
            Password: <code>DemoAdmin123!</code>
          </p>
          <button
            disabled={busy}
            onClick={() => signIn("admin.demo@example.test", "DemoAdmin123!")}
          >
            Try admin demo
          </button>
        </div>
      </div>
      <ErrorBox error={error} />
    </section>
  );
}

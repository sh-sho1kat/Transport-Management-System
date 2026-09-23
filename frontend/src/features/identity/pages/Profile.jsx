import { useState } from "react";
import { api } from "@/shared/api/client";
import { Field, ErrorBox, Badge } from "@/shared/ui/index.jsx";
export default function Profile({ user, onUpdate }) {
  const [error, setError] = useState(null),
    [message, setMessage] = useState(""),
    [busy, setBusy] = useState(false);
  return (
    <>
      <div className="page-intro">
        <span className="eyebrow">YOUR ACCOUNT</span>
        <h1>Profile & contact details</h1>
      </div>
      <section className="card narrow">
        <Badge>{user.role}</Badge>
        <p>{user.email}</p>
        <ErrorBox error={error} />
        {message && (
          <div className="notice success" role="status">
            {message}
          </div>
        )}
        <form
          onSubmit={async (e) => {
            e.preventDefault();
            setBusy(true);
            setError(null);
            setMessage("");
            try {
              onUpdate(
                await api("/me", {
                  method: "PATCH",
                  body: Object.fromEntries(new FormData(e.currentTarget)),
                }),
              );
              setMessage(
                "Profile updated. Existing ticket contact details remain unchanged.",
              );
            } catch (e) {
              setError(e);
            } finally {
              setBusy(false);
            }
          }}
        >
          <Field
            label="Full name"
            name="displayName"
            required
            maxLength={100}
            defaultValue={user.displayName}
          />
          <Field
            label="Phone number"
            name="phone"
            required
            maxLength={30}
            defaultValue={user.phone}
          />
          <button disabled={busy}>{busy ? "Saving…" : "Save changes"}</button>
        </form>
        <p className="muted small">
          To change your password, sign out and use “Forgot password?” on the
          sign-in page.
        </p>
      </section>
    </>
  );
}

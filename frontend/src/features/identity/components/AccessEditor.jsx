import { useState } from "react";

import { api } from "@/shared/api/client";
import { Field, ErrorBox, Modal } from "@/shared/ui/index.jsx";
export default function AccessEditor({ value, onClose, onSaved }) {
  const [error, setError] = useState(null),
    [busy, setBusy] = useState(false);
  return (
    <Modal
      title={
        value.active ? "Deactivate staff access" : "Reactivate staff access"
      }
      onClose={onClose}
    >
      <p>
        {value.displayName} · {value.email}
      </p>
      <ErrorBox error={error} />
      <form
        onSubmit={async (e) => {
          e.preventDefault();
          const reason = new FormData(e.currentTarget).get("reason");
          setBusy(true);
          try {
            await api("/admin/staff/" + value.id + "/active", {
              method: "PATCH",
              body: { active: !value.active, reason },
            });
            onSaved();
          } catch (e) {
            setError(e);
          } finally {
            setBusy(false);
          }
        }}
      >
        <Field label="Reason" name="reason" required maxLength={255} />
        <button disabled={busy}>
          {busy ? "Saving…" : "Confirm access change"}
        </button>
      </form>
    </Modal>
  );
}

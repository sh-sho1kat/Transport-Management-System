import { useState } from "react";
import { api } from "@/shared/api/client";

import { Field, ErrorBox, Modal } from "@/shared/ui/index.jsx";

export default function FareEditor({ trip, onClose, onSaved }) {
  const [error, setError] = useState(null),
    [busy, setBusy] = useState(false);
  return (
    <Modal title="Update trip fare" onClose={onClose}>
      <p>
        Existing bookings and active holds keep their quoted price. The new fare
        applies to new sales.
      </p>
      <ErrorBox error={error} />
      <form
        onSubmit={async (e) => {
          e.preventDefault();
          const data = Object.fromEntries(new FormData(e.currentTarget));
          setBusy(true);
          try {
            await api(`/admin/trips/${trip.id}/fare`, {
              method: "PATCH",
              body: { fareMinor: Number(data.fareMinor), reason: data.reason },
            });
            onSaved();
            onClose();
          } catch (err) {
            setError(err);
          } finally {
            setBusy(false);
          }
        }}
      >
        <Field
          label={`Fare per seat in minor units (${trip.currency}; BDT 12500 = 125.00)`}
          name="fareMinor"
          type="number"
          min={1}
          max={100000000}
          required
          defaultValue={trip.fareMinor}
        />
        <Field label="Reason" name="reason" required maxLength={180} />
        <button disabled={busy}>{busy ? "Saving…" : "Save fare"}</button>
      </form>
    </Modal>
  );
}

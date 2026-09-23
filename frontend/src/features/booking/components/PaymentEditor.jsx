import { useState } from "react";
import { api } from "@/shared/api/client";
import { money } from "@/shared/lib/format";
import { Field, ErrorBox, Modal } from "@/shared/ui/index.jsx";

export default function PaymentEditor({ booking, admin, onClose, onSaved }) {
  const [error, setError] = useState(null),
    [busy, setBusy] = useState(false);
  const status =
    booking.paymentStatus === "REFUND_DUE"
      ? "REFUNDED"
      : booking.paymentStatus === "PAID" && admin
        ? "UNPAID"
        : "PAID";
  return (
    <Modal title="Update payment" onClose={onClose}>
      <p>
        {booking.contactName} · {booking.reference}
      </p>
      <p>
        {money(booking.amountMinor, booking.currency)} · {booking.paymentStatus}{" "}
        → {status}
      </p>
      <p>
        {status === "PAID"
          ? "Confirm only after receiving the full cash amount."
          : status === "REFUNDED"
            ? "Confirm only after returning the cash to the customer."
            : "Administrator correction: explain why the payment was recorded incorrectly."}
      </p>
      <ErrorBox error={error} />
      <form
        onSubmit={async (e) => {
          e.preventDefault();
          const reason = new FormData(e.currentTarget).get("reason");
          setBusy(true);
          try {
            await api(`/bookings/${booking.id}/payment`, {
              method: "PATCH",
              body: { status, expectedStatus: booking.paymentStatus, reason },
            });
            await onSaved();
            onClose();
          } catch (err) {
            setError(err);
          } finally {
            setBusy(false);
          }
        }}
      >
        <Field
          label="Reason / collection note"
          name="reason"
          required
          maxLength={180}
        />
        <button disabled={busy}>
          {busy ? "Saving…" : "Confirm payment update"}
        </button>
      </form>
    </Modal>
  );
}

import { useEffect, useState } from "react";
import { api, money, dateTime } from "../api";
import { Field, ErrorBox, Modal, Loading } from "../components/UI";

export function TicketView({ booking: b, onClose }) {
  return (
    <Modal title="Ticket / payment record" onClose={onClose}>
      <div className="print-ticket">
        <h2>Wayline</h2>
        <h3>
          {b.origin} → {b.destination}
        </h3>
        <p>{b.reference}</p>
        <p>{dateTime(b.departureAt)}</p>
        <p>
          {b.contactName} · {b.contactPhone}
        </p>
        <p>Seats: {b.seatNos.join(", ")}</p>
        <p>Total: {money(b.amountMinor, b.currency)}</p>
        <p>
          {b.status} · {b.paymentStatus} ·{" "}
          {b.paymentMethod.replaceAll("_", " ")}
        </p>
        <p>
          {b.paymentStatus === "PAID"
            ? "Cash payment recorded."
            : "Check the payment status above; this ticket alone does not confirm payment."}
        </p>
      </div>
      <button onClick={() => window.print()}>Print / save PDF</button>
    </Modal>
  );
}

export function CounterSale({ trip, onClose, onSaved }) {
  const [seats, setSeats] = useState(null),
    [selected, setSelected] = useState([]),
    [error, setError] = useState(null),
    [busy, setBusy] = useState(false),
    [ticket, setTicket] = useState(null),
    [pending, setPending] = useState(null);
  const refresh = () =>
    api(`/trips/${trip.id}/seats`).then(setSeats).catch(setError);
  useEffect(() => {
    refresh();
  }, [trip.id]);
  async function submit(e) {
    e.preventDefault();
    setError(null);
    setBusy(true);
    const body = pending?.body || {
      ...Object.fromEntries(new FormData(e.currentTarget)),
      tripId: trip.id,
      seatNos: selected,
      expectedAmountMinor: trip.fareMinor * selected.length,
    };
    const attempt = pending || { body, key: crypto.randomUUID() };
    setPending(attempt);
    try {
      const result = await api("/counter/bookings", {
        method: "POST",
        ...attempt,
      });
      setTicket(result);
      setPending(null);
      onSaved();
    } catch (err) {
      setError(err);
      if (err.status && err.status < 500) {
        setPending(null);
        refresh();
      }
    } finally {
      setBusy(false);
    }
  }
  if (ticket) return <TicketView booking={ticket} onClose={onClose} />;
  return (
    <Modal title="Sell a walk-in ticket" onClose={busy ? () => {} : onClose}>
      <p>
        {trip.origin} → {trip.destination} · {dateTime(trip.departureAt)}
      </p>
      <ErrorBox error={error} />
      {pending && !busy && (
        <p>
          Result not confirmed. Retry the same sale before starting another.
          Check the manifest if you close this window.
        </p>
      )}
      {!seats ? (
        <Loading />
      ) : (
        <form onSubmit={submit}>
          <fieldset disabled={busy || !!pending}>
            <p>
              Select up to four seats. Held and booked seats cannot be sold.
            </p>
            <div className="layout-editor">
              {seats.map((s) => (
                <button
                  type="button"
                  key={s.label}
                  disabled={
                    s.status !== "AVAILABLE" ||
                    (!selected.includes(s.label) && selected.length >= 4)
                  }
                  aria-pressed={selected.includes(s.label)}
                  className={
                    "seat " +
                    (selected.includes(s.label)
                      ? "selected"
                      : s.status.toLowerCase())
                  }
                  onClick={() =>
                    setSelected(
                      selected.includes(s.label)
                        ? selected.filter((v) => v !== s.label)
                        : [...selected, s.label],
                    )
                  }
                >
                  {s.label}
                </button>
              ))}
            </div>
            <Field
              label="Passenger name"
              name="contactName"
              required
              maxLength={100}
            />
            <Field label="Phone" name="contactPhone" required maxLength={30} />
            <Field
              label="Email (optional)"
              name="contactEmail"
              type="email"
              maxLength={254}
            />
            <Field label="Payment">
              <select name="paymentStatus">
                <option value="UNPAID">Unpaid — collect on boarding</option>
                <option value="PAID">Paid — cash received at counter</option>
              </select>
            </Field>
          </fieldset>
          <p>
            Total:{" "}
            <strong>
              {money(trip.fareMinor * selected.length, trip.currency)}
            </strong>
          </p>
          <p>
            No passenger account is required. Choose paid only after receiving
            the full amount.
          </p>
          <button disabled={busy || !selected.length}>
            {busy ? "Saving…" : pending ? "Retry same sale" : "Issue ticket"}
          </button>
        </form>
      )}
    </Modal>
  );
}

export function PaymentEditor({ booking, admin, onClose, onSaved }) {
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

export function FareEditor({ trip, onClose, onSaved }) {
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

import TicketView from "./TicketView";
import { useEffect, useState } from "react";
import { api } from "@/shared/api/client";
import { money, dateTime } from "@/shared/lib/format";
import { Field, ErrorBox, Modal, Loading } from "@/shared/ui/index.jsx";

export default function CounterSale({ trip, onClose, onSaved }) {
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

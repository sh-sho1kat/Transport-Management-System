import { useEffect, useState } from "react";
import { Ticket, Printer, ArrowRight } from "lucide-react";
import { api } from "@/shared/api/client";
import { money, dateTime } from "@/shared/lib/format";
import {
  ErrorBox,
  Badge,
  Empty,
  Loading,
  Pager,
  Modal,
  ReasonDialog,
} from "@/shared/ui/index.jsx";
export default function Bookings() {
  const [page, setPage] = useState(null),
    [number, setNumber] = useState(0),
    [error, setError] = useState(null),
    [ticket, setTicket] = useState(null),
    [cancel, setCancel] = useState(null);
  const refresh = () =>
    api("/bookings?page=" + number)
      .then(setPage)
      .catch(setError);
  useEffect(() => {
    refresh();
  }, [number]);
  return (
    <>
      <div className="page-intro">
        <span className="eyebrow">YOUR TRAVEL LOG</span>
        <h1>My reservations</h1>
        <p>Tickets, travel details, and changes — all in one place.</p>
      </div>
      <ErrorBox error={error} />
      {!page ? (
        <Loading />
      ) : page.items.length === 0 ? (
        <Empty>
          Your first journey is waiting. Find a trip to make a reservation.
        </Empty>
      ) : (
        <div className="trip-list">
          {page.items.map((b) => (
            <article key={b.id} className="card trip-card">
              <div>
                <Badge>{b.status}</Badge>
                <h3>
                  {b.origin} <ArrowRight size={18} /> {b.destination}
                </h3>
                <p>
                  {dateTime(b.departureAt)} · Seats {b.seatNos.join(", ")}
                </p>
                <span className="muted small">{b.reference}</span>
              </div>
              <div className="trip-price">
                <strong>{money(b.amountMinor, b.currency)}</strong>
                <Badge>{b.paymentStatus}</Badge>
                <div className="actions">
                  <button className="secondary" onClick={() => setTicket(b)}>
                    <Ticket size={16} /> Ticket
                  </button>
                  {b.status === "CONFIRMED" && (
                    <button
                      className="text-button danger-text"
                      onClick={() => setCancel(b)}
                    >
                      Cancel
                    </button>
                  )}
                </div>
              </div>
            </article>
          ))}
        </div>
      )}
      <Pager page={page} onChange={setNumber} />
      {ticket && (
        <Modal title="Journey ticket" onClose={() => setTicket(null)}>
          <div className="ticket-print">
            <span className="eyebrow">WAYLINE · TRAVEL CONFIRMATION</span>
            <h2>
              {ticket.origin} → {ticket.destination}
            </h2>
            <Badge>{ticket.status}</Badge>
            <dl>
              <dt>Reference</dt>
              <dd>{ticket.reference}</dd>
              <dt>Departure</dt>
              <dd>{dateTime(ticket.departureAt)}</dd>
              <dt>Passenger</dt>
              <dd>{ticket.contactName}</dd>
              <dt>Seats</dt>
              <dd>{ticket.seatNos.join(", ")}</dd>
              <dt>Total</dt>
              <dd>{money(ticket.amountMinor, ticket.currency)}</dd>
              <dt>Payment</dt>
              <dd>
                {ticket.paymentMethod.replaceAll("_", " ")} ·{" "}
                {ticket.paymentStatus}
              </dd>
              <dt>Cancellation cutoff</dt>
              <dd>
                {dateTime(
                  new Date(ticket.departureAt).getTime() -
                    ticket.cancellationHours * 3600000,
                )}
              </dd>
            </dl>
            <p className="muted">
              {ticket.paymentStatus === "PAID"
                ? "Cash payment recorded by staff."
                : "Payment status is shown above. An unpaid reservation is not proof of payment."}
            </p>
          </div>
          <button onClick={() => window.print()}>
            <Printer size={16} /> Print / save as PDF
          </button>
        </Modal>
      )}
      {cancel && (
        <ReasonDialog
          title="Cancel reservation"
          onClose={() => setCancel(null)}
          onConfirm={async (reason) => {
            await api("/bookings/" + cancel.id + "/cancel", {
              method: "POST",
              body: { reason },
            });
            refresh();
          }}
        />
      )}
    </>
  );
}

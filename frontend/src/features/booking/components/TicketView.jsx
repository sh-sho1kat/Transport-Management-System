import { money, dateTime } from "@/shared/lib/format";
import { Modal } from "@/shared/ui/index.jsx";

export default function TicketView({ booking: b, onClose }) {
  return (
    <Modal title="Ticket / payment record" onClose={onClose}>
      <div className="ticket-print">
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

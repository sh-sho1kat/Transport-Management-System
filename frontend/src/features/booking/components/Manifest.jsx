import { can } from "@/shared/auth/access";
import PaymentEditor from "./PaymentEditor";
import TicketView from "./TicketView";
import { useState } from "react";
import { Download } from "lucide-react";
import { api } from "@/shared/api/client";
import { money, dateTime } from "@/shared/lib/format";
import { csvDownload } from "@/shared/lib/csv";
import { Empty, Modal, Action, ReasonDialog } from "@/shared/ui/index.jsx";
export default function Manifest({ value, user, onClose, onRefresh }) {
  const admin = can(user, "BOOKING_CANCEL_OVERRIDE"),
    counter = can(user, "BOOKING_READ_ALL"),
    collect =
      can(user, "PAYMENT_COLLECT_ALL") ||
      (can(user, "PAYMENT_COLLECT_ASSIGNED") &&
        ["PUBLISHED", "DEPARTED"].includes(value.trip.status));
  const [cancel, setCancel] = useState(null),
    [payment, setPayment] = useState(null),
    [ticket, setTicket] = useState(null);
  return (
    <Modal title="Passenger manifest" onClose={onClose}>
      <p>
        {value.trip.origin} → {value.trip.destination} ·{" "}
        {dateTime(value.trip.departureAt)}
      </p>
      <button
        className="secondary"
        onClick={() =>
          csvDownload("manifest.csv", [
            ["Reference", "Name", "Seats", "Phone", "Email", "Payment"],
            ...value.bookings.map((b) => [
              b.reference,
              b.contactName,
              b.seatNos.join(" "),
              b.contactPhone,
              b.contactEmail,
              b.paymentStatus,
            ]),
          ])
        }
      >
        <Download size={16} /> Export CSV
      </button>
      {!value.bookings.length ? (
        <Empty>No reservations on this trip.</Empty>
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Passenger</th>
                <th>Seats</th>
                <th>Contact</th>
                <th>Payment</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {value.bookings.map((b) => (
                <tr key={b.id}>
                  <td>
                    {b.contactName}
                    <small>{b.reference}</small>
                  </td>
                  <td>{b.seatNos.join(", ")}</td>
                  <td>
                    {b.contactPhone}
                    <small>{b.contactEmail}</small>
                  </td>
                  <td>
                    {money(b.amountMinor, b.currency)}
                    <small>
                      {b.paymentStatus} · {b.status}
                    </small>
                  </td>
                  <td>
                    {(admin || counter) && (
                      <button
                        className="secondary"
                        onClick={() => setTicket(b)}
                      >
                        Ticket
                      </button>
                    )}
                    {((b.status === "CONFIRMED" &&
                      b.paymentStatus === "UNPAID" &&
                      collect) ||
                      (can(user, "PAYMENT_CORRECT") &&
                        b.status === "CONFIRMED" &&
                        b.paymentStatus === "PAID") ||
                      (can(user, "PAYMENT_REFUND") &&
                        b.paymentStatus === "REFUND_DUE")) && (
                      <button
                        className="secondary"
                        onClick={() => setPayment(b)}
                      >
                        Update payment
                      </button>
                    )}
                    {(admin || counter) && b.status === "CONFIRMED" && (
                      <button
                        className="text-button danger-text"
                        onClick={() => setCancel(b)}
                      >
                        Cancel booking
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      {payment && (
        <PaymentEditor
          booking={payment}
          admin={can(user, "PAYMENT_CORRECT")}
          onClose={() => setPayment(null)}
          onSaved={onRefresh}
        />
      )}
      {ticket && (
        <TicketView booking={ticket} onClose={() => setTicket(null)} />
      )}
      {cancel && (
        <ReasonDialog
          title={
            admin
              ? "Administrator cancellation override"
              : "Cancel reservation within policy"
          }
          onClose={() => setCancel(null)}
          onConfirm={async (reason) => {
            await api("/bookings/" + cancel.id + "/cancel", {
              method: "POST",
              body: { reason },
            });
            await onRefresh();
          }}
        />
      )}
    </Modal>
  );
}

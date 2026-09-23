import { useEffect, useState } from "react";
import { Plus, Users, Download } from "lucide-react";
import { api, money, dateTime, localDateTime, csvDownload } from "../api";
import {
  Field,
  ErrorBox,
  Badge,
  Empty,
  Loading,
  Pager,
  Modal,
  Action,
  ReasonDialog,
} from "../components/UI";
import { CounterSale, PaymentEditor, FareEditor, TicketView } from "./Counter";
export default function Operations({ driver = false, counter = false }) {
  const [page, setPage] = useState(null),
    [number, setNumber] = useState(0),
    [error, setError] = useState(null),
    [editing, setEditing] = useState(null),
    [manifest, setManifest] = useState(null),
    [cancel, setCancel] = useState(null),
    [sale, setSale] = useState(null),
    [fare, setFare] = useState(null);
  const prefix = driver ? "/driver" : counter ? "/counter" : "/admin";
  const refresh = () =>
    api(prefix + "/trips" + (driver ? "" : "?page=" + number))
      .then((p) => setPage(driver ? { items: p } : p))
      .catch(setError);
  useEffect(() => {
    refresh();
  }, [number, driver, counter]);
  async function act(path, body) {
    try {
      setError(null);
      await api(path, {
        method: path.endsWith("status") ? "PATCH" : "POST",
        body,
      });
      refresh();
    } catch (e) {
      setError(e);
    }
  }
  return (
    <>
      <div className="section-head page-intro">
        <div>
          <span className="eyebrow">
            {driver ? "DRIVER WORKSPACE" : "DISPATCH & SCHEDULING"}
          </span>
          <h1>
            {driver
              ? "My assigned trips"
              : counter
                ? "Ticket counter"
                : "Trip operations"}
          </h1>
          <p>
            {driver
              ? "Your timetable and passenger manifests."
              : "Plan departures, assign your team, and manage reservations."}
          </p>
        </div>
        {!driver && !counter && (
          <button onClick={() => setEditing({})}>
            <Plus size={17} /> Schedule trip
          </button>
        )}
      </div>
      <ErrorBox error={error} />
      {!page ? (
        <Loading />
      ) : !page.items.length ? (
        <Empty>No trips scheduled yet.</Empty>
      ) : (
        <div className="trip-list">
          {page.items.map((t) => (
            <article className="card operation-card" key={t.id}>
              <div className="section-head">
                <div>
                  <Badge>{t.status}</Badge>
                  <h3>
                    {t.origin} → {t.destination}
                  </h3>
                  <p>
                    {dateTime(t.departureAt)} → {dateTime(t.arrivalAt)}
                  </p>
                  <p className="muted">
                    {t.busRegistration} · {t.driverName} ·{" "}
                    {money(t.fareMinor, t.currency)} per seat
                  </p>
                </div>
                <div className="actions">
                  {!driver &&
                    t.status === "PUBLISHED" &&
                    new Date(t.departureAt) > new Date() && (
                      <button onClick={() => setSale(t)}>
                        Sell walk-in ticket
                      </button>
                    )}
                  {!driver &&
                    !counter &&
                    ["DRAFT", "PUBLISHED"].includes(t.status) &&
                    new Date(t.departureAt) > new Date() && (
                      <button className="secondary" onClick={() => setFare(t)}>
                        Update fare
                      </button>
                    )}
                  {t.status === "DRAFT" && !driver && !counter && (
                    <>
                      <button
                        className="secondary"
                        onClick={() => setEditing(t)}
                      >
                        Edit draft
                      </button>
                      <Action
                        onClick={() => act("/admin/trips/" + t.id + "/publish")}
                      >
                        Publish
                      </Action>
                    </>
                  )}
                  {t.status !== "DRAFT" && (
                    <Action
                      className="secondary"
                      onClick={async () => {
                        try {
                          setManifest({
                            trip: t,
                            bookings: await api(
                              prefix + "/trips/" + t.id + "/manifest",
                            ),
                          });
                        } catch (e) {
                          setError(e);
                        }
                      }}
                    >
                      <Users size={16} /> Manifest
                    </Action>
                  )}
                  {t.status === "PUBLISHED" && !counter && (
                    <Action
                      className="secondary"
                      onClick={() =>
                        act(prefix + "/trips/" + t.id + "/status", {
                          status: "DEPARTED",
                        })
                      }
                    >
                      Mark departed
                    </Action>
                  )}
                  {t.status === "DEPARTED" && !counter && (
                    <Action
                      onClick={() =>
                        act(prefix + "/trips/" + t.id + "/status", {
                          status: "COMPLETED",
                        })
                      }
                    >
                      Complete trip
                    </Action>
                  )}
                  {!driver &&
                    !counter &&
                    ["DRAFT", "PUBLISHED"].includes(t.status) && (
                      <button
                        className="text-button danger-text"
                        onClick={() => setCancel(t)}
                      >
                        Cancel trip
                      </button>
                    )}
                </div>
              </div>
            </article>
          ))}
        </div>
      )}
      <Pager page={page} onChange={setNumber} />
      {sale && (
        <CounterSale
          trip={sale}
          onClose={() => setSale(null)}
          onSaved={refresh}
        />
      )}
      {fare && (
        <FareEditor
          trip={fare}
          onClose={() => setFare(null)}
          onSaved={refresh}
        />
      )}
      {editing && (
        <TripEditor
          value={editing}
          onClose={() => setEditing(null)}
          onSaved={() => {
            setEditing(null);
            refresh();
          }}
        />
      )}
      {cancel && (
        <ReasonDialog
          title="Cancel trip and all reservations"
          onClose={() => setCancel(null)}
          onConfirm={async (reason) => {
            await api("/admin/trips/" + cancel.id + "/cancel", {
              method: "POST",
              body: { reason },
            });
            refresh();
          }}
        />
      )}
      {manifest && (
        <Manifest
          value={manifest}
          admin={!driver && !counter}
          counter={counter}
          onClose={() => setManifest(null)}
          onRefresh={async () => {
            setManifest({
              ...manifest,
              bookings: await api(
                prefix + "/trips/" + manifest.trip.id + "/manifest",
              ),
            });
            refresh();
          }}
        />
      )}
    </>
  );
}
function TripEditor({ value, onClose, onSaved }) {
  const [catalog, setCatalog] = useState(null),
    [error, setError] = useState(null),
    [busy, setBusy] = useState(false);
  useEffect(() => {
    Promise.all([
      api("/admin/routes"),
      api("/admin/buses"),
      api("/admin/staff"),
    ])
      .then(([routes, buses, staff]) =>
        setCatalog({
          routes,
          buses,
          drivers: staff.filter((s) => s.role === "DRIVER"),
        }),
      )
      .catch(setError);
  }, []);
  async function submit(e) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    const r = Object.fromEntries(new FormData(e.currentTarget));
    for (const k of ["departureAt", "arrivalAt", "salesCloseAt"])
      r[k] = new Date(r[k]).toISOString();
    r.fareMinor = Number(r.fareMinor);
    try {
      await api("/admin/trips" + (value.id ? "/" + value.id : ""), {
        method: value.id ? "PATCH" : "POST",
        body: r,
      });
      onSaved();
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }
  return (
    <Modal
      title={value.id ? "Edit draft trip" : "Schedule a trip"}
      onClose={onClose}
    >
      <ErrorBox error={error} />
      {!catalog ? (
        <Loading />
      ) : (
        <form onSubmit={submit}>
          {[
            ["routeId", "Route", "routes", "name"],
            ["busId", "Bus", "buses", "registration"],
            ["driverId", "Driver", "drivers", "displayName"],
          ].map(([name, label, key, title]) => (
            <Field label={label} key={name}>
              <select name={name} required defaultValue={value[name] || ""}>
                <option value="">Choose {label.toLowerCase()}</option>
                {catalog[key]
                  .filter((v) => v.active !== false)
                  .map((v) => (
                    <option key={v.id} value={v.id}>
                      {v[title]}
                    </option>
                  ))}
              </select>
            </Field>
          ))}
          <p className="muted small">
            Enter times in your device timezone:{" "}
            {Intl.DateTimeFormat().resolvedOptions().timeZone}.
          </p>
          {[
            ["departureAt", "Departure"],
            ["arrivalAt", "Arrival"],
            ["salesCloseAt", "Sales close"],
          ].map(([name, label]) => (
            <Field
              label={label}
              name={name}
              key={name}
              type="datetime-local"
              required
              defaultValue={value[name] ? localDateTime(value[name]) : ""}
            />
          ))}
          <div className="form-row">
            <Field
              label="Fare in minor units (e.g. 12500 = BDT 125.00)"
              name="fareMinor"
              type="number"
              min="1"
              max="100000000"
              required
              defaultValue={value.fareMinor || 12500}
            />
            <Field
              label="Currency (ISO code)"
              name="currency"
              required
              pattern="[A-Z]{3}"
              defaultValue={value.currency || "BDT"}
            />
          </div>
          <p className="muted">
            Saved as a draft. Publishing checks bus and driver availability,
            including turnaround time.
          </p>
          <button disabled={busy}>{busy ? "Saving…" : "Save draft"}</button>
        </form>
      )}
    </Modal>
  );
}
function Manifest({ value, admin, counter, onClose, onRefresh }) {
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
                      (admin ||
                        counter ||
                        ["PUBLISHED", "DEPARTED"].includes(
                          value.trip.status,
                        ))) ||
                      (admin &&
                        b.status === "CONFIRMED" &&
                        b.paymentStatus === "PAID") ||
                      ((admin || counter) &&
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
          admin={admin}
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

import { useEffect, useState } from "react";
import { ArrowRight, MapPin, Clock, Armchair, Ticket } from "lucide-react";
import { api, money, dateTime } from "../api";
import {
  Field,
  ErrorBox,
  Badge,
  Empty,
  Loading,
  Pager,
  Modal,
  Action,
} from "../components/UI";
export default function Journeys({ user, onSignIn, onBooked }) {
  const [stops, setStops] = useState([]),
    [query, setQuery] = useState({}),
    [page, setPage] = useState(null),
    [error, setError] = useState(null),
    [selectedTrip, setSelectedTrip] = useState(null);
  useEffect(() => {
    api("/stops").then(setStops).catch(setError);
  }, []);
  useEffect(() => {
    let live = true;
    setPage(null);
    api("/trips?" + new URLSearchParams(query))
      .then((p) => live && setPage(p))
      .catch((e) => live && setError(e));
    return () => {
      live = false;
    };
  }, [query]);
  return (
    <>
      <div className="hero">
        <div>
          <span className="eyebrow">MAKE ROOM FOR YOUR NEXT JOURNEY</span>
          <h1>Where are you headed?</h1>
          <p>Comfortable seats. Clear fares. A journey that starts here.</p>
        </div>
        <div className="hero-mark">
          <MapPin size={42} />
          <span>
            GOOD JOURNEYS
            <br />
            START WITH A PLAN
          </span>
        </div>
      </div>
      <form
        className="search-bar card"
        onSubmit={(e) => {
          e.preventDefault();
          setError(null);
          setQuery(
            Object.fromEntries(
              [...new FormData(e.currentTarget)].filter(([, v]) => v),
            ),
          );
        }}
      >
        <Field label="From">
          <select name="origin">
            <option value="">All departure stops</option>
            {stops.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name} · {s.city}
              </option>
            ))}
          </select>
        </Field>
        <Field label="To">
          <select name="destination">
            <option value="">All destinations</option>
            {stops.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name} · {s.city}
              </option>
            ))}
          </select>
        </Field>
        <Field label="Departure date (operator time)" name="date" type="date" />
        <button>
          Find trips <ArrowRight size={18} />
        </button>
      </form>
      <ErrorBox error={error} />
      <div className="section-head">
        <div>
          <span className="eyebrow">EXPLORE THE TIMETABLE</span>
          <h2>Available departures</h2>
        </div>
        <span className="muted">{page?.totalItems ?? "…"} trips</span>
      </div>
      {!page ? (
        <Loading />
      ) : page.items.length === 0 ? (
        <Empty>No trips match your search. Try another date or route.</Empty>
      ) : (
        <div className="trip-list">
          {page.items.map((t) => (
            <article className="card trip-card" key={t.id}>
              <div className="trip-main">
                <div className="trip-title">
                  <Badge>{t.currency}</Badge>
                  <span className="muted">{t.busRegistration}</span>
                </div>
                <h3>
                  {t.origin} <ArrowRight size={20} /> {t.destination}
                </h3>
                <div className="details">
                  <span>
                    <Clock size={16} />
                    {dateTime(t.departureAt)}
                  </span>
                  <span>Arrives {dateTime(t.arrivalAt)}</span>
                </div>
                <p className="muted">
                  {t.cancellationHours}-hour cancellation cutoff · Pay on board
                </p>
              </div>
              <div className="trip-price">
                <strong>{money(t.fareMinor, t.currency)}</strong>
                <span className="muted">per seat</span>
                <button
                  onClick={() => {
                    if (!user) onSignIn();
                    else if (user.role === "PASSENGER") setSelectedTrip(t);
                  }}
                  disabled={user && user.role !== "PASSENGER"}
                >
                  Choose seats <Armchair size={17} />
                </button>
              </div>
            </article>
          ))}
        </div>
      )}
      <Pager page={page} onChange={(p) => setQuery({ ...query, page: p })} />
      {selectedTrip && (
        <SeatPicker
          trip={selectedTrip}
          user={user}
          onClose={() => setSelectedTrip(null)}
          onBooked={onBooked}
        />
      )}
    </>
  );
}
function SeatPicker({ trip, user, onClose, onBooked }) {
  const [seats, setSeats] = useState([]),
    [selected, setSelected] = useState([]),
    [hold, setHold] = useState(null),
    [error, setError] = useState(null),
    [seconds, setSeconds] = useState(0),
    [busy, setBusy] = useState(false);
  const storageKey = "wayline-checkout-" + user.id + "-" + trip.id;
  const [attempt, setAttempt] = useState(() => {
    try {
      return JSON.parse(sessionStorage.getItem(storageKey) || "null");
    } catch {
      return null;
    }
  });
  const remember = (value) => {
    setAttempt(value);
    if (value) sessionStorage.setItem(storageKey, JSON.stringify(value));
    else sessionStorage.removeItem(storageKey);
  };
  useEffect(() => {
    let live = true;
    const update = () =>
      api("/trips/" + trip.id + "/seats")
        .then((s) => live && setSeats(s))
        .catch((e) => live && setError(e));
    update();
    const timer = setInterval(update, 10000);
    return () => {
      live = false;
      clearInterval(timer);
    };
  }, [trip.id]);
  useEffect(() => {
    if (attempt?.holdId)
      api("/holds/" + attempt.holdId)
        .then((h) => {
          setHold(h);
          setSelected(h.seatNos);
        })
        .catch(setError);
  }, []);
  useEffect(() => {
    if (!hold) return;
    const tick = () =>
      setSeconds(
        Math.max(0, Math.ceil((new Date(hold.expiresAt) - Date.now()) / 1000)),
      );
    tick();
    const timer = setInterval(tick, 1000);
    return () => clearInterval(timer);
  }, [hold]);
  async function reserve() {
    setError(null);
    try {
      const h = await api("/holds", {
        method: "POST",
        body: { tripId: trip.id, seatNos: selected },
      });
      setHold(h);
      remember({ holdId: h.id, key: crypto.randomUUID() });
    } catch (e) {
      setError(e);
      setSeats(await api("/trips/" + trip.id + "/seats"));
    }
  }
  async function release() {
    setError(null);
    try {
      if (hold) await api("/holds/" + hold.id, { method: "DELETE" });
      setHold(null);
      setSelected([]);
      remember(null);
      setSeats(await api("/trips/" + trip.id + "/seats"));
    } catch (e) {
      setError(e);
    }
  }
  async function confirm(e) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    const body = attempt.body || {
      holdId: hold.id,
      ...Object.fromEntries(new FormData(e.currentTarget)),
    };
    remember({ ...attempt, body });
    try {
      await api("/bookings", { method: "POST", body, key: attempt.key });
      remember(null);
      onBooked();
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }
  return (
    <Modal title="Choose your seats" onClose={onClose}>
      <p>
        {trip.origin} → {trip.destination} · {dateTime(trip.departureAt)}
      </p>
      <ErrorBox error={error} />
      <div className="seat-layout">
        <div className="bus-cabin">
          <div className="bus-front">FRONT OF BUS</div>
          <div
            className="seat-grid"
            style={{
              gridTemplateColumns: `repeat(${Math.max(1, ...seats.map((s) => s.columnNumber))}, 1fr)`,
            }}
          >
            {seats.map((s) => (
              <button
                key={s.label}
                style={{ gridRow: s.rowNumber, gridColumn: s.columnNumber }}
                className={
                  "seat " +
                  (selected.includes(s.label)
                    ? "selected"
                    : s.status.toLowerCase())
                }
                disabled={!!hold || s.status !== "AVAILABLE"}
                aria-label={`Seat ${s.label}, ${s.status.toLowerCase()}`}
                aria-pressed={selected.includes(s.label)}
                onClick={() =>
                  setSelected((v) =>
                    v.includes(s.label)
                      ? v.filter((x) => x !== s.label)
                      : v.length < 4
                        ? [...v, s.label]
                        : v,
                  )
                }
              >
                {s.label}
              </button>
            ))}
          </div>
          <div className="seat-legend">Available · Selected · Unavailable</div>
        </div>
        <div className="booking-summary">
          <span className="eyebrow">YOUR SELECTION</span>
          <h3>
            {selected.length ? selected.join(", ") : "Choose up to 4 seats"}
          </h3>
          <p>Entire-route travel · Pay on board</p>
          <strong className="total">
            {money(
              hold?.amountMinor ?? trip.fareMinor * selected.length,
              trip.currency,
            )}
          </strong>
          <p className="muted">
            Cancel before{" "}
            {dateTime(
              new Date(trip.departureAt).getTime() -
                trip.cancellationHours * 3600000,
            )}
            .
          </p>
          {!hold ? (
            <Action disabled={!selected.length} onClick={reserve}>
              Hold seats & continue
            </Action>
          ) : (
            <>
              <div className="notice">
                {hold.status === "CONSUMED"
                  ? "Confirmation received previously. Retry to recover your ticket."
                  : seconds > 0
                    ? `Seats held for ${Math.floor(seconds / 60)}:${String(seconds % 60).padStart(2, "0")}`
                    : "Hold expired. Release and select again, or retry a pending confirmation."}
              </div>
              <form onSubmit={confirm}>
                <Field
                  label="Passenger name"
                  name="contactName"
                  required
                  maxLength={100}
                  defaultValue={attempt?.body?.contactName || user.displayName}
                  readOnly={!!attempt?.body}
                />
                <Field
                  label="Contact email"
                  name="contactEmail"
                  type="email"
                  required
                  maxLength={254}
                  defaultValue={attempt?.body?.contactEmail || user.email}
                  readOnly={!!attempt?.body}
                />
                <Field
                  label="Phone"
                  name="contactPhone"
                  required
                  maxLength={30}
                  defaultValue={attempt?.body?.contactPhone || user.phone}
                  readOnly={!!attempt?.body}
                />
                <button disabled={busy || (!attempt?.body && seconds === 0)}>
                  <Ticket size={17} />
                  {busy
                    ? "Confirming…"
                    : attempt?.body
                      ? "Retry confirmation"
                      : "Confirm reservation"}
                </button>
              </form>
              <Action className="text-button" onClick={release}>
                Release seats / start again
              </Action>
            </>
          )}
        </div>
      </div>
      <p className="muted small">
        Closing this window keeps an active hold until its expiry. Reopen this
        trip to continue.
      </p>
    </Modal>
  );
}

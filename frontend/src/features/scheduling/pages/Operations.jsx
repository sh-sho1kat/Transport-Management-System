import { can } from "@/shared/auth/access";
import Manifest from "@/features/booking/components/Manifest";
import TripEditor from "@/features/scheduling/components/TripEditor";
import { useEffect, useState } from "react";
import { Plus, Users } from "lucide-react";
import { api } from "@/shared/api/client";
import { money, dateTime } from "@/shared/lib/format";

import {
  ErrorBox,
  Badge,
  Empty,
  Loading,
  Pager,
  Action,
  ReasonDialog,
} from "@/shared/ui/index.jsx";
import CounterSale from "@/features/booking/components/CounterSale";

import FareEditor from "@/features/scheduling/components/FareEditor";

export default function Operations({ user, driver = false, counter = false }) {
  const manage = can(user, "TRIP_MANAGE"),
    sell = can(user, "COUNTER_SELL"),
    operate = manage || can(user, "TRIP_OPERATE_ASSIGNED");
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
            {driver
              ? "DRIVER WORKSPACE"
              : counter
                ? "COUNTER SALES"
                : "DISPATCH & SCHEDULING"}
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
              : counter
                ? "Sell walk-in tickets, collect fares, and manage customer reservations."
                : "Plan departures, assign your team, and manage reservations."}
          </p>
        </div>
        {manage && (
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
                  {sell &&
                    t.status === "PUBLISHED" &&
                    new Date(t.departureAt) > new Date() && (
                      <button onClick={() => setSale(t)}>
                        Sell walk-in ticket
                      </button>
                    )}
                  {manage &&
                    ["DRAFT", "PUBLISHED"].includes(t.status) &&
                    new Date(t.departureAt) > new Date() && (
                      <button className="secondary" onClick={() => setFare(t)}>
                        Update fare
                      </button>
                    )}
                  {t.status === "DRAFT" && manage && (
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
                  {t.status === "PUBLISHED" && operate && (
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
                  {t.status === "DEPARTED" && operate && (
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
                  {manage && ["DRAFT", "PUBLISHED"].includes(t.status) && (
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
          user={user}
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

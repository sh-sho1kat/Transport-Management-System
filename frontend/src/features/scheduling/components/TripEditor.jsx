import { can } from "@/shared/auth/access";
import { useEffect, useState } from "react";

import { api } from "@/shared/api/client";
import { localDateTime } from "@/shared/lib/format";

import { Field, ErrorBox, Loading, Modal } from "@/shared/ui/index.jsx";
export default function TripEditor({ value, onClose, onSaved }) {
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
          drivers: staff.filter((s) => can(s, "TRIP_OPERATE_ASSIGNED")),
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

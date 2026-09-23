import { staffRoles, roleLabel } from "@/shared/auth/access";
import { useState } from "react";

import { api } from "@/shared/api/client";
import { Field, ErrorBox, Modal } from "@/shared/ui/index.jsx";
export default function Editor({ kind, value, stops, onClose, onSaved }) {
  const [error, setError] = useState(null),
    [busy, setBusy] = useState(false),
    [seatCount, setSeatCount] = useState(value.seats?.length || 40),
    [layout, setLayout] = useState(value.seats || makeSeats(40)),
    [routeStops, setRouteStops] = useState(
      value.stops?.map((s) => s.id) || ["", ""],
    );
  function makeSeats(count) {
    return Array.from({ length: count }, (_, i) => ({
      label: String.fromCharCode(65 + Math.floor(i / 4)) + ((i % 4) + 1),
      rowNumber: Math.floor(i / 4) + 1,
      columnNumber: (i % 4) + 1,
      blocked: false,
    }));
  }
  async function submit(e) {
    e.preventDefault();
    setError(null);
    setBusy(true);
    let body = Object.fromEntries(new FormData(e.currentTarget));
    if (kind !== "staff") body.active = body.active === "on";
    if (kind === "buses") body.seats = layout;
    if (kind === "routes") body.stopIds = routeStops;
    try {
      await api("/admin/" + kind + (value.id ? "/" + value.id : ""), {
        method: value.id ? "PATCH" : "POST",
        body,
      });
      onSaved();
    } catch (e) {
      setError(e);
    } finally {
      setBusy(false);
    }
  }
  const field = (name, label, type = "text", extra = {}) => (
    <Field
      key={name}
      label={label}
      name={name}
      type={type}
      required
      defaultValue={value[name] || ""}
      {...extra}
    />
  );
  return (
    <Modal
      title={
        (value.id ? "Edit " : "Add ") +
        (kind === "buses"
          ? "bus"
          : kind === "staff"
            ? "staff member"
            : kind.slice(0, -1))
      }
      onClose={onClose}
    >
      <ErrorBox error={error} />
      <form onSubmit={submit}>
        {kind === "stops" && (
          <>
            {field("name", "Stop name", "text", { maxLength: 100 })}
            {field("city", "City", "text", { maxLength: 100 })}
            {field("address", "Terminal address", "text", { maxLength: 255 })}
          </>
        )}
        {kind === "buses" && (
          <>
            {field("registration", "Registration number", "text", {
              maxLength: 40,
            })}
            {field("busType", "Bus type", "text", { maxLength: 60 })}
            <div className="form-row">
              <Field
                label="Number of seats"
                type="number"
                min="1"
                max="100"
                value={seatCount}
                onChange={(e) => setSeatCount(Number(e.target.value))}
              />
              <button
                type="button"
                className="secondary"
                onClick={() =>
                  setLayout(makeSeats(Math.min(100, Math.max(1, seatCount))))
                }
              >
                Generate layout
              </button>
            </div>
            <p className="muted small">
              Click a seat to block it from sale. Existing trip inventories
              remain unchanged.
            </p>
            <div className="layout-editor">
              {layout.map((s, i) => (
                <button
                  type="button"
                  key={s.label}
                  className={"seat " + (s.blocked ? "blocked" : "available")}
                  aria-pressed={s.blocked}
                  aria-label={`${s.label} ${s.blocked ? "blocked" : "sellable"}`}
                  onClick={() =>
                    setLayout(
                      layout.map((v, j) =>
                        j === i ? { ...v, blocked: !v.blocked } : v,
                      ),
                    )
                  }
                >
                  {s.label}
                </button>
              ))}
            </div>
          </>
        )}
        {kind === "routes" && (
          <>
            {field("code", "Route code", "text", { maxLength: 40 })}
            {field("name", "Route name", "text", { maxLength: 100 })}
            <p className="muted">
              Stops are ordered from origin to destination. Reservations cover
              the entire route.
            </p>
            {routeStops.map((s, i) => (
              <div className="form-row" key={i}>
                <Field
                  label={
                    i === 0
                      ? "Origin"
                      : i === routeStops.length - 1
                        ? "Destination"
                        : `Stop ${i + 1}`
                  }
                >
                  <select
                    required
                    value={s}
                    onChange={(e) =>
                      setRouteStops(
                        routeStops.map((v, j) =>
                          i === j ? e.target.value : v,
                        ),
                      )
                    }
                  >
                    <option value="">Choose stop</option>
                    {stops.map((stop) => (
                      <option
                        disabled={!stop.active}
                        key={stop.id}
                        value={stop.id}
                      >
                        {stop.name}
                      </option>
                    ))}
                  </select>
                </Field>
                {routeStops.length > 2 && (
                  <button
                    type="button"
                    className="text-button"
                    onClick={() =>
                      setRouteStops(routeStops.filter((_, j) => j !== i))
                    }
                  >
                    Remove
                  </button>
                )}
              </div>
            ))}
            <button
              type="button"
              disabled={routeStops.length >= 30}
              className="secondary"
              onClick={() => setRouteStops([...routeStops, ""])}
            >
              Add stop
            </button>
          </>
        )}
        {kind === "staff" && (
          <>
            {field("displayName", "Full name", "text", { maxLength: 100 })}
            {field("email", "Email", "email", { maxLength: 254 })}
            {field("phone", "Phone", "tel", { maxLength: 30 })}
            {field("password", "Initial password", "password", {
              minLength: 10,
              maxLength: 72,
              autoComplete: "new-password",
            })}
            <Field label="Role">
              <select name="role">
                {staffRoles.map((role) => (
                  <option key={role} value={role}>
                    {roleLabel(role)}
                  </option>
                ))}
              </select>
            </Field>
          </>
        )}
        {kind !== "staff" && (
          <label className="checkbox">
            <input
              type="checkbox"
              name="active"
              defaultChecked={value.active !== false}
            />{" "}
            Active (uncheck to archive)
          </label>
        )}
        <button disabled={busy}>
          {busy
            ? "Saving…"
            : "Save " + (kind === "staff" ? "staff member" : "changes")}
        </button>
      </form>
    </Modal>
  );
}

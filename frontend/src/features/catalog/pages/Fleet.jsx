import AccessEditor from "@/features/identity/components/AccessEditor";
import Editor from "@/features/catalog/components/CatalogEditor";
import { useEffect, useState } from "react";
import { Plus, ArrowRight } from "lucide-react";
import { api } from "@/shared/api/client";
import { ErrorBox, Badge, Empty, Loading } from "@/shared/ui/index.jsx";
export default function Fleet({ kind }) {
  const [items, setItems] = useState(null),
    [stops, setStops] = useState([]),
    [error, setError] = useState(null),
    [editing, setEditing] = useState(null),
    [activation, setActivation] = useState(null);
  const refresh = () =>
    api("/admin/" + kind)
      .then(setItems)
      .catch(setError);
  useEffect(() => {
    setItems(null);
    setEditing(null);
    setError(null);
    refresh();
    if (kind === "routes") api("/admin/stops").then(setStops).catch(setError);
  }, [kind]);
  return (
    <>
      <div className="section-head page-intro">
        <div>
          <span className="eyebrow">OPERATIONS / {kind.toUpperCase()}</span>
          <h1>
            {
              {
                buses: "Fleet & seat layouts",
                stops: "Stops & terminals",
                routes: "Routes & itineraries",
                staff: "Team & roles",
              }[kind]
            }
          </h1>
          <p>Manage the resources that keep your journeys running.</p>
        </div>
        <button onClick={() => setEditing({})}>
          <Plus size={18} /> Add{" "}
          {kind === "buses"
            ? "bus"
            : kind === "staff"
              ? "staff member"
              : kind.slice(0, -1)}
        </button>
      </div>
      <ErrorBox error={error} />
      {!items ? (
        <Loading />
      ) : !items.length ? (
        <Empty>No {kind} yet. Add your first record to get started.</Empty>
      ) : (
        <div className="resource-grid">
          {items.map((item) => (
            <article className="card resource" key={item.id}>
              <Badge>
                {item.role ?? (item.active ? "ACTIVE" : "ARCHIVED")}
              </Badge>
              <h3>{item.registration || item.name || item.displayName}</h3>
              <p className="muted">
                {item.busType || item.city || item.code || item.email}
              </p>
              {item.seats && (
                <p>
                  {item.seats.filter((s) => !s.blocked).length} sellable seats /{" "}
                  {item.seats.length} total
                </p>
              )}
              {item.stops && (
                <div className="itinerary">
                  {item.stops.map((s, i) => (
                    <span key={s.id}>
                      {i > 0 && <ArrowRight size={13} />} {s.name}
                    </span>
                  ))}
                </div>
              )}
              {kind === "staff" && (
                <button
                  className="secondary"
                  onClick={() => setActivation(item)}
                >
                  {item.active ? "Deactivate access" : "Reactivate access"}
                </button>
              )}
              {kind !== "staff" && (
                <button className="secondary" onClick={() => setEditing(item)}>
                  Edit details
                </button>
              )}
            </article>
          ))}
        </div>
      )}
      {activation && (
        <AccessEditor
          value={activation}
          onClose={() => setActivation(null)}
          onSaved={() => {
            setActivation(null);
            refresh();
          }}
        />
      )}
      {editing && (
        <Editor
          key={kind + editing.id}
          kind={kind}
          value={editing}
          stops={stops}
          onClose={() => setEditing(null)}
          onSaved={() => {
            setEditing(null);
            refresh();
          }}
        />
      )}
    </>
  );
}

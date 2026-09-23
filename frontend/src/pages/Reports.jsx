import { useEffect, useState } from "react";
import { Download } from "lucide-react";
import { api, money, dateTime, csvDownload } from "../api";
import { ErrorBox, Empty, Loading, Pager, Badge } from "../components/UI";
export default function Reports({ audit = false }) {
  const [page, setPage] = useState(null),
    [number, setNumber] = useState(0),
    [error, setError] = useState(null);
  useEffect(() => {
    setPage(null);
    api(
      (audit ? "/admin/audit" : "/admin/reports/occupancy") + "?page=" + number,
    )
      .then(setPage)
      .catch(setError);
  }, [number, audit]);
  return (
    <>
      <div className="section-head page-intro">
        <div>
          <span className="eyebrow">OPERATOR INSIGHTS</span>
          <h1>{audit ? "Activity log" : "Occupancy & reservations"}</h1>
          <p>
            {audit
              ? "A record of important account and operational changes."
              : "Confirmed booking value is not collected revenue. Payments are due on board."}
          </p>
        </div>
        {!audit && page && (
          <button
            className="secondary"
            onClick={() =>
              csvDownload("occupancy-page-" + (number + 1) + ".csv", [
                [
                  "Route",
                  "Departure",
                  "Currency",
                  "Sellable seats",
                  "Booked seats",
                  "Occupancy %",
                  "Confirmed bookings",
                  "Cancelled bookings",
                  "Booked value minor",
                ],
                ...page.items.map((r) => [
                  r.routeName,
                  r.departureAt,
                  r.currency,
                  r.sellableSeats,
                  r.bookedSeats,
                  r.occupancyPercent,
                  r.confirmedBookings,
                  r.cancelledBookings,
                  r.bookedValueMinor,
                ]),
              ])
            }
          >
            <Download size={16} /> Export this page
          </button>
        )}
      </div>
      <ErrorBox error={error} />
      {!page ? (
        <Loading />
      ) : !page.items.length ? (
        <Empty>No records yet.</Empty>
      ) : (
        <div className="card table-wrap">
          <table>
            <thead>
              <tr>
                {(audit
                  ? ["When", "Who", "Action", "Reason", "Resource"]
                  : [
                      "Route / departure",
                      "Seats",
                      "Occupancy",
                      "Reservations",
                      "Booked value",
                    ]
                ).map((h) => (
                  <th key={h}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {page.items.map((r) =>
                audit ? (
                  <tr key={r.id}>
                    <td>{dateTime(r.occurredAt)}</td>
                    <td>{r.actor}</td>
                    <td>
                      <Badge>{r.action}</Badge>
                    </td>
                    <td>{r.reason || "—"}</td>
                    <td className="small">{r.resourceId}</td>
                  </tr>
                ) : (
                  <tr key={r.tripId}>
                    <td>
                      {r.routeName}
                      <small>{dateTime(r.departureAt)}</small>
                    </td>
                    <td>
                      {r.bookedSeats} / {r.sellableSeats}
                    </td>
                    <td>
                      <div className="occupancy">
                        <div style={{ width: r.occupancyPercent + "%" }} />
                      </div>
                      {r.occupancyPercent}%
                    </td>
                    <td>
                      {r.confirmedBookings} confirmed
                      <small>{r.cancelledBookings} cancelled</small>
                    </td>
                    <td>
                      {money(r.bookedValueMinor, r.currency)}
                      <small>Unpaid</small>
                    </td>
                  </tr>
                ),
              )}
            </tbody>
          </table>
        </div>
      )}
      <Pager page={page} onChange={setNumber} />
    </>
  );
}

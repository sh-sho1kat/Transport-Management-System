import { lazy, Suspense } from "react";
import { Loading } from "@/shared/ui";
import {
  Search,
  Ticket,
  CalendarDays,
  BusFront,
  Route,
  MapPin,
  Users,
  BarChart3,
  History,
} from "lucide-react";
import { can, operationsMode } from "@/shared/auth/access";
const Auth = lazy(() => import("@/features/identity/pages/Auth"));
const Profile = lazy(() => import("@/features/identity/pages/Profile"));
const Journeys = lazy(() => import("@/features/booking/pages/Journeys"));
const Bookings = lazy(() => import("@/features/booking/pages/Bookings"));
const Fleet = lazy(() => import("@/features/catalog/pages/Fleet"));
const Operations = lazy(() => import("@/features/scheduling/pages/Operations"));
const Reports = lazy(() => import("@/features/reporting/pages/Reports"));
export const features = [
  {
    id: "journeys",
    label: "Find a journey",
    icon: Search,
    visible: (u) => !u || can(u, "SELF_BOOK"),
    render: (c) => (
      <Journeys
        user={c.user}
        onSignIn={() => c.nav("auth")}
        onBooked={() => c.nav("bookings")}
      />
    ),
  },
  {
    id: "bookings",
    label: "My reservations",
    icon: Ticket,
    visible: (u) => can(u, "SELF_BOOK"),
    render: () => <Bookings />,
  },
  {
    id: "operations",
    label: (u) =>
      can(u, "TRIP_MANAGE")
        ? "Trip operations"
        : can(u, "COUNTER_SELL")
          ? "Ticket counter"
          : "Assigned trips",
    icon: CalendarDays,
    visible: (u) => can(u, "TRIP_READ_ALL") || can(u, "TRIP_OPERATE_ASSIGNED"),
    render: (c) => <Operations user={c.user} {...operationsMode(c.user)} />,
  },
  ...[
    ["buses", "Fleet & seats", BusFront],
    ["routes", "Routes", Route],
    ["stops", "Stops & terminals", MapPin],
    ["staff", "Team & roles", Users],
  ].map(([id, label, icon]) => ({
    id,
    label,
    icon,
    visible: (u) => can(u, id === "staff" ? "STAFF_MANAGE" : "CATALOG_MANAGE"),
    render: () => <Fleet key={id} kind={id} />,
  })),
  {
    id: "reports",
    label: "Reports",
    icon: BarChart3,
    visible: (u) => can(u, "REPORT_READ"),
    render: () => <Reports key="reports" />,
  },
  {
    id: "audit",
    label: "Activity log",
    icon: History,
    visible: (u) => can(u, "AUDIT_READ"),
    render: () => <Reports key="audit" audit />,
  },
];
export const homePage = (user) =>
  features.find((f) => f.visible(user))?.id || "profile";
export const navigation = (user) =>
  features
    .filter((f) => f.visible(user))
    .map((f) => [
      f.id,
      typeof f.label === "function" ? f.label(user) : f.label,
      f.icon,
    ]);
export function FeaturePage(context) {
  return (
    <Suspense fallback={<Loading />}>
      <ResolvedPage {...context} />
    </Suspense>
  );
}
function ResolvedPage(context) {
  const { page, user, login, setUser } = context;
  if (page === "auth" || page === "reset")
    return (
      <Auth onLogin={login} initial={page === "reset" ? "reset" : "login"} />
    );
  if (page === "profile" && user)
    return <Profile user={user} onUpdate={setUser} />;
  const feature = features.find((f) => f.id === page && f.visible(user));
  return feature ? (
    feature.render(context)
  ) : (
    <p>This page is not available for your account.</p>
  );
}

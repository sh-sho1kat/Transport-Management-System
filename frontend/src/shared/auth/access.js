import grants from "./role-permissions.js";
export function can(user, permission) {
  return !!user && (grants[user.role] || []).includes(permission);
}
export const roleLabel = (role) => role.toLowerCase().replaceAll("_", " ");
export const staffRoles = Object.keys(grants)
  .filter((role) => !grants[role].includes("SELF_BOOK"))
  .sort((a, b) => grants[a].length - grants[b].length);
export const operationsMode = (user) => ({
  driver: !can(user, "TRIP_READ_ALL") && can(user, "TRIP_OPERATE_ASSIGNED"),
  counter: !can(user, "TRIP_MANAGE") && can(user, "COUNTER_SELL"),
});

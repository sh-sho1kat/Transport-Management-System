let token;
export async function csrf() {
  const res = await fetch("/api/v1/auth/csrf", { credentials: "include" });
  if (!res.ok)
    throw new Error("Cannot connect to the server. Please try again.");
  token = await res.json();
  return token;
}
export async function api(path, { method = "GET", body, key, signal } = {}) {
  const headers = {};
  if (body !== undefined) headers["Content-Type"] = "application/json";
  if (key) headers["Idempotency-Key"] = key;
  if (!["GET", "HEAD"].includes(method)) {
    if (!token) await csrf();
    headers[token.headerName] = token.token;
  }
  const res = await fetch("/api/v1" + path, {
    method,
    credentials: "include",
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
    signal: signal || AbortSignal.timeout(20000),
  });
  const data = res.status === 204 ? null : await res.json().catch(() => null);
  if (!res.ok) {
    if (res.status === 403) token = undefined;
    if (res.status === 401 && path !== "/me" && !path.startsWith("/auth/"))
      window.dispatchEvent(new Event("wayline-session-expired"));
    const error = new Error(data?.message || `Request failed (${res.status}).`);
    error.status = res.status;
    error.fields = data?.fieldErrors;
    throw error;
  }
  if (path === "/auth/login" || path === "/auth/logout") await csrf();
  return data;
}
export const money = (minor, currency = "BDT") =>
  new Intl.NumberFormat(undefined, { style: "currency", currency }).format(
    minor /
      10 **
        new Intl.NumberFormat(undefined, {
          style: "currency",
          currency,
        }).resolvedOptions().maximumFractionDigits,
  );
export const dateTime = (value) =>
  new Date(value).toLocaleString(undefined, {
    dateStyle: "medium",
    timeStyle: "short",
  });
export const localDateTime = (value) => {
  const d = new Date(value);
  return new Date(d.getTime() - d.getTimezoneOffset() * 60000)
    .toISOString()
    .slice(0, 16);
};
export function csvDownload(name, rows) {
  const safe = (value) =>
    '"' +
    String(value ?? "")
      .replace(/^[=+@\-\t\r]/, "'$&")
      .replaceAll('"', '""') +
    '"';
  const blob = new Blob([rows.map((r) => r.map(safe).join(",")).join("\r\n")], {
    type: "text/csv;charset=utf-8",
  });
  const link = document.createElement("a");
  link.href = URL.createObjectURL(blob);
  link.download = name;
  link.click();
  setTimeout(() => URL.revokeObjectURL(link.href), 1000);
}

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

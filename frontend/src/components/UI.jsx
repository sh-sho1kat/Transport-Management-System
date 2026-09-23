import { useState, useEffect, useRef } from "react";
import { LoaderCircle, AlertCircle, ArrowLeft, ArrowRight } from "lucide-react";
export function Field({ label, children, ...props }) {
  return (
    <label className="field">
      <span>{label}</span>
      {children || <input {...props} />}
    </label>
  );
}
export function ErrorBox({ error }) {
  return error ? (
    <div className="notice error" role="alert">
      <AlertCircle size={18} />
      <div>
        {error.message || error}
        {error.fields?.map((f, i) => (
          <div key={i}>
            {f.field}: {f.message}
          </div>
        ))}
      </div>
    </div>
  ) : null;
}
export function Badge({ children }) {
  return (
    <span className={"badge " + String(children).toLowerCase()}>
      {String(children).replaceAll("_", " ")}
    </span>
  );
}
export function Empty({ children = "No records yet." }) {
  return <div className="empty">{children}</div>;
}
export function Loading() {
  return (
    <div className="empty">
      <LoaderCircle className="spin" size={22} /> Loading…
    </div>
  );
}
export function Pager({ page, onChange }) {
  return page && page.totalPages > 1 ? (
    <div className="pager">
      <button
        className="secondary"
        disabled={page.page === 0}
        onClick={() => onChange(page.page - 1)}
      >
        <ArrowLeft size={16} /> Previous
      </button>
      <span>
        Page {page.page + 1} of {page.totalPages}
      </span>
      <button
        className="secondary"
        disabled={page.page + 1 >= page.totalPages}
        onClick={() => onChange(page.page + 1)}
      >
        Next <ArrowRight size={16} />
      </button>
    </div>
  ) : null;
}
export function Action({
  onClick,
  children,
  className = "",
  disabled = false,
}) {
  const [busy, setBusy] = useState(false);
  return (
    <button
      className={className}
      disabled={busy || disabled}
      onClick={async () => {
        setBusy(true);
        try {
          await onClick();
        } finally {
          setBusy(false);
        }
      }}
    >
      {busy ? <LoaderCircle className="spin" size={16} /> : null}
      {children}
    </button>
  );
}
export function Modal({ title, children, onClose }) {
  const dialog = useRef(null);
  const close = useRef(onClose);
  close.current = onClose;
  useEffect(() => {
    const previous = document.activeElement;
    const priorOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    dialog.current.querySelector("button, input, select")?.focus();
    const keyboard = (event) => {
      const dialogs = document.querySelectorAll('[role="dialog"]');
      if (dialogs[dialogs.length - 1] !== dialog.current) return;
      if (event.key === "Escape") {
        event.preventDefault();
        close.current();
      }
      if (event.key === "Tab") {
        const controls = [
          ...dialog.current.querySelectorAll(
            "button:not(:disabled), input:not(:disabled), select:not(:disabled), a[href]",
          ),
        ].filter((e) => e.offsetParent !== null);
        const first = controls[0],
          last = controls[controls.length - 1];
        if (event.shiftKey && document.activeElement === first) {
          event.preventDefault();
          last?.focus();
        } else if (!event.shiftKey && document.activeElement === last) {
          event.preventDefault();
          first?.focus();
        }
      }
    };
    document.addEventListener("keydown", keyboard);
    return () => {
      document.removeEventListener("keydown", keyboard);
      document.body.style.overflow = priorOverflow;
      previous?.focus();
    };
  }, []);
  return (
    <div className="modal-backdrop" onClick={onClose}>
      <section
        ref={dialog}
        className="modal"
        role="dialog"
        aria-modal="true"
        aria-label={title}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="section-head">
          <h2>{title}</h2>
          <button
            className="secondary"
            onClick={onClose}
            aria-label="Close dialog"
          >
            Close
          </button>
        </div>
        {children}
      </section>
    </div>
  );
}
export function ReasonDialog({ title, onConfirm, onClose }) {
  const [reason, setReason] = useState(""),
    [error, setError] = useState(null),
    [busy, setBusy] = useState(false);
  return (
    <Modal title={title} onClose={onClose}>
      <form
        onSubmit={async (e) => {
          e.preventDefault();
          setBusy(true);
          try {
            await onConfirm(reason);
            onClose();
          } catch (e) {
            setError(e);
          } finally {
            setBusy(false);
          }
        }}
      >
        <p>This action will be recorded in the activity log.</p>
        <ErrorBox error={error} />
        <Field
          label="Reason"
          required
          maxLength={255}
          value={reason}
          onChange={(e) => setReason(e.target.value)}
        />
        <button className="danger" disabled={busy}>
          {busy ? "Saving…" : "Confirm cancellation"}
        </button>
      </form>
    </Modal>
  );
}

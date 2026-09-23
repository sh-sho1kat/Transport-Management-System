import { createRoot } from "react-dom/client";
import App from "@/app/App.jsx";
import "@/app/styles.css";
import ErrorBoundary from "@/app/ErrorBoundary";
createRoot(document.getElementById("root")).render(
  <ErrorBoundary>
    <App />
  </ErrorBoundary>,
);

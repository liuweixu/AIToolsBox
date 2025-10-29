import "./index.css";
import { createRoot } from "react-dom/client";
import { Toaster } from "sonner";
import "@ant-design/v5-patch-for-react-19";
import { BrowserRouter } from "react-router-dom";
import App from "./App";

createRoot(document.getElementById("root")!).render(
  <BrowserRouter>
    <App />
    <Toaster />
  </BrowserRouter>
);

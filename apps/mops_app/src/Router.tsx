import { createBrowserRouter, Outlet, RouterProvider } from "react-router-dom";
import { Shell } from "./components/shell";
import { ChatPage } from "./pages/chat";
import { PlanPage } from "./pages/plan";
import { SpendPage } from "./pages/spend";
import { SettingsPage } from "./pages/settings";

const router = createBrowserRouter([
  {
    path: "/",
    element: (
      <Shell>
        <Outlet />
      </Shell>
    ),
    children: [
      {
        path: "/",
        element: <ChatPage />,
      },
      {
        path: "/plan",
        element: <PlanPage />,
      },
      {
        path: "/spend",
        element: <SpendPage />,
      },
      {
        path: "/settings",
        element: <SettingsPage />,
      },
    ],
  },
]);

export function Router() {
  return <RouterProvider router={router} />;
}

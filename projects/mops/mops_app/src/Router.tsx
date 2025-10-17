import {
  createBrowserRouter,
  Navigate,
  Outlet,
  RouterProvider,
} from "react-router-dom";
import { Shell } from "./components/shell";
import { PlanPage } from "./pages/plan";
import { SpendPage } from "./pages/spend";
import { SettingsPage } from "./pages/settings";
import { CounterPage } from "./pages/counter";

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
        index: true,
        element: <Navigate to="/spend" replace />,
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
      {
        path: "/counter",
        element: <CounterPage />,
      },
    ],
  },
]);

export function Router() {
  return <RouterProvider router={router} />;
}

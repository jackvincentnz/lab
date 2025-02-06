import { createBrowserRouter, Outlet, RouterProvider } from "react-router-dom";
import { Shell } from "./components/shell";
import { HomePage } from "./pages/home";
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
        element: <HomePage />,
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

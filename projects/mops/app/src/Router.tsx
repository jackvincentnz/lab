import {
  createMemoryRouter,
  createBrowserRouter,
  Navigate,
  Outlet,
  RouterProvider,
  type RouteObject,
} from "react-router-dom";
import { Shell } from "./components/shell";
import { PlanPage } from "./pages/plan";
import { SpendPage } from "./pages/spend";
import { SettingsPage } from "./pages/settings";
import { CounterPage } from "./pages/counter";

export const appRoutes: RouteObject[] = [
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
];

export function createAppRouter() {
  return createBrowserRouter(appRoutes);
}

export function createTestRouter(initialEntries: string[] = ["/"]) {
  return createMemoryRouter(appRoutes, {
    initialEntries,
  });
}

export function Router() {
  return <RouterProvider router={createAppRouter()} />;
}

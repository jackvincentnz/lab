import { createBrowserRouter, Outlet, RouterProvider } from "react-router-dom";
import { HomePage } from "./pages/home";
import { Shell } from "./components/shell";

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
    ],
  },
]);

export function Router() {
  return <RouterProvider router={router} />;
}

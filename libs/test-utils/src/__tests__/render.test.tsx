import { createContext, useContext, type PropsWithChildren } from "react";
import { MonthRangePicker } from "@lab/bubbles";
import { Button } from "@mantine/core";
import { modals } from "@mantine/modals";
import { gql } from "@apollo/client";
import { useQuery } from "@apollo/client/react";
import { useLocation, useParams } from "react-router-dom";
import { expect, it, render, screen, userEvent } from "../index.js";

const Context = createContext("missing");

it("composes app wrappers with Mantine and modals, including rerender", async () => {
  function Wrapper({ children }: PropsWithChildren) {
    return <Context.Provider value="app">{children}</Context.Provider>;
  }
  function Component({ label }: { label: string }) {
    return (
      <Button
        onClick={() =>
          modals.open({ title: "Dialog", children: "Modal content" })
        }
      >
        {useContext(Context)} {label}
      </Button>
    );
  }
  const { rerender } = render(<Component label="before" />, {
    wrapper: Wrapper,
  });
  rerender(<Component label="after" />);
  await userEvent.click(screen.getByRole("button", { name: "app after" }));
  expect(await screen.findByText("Modal content")).toBeInTheDocument();
});

it("starts a memory router at the requested route", () => {
  function Location() {
    return <span>{useLocation().pathname}</span>;
  }
  render(<Location />, { route: "/tasks" });
  expect(screen.getByText("/tasks")).toBeInTheDocument();
});

it("matches route parameters when a path is supplied", () => {
  function Task() {
    return <span>Task {useParams()["id"]}</span>;
  }
  render(<Task />, { route: "/tasks/42", path: "/tasks/:id" });
  expect(screen.getByText("Task 42")).toBeInTheDocument();
});

it("creates a fresh Apollo cache for each render", async () => {
  const query = gql`
    query Greeting {
      greeting
    }
  `;
  function Greeting() {
    const { data } = useQuery<{ greeting: string }>(query);
    return <span>{data?.greeting ?? "Loading"}</span>;
  }
  const first = render(<Greeting />, {
    mockedProvider: {
      mocks: [{ request: { query }, result: { data: { greeting: "First" } } }],
    },
  });
  expect(await screen.findByText("First")).toBeInTheDocument();
  first.unmount();
  render(<Greeting />, {
    mockedProvider: {
      mocks: [{ request: { query }, result: { data: { greeting: "Second" } } }],
    },
  });
  expect(await screen.findByText("Second")).toBeInTheDocument();
});

it("cleans up earlier renders between tests", () => {
  // Mantine retains an empty shared portal; rendered content must be gone.
  expect(document.body.textContent).toBe("");
  render(<Button>Standalone</Button>);
  expect(
    screen.getByRole("button", { name: "Standalone" }),
  ).toBeInTheDocument();
});

it("renders bubbles components with the shared providers", async () => {
  render(<MonthRangePicker label="Choose months" />);
  const button = screen.getByRole("button", {
    name: "Choose months: Select Range",
  });
  await userEvent.click(button);
  expect(await screen.findByRole("dialog")).toBeInTheDocument();
});

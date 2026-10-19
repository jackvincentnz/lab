import { expect, it, render, screen } from "@lab/test-utils";
import { Timeline } from "../Timeline";

it("renders the title, message and time of each item", () => {
  render(
    <Timeline
      items={[
        { title: "First title", message: "First message", when: "Monday" },
        { title: "Second title", message: "Second message", when: "Tuesday" },
      ]}
    />,
  );

  expect(screen.getByText("First title")).toBeInTheDocument();
  expect(screen.getByText("First message")).toBeInTheDocument();
  expect(screen.getByText("Monday")).toBeInTheDocument();
  expect(screen.getByText("Second title")).toBeInTheDocument();
  expect(screen.getByText("Second message")).toBeInTheDocument();
  expect(screen.getByText("Tuesday")).toBeInTheDocument();
});

it("renders no items when the list is empty", () => {
  const { container } = render(<Timeline items={[]} />);

  expect(
    container.querySelector(".mantine-Timeline-root"),
  ).toBeEmptyDOMElement();
});

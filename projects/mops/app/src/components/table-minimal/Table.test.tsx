import { describe, expect, render, screen, test } from "../../test";
import { Table } from "./Table";
import { data } from "./data";

describe("Table", async () => {
  test("renders headers", async () => {
    render(<Table />);

    expect(await screen.findByText(/First Name/)).toBeInTheDocument();
    expect(await screen.findByText(/Last Name/)).toBeInTheDocument();
    expect(await screen.findByText(/Address/)).toBeInTheDocument();
    expect(await screen.findByText(/City/)).toBeInTheDocument();
    expect(await screen.findByText(/State/)).toBeInTheDocument();
  });

  test("renders rows", async () => {
    render(<Table />);

    for (const entry of data) {
      expect(await screen.findByText(entry.firstName)).toBeInTheDocument();
      expect(await screen.findByText(entry.lastName)).toBeInTheDocument();
      expect(await screen.findByText(entry.address)).toBeInTheDocument();
      expect(await screen.findByText(entry.city)).toBeInTheDocument();
      expect(await screen.findByText(entry.state)).toBeInTheDocument();
    }
  });
});

import { useState } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { GuidesDrawer } from "./components/GuidesDrawer";
import { Sidebar } from "./components/Sidebar";
import { Empty } from "./components/Status";
import { CollectionPage } from "./pages/CollectionPage";
import { ProblemPage } from "./pages/ProblemPage";
import { SignalPage } from "./pages/SignalPage";
import { SolutionPage } from "./pages/SolutionPage";
import { StrategyPage } from "./pages/StrategyPage";

export function App() {
  const [guides, setGuides] = useState(false);
  return (
    <div className="workspace">
      <a className="skip-link" href="#content">
        Skip to content
      </a>
      <Sidebar onOpenGuides={() => setGuides(true)} />
      <main id="content" tabIndex={-1}>
        <Routes>
          <Route path="/" element={<Navigate to="/signals" replace />} />
          <Route
            path="/signals"
            element={<CollectionPage key="signals" kind="signals" />}
          />
          <Route path="/signals/:id" element={<SignalPage />} />
          <Route
            path="/problems"
            element={<CollectionPage key="problems" kind="problems" />}
          />
          <Route path="/problems/:id" element={<ProblemPage />} />
          <Route
            path="/solutions"
            element={<CollectionPage key="solutions" kind="solutions" />}
          />
          <Route path="/solutions/:id" element={<SolutionPage />} />
          <Route path="/strategy" element={<StrategyPage />} />
          <Route
            path="*"
            element={
              <div className="page">
                <Empty>
                  Page not found. Choose a collection from the navigation.
                </Empty>
              </div>
            }
          />
        </Routes>
      </main>
      <GuidesDrawer opened={guides} onClose={() => setGuides(false)} />
    </div>
  );
}

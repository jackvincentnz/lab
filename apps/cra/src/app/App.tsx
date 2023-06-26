import screenfull from "screenfull";

import Game from "../tic_tac_toe/Game";
import Tasks from "../tasks";

function App() {
  return (
    <>
      <Game />
      <br />
      <button onClick={handleClick}>Fullscreen</button>
      <Tasks />
    </>
  );
}

function handleClick() {
  if (screenfull.isEnabled) {
    screenfull.request();
  }
}

export default App;

import Game from "../tic_tac_toe/Game";
import screenfull from "screenfull";

function App() {
  return (
    <>
      <Game />
      <br />
      <button onClick={handleClick}>Fullscreen</button>
    </>
  );
}

function handleClick() {
  if (screenfull.isEnabled) {
    screenfull.request();
  }
}

export default App;

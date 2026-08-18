import { Routes, Route } from "react-router-dom";

import Layout from "./components/Layout/Layout";
import Home from "./pages/Home";
import TorneoDetalle from "./pages/TorneoDetalle";
import PartidoDetalle from "./pages/PartidoDetalle";
import Login from "./pages/Login";
import Register from "./pages/Register";
import MisTorneos from "./pages/MisTorneos";

function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<Home />} />
        <Route path="/torneo/:id" element={<TorneoDetalle />} />
        <Route path="/partido/:id" element={<PartidoDetalle />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/mis-torneos" element={<MisTorneos />} />
      </Route>
    </Routes>
  );
}

export default App;

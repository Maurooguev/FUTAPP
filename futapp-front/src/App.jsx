import { Routes, Route } from "react-router-dom";

import Layout from "./components/Layout/Layout";
import Home from "./pages/Home";
import TorneoDetalle from "./pages/TorneoDetalle";
import PartidoDetalle from "./pages/PartidoDetalle";
import Login from "./pages/Login";
import MisTorneos from "./pages/MisTorneos";
import EquipoPerfil from "./pages/EquipoPerfil";
import JugadorPerfil from "./pages/JugadorPerfil";
import Solicitudes from "./pages/Solicitudes";
import MiPerfilJugador from "./pages/MiPerfilJugador";
import MiPerfilRedirect from "./pages/MiPerfilRedirect";
import MisEquipos from "./pages/MisEquipos";
import ForgotPassword from "./pages/ForgotPassword";
import ResetPassword from "./pages/ResetPassword";
import Cuenta from "./pages/Cuenta";
import Notificaciones from "./pages/Notificaciones";
import PerfilOrganizador from "./pages/PerfilOrganizador";
import MiPerfilOrganizador from "./pages/MiPerfilOrganizador";
import MiPerfilOrganizadorRedirect from "./pages/MiPerfilOrganizadorRedirect";
import Favoritos from "./pages/Favoritos";
import { FEATURES } from "./config/features";
import AdminOrganizadores from "./pages/AdminOrganizadores";
import useAuth from "./context/useAuth";
import { Navigate, useLocation } from "react-router-dom";

function App() {
  const { usuario } = useAuth();
  const location = useLocation();
  if (usuario?.passwordChangeRequired && location.pathname !== "/cuenta") return <Navigate to="/cuenta" replace />;
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<Home />} />
        <Route path="/torneo/:id" element={<TorneoDetalle />} />
        <Route path="/partido/:id" element={<PartidoDetalle />} />
        <Route path="/equipo/:id" element={<EquipoPerfil />} />
        <Route path="/jugador/:id" element={<JugadorPerfil />} />
        {FEATURES.claims && <Route path="/solicitudes" element={<Solicitudes />} />}
        {FEATURES.playerAccounts && <Route path="/mi-perfil-jugador" element={<MiPerfilRedirect />} />}
        {FEATURES.playerAccounts && <Route path="/mi-perfil-jugador/editar" element={<MiPerfilJugador />} />}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Navigate to="/login" replace />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />
        <Route path="/cuenta" element={<Cuenta />} />
        {FEATURES.notifications && <Route path="/notificaciones" element={<Notificaciones />} />}
        {FEATURES.favorites && <Route path="/favoritos" element={<Favoritos />} />}
        <Route path="/mis-torneos" element={<MisTorneos />} />
        {FEATURES.teamDirectorAccounts && <Route path="/mis-equipos" element={<MisEquipos />} />}
        <Route path="/organizador/:id" element={<PerfilOrganizador />} />
        <Route path="/mi-perfil-organizador" element={<MiPerfilOrganizadorRedirect />} />
        <Route path="/mi-perfil-organizador/editar" element={<MiPerfilOrganizador />} />
        <Route path="/admin/organizadores" element={<AdminOrganizadores />} />
      </Route>
    </Routes>
  );
}

export default App;

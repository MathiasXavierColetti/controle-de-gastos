import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ThemeProvider } from './components/ThemeContext'; // Ajuste o caminho se necessário
import Login from './components/Login';
import Dashboard from './components/Dashboard';
import Pessoas from './components/Pessoas';
import Grupos from './components/Grupos';
import TipoDeGasto from "./components/TipoDeGasto.jsx";
import Gastos from "./pages/Gastos.jsx";
import RelatorioPizza from "./components/RelatorioPizza.jsx";
import 'bootstrap/dist/css/bootstrap.min.css';

function App() {
    return (
        <ThemeProvider>
            <Router>
                <Routes>
                    <Route path="/" element={<Login />} />
                    <Route path="/dashboard" element={<Dashboard />} />
                    <Route path="/pessoas" element={<Pessoas />} />
                    <Route path="/grupos" element={<Grupos />} />
                    <Route path="/tipo-de-gastos" element={<TipoDeGasto />} />
                    <Route path="/gastos" element={<Gastos />} />
                    <Route path="/relatorio-pizza" element={<RelatorioPizza />} />
                </Routes>
            </Router>
        </ThemeProvider>
    );
}

export default App;
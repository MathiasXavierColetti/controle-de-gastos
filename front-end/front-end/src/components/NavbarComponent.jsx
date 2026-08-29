import React from 'react';
import { Navbar, Nav, Container, Button } from 'react-bootstrap';
import { Link, useLocation } from 'react-router-dom';
import { useTheme } from './ThemeContext'; // Importe o hook do tema

export default function NavbarComponent() {
    const location = useLocation();
    const { darkMode, toggleTheme } = useTheme();
    const isActive = (path) => location.pathname === path;

    return (
        <Navbar
            bg={darkMode ? 'dark' : 'white'}
            variant={darkMode ? 'dark' : 'light'}
            expand="lg"
            className="shadow-sm sticky-top py-3 transition-all"
        >
            <Container>
                {/* Logo / Marca */}
                <Navbar.Brand
                    as={Link}
                    to="/"
                    className="fw-bold fs-5 d-flex align-items-center gap-2"
                    style={{ color: '#7c3aed' }}
                >
                    <span>💸</span> Controle de Gastos
                </Navbar.Brand>

                {/* Botão de alternar tema e menu mobile lado a lado no celular */}
                <div className="d-flex align-items-center gap-2">
                    <Button
                        variant={darkMode ? 'outline-light' : 'outline-dark'}
                        size="sm"
                        className="rounded-circle p-2 d-flex align-items-center justify-content-center"
                        style={{ width: '38px', height: '38px' }}
                        onClick={toggleTheme}
                        title="Alternar Modo Escuro"
                    >
                        {darkMode ? '☀️' : '🌙'}
                    </Button>

                    <Navbar.Toggle aria-controls="basic-navbar-nav" className="border-0 shadow-none" />
                </div>

                <Navbar.Collapse id="basic-navbar-nav">
                    <Nav className="ms-auto gap-1 align-items-lg-center mt-3 mt-lg-0">
                        <Nav.Link
                            as={Link}
                            to="/"
                            className={`fw-semibold px-3 py-2 rounded-pill ${
                                isActive('/') ? 'text-white' : (darkMode ? 'text-light' : 'text-secondary')
                            }`}
                            style={{
                                backgroundColor: isActive('/') ? '#7c3aed' : 'transparent'
                            }}
                        >
                            Início
                        </Nav.Link>

                        <Nav.Link
                            as={Link}
                            to="/pessoas"
                            className={`fw-semibold px-3 py-2 rounded-pill ${
                                isActive('/pessoas') ? 'text-white' : (darkMode ? 'text-light' : 'text-secondary')
                            }`}
                            style={{
                                backgroundColor: isActive('/pessoas') ? '#7c3aed' : 'transparent'
                            }}
                        >
                            Pessoas
                        </Nav.Link>

                        <Nav.Link
                            as={Link}
                            to="/grupos"
                            className={`fw-semibold px-3 py-2 rounded-pill ${
                                isActive('/grupos') ? 'text-white' : (darkMode ? 'text-light' : 'text-secondary')
                            }`}
                            style={{
                                backgroundColor: isActive('/grupos') ? '#7c3aed' : 'transparent'
                            }}
                        >
                            Grupos
                        </Nav.Link>

                        <Nav.Link
                            as={Link}
                            to="/gastos"
                            className={`fw-semibold px-3 py-2 rounded-pill ${
                                isActive('/gastos') ? 'text-white' : (darkMode ? 'text-light' : 'text-secondary')
                            }`}
                            style={{
                                backgroundColor: isActive('/gastos') ? '#7c3aed' : 'transparent'
                            }}
                        >
                            Gastos
                        </Nav.Link>

                        <Nav.Link
                            as={Link}
                            to="/tipo-de-gastos"
                            className={`fw-semibold px-3 py-2 rounded-pill ${
                                isActive('/tipo-de-gastos') ? 'text-white' : (darkMode ? 'text-light' : 'text-secondary')
                            }`}
                            style={{
                                backgroundColor: isActive('/tipo-de-gastos') ? '#7c3aed' : 'transparent'
                            }}
                        >
                            Tipos de Gasto
                        </Nav.Link>
                    </Nav>
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
}
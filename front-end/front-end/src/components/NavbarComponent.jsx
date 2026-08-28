import React from 'react';
import {Navbar, Nav, Container} from 'react-bootstrap';
import {useLocation} from 'react-router-dom';

export default function NavbarComponent() {
    const location = useLocation();

    return (
        <Navbar bg="white" expand="lg" className="shadow-sm mb-4 py-3">
            <Container>
                <Navbar.Brand href="/pessoas" className="fw-bold" style={{color: '#7c3aed'}}>
                    Controle de Gastos
                </Navbar.Brand>
                <Navbar.Toggle aria-controls="basic-navbar-nav"/>
                <Navbar.Collapse id="basic-navbar-nav">
                    <Nav className="ms-auto gap-2 align-items-lg-center">
                        <Nav.Link
                            href="/pessoas"
                            className="fw-semibold px-3 rounded-3"
                            style={{color: location.pathname === '/pessoas' ? '#7c3aed' : '#4b5563'}}
                        >
                            Pessoas
                        </Nav.Link>
                        <Nav.Link
                            href="/grupos"
                            className="fw-semibold px-3 rounded-3"
                            style={{color: location.pathname === '/grupos' ? '#7c3aed' : '#4b5563'}}
                        >
                            Grupos
                        </Nav.Link>
                        <Nav.Link
                            href="/gastos"
                            className="fw-semibold px-3 rounded-3"
                            style={{color: location.pathname === '/gastos' ? '#7c3aed' : '#4b5563'}}
                        >
                            Gastos
                        </Nav.Link>
                        <Nav.Link
                            href="/tipo-de-gastos"
                            className="fw-semibold px-3 rounded-3"
                            style={{color: location.pathname === '/tipo-de-gastos' ? '#7c3aed' : '#4b5563'}}
                        >
                            Tipos de Gasto
                        </Nav.Link>
                        <Nav.Link
                            href="/vinculos"
                            className="fw-semibold px-3 rounded-3"
                            style={{color: location.pathname === '/vinculos' ? '#7c3aed' : '#4b5563'}}
                        >
                            Vínculos
                        </Nav.Link>
                    </Nav>
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
}
import React from 'react';
import { Container, Card, Row, Col } from 'react-bootstrap';
import NavbarComponent from './NavbarComponent';

export default function Dashboard() {
    const userName = localStorage.getItem('userName') || 'Usuário';

    return (
        <div className="bg-light min-vh-100 pb-5">
            {/* Menu de navegação padronizado */}
            <NavbarComponent />

            {/* Conteúdo da Dashboard */}
            <Container>
                <Row className="mb-4">
                    <Col>
                        <h2 className="fw-bold text-dark">Visão Geral</h2>
                        <p className="text-muted">Acompanhe suas finanças em tempo real.</p>
                    </Col>
                </Row>

                <Row>
                    <Col md={12}>
                        <Card className="border-0 shadow-sm p-4 rounded-4">
                            <Card.Body>
                                <h4 className="fw-semibold" style={{ color: '#6d28d9' }}>Seja muito bem-vindo(a), {userName}! 🎉</h4>
                                <p className="text-muted mt-2">
                                    O seu painel está conectado. Utilize o menu superior para alternar facilmente entre o cadastro de Pessoas, Grupos e Vínculos.
                                </p>
                            </Card.Body>
                        </Card>
                    </Col>
                </Row>
            </Container>
        </div>
    );
}
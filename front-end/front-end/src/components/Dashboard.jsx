import React from 'react';
import { Container, Card, Row, Col, Button } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import NavbarComponent from './NavbarComponent';

export default function Dashboard() {
    const navigate = useNavigate();
    const userName = localStorage.getItem('userName') || 'Usuário';

    return (
        /* Removido o 'bg-light' daqui para deixar o tema global mandar na cor de fundo */
        <div className="min-vh-100 pb-5">
            {/* Menu de navegação padronizado */}
            <NavbarComponent />

            {/* Conteúdo da Dashboard */}
            <Container className="py-4">
                {/* Boas-vindas personalizado */}
                <Row className="mb-4">
                    <Col>
                        <div className="p-4 rounded-4 text-white shadow-sm" style={{ background: 'linear-gradient(135deg, #7c3aed 0%, #4f46e5 100%)' }}>
                            <h2 className="fw-bold mb-1">Olá, {userName}! 👋</h2>
                            <p className="mb-0 text-white-50">Aqui está o resumo rápido das suas finanças hoje.</p>
                        </div>
                    </Col>
                </Row>

                {/* Atalhos Rápidos / Cards Mobile-First */}
                <Row className="g-3">
                    <Col xs={12} md={6}>
                        <Card className="border-0 shadow-sm rounded-4 h-100 p-3">
                            <Card.Body className="d-flex flex-column justify-content-between">
                                <div>
                                    <div className="fs-1 mb-2">👥</div>
                                    <h4 className="fw-bold">Grupos</h4>
                                    <p className="text-muted small">
                                        Gerencie seus círculos de despesas, viagens ou contas compartilhadas.
                                    </p>
                                </div>
                                <Button
                                    onClick={() => navigate('/grupos')}
                                    className="w-100 mt-3 rounded-pill border-0 py-2 fw-semibold"
                                    style={{ backgroundColor: '#7c3aed' }}
                                >
                                    Acessar Grupos
                                </Button>
                            </Card.Body>
                        </Card>
                    </Col>

                    <Col xs={12} md={6}>
                        <Card className="border-0 shadow-sm rounded-4 h-100 p-3">
                            <Card.Body className="d-flex flex-column justify-content-between">
                                <div>
                                    <div className="fs-1 mb-2">🏷️</div>
                                    <h4 className="fw-bold">Pessoas</h4>
                                    <p className="text-muted small">
                                        Cadastre e gerencie os participantes envolvidos nas transações.
                                    </p>
                                </div>
                                <Button
                                    onClick={() => navigate('/pessoas')}
                                    className="w-100 mt-3 rounded-pill border-0 py-2 fw-semibold"
                                    style={{ backgroundColor: '#7c3aed' }}
                                >
                                    Acessar Pessoas
                                </Button>
                            </Card.Body>
                        </Card>
                    </Col>
                </Row>
            </Container>
        </div>
    );
}
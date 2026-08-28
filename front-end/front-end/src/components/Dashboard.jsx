import React from 'react';
import { Container, Navbar, Button, Card, Row, Col } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';

export default function Dashboard() {
    const navigate = useNavigate();

    // Recupera o nome salvo no navegador após o login (ou define um padrão)
    const userName = localStorage.getItem('userName') || 'Usuário';

    const handleLogout = () => {
        localStorage.clear(); // Limpa token e dados
        navigate('/'); // Manda de volta para o login
    };

    return (
        <div className="bg-light min-vh-100">
            {/* Barra superior */}
            <Navbar bg="white" variant="light" expand="lg" className="shadow-sm px-4 mb-4">
                <Navbar.Brand href="#" className="fw-bold text-primary">💰 Controle de Gastos</Navbar.Brand>
                <Navbar.Toggle />
                <Navbar.Collapse className="justify-content-end">
                    <Navbar.Text className="me-3 text-dark">
                        Olá, <strong className="text-primary">{userName}</strong>
                    </Navbar.Text>
                    <Button variant="outline-danger" size="sm" onClick={handleLogout}>
                        Sair
                    </Button>
                </Navbar.Collapse>
            </Navbar>

            {/* Conteúdo da Dashboard */}
            <Container>
                <Row className="mb-4">
                    <Col>
                        <h2 className="fw-bold">Visão Geral</h2>
                        <p className="text-muted">Acompanhe suas finanças em tempo real.</p>
                    </Col>
                </Row>

                <Row>
                    <Col md={12}>
                        <Card className="border-0 shadow-sm p-4">
                            <Card.Body>
                                <h4 className="fw-semibold">Seja muito bem-vindo(a), {userName}! 🎉</h4>
                                <p className="text-muted mt-2">
                                    O sistema está conectado ao seu backend no Render. Nos próximos passos, vamos adicionar aqui os gráficos de pizza e o cadastro das suas despesas.
                                </p>
                            </Card.Body>
                        </Card>
                    </Col>
                </Row>
            </Container>
        </div>
    );
}
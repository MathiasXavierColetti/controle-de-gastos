import React, { useState } from 'react';
import { Container, Row, Col, Card, Form, Button, Alert, Spinner } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

export default function Login() {
    const [cpf, setCpf] = useState('');
    const [senha, setSenha] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!cpf || !senha) {
            setError('Por favor, preencha todos os campos.');
            return;
        }

        setError('');
        setLoading(true);

        try {
            // Faz o POST para o Spring Boot exatamente como no seu Swagger
            const response = await api.post('/api/v1/auth/login', {
                cpf,
                senha
            });

            // Extrai os dados retornados pela sua API Java
            const { token, nomePessoa } = response.data;

            if (token) {
                localStorage.setItem('token', token);
                localStorage.setItem('userName', nomePessoa || cpf);
                navigate('/dashboard');
            } else {
                setError('Token não retornado pelo servidor.');
            }

        } catch (err) {
            console.error('Erro detalhado:', err);
            const errorMsg = err.response?.data?.message || 'CPF ou senha inválidos. Verifique suas credenciais.';
            setError(errorMsg);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div
            className="min-vh-100 d-flex align-items-center justify-content-center"
            style={{
                background: 'linear-gradient(135deg, #2e1065 0%, #4c1d95 50%, #6d28d9 100%)'
            }}
        >
            <Container>
                <Row className="justify-content-center">
                    <Col xs={12} sm={9} md={7} lg={5}>
                        <Card className="border-0 shadow-lg p-4 p-md-5 rounded-4" style={{ backgroundColor: 'rgba(255, 255, 255, 0.98)', backdropFilter: 'blur(10px)' }}>
                            <Card.Body>
                                <div className="text-center mb-4">
                                    <div
                                        className="d-inline-flex align-items-center justify-content-center rounded-circle mb-3 shadow-sm"
                                        style={{ width: '64px', height: '64px', backgroundColor: '#f3e8ff', color: '#7c3aed' }}
                                    >
                                        <span className="fs-3">💰</span>
                                    </div>
                                    <h3 className="fw-bold text-dark mb-1">Controle de Gastos</h3>
                                    <p className="text-muted small">Entre com suas credenciais para acessar</p>
                                </div>

                                {error && <Alert variant="danger" className="py-2 small text-center rounded-3 border-0 bg-danger-subtle text-danger mb-4">{error}</Alert>}

                                <Form onSubmit={handleSubmit}>
                                    <Form.Group className="mb-3" controlId="formCpf">
                                        <Form.Label className="small fw-semibold text-secondary">CPF</Form.Label>
                                        <Form.Control
                                            type="text"
                                            placeholder="Digite seu CPF"
                                            value={cpf}
                                            onChange={(e) => setCpf(e.target.value)}
                                            size="lg"
                                            className="rounded-3 border-light-subtle bg-light text-dark shadow-none fs-6"
                                            style={{ padding: '12px 16px' }}
                                            disabled={loading}
                                        />
                                    </Form.Group>

                                    <Form.Group className="mb-4" controlId="formSenha">
                                        <Form.Label className="small fw-semibold text-secondary">Senha</Form.Label>
                                        <Form.Control
                                            type="password"
                                            placeholder="Sua senha secreta"
                                            value={senha}
                                            onChange={(e) => setSenha(e.target.value)}
                                            size="lg"
                                            className="rounded-3 border-light-subtle bg-light text-dark shadow-none fs-6"
                                            style={{ padding: '12px 16px' }}
                                            disabled={loading}
                                        />
                                    </Form.Group>

                                    <Button
                                        type="submit"
                                        className="w-100 py-3 fw-semibold rounded-3 border-0 shadow-sm"
                                        style={{
                                            backgroundColor: '#7c3aed',
                                            transition: 'all 0.2s ease-in-out'
                                        }}
                                        disabled={loading}
                                    >
                                        {loading ? (
                                            <>
                                                <Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" className="me-2" />
                                                Entrando...
                                            </>
                                        ) : (
                                            'Acessar Sistema'
                                        )}
                                    </Button>
                                </Form>
                            </Card.Body>
                        </Card>
                    </Col>
                </Row>
            </Container>
        </div>
    );
}
import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Form, Button, Table, Alert, Spinner, Modal } from 'react-bootstrap';
import NavbarComponent from './NavbarComponent';
import api from '../services/api';

export default function Pessoas() {
    const [nome, setNome] = useState('');
    const [cpf, setCpf] = useState('');
    const [senha, setSenha] = useState('');
    const [pessoas, setPessoas] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    // Estados para Edição
    const [showEditModal, setShowEditModal] = useState(false);
    const [editingPessoa, setEditingPessoa] = useState(null);
    const [editNome, setEditNome] = useState('');
    const [editCpf, setEditCpf] = useState('');

    const fetchPessoas = async () => {
        try {
            const response = await api.get('/api/v1/usuarios');
            setPessoas(response.data);
        } catch (err) {
            console.error('Erro ao buscar pessoas:', err);
        }
    };

    useEffect(() => {
        fetchPessoas();
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!nome || !cpf || !senha) {
            setError('Preencha todos os campos.');
            return;
        }

        setError('');
        setSuccess('');
        setLoading(true);

        try {
            await api.post('/api/v1/usuarios', { nome, cpf, senha });
            setSuccess('Pessoa cadastrada com sucesso!');
            setNome('');
            setCpf('');
            setSenha('');
            fetchPessoas();
        } catch (err) {
            console.error(err);
            setError(err.response?.data?.message || 'Erro ao cadastrar pessoa.');
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Tem certeza que deseja excluir esta pessoa?')) return;
        try {
            await api.delete(`/api/v1/usuarios/${id}`);
            setSuccess('Pessoa excluída com sucesso!');
            fetchPessoas();
        } catch (err) {
            console.error(err);
            setError('Erro ao excluir pessoa.');
        }
    };

    const handleOpenEdit = (p) => {
        setEditingPessoa(p);
        setEditNome(p.nome || p.nomePessoa);
        setEditCpf(p.cpf);
        setShowEditModal(true);
    };

    const handleUpdate = async (e) => {
        e.preventDefault();
        const id = editingPessoa.id || editingPessoa.usuarioId;
        try {
            await api.put(`/api/v1/usuarios/${id}`, {
                nome: editNome,
                cpf: editCpf
            });
            setShowEditModal(false);
            setSuccess('Pessoa atualizada com sucesso!');
            fetchPessoas();
        } catch (err) {
            console.error(err);
            setError('Erro ao atualizar pessoa.');
        }
    };

    return (
        <div className="bg-light min-vh-100 pb-5">
            <NavbarComponent />
            <Container>
                <Row className="mb-4">
                    <Col>
                        <h2 className="fw-bold text-dark">Cadastro de Pessoas</h2>
                        <p className="text-muted">Gerencie os usuários e acessos do sistema.</p>
                    </Col>
                </Row>

                {error && <Alert variant="danger" className="py-2 rounded-3">{error}</Alert>}
                {success && <Alert variant="success" className="py-2 rounded-3">{success}</Alert>}

                <Row className="g-4">
                    <Col lg={4}>
                        <Card className="border-0 shadow-sm p-4 rounded-4">
                            <Card.Body>
                                <h5 className="fw-bold mb-3" style={{ color: '#6d28d9' }}>Nova Pessoa</h5>
                                <Form onSubmit={handleSubmit}>
                                    <Form.Group className="mb-3">
                                        <Form.Label className="small fw-semibold text-secondary">Nome Completo</Form.Label>
                                        <Form.Control
                                            type="text"
                                            placeholder="Ex: Maria Silva"
                                            value={nome}
                                            onChange={(e) => setNome(e.target.value)}
                                            className="rounded-3 bg-light border-0 shadow-none"
                                        />
                                    </Form.Group>

                                    <Form.Group className="mb-3">
                                        <Form.Label className="small fw-semibold text-secondary">CPF</Form.Label>
                                        <Form.Control
                                            type="text"
                                            placeholder="Somente números"
                                            value={cpf}
                                            onChange={(e) => setCpf(e.target.value)}
                                            className="rounded-3 bg-light border-0 shadow-none"
                                        />
                                    </Form.Group>

                                    <Form.Group className="mb-4">
                                        <Form.Label className="small fw-semibold text-secondary">Senha</Form.Label>
                                        <Form.Control
                                            type="password"
                                            placeholder="Senha de acesso"
                                            value={senha}
                                            onChange={(e) => setSenha(e.target.value)}
                                            className="rounded-3 bg-light border-0 shadow-none"
                                        />
                                    </Form.Group>

                                    <Button
                                        type="submit"
                                        className="w-100 py-2 fw-semibold rounded-3 border-0 shadow-sm"
                                        style={{ backgroundColor: '#7c3aed' }}
                                        disabled={loading}
                                    >
                                        {loading ? <Spinner size="sm" /> : 'Cadastrar Pessoa'}
                                    </Button>
                                </Form>
                            </Card.Body>
                        </Card>
                    </Col>

                    <Col lg={8}>
                        <Card className="border-0 shadow-sm p-4 rounded-4">
                            <Card.Body>
                                <h5 className="fw-bold mb-3 text-dark">Pessoas Cadastradas</h5>
                                <div className="table-responsive">
                                    <Table hover align="middle" className="mb-0">
                                        <thead className="table-light">
                                        <tr>
                                            <th>ID</th>
                                            <th>Nome</th>
                                            <th>CPF</th>
                                            <th className="text-end">Ações</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        {pessoas.length === 0 ? (
                                            <tr>
                                                <td colSpan="4" className="text-center text-muted py-4">Nenhuma pessoa cadastrada.</td>
                                            </tr>
                                        ) : (
                                            pessoas.map((p) => {
                                                const id = p.id || p.usuarioId;
                                                return (
                                                    <tr key={id}>
                                                        <td className="fw-semibold">#{id}</td>
                                                        <td>{p.nome || p.nomePessoa}</td>
                                                        <td className="text-muted">{p.cpf}</td>
                                                        <td className="text-end">
                                                            <Button
                                                                variant="outline-primary"
                                                                size="sm"
                                                                className="me-2 rounded-2"
                                                                onClick={() => handleOpenEdit(p)}
                                                            >
                                                                Editar
                                                            </Button>
                                                            <Button
                                                                variant="outline-danger"
                                                                size="sm"
                                                                className="rounded-2"
                                                                onClick={() => handleDelete(id)}
                                                            >
                                                                Excluir
                                                            </Button>
                                                        </td>
                                                    </tr>
                                                );
                                            })
                                        )}
                                        </tbody>
                                    </Table>
                                </div>
                            </Card.Body>
                        </Card>
                    </Col>
                </Row>

                {/* Modal de Edição */}
                <Modal show={showEditModal} onHide={() => setShowEditModal(false)} centered>
                    <Modal.Header closeButton className="border-0">
                        <Modal.Title className="fw-bold" style={{ color: '#6d28d9' }}>Editar Pessoa</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        <Form onSubmit={handleUpdate}>
                            <Form.Group className="mb-3">
                                <Form.Label className="small fw-semibold text-secondary">Nome Completo</Form.Label>
                                <Form.Control
                                    type="text"
                                    value={editNome}
                                    onChange={(e) => setEditNome(e.target.value)}
                                    className="rounded-3 bg-light border-0"
                                />
                            </Form.Group>
                            <Form.Group className="mb-4">
                                <Form.Label className="small fw-semibold text-secondary">CPF</Form.Label>
                                <Form.Control
                                    type="text"
                                    value={editCpf}
                                    onChange={(e) => setEditCpf(e.target.value)}
                                    className="rounded-3 bg-light border-0"
                                />
                            </Form.Group>
                            <Button type="submit" className="w-100 py-2 rounded-3 border-0" style={{ backgroundColor: '#7c3aed' }}>
                                Salvar Alterações
                            </Button>
                        </Form>
                    </Modal.Body>
                </Modal>
            </Container>
        </div>
    );
}
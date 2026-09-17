import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Form, Button, Table, Alert, Spinner, Modal } from 'react-bootstrap';
import NavbarComponent from './NavbarComponent';
import api from '../services/api';

export default function TipoDeGasto() {
    const [nome, setNome] = useState('');
    const [tipos, setTipos] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    // Estados para Edição
    const [showEditModal, setShowEditModal] = useState(false);
    const [editingTipo, setEditingTipo] = useState(null);
    const [editNome, setEditNome] = useState('');

    const fetchTipos = async () => {
        try {
            const response = await api.get('/api/v1/tipos-de-gasto');
            setTipos(response.data);
        } catch (err) {
            console.error('Erro ao buscar tipos de gasto:', err);
        }
    };

    useEffect(() => {
        fetchTipos();
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!nome) {
            setError('Preencha o nome da categoria.');
            return;
        }

        setError('');
        setSuccess('');
        setLoading(true);

        try {
            await api.post('/api/v1/tipos-de-gasto', { nome });
            setSuccess('Categoria cadastrada com sucesso!');
            setNome('');
            fetchTipos();
        } catch (err) {
            console.error(err);
            setError(err.response?.data?.message || 'Erro ao cadastrar categoria.');
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Tem certeza que deseja excluir esta categoria?')) return;
        try {
            await api.delete(`/api/v1/tipos-de-gasto/${id}`);
            setSuccess('Categoria excluída com sucesso!');
            fetchTipos();
        } catch (err) {
            console.error(err);
            setError(err.response?.data?.message || 'Erro ao excluir categoria.');
        }
    };

    const handleOpenEdit = (t) => {
        setEditingTipo(t);
        setEditNome(t.nome);
        setShowEditModal(true);
    };

    const handleUpdate = async (e) => {
        e.preventDefault();
        if (!editNome) {
            setError('O nome não pode ser vazio.');
            return;
        }

        try {
            await api.put(`/api/v1/tipos-de-gasto/${editingTipo.id}`, { nome: editNome });
            setShowEditModal(false);
            setSuccess('Categoria atualizada com sucesso!');
            setError('');
            fetchTipos();
        } catch (err) {
            console.error(err);
            setError(err.response?.data?.message || 'Erro ao atualizar categoria.');
        }
    };

    return (
        <div className="bg-light min-vh-100 pb-5">
            <NavbarComponent />
            <Container className="py-4">
                <Row className="mb-4">
                    <Col>
                        <h2 className="fw-bold text-dark">Tipos de Gasto (Categorias)</h2>
                        <p className="text-muted">Gerencie as categorias para organizar os lançamentos de forma prática.</p>
                    </Col>
                </Row>

                {error && <Alert variant="danger" className="py-2 rounded-3 shadow-sm">{error}</Alert>}
                {success && <Alert variant="success" className="py-2 rounded-3 shadow-sm">{success}</Alert>}

                <Row className="g-4">
                    <Col lg={4}>
                        <Card className="border-0 shadow-sm p-4 rounded-4">
                            <Card.Body>
                                <h5 className="fw-bold mb-3" style={{ color: '#6d28d9' }}>Nova Categoria</h5>
                                <Form onSubmit={handleSubmit}>
                                    <Form.Group className="mb-3">
                                        <Form.Label className="small fw-semibold text-secondary">Nome da Categoria</Form.Label>
                                        <Form.Control
                                            type="text"
                                            placeholder="Ex: Alimentação, Lazer..."
                                            value={nome}
                                            onChange={(e) => setNome(e.target.value)}
                                            className="rounded-3 bg-light border-0 shadow-none py-2"
                                        />
                                    </Form.Group>

                                    <Button
                                        type="submit"
                                        className="w-100 py-2 fw-semibold rounded-3 border-0 shadow-sm mt-2"
                                        style={{ backgroundColor: '#7c3aed' }}
                                        disabled={loading}
                                    >
                                        {loading ? <Spinner size="sm" animation="border" /> : 'Cadastrar Categoria'}
                                    </Button>
                                </Form>
                            </Card.Body>
                        </Card>
                    </Col>

                    <Col lg={8}>
                        <Card className="border-0 shadow-sm p-4 rounded-4">
                            <Card.Body>
                                <h5 className="fw-bold mb-3 text-dark">Categorias Cadastradas</h5>
                                <div className="table-responsive">
                                    <Table hover align="middle" className="mb-0">
                                        <thead className="table-light">
                                        <tr>
                                            <th className="py-3">ID</th>
                                            <th className="py-3">Nome</th>
                                            <th className="text-end py-3">Ações</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        {tipos.length === 0 ? (
                                            <tr>
                                                <td colSpan="3" className="text-center text-muted py-4">
                                                    Nenhuma categoria cadastrada.
                                                </td>
                                            </tr>
                                        ) : (
                                            tipos.map((t) => (
                                                <tr key={t.id}>
                                                    <td className="fw-semibold text-secondary">#{t.id}</td>
                                                    <td className="fw-medium text-dark">{t.nome}</td>
                                                    <td className="text-end">
                                                        <Button
                                                            variant="outline-primary"
                                                            size="sm"
                                                            className="me-2 rounded-2 px-3"
                                                            onClick={() => handleOpenEdit(t)}
                                                        >
                                                            Editar
                                                        </Button>
                                                        <Button
                                                            variant="outline-danger"
                                                            size="sm"
                                                            className="rounded-2 px-3"
                                                            onClick={() => handleDelete(t.id)}
                                                        >
                                                            Excluir
                                                        </Button>
                                                    </td>
                                                </tr>
                                            ))
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
                    <Modal.Header closeButton className="border-0 pb-0">
                        <Modal.Title className="fw-bold" style={{ color: '#6d28d9' }}>Editar Categoria</Modal.Title>
                    </Modal.Header>
                    <Modal.Body className="pt-3">
                        <Form onSubmit={handleUpdate}>
                            <Form.Group className="mb-4">
                                <Form.Label className="small fw-semibold text-secondary">Nome da Categoria</Form.Label>
                                <Form.Control
                                    type="text"
                                    value={editNome}
                                    onChange={(e) => setEditNome(e.target.value)}
                                    className="rounded-3 bg-light border-0 py-2"
                                />
                            </Form.Group>

                            <Button
                                type="submit"
                                className="w-100 py-2 rounded-3 border-0 fw-semibold shadow-sm"
                                style={{ backgroundColor: '#7c3aed' }}
                            >
                                Salvar Alterações
                            </Button>
                        </Form>
                    </Modal.Body>
                </Modal>
            </Container>
        </div>
    );
}
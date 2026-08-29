import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Form, Button, Table, Alert, Spinner, Modal } from 'react-bootstrap';
import NavbarComponent from './NavbarComponent';
import api from '../services/api';

export default function Pessoas() {
    const [nome, setNome] = useState('');
    const [cpf, setCpf] = useState('');
    const [senha, setSenha] = useState('');
    const [pessoas, setPessoas] = useState([]);

    // Estados de feedback e carregamento
    const [loading, setLoading] = useState(false);
    const [tableLoading, setTableLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    // Estados para Edição de Perfil
    const [showEditModal, setShowEditModal] = useState(false);
    const [editingPessoaId, setEditingPessoaId] = useState(null);
    const [editNome, setEditNome] = useState('');
    const [editCpf, setEditCpf] = useState('');
    const [editModalError, setEditModalError] = useState('');
    const [editModalLoading, setEditModalLoading] = useState(false);

    // Estados para Reset de Senha
    const [showResetModal, setShowResetModal] = useState(false);
    const [resetPessoaId, setResetPessoaId] = useState(null);
    const [resetPessoaNome, setResetPessoaNome] = useState('');
    const [novaSenha, setNovaSenha] = useState('');
    const [resetModalError, setResetModalError] = useState('');
    const [resetModalLoading, setResetModalLoading] = useState(false);

    const fetchPessoas = async () => {
        setTableLoading(true);
        try {
            const response = await api.get('/api/v1/usuarios');
            setPessoas(response.data);
        } catch (err) {
            console.error('Erro ao buscar usuários:', err);
            setError('Não foi possível carregar a lista de usuários.');
        } finally {
            setTableLoading(false);
        }
    };

    useEffect(() => {
        fetchPessoas();
    }, []);

    const clearAlerts = () => {
        setError('');
        setSuccess('');
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        clearAlerts();

        if (!nome || !cpf || !senha) {
            setError('Preencha todos os campos.');
            return;
        }

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
            const msg = err.response?.data?.message || 'Erro ao cadastrar pessoa.';
            setError(msg);
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Tem certeza que deseja excluir esta pessoa?')) return;
        clearAlerts();

        try {
            await api.delete(`/api/v1/usuarios/${id}`);
            setSuccess('Pessoa excluída com sucesso!');
            fetchPessoas();
        } catch (err) {
            console.error(err);
            const msg = err.response?.data?.message || 'Erro ao excluir pessoa.';
            setError(msg);
        }
    };

    // --- Lógica de Edição ---
    const handleOpenEdit = (p) => {
        setEditingPessoaId(p.id);
        setEditNome(p.nome);
        setEditCpf(p.cpf);
        setEditModalError('');
        setShowEditModal(true);
    };

    const handleUpdate = async (e) => {
        e.preventDefault();
        setEditModalError('');

        if (!editNome || !editCpf) {
            setEditModalError('Preencha o nome e o CPF.');
            return;
        }

        setEditModalLoading(true);

        try {
            await api.put(`/api/v1/usuarios/${editingPessoaId}`, {
                nome: editNome,
                cpf: editCpf
            });
            setShowEditModal(false);
            setSuccess('Pessoa atualizada com sucesso!');
            fetchPessoas();
        } catch (err) {
            console.error(err);
            const msg = err.response?.data?.message || 'Erro ao atualizar pessoa.';
            setEditModalError(msg);
        } finally {
            setEditModalLoading(false);
        }
    };

    // --- Lógica de Reset de Senha ---
    const handleOpenReset = (p) => {
        setResetPessoaId(p.id);
        setResetPessoaNome(p.nome);
        setNovaSenha('');
        setResetModalError('');
        setShowResetModal(true);
    };

    const handleResetSenha = async (e) => {
        e.preventDefault();
        setResetModalError('');

        if (!novaSenha) {
            setResetModalError('Informe a nova senha.');
            return;
        }

        setResetModalLoading(true);

        try {
            await api.patch(`/api/v1/usuarios/${resetPessoaId}/reset-senha`, {
                novaSenha
            });
            setShowResetModal(false);
            setSuccess(`Senha de ${resetPessoaNome} alterada com sucesso!`);
        } catch (err) {
            console.error(err);
            const msg = err.response?.data?.message || 'Erro ao resetar senha.';
            setResetModalError(msg);
        } finally {
            setResetModalLoading(false);
        }
    };

    return (
        <div className="bg-light min-vh-100 pb-5">
            <NavbarComponent />
            <Container className="pt-4">
                <Row className="mb-4">
                    <Col>
                        <h2 className="fw-bold text-dark">Cadastro de Pessoas</h2>
                        <p className="text-muted">Gerencie os usuários e acessos do sistema.</p>
                    </Col>
                </Row>

                {error && (
                    <Alert variant="danger" dismissible onClose={() => setError('')} className="py-2 rounded-3">
                        {error}
                    </Alert>
                )}
                {success && (
                    <Alert variant="success" dismissible onClose={() => setSuccess('')} className="py-2 rounded-3">
                        {success}
                    </Alert>
                )}

                <Row className="g-4">
                    {/* Formulário de Cadastro */}
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
                                        {loading ? <Spinner animation="border" size="sm" /> : 'Cadastrar Pessoa'}
                                    </Button>
                                </Form>
                            </Card.Body>
                        </Card>
                    </Col>

                    {/* Tabela de Pessoas */}
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
                                        {tableLoading ? (
                                            <tr>
                                                <td colSpan="4" className="text-center py-4">
                                                    <Spinner animation="border" variant="primary" />
                                                </td>
                                            </tr>
                                        ) : pessoas.length === 0 ? (
                                            <tr>
                                                <td colSpan="4" className="text-center text-muted py-4">
                                                    Nenhuma pessoa cadastrada.
                                                </td>
                                            </tr>
                                        ) : (
                                            pessoas.map((p) => (
                                                <tr key={p.id}>
                                                    <td className="fw-semibold">#{p.id}</td>
                                                    <td>{p.nome}</td>
                                                    <td className="text-muted">{p.cpf}</td>
                                                    <td className="text-end">
                                                        <Button
                                                            variant="outline-primary"
                                                            size="sm"
                                                            className="me-1 rounded-2"
                                                            onClick={() => handleOpenEdit(p)}
                                                        >
                                                            Editar
                                                        </Button>
                                                        <Button
                                                            variant="outline-warning"
                                                            size="sm"
                                                            className="me-1 rounded-2"
                                                            onClick={() => handleOpenReset(p)}
                                                        >
                                                            Senha
                                                        </Button>
                                                        <Button
                                                            variant="outline-danger"
                                                            size="sm"
                                                            className="rounded-2"
                                                            onClick={() => handleDelete(p.id)}
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
                    <Modal.Header closeButton className="border-0">
                        <Modal.Title className="fw-bold" style={{ color: '#6d28d9' }}>Editar Pessoa</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        {editModalError && (
                            <Alert variant="danger" dismissible onClose={() => setEditModalError('')} className="py-2 rounded-3">
                                {editModalError}
                            </Alert>
                        )}
                        <Form onSubmit={handleUpdate}>
                            <Form.Group className="mb-3">
                                <Form.Label className="small fw-semibold text-secondary">Nome Completo</Form.Label>
                                <Form.Control
                                    type="text"
                                    value={editNome}
                                    onChange={(e) => setEditNome(e.target.value)}
                                    className="rounded-3 bg-light border-0 shadow-none"
                                />
                            </Form.Group>

                            <Form.Group className="mb-4">
                                <Form.Label className="small fw-semibold text-secondary">CPF</Form.Label>
                                <Form.Control
                                    type="text"
                                    value={editCpf}
                                    onChange={(e) => setEditCpf(e.target.value)}
                                    className="rounded-3 bg-light border-0 shadow-none"
                                />
                            </Form.Group>

                            <Button
                                type="submit"
                                className="w-100 py-2 rounded-3 border-0"
                                style={{ backgroundColor: '#7c3aed' }}
                                disabled={editModalLoading}
                            >
                                {editModalLoading ? <Spinner animation="border" size="sm" /> : 'Salvar Alterações'}
                            </Button>
                        </Form>
                    </Modal.Body>
                </Modal>

                {/* Modal de Reset de Senha */}
                <Modal show={showResetModal} onHide={() => setShowResetModal(false)} centered>
                    <Modal.Header closeButton className="border-0">
                        <Modal.Title className="fw-bold text-warning">Resetar Senha</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        <p className="text-muted small">
                            Redefinindo a senha do usuário: <strong>{resetPessoaNome}</strong>
                        </p>

                        {resetModalError && (
                            <Alert variant="danger" dismissible onClose={() => setResetModalError('')} className="py-2 rounded-3">
                                {resetModalError}
                            </Alert>
                        )}

                        <Form onSubmit={handleResetSenha}>
                            <Form.Group className="mb-4">
                                <Form.Label className="small fw-semibold text-secondary">Nova Senha</Form.Label>
                                <Form.Control
                                    type="password"
                                    placeholder="Digite a nova senha"
                                    value={novaSenha}
                                    onChange={(e) => setNovaSenha(e.target.value)}
                                    className="rounded-3 bg-light border-0 shadow-none"
                                />
                            </Form.Group>

                            <Button
                                type="submit"
                                variant="warning"
                                className="w-100 py-2 rounded-3 border-0 text-white fw-semibold"
                                disabled={resetModalLoading}
                            >
                                {resetModalLoading ? <Spinner animation="border" size="sm" /> : 'Atualizar Senha'}
                            </Button>
                        </Form>
                    </Modal.Body>
                </Modal>
            </Container>
        </div>
    );
}
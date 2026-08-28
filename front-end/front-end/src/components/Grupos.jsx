import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Form, Button, Table, Alert, Spinner, Modal, Badge } from 'react-bootstrap';
import NavbarComponent from './NavbarComponent';
import api from '../services/api';

export default function Grupos() {
    const [grupos, setGrupos] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    // Estados para o Modal de Criar/Editar Grupo
    const [showCriarModal, setShowCriarModal] = useState(false);
    const [editandoGrupoId, setEditandoGrupoId] = useState(null);
    const [nome, setNome] = useState('');
    const [descricao, setDescricao] = useState('');

    // Estados para o Modal de Gerenciar Membros
    const [grupoSelecionado, setGrupoSelecionado] = useState(null);
    const [showMembrosModal, setShowMembrosModal] = useState(false);
    const [cpfMembro, setCpfMembro] = useState('');

    const carregarGrupos = async () => {
        try {
            setLoading(true);
            setError('');
            const response = await api.get('/api/v1/grupos/todos');
            setGrupos(Array.isArray(response.data) ? response.data : []);
        } catch (err) {
            console.error('Erro ao carregar grupos:', err);
            setError('Erro ao carregar os grupos.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        carregarGrupos();
    }, []);

    // Salvar (Criar ou Atualizar) Grupo
    const handleSubmitGrupo = async (e) => {
        e.preventDefault();
        if (!nome) {
            setError('O nome do grupo é obrigatório.');
            return;
        }

        setError('');
        setSuccess('');

        try {
            if (editandoGrupoId) {
                await api.put(`/api/v1/grupos/${editandoGrupoId}`, { nome, descricao });
                setSuccess('Grupo atualizado com sucesso!');
            } else {
                await api.post('/api/v1/grupos', { nome, descricao });
                setSuccess('Grupo criado com sucesso!');
            }

            fecharModalCriar();
            carregarGrupos();
        } catch (err) {
            console.error(err);
            setError(err.response?.data?.message || 'Erro ao salvar grupo.');
        }
    };

    const abrirModalCriar = () => {
        setEditandoGrupoId(null);
        setNome('');
        setDescricao('');
        setShowCriarModal(true);
    };

    const abrirModalEditar = (grupo) => {
        setEditandoGrupoId(grupo.id);
        setNome(grupo.nome);
        setDescricao(grupo.descricao && grupo.descricao !== 'EMPTY_STRING' ? grupo.descricao : '');
        setShowCriarModal(true);
    };

    const fecharModalCriar = () => {
        setShowCriarModal(false);
        setEditandoGrupoId(null);
        setNome('');
        setDescricao('');
    };

    const excluirGrupo = async (id) => {
        if (!window.confirm('Tem certeza que deseja excluir este grupo?')) return;
        setError('');
        setSuccess('');

        try {
            await api.delete(`/api/v1/grupos/${id}`);
            setSuccess('Grupo excluído com sucesso!');
            carregarGrupos();
        } catch (err) {
            console.error(err);
            setError(err.response?.data?.message || 'Erro ao excluir grupo.');
        }
    };

    const adicionarMembro = async (e) => {
        e.preventDefault();
        if (!cpfMembro) return;

        setError('');
        setSuccess('');
        const cpfLimpo = cpfMembro.replace(/\D/g, '');

        try {
            const response = await api.post(`/api/v1/grupos/${grupoSelecionado.id}/membros`, { cpf: cpfLimpo });
            setGrupoSelecionado(response.data);
            setCpfMembro('');
            setSuccess('Membro anexado ao grupo com sucesso!');
            carregarGrupos();
        } catch (err) {
            console.error(err);
            setError(err.response?.data?.message || 'Erro ao anexar membro.');
        }
    };

    const removerMembro = async (membroId) => {
        if (!window.confirm('Deseja remover este membro do grupo?')) return;
        setError('');
        setSuccess('');

        try {
            const response = await api.delete(`/api/v1/grupos/${grupoSelecionado.id}/membros/${membroId}`);
            setGrupoSelecionado(response.data);
            setSuccess('Membro removido com sucesso!');
            carregarGrupos();
        } catch (err) {
            console.error(err);
            setError('Erro ao remover membro.');
        }
    };

    return (
        <div className="bg-light min-vh-100 pb-5">
            <NavbarComponent />

            <Container className="py-4">
                <div className="d-flex justify-content-between align-items-center mb-4">
                    <div>
                        <h2 className="fw-bold text-dark">Gerenciamento de Grupos</h2>
                        <p className="text-muted mb-0">Organize seus grupos e gerencie os participantes.</p>
                    </div>
                    <Button
                        onClick={abrirModalCriar}
                        className="py-2 px-3 fw-semibold rounded-3 border-0 shadow-sm"
                        style={{ backgroundColor: '#7c3aed' }}
                    >
                        + Novo Grupo
                    </Button>
                </div>

                {error && <Alert variant="danger" className="py-2 rounded-3 shadow-sm">{error}</Alert>}
                {success && <Alert variant="success" className="py-2 rounded-3 shadow-sm">{success}</Alert>}

                {loading ? (
                    <div className="text-center py-5">
                        <Spinner animation="border" variant="primary" />
                    </div>
                ) : grupos.length === 0 ? (
                    <Card className="border-0 shadow-sm p-4 rounded-4 text-center">
                        <p className="text-muted mb-0">Nenhum grupo cadastrado.</p>
                    </Card>
                ) : (
                    <Row xs={1} md={2} lg={3} className="g-4">
                        {grupos.map((grupo) => (
                            <Col key={grupo.id}>
                                <Card className="h-100 border-0 shadow-sm p-3 rounded-4 d-flex flex-column">
                                    <Card.Body className="d-flex flex-column">
                                        <div className="d-flex justify-content-between align-items-start mb-2">
                                            <Card.Title className="fw-bold mb-0" style={{ color: '#6d28d9' }}>
                                                {grupo.nome}
                                            </Card.Title>
                                            <Badge bg="secondary" className="px-2 py-1">
                                                {grupo.membros ? grupo.membros.length : 0} membros
                                            </Badge>
                                        </div>

                                        <Card.Text className="text-muted small flex-grow-1 mb-3">
                                            {grupo.descricao && grupo.descricao !== 'EMPTY_STRING'
                                                ? grupo.descricao
                                                : 'Sem descrição informada.'}
                                        </Card.Text>

                                        <div className="d-flex flex-column gap-2 mt-auto">
                                            <Button
                                                variant="outline-primary"
                                                size="sm"
                                                className="w-100 rounded-2"
                                                onClick={() => {
                                                    setGrupoSelecionado(grupo);
                                                    setShowMembrosModal(true);
                                                }}
                                            >
                                                Gerenciar / Ver Membros
                                            </Button>

                                            <div className="d-flex gap-2">
                                                <Button
                                                    variant="outline-secondary"
                                                    size="sm"
                                                    className="w-50 rounded-2"
                                                    onClick={() => abrirModalEditar(grupo)}
                                                >
                                                    Editar
                                                </Button>
                                                <Button
                                                    variant="outline-danger"
                                                    size="sm"
                                                    className="w-50 rounded-2"
                                                    onClick={() => excluirGrupo(grupo.id)}
                                                >
                                                    Excluir
                                                </Button>
                                            </div>
                                        </div>
                                    </Card.Body>
                                </Card>
                            </Col>
                        ))}
                    </Row>
                )}

                {/* Modal de Criação / Edição de Grupo */}
                <Modal show={showCriarModal} onHide={fecharModalCriar} centered>
                    <Modal.Header closeButton className="border-0 pb-0">
                        <Modal.Title className="fw-bold" style={{ color: '#6d28d9' }}>
                            {editandoGrupoId ? 'Editar Grupo' : 'Criar Novo Grupo'}
                        </Modal.Title>
                    </Modal.Header>
                    <Form onSubmit={handleSubmitGrupo}>
                        <Modal.Body className="pt-3">
                            <Form.Group className="mb-3">
                                <Form.Label className="small fw-semibold text-secondary">Nome do Grupo</Form.Label>
                                <Form.Control
                                    type="text"
                                    placeholder="Ex: Viagem, Família..."
                                    value={nome}
                                    onChange={(e) => setNome(e.target.value)}
                                    className="rounded-3 bg-light border-0 py-2"
                                    required
                                />
                            </Form.Group>
                            <Form.Group className="mb-3">
                                <Form.Label className="small fw-semibold text-secondary">Descrição</Form.Label>
                                <Form.Control
                                    as="textarea"
                                    rows={3}
                                    placeholder="Descreva o objetivo..."
                                    value={descricao}
                                    onChange={(e) => setDescricao(e.target.value)}
                                    className="rounded-3 bg-light border-0"
                                />
                            </Form.Group>
                        </Modal.Body>
                        <Modal.Footer className="border-0 pt-0">
                            <Button variant="secondary" onClick={fecharModalCriar} className="rounded-3">
                                Cancelar
                            </Button>
                            <Button type="submit" className="rounded-3 border-0 px-4" style={{ backgroundColor: '#7c3aed' }}>
                                {editandoGrupoId ? 'Salvar Alterações' : 'Salvar Grupo'}
                            </Button>
                        </Modal.Footer>
                    </Form>
                </Modal>

                {/* Modal de Gerenciamento de Membros */}
                <Modal show={showMembrosModal} onHide={() => setShowMembrosModal(false)} size="lg" centered>
                    <Modal.Header closeButton className="border-0 pb-0">
                        <Modal.Title className="fw-bold" style={{ color: '#6d28d9' }}>
                            Grupo: {grupoSelecionado?.nome}
                        </Modal.Title>
                    </Modal.Header>
                    <Modal.Body className="pt-3">
                        <Form onSubmit={adicionarMembro} className="mb-4">
                            <Form.Label className="small fw-semibold text-secondary">Adicionar Membro Existente (por CPF)</Form.Label>
                            <div className="d-flex gap-2">
                                <Form.Control
                                    type="text"
                                    placeholder="Digite o CPF do usuário..."
                                    value={cpfMembro}
                                    onChange={(e) => setCpfMembro(e.target.value)}
                                    className="rounded-3 bg-light border-0 py-2"
                                    required
                                />
                                <Button type="submit" className="rounded-3 px-4 border-0" style={{ backgroundColor: '#7c3aed' }}>
                                    Adicionar
                                </Button>
                            </div>
                        </Form>

                        <h6 className="fw-bold mb-3 text-dark">Membros do Grupo</h6>
                        <div className="table-responsive">
                            <Table hover align="middle" className="mb-0">
                                <thead className="table-light">
                                <tr>
                                    <th className="py-3">ID</th>
                                    <th className="py-3">Nome</th>
                                    <th className="py-3">CPF</th>
                                    <th className="text-end py-3">Ações</th>
                                </tr>
                                </thead>
                                <tbody>
                                {grupoSelecionado?.membros && grupoSelecionado.membros.length > 0 ? (
                                    grupoSelecionado.membros.map((membro) => (
                                        <tr key={membro.id}>
                                            <td className="fw-semibold text-secondary">#{membro.id}</td>
                                            <td className="fw-medium text-dark">{membro.nome}</td>
                                            <td className="text-muted">{membro.cpf}</td>
                                            <td className="text-end">
                                                <Button
                                                    variant="outline-danger"
                                                    size="sm"
                                                    className="rounded-2 px-3"
                                                    onClick={() => removerMembro(membro.id)}
                                                >
                                                    Remover
                                                </Button>
                                            </td>
                                        </tr>
                                    ))
                                ) : (
                                    <tr>
                                        <td colSpan="4" className="text-center text-muted py-4">
                                            Nenhum membro neste grupo ainda.
                                        </td>
                                    </tr>
                                )}
                                </tbody>
                            </Table>
                        </div>
                    </Modal.Body>
                    <Modal.Footer className="border-0 pt-0">
                        <Button variant="secondary" onClick={() => setShowMembrosModal(false)} className="rounded-3">
                            Fechar
                        </Button>
                    </Modal.Footer>
                </Modal>
            </Container>
        </div>
    );
}
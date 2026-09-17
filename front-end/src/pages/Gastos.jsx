import React, {useState, useEffect} from 'react';
import {
    Container, Row, Col, Card, Form, Button, Table,
    Alert, Spinner, Modal, Badge, ProgressBar
} from 'react-bootstrap';
import NavbarComponent from '../components/NavbarComponent';
import api from '../services/api';

export default function Gastos() {
    // Listas do Backend
    const [grupos, setGrupos] = useState([]);
    const [grupoSelecionadoId, setGrupoSelecionadoId] = useState('');
    const [gastos, setGastos] = useState([]);
    const [tiposGasto, setTiposGasto] = useState([]);

    // Estados de UI
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    // Filtros de Relatório
    const [filtroInicio, setFiltroInicio] = useState('');
    const [filtroFim, setFiltroFim] = useState('');
    const [filtroTipoId, setFiltroTipoId] = useState('');

    // Modal de Cadastro/Edição
    const [showModal, setShowModal] = useState(false);
    const [editandoId, setEditandoId] = useState(null);
    const [descricao, setDescricao] = useState('');
    const [valor, setValor] = useState('');
    const [data, setData] = useState(new Date().toISOString().split('T')[0]);
    const [tipoDeGastoId, setTipoDeGastoId] = useState('');

    useEffect(() => {
        carregarDadosIniciais();
    }, []);

    useEffect(() => {
        if (grupoSelecionadoId) {
            carregarRelatorio();
        } else {
            setGastos([]);
        }
    }, [grupoSelecionadoId]);

    const carregarDadosIniciais = async () => {
        try {
            setLoading(true);
            setError('');

            // Busca os grupos do usuário logado
            const resGrupos = await api.get('/api/v1/grupos/meus');
            const listaGrupos = Array.isArray(resGrupos.data) ? resGrupos.data : [];
            setGrupos(listaGrupos);

            if (listaGrupos.length > 0) {
                setGrupoSelecionadoId(listaGrupos[0].id);
            }

            // Busca os tipos de gasto disponíveis
            try {
                const resTipos = await api.get('/api/v1/tipos-de-gasto');
                const listaTipos = Array.isArray(resTipos.data) ? resTipos.data : [];
                setTiposGasto(listaTipos);
            } catch (errTipos) {
                console.warn('Aviso ao buscar tipos de gasto:', errTipos);
            }

        } catch (err) {
            console.error('Erro ao carregar dados iniciais:', err);
            setError('Não foi possível carregar os dados iniciais. Verifique sua conexão.');
        } finally {
            setLoading(false);
        }
    };

    const carregarRelatorio = async () => {
        if (!grupoSelecionadoId) return;

        try {
            setLoading(true);
            setError('');

            let url = `/api/v1/gastos/relatorio?grupoId=${grupoSelecionadoId}`;
            if (filtroInicio) url += `&inicio=${filtroInicio}`;
            if (filtroFim) url += `&fim=${filtroFim}`;
            if (filtroTipoId) url += `&tipoDeGastoId=${filtroTipoId}`;

            const response = await api.get(url);
            setGastos(Array.isArray(response.data) ? response.data : []);
        } catch (err) {
            console.error('Erro ao buscar gastos:', err);
            setError(err.response?.data?.message || 'Erro ao carregar o relatório de gastos.');
        } finally {
            setLoading(false);
        }
    };

    const handleSubmitGasto = async (e) => {
        e.preventDefault();

        if (!descricao || !valor || !data || !tipoDeGastoId || !grupoSelecionadoId) {
            setError('Preencha todos os campos obrigatórios.');
            return;
        }

        setError('');
        setSuccess('');

        const payload = {
            descricao: descricao.trim(),
            valor: parseFloat(valor),
            data,
            grupoId: Number(grupoSelecionadoId),
            tipoDeGastoId: Number(tipoDeGastoId)
        };

        try {
            if (editandoId) {
                await api.put(`/api/v1/gastos/${editandoId}`, payload);
                setSuccess('Gasto atualizado com sucesso!');
            } else {
                await api.post('/api/v1/gastos', payload);
                setSuccess('Gasto cadastrado com sucesso!');
            }

            fecharModal();
            carregarRelatorio();
        } catch (err) {
            console.error('Erro ao salvar gasto:', err);
            setError(err.response?.data?.message || 'Erro ao salvar o gasto.');
        }
    };

    const abrirModalCriar = () => {
        setEditandoId(null);
        setDescricao('');
        setValor('');
        setData(new Date().toISOString().split('T')[0]);
        setTipoDeGastoId(tiposGasto.length > 0 ? tiposGasto[0].id : '');
        setShowModal(true);
    };

    const abrirModalEditar = (gasto) => {
        setEditandoId(gasto.id);
        setDescricao(gasto.descricao);
        setValor(gasto.valor);
        setData(gasto.data);
        setTipoDeGastoId(gasto.tipoDeGastoId || '');
        setShowModal(true);
    };

    const fecharModal = () => {
        setShowModal(false);
        setEditandoId(null);
    };

    const excluirGasto = async (id) => {
        if (!window.confirm('Deseja realmente excluir este gasto?')) return;

        setError('');
        setSuccess('');

        try {
            await api.delete(`/api/v1/gastos/${id}`);
            setSuccess('Gasto removido com sucesso!');
            carregarRelatorio();
        } catch (err) {
            console.error('Erro ao excluir:', err);
            setError(err.response?.data?.message || 'Não foi possível excluir o gasto.');
        }
    };

    const limparFiltros = () => {
        setFiltroInicio('');
        setFiltroFim('');
        setFiltroTipoId('');
    };

    // Cálculos para Dashboard Visual
    const valorTotalGeral = gastos.reduce((acc, item) => acc + Number(item.valor || 0), 0);

    const gastosPorTipo = gastos.reduce((acc, item) => {
        const nomeTipo = item.tipoDeGastoNome || 'Outros';
        acc[nomeTipo] = (acc[nomeTipo] || 0) + Number(item.valor || 0);
        return acc;
    }, {});

    return (
        <div className="bg-light min-vh-100 pb-5">
            <NavbarComponent/>

            <Container className="py-4">
                {/* Cabeçalho */}
                <div className="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3">
                    <div>
                        <h2 className="fw-bold text-dark mb-1">Controle de Gastos</h2>
                        <p className="text-muted mb-0">Gerencie e analise as despesas por grupo e categoria.</p>
                    </div>
                    <Button
                        onClick={abrirModalCriar}
                        className="py-2 px-4 fw-semibold rounded-3 border-0 shadow-sm"
                        style={{backgroundColor: '#7c3aed'}}
                        disabled={grupos.length === 0}
                    >
                        + Novo Gasto
                    </Button>
                </div>

                {/* Notificações */}
                {error && <Alert variant="danger" dismissible onClose={() => setError('')}
                                 className="py-2 rounded-3 shadow-sm">{error}</Alert>}
                {success && <Alert variant="success" dismissible onClose={() => setSuccess('')}
                                   className="py-2 rounded-3 shadow-sm">{success}</Alert>}

                {/* Filtros */}
                <Card className="border-0 shadow-sm p-4 rounded-4 mb-4">
                    <Row className="g-3 align-items-end">
                        <Col lg={3} md={6}>
                            <Form.Group>
                                <Form.Label className="small fw-semibold text-secondary">Grupo</Form.Label>
                                <Form.Select
                                    value={grupoSelecionadoId}
                                    onChange={(e) => setGrupoSelecionadoId(e.target.value)}
                                    className="rounded-3 bg-light border-0 py-2"
                                >
                                    {grupos.length === 0 ? (
                                        <option value="">Nenhum grupo cadastrado</option>
                                    ) : (
                                        grupos.map((g) => (
                                            <option key={g.id} value={g.id}>{g.nome}</option>
                                        ))
                                    )}
                                </Form.Select>
                            </Form.Group>
                        </Col>
                        <Col lg={2} md={3}>
                            <Form.Group>
                                <Form.Label className="small fw-semibold text-secondary">Início</Form.Label>
                                <Form.Control
                                    type="date"
                                    value={filtroInicio}
                                    onChange={(e) => setFiltroInicio(e.target.value)}
                                    className="rounded-3 bg-light border-0 py-2"
                                />
                            </Form.Group>
                        </Col>
                        <Col lg={2} md={3}>
                            <Form.Group>
                                <Form.Label className="small fw-semibold text-secondary">Fim</Form.Label>
                                <Form.Control
                                    type="date"
                                    value={filtroFim}
                                    onChange={(e) => setFiltroFim(e.target.value)}
                                    className="rounded-3 bg-light border-0 py-2"
                                />
                            </Form.Group>
                        </Col>
                        <Col lg={3} md={6}>
                            <Form.Group>
                                <Form.Label className="small fw-semibold text-secondary">Tipo de Gasto</Form.Label>
                                <Form.Select
                                    value={filtroTipoId}
                                    onChange={(e) => setFiltroTipoId(e.target.value)}
                                    className="rounded-3 bg-light border-0 py-2"
                                >
                                    <option value="">Todos os tipos</option>
                                    {tiposGasto.map((t) => (
                                        <option key={t.id} value={t.id}>{t.nome}</option>
                                    ))}
                                </Form.Select>
                            </Form.Group>
                        </Col>
                        <Col lg={2} md={6} className="d-flex gap-2">
                            <Button
                                onClick={carregarRelatorio}
                                className="w-100 rounded-3 border-0 py-2 fw-semibold"
                                style={{backgroundColor: '#6d28d9'}}
                            >
                                Filtrar
                            </Button>
                            <Button
                                variant="outline-secondary"
                                onClick={limparFiltros}
                                className="rounded-3 py-2"
                                title="Limpar Filtros"
                            >
                                ✕
                            </Button>
                        </Col>
                    </Row>
                </Card>

                {/* Dashboard de Progresso / Categoria */}
                {Object.keys(gastosPorTipo).length > 0 && (
                    <Card className="border-0 shadow-sm p-4 rounded-4 mb-4">
                        <div className="d-flex justify-content-between align-items-center mb-3">
                            <h5 className="fw-bold mb-0" style={{color: '#6d28d9'}}>Distribuição de Gastos</h5>
                            <Badge bg="primary" className="fs-6 px-3 py-2" style={{backgroundColor: '#7c3aed'}}>
                                Total: R$ {valorTotalGeral.toLocaleString('pt-BR', {
                                minimumFractionDigits: 2,
                                maximumFractionDigits: 2
                            })}
                            </Badge>
                        </div>
                        <div className="d-flex flex-column gap-3">
                            {Object.entries(gastosPorTipo).map(([tipo, totalCategoria], idx) => {
                                const percentual = valorTotalGeral > 0 ? (totalCategoria / valorTotalGeral) * 100 : 0;
                                return (
                                    <div key={idx}>
                                        <div
                                            className="d-flex justify-content-between small fw-semibold text-secondary mb-1">
                                            <span>{tipo}</span>
                                            <span>
                                                R$ {totalCategoria.toLocaleString('pt-BR', {
                                                minimumFractionDigits: 2,
                                                maximumFractionDigits: 2
                                            })} ({percentual.toFixed(1)}%)
                                            </span>
                                        </div>
                                        <ProgressBar
                                            now={percentual}
                                            style={{height: '8px', backgroundColor: '#f1f5f9'}}
                                            className="rounded-pill"
                                        />
                                    </div>
                                );
                            })}
                        </div>
                    </Card>
                )}

                {/* Tabela de Lançamentos */}
                {loading ? (
                    <div className="text-center py-5">
                        <Spinner animation="border" style={{color: '#7c3aed'}}/>
                    </div>
                ) : gastos.length === 0 ? (
                    <Card className="border-0 shadow-sm p-5 rounded-4 text-center">
                        <p className="text-muted mb-0 fs-5">Nenhum gasto encontrado para os filtros selecionados.</p>
                    </Card>
                ) : (
                    <Card className="border-0 shadow-sm rounded-4 overflow-hidden">
                        <div className="table-responsive">
                            <Table hover align="middle" className="mb-0">
                                <thead className="table-light">
                                <tr>
                                    <th className="py-3 ps-4">Data</th>
                                    <th className="py-3">Descrição</th>
                                    <th className="py-3">Categoria</th>
                                    <th className="py-3">Usuário</th>
                                    <th className="py-3">Valor</th>
                                    <th className="text-end py-3 pe-4">Ações</th>
                                </tr>
                                </thead>
                                <tbody>
                                {gastos.map((gasto) => (
                                    <tr key={gasto.id}>
                                        <td className="ps-4 text-muted">
                                            {gasto.data ? new Date(gasto.data + 'T00:00:00').toLocaleDateString('pt-BR') : '-'}
                                        </td>
                                        <td className="fw-semibold text-dark">{gasto.descricao}</td>
                                        <td>
                                            <Badge bg="light" text="dark" className="border px-2 py-1 fw-normal">
                                                {gasto.tipoDeGastoNome || 'Outros'}
                                            </Badge>
                                        </td>
                                        <td className="text-secondary">{gasto.usuarioNome}</td>
                                        <td className="fw-bold text-danger">
                                            R$ {Number(gasto.valor).toLocaleString('pt-BR', {
                                            minimumFractionDigits: 2,
                                            maximumFractionDigits: 2
                                        })}
                                        </td>
                                        <td className="text-end pe-4">
                                            <Button
                                                variant="outline-secondary"
                                                size="sm"
                                                className="rounded-2 me-2 px-3"
                                                onClick={() => abrirModalEditar(gasto)}
                                            >
                                                Editar
                                            </Button>
                                            <Button
                                                variant="outline-danger"
                                                size="sm"
                                                className="rounded-2 px-3"
                                                onClick={() => excluirGasto(gasto.id)}
                                            >
                                                Excluir
                                            </Button>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </Table>
                        </div>
                    </Card>
                )}

                {/* Modal de Criação / Edição */}
                <Modal show={showModal} onHide={fecharModal} centered>
                    <Modal.Header closeButton className="border-0 pb-0">
                        <Modal.Title className="fw-bold" style={{color: '#6d28d9'}}>
                            {editandoId ? 'Editar Gasto' : 'Novo Gasto'}
                        </Modal.Title>
                    </Modal.Header>
                    <Form onSubmit={handleSubmitGasto}>
                        <Modal.Body className="pt-3">
                            <Form.Group className="mb-3">
                                <Form.Label className="small fw-semibold text-secondary">Descrição</Form.Label>
                                <Form.Control
                                    type="text"
                                    placeholder="Ex: Mercado, Conta de Luz..."
                                    value={descricao}
                                    onChange={(e) => setDescricao(e.target.value)}
                                    className="rounded-3 bg-light border-0 py-2"
                                    required
                                />
                            </Form.Group>

                            <Row>
                                <Col md={6}>
                                    <Form.Group className="mb-3">
                                        <Form.Label className="small fw-semibold text-secondary">Valor (R$)</Form.Label>
                                        <Form.Control
                                            type="number"
                                            step="0.01"
                                            placeholder="0.00"
                                            value={valor}
                                            onChange={(e) => setValor(e.target.value)}
                                            className="rounded-3 bg-light border-0 py-2"
                                            required
                                        />
                                    </Form.Group>
                                </Col>
                                <Col md={6}>
                                    <Form.Group className="mb-3">
                                        <Form.Label className="small fw-semibold text-secondary">Data</Form.Label>
                                        <Form.Control
                                            type="date"
                                            value={data}
                                            onChange={(e) => setData(e.target.value)}
                                            className="rounded-3 bg-light border-0 py-2"
                                            required
                                        />
                                    </Form.Group>
                                </Col>
                            </Row>

                            <Form.Group className="mb-3">
                                <Form.Label className="small fw-semibold text-secondary">Tipo de Gasto</Form.Label>
                                <Form.Select
                                    value={tipoDeGastoId}
                                    onChange={(e) => setTipoDeGastoId(e.target.value)}
                                    className="rounded-3 bg-light border-0 py-2"
                                    required
                                >
                                    <option value="">Selecione o tipo...</option>
                                    {tiposGasto.map((t) => (
                                        <option key={t.id} value={t.id}>{t.nome}</option>
                                    ))}
                                </Form.Select>
                            </Form.Group>
                        </Modal.Body>
                        <Modal.Footer className="border-0 pt-0">
                            <Button variant="secondary" onClick={fecharModal} className="rounded-3">
                                Cancelar
                            </Button>
                            <Button type="submit" className="rounded-3 border-0 px-4"
                                    style={{backgroundColor: '#7c3aed'}}>
                                {editandoId ? 'Salvar Alterações' : 'Cadastrar Gasto'}
                            </Button>
                        </Modal.Footer>
                    </Form>
                </Modal>
            </Container>
        </div>
    );
}
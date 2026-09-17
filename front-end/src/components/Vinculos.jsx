import React, {useState, useEffect} from 'react';
import {Container, Row, Col, Card, Form, Button, Alert, Spinner} from 'react-bootstrap';
import NavbarComponent from './NavbarComponent';
import api from '../services/api';

export default function Vinculos() {
    const [pessoas, setPessoas] = useState([]);
    const [grupos, setGrupos] = useState([]);
    const [selectedPessoa, setSelectedPessoa] = useState('');
    const [selectedGrupo, setSelectedGrupo] = useState('');

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    useEffect(() => {
        const loadData = async () => {
            try {
                const [resPessoas, resGrupos] = await Promise.all([
                    api.get('/api/v1/usuarios'),
                    api.get('/api/v1/grupos')
                ]);
                setPessoas(resPessoas.data);
                setGrupos(resGrupos.data);
            } catch (err) {
                console.error('Erro ao carregar dados para vínculo', err);
            }
        };
        loadData();
    }, []);

    const handleVincular = async (e) => {
        e.preventDefault();
        if (!selectedPessoa || !selectedGrupo) {
            setError('Selecione uma pessoa e um grupo.');
            return;
        }

        setError('');
        setSuccess('');
        setLoading(true);

        try {
            await api.post(`/api/v1/usuarios/${selectedPessoa}/grupos/${selectedGrupo}`);
            setSuccess('Grupo vinculado à pessoa com sucesso!');
            setSelectedPessoa('');
            setSelectedGrupo('');
        } catch (err) {
            console.error(err);
            setError(err.response?.data?.message || 'Erro ao realizar o vínculo.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="bg-light min-vh-100 pb-5">
            <NavbarComponent/>
            <Container>
                <Row className="mb-4">
                    <Col>
                        <h2 className="fw-bold text-dark">Vincular Grupo a Pessoa</h2>
                        <p className="text-muted">Associe usuários aos seus respectivos grupos de controle.</p>
                    </Col>
                </Row>

                <Row className="justify-content-center">
                    <Col md={8} lg={6}>
                        <Card className="border-0 shadow-sm p-4 p-md-5 rounded-4">
                            <Card.Body>
                                <h4 className="fw-bold mb-4 text-center" style={{color: '#6d28d9'}}>Gerenciar
                                    Associações</h4>

                                {error && <Alert variant="danger" className="py-2 small rounded-3">{error}</Alert>}
                                {success && <Alert variant="success" className="py-2 small rounded-3">{success}</Alert>}

                                <Form onSubmit={handleVincular}>
                                    <Form.Group className="mb-3">
                                        <Form.Label className="small fw-semibold text-secondary">Selecione a
                                            Pessoa</Form.Label>
                                        <Form.Select
                                            value={selectedPessoa}
                                            onChange={(e) => setSelectedPessoa(e.target.value)}
                                            className="rounded-3 bg-light border-0 shadow-none py-2"
                                        >
                                            <option value="">Escolha uma pessoa...</option>
                                            {pessoas.map((p) => (
                                                <option key={p.id || p.usuarioId} value={p.id || p.usuarioId}>
                                                    {p.nome || p.nomePessoa} ({p.cpf})
                                                </option>
                                            ))}
                                        </Form.Select>
                                    </Form.Group>

                                    <Form.Group className="mb-4">
                                        <Form.Label className="small fw-semibold text-secondary">Selecione o
                                            Grupo</Form.Label>
                                        <Form.Select
                                            value={selectedGrupo}
                                            onChange={(e) => setSelectedGrupo(e.target.value)}
                                            className="rounded-3 bg-light border-0 shadow-none py-2"
                                        >
                                            <option value="">Escolha um grupo...</option>
                                            {grupos.map((g) => (
                                                <option key={g.id} value={g.id}>
                                                    {g.nome || g.descricao}
                                                </option>
                                            ))}
                                        </Form.Select>
                                    </Form.Group>

                                    <Button
                                        type="submit"
                                        className="w-100 py-3 fw-semibold rounded-3 border-0 shadow-sm"
                                        style={{backgroundColor: '#7c3aed'}}
                                        disabled={loading}
                                    >
                                        {loading ? <Spinner size="sm"/> : 'Realizar Vínculo'}
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
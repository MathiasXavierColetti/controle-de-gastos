import React, { useState, useEffect, useCallback } from 'react';
import api from '../services/api'; // Ajuste o caminho conforme o seu projeto

// Se usar Recharts, descomente as linhas abaixo:
// import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const CORES_GRAFICO = [
    '#0088FE', '#00C49F', '#FFBB28', '#FF8042',
    '#8884d8', '#82ca9d', '#ffc658', '#d0ed57', '#a4de6c'
];

export default function RelatorioPizza() {
    const [grupos, setGrupos] = useState([]);
    const [grupoSelecionado, setGrupoSelecionado] = useState('');
    const [tiposGasto, setTiposGasto] = useState([]);
    const [dadosGrafico, setDadosGrafico] = useState([]);

    const [loading, setLoading] = useState(true);
    const [erro, setErro] = useState(null);

    // Normaliza retornos do Spring (seja Array simples ou Page do Spring)
    const extrairArray = (data) => {
        if (Array.isArray(data)) return data;
        if (data && Array.isArray(data.content)) return data.content;
        return [];
    };

    // 1. Carregar Grupos
    const carregarGrupos = useCallback(async () => {
        try {
            const response = await api.get('/api/v1/grupos');
            const listaGrupos = extrairArray(response.data);
            setGrupos(listaGrupos);

            // Seleciona o primeiro grupo automaticamente, se existir
            if (listaGrupos.length > 0 && !grupoSelecionado) {
                setGrupoSelecionado(listaGrupos[0].id.toString());
            }
        } catch (err) {
            console.error('Erro ao buscar grupos:', err);
            setGrupos([]); // Garante que nunca fique undefined
        }
    }, [grupoSelecionado]);

    // 2. Carregar Tipos de Gasto / Relatório
    const carregarDadosRelatorio = useCallback(async () => {
        setLoading(true);
        setErro(null);

        try {
            // Exemplo de chamada para buscar tipos de gasto ou gastos por grupo
            const response = await api.get('/api/v1/tipos-de-gasto');
            const listaTipos = extrairArray(response.data);
            setTiposGasto(listaTipos);

            // Mapeia os dados para o formato do gráfico
            const dadosFormatados = listaTipos.map((tipo) => ({
                name: tipo.nome || tipo.descricao || 'Sem Nome',
                value: Number(tipo.valorTotal || tipo.orcamento || tipo.valor || 0)
            })).filter(item => item.value > 0);

            setDadosGrafico(dadosFormatados);
        } catch (err) {
            console.error('Erro ao carregar relatório:', err);
            setErro('Não foi possível carregar os dados do relatório.');
            setDadosGrafico([]);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        carregarGrupos();
        carregarDadosRelatorio();
    }, [carregarGrupos, carregarDadosRelatorio]);

    const handleGrupoChange = (e) => {
        setGrupoSelecionado(e.target.value);
        // Pode disparar nova busca de gastos filtrada pelo ID do grupo se necessário
    };

    // Cálculo de percentual para exibição em lista/tabela
    const valorTotalGeral = (dadosGrafico || []).reduce((acc, curr) => acc + curr.value, 0);

    return (
        <div style={{ padding: '24px', maxWidth: '1000px', margin: '0 auto' }}>
            <h2>Relatório de Gastos por Categoria</h2>

            {/* Filtro de Grupos */}
            <div style={{ marginBottom: '20px' }}>
                <label htmlFor="select-grupo" style={{ fontWeight: 'bold', marginRight: '10px' }}>
                    Selecione o Grupo:
                </label>
                <select
                    id="select-grupo"
                    value={grupoSelecionado}
                    onChange={handleGrupoChange}
                    style={{ padding: '8px 12px', borderRadius: '4px', border: '1px solid #ccc' }}
                >
                    <option value="">Todos os Grupos</option>
                    {/* Mapeamento 100% protegido contra erros de array */}
                    {Array.isArray(grupos) && grupos.length > 0 ? (
                        grupos.map((grupo) => (
                            <option key={grupo.id} value={grupo.id}>
                                {grupo.nome || `Grupo ${grupo.id}`}
                            </option>
                        ))
                    ) : (
                        <option disabled value="">Nenhum grupo encontrado</option>
                    )}
                </select>
            </div>

            {/* Estados de Carregamento e Erro */}
            {loading && <p>Carregando dados do gráfico...</p>}
            {erro && <p style={{ color: 'red' }}>{erro}</p>}

            {!loading && !erro && (
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '32px', marginTop: '20px' }}>

                    {/* Área do Gráfico */}
                    <div style={{ flex: '1 1 400px', minHeight: '300px', background: '#f9f9f9', padding: '16px', borderRadius: '8px' }}>
                        <h3>Distribuição em Pizza</h3>

                        {dadosGrafico.length === 0 ? (
                            <p>Nenhum dado cadastrado para exibir o gráfico.</p>
                        ) : (
                            /* Caso use Recharts, pode utilizar esta estrutura: */
                            /*
                            <ResponsiveContainer width="100%" height={300}>
                              <PieChart>
                                <Pie
                                  data={dadosGrafico}
                                  cx="50%"
                                  cy="50%"
                                  outerRadius={100}
                                  dataKey="value"
                                  label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                                >
                                  {dadosGrafico.map((entry, index) => (
                                    <Cell key={`cell-${index}`} fill={CORES_GRAFICO[index % CORES_GRAFICO.length]} />
                                  ))}
                                </Pie>
                                <Tooltip formatter={(value) => `R$ ${Number(value).toFixed(2)}`} />
                                <Legend />
                              </PieChart>
                            </ResponsiveContainer>
                            */

                            /* Visualização alternativa/resumo de gráfico em CSS/Barra se não usar Recharts */
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginTop: '16px' }}>
                                {dadosGrafico.map((item, index) => {
                                    const percentual = valorTotalGeral > 0 ? ((item.value / valorTotalGeral) * 100).toFixed(1) : 0;
                                    const cor = CORES_GRAFICO[index % CORES_GRAFICO.length];

                                    return (
                                        <div key={index}>
                                            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '4px' }}>
                        <span>
                          <strong style={{ color: cor }}>●</strong> {item.name}
                        </span>
                                                <span>R$ {item.value.toFixed(2)} ({percentual}%)</span>
                                            </div>
                                            <div style={{ width: '100%', height: '10px', backgroundColor: '#e0e0e0', borderRadius: '5px' }}>
                                                <div
                                                    style={{
                                                        width: `${percentual}%`,
                                                        height: '100%',
                                                        backgroundColor: cor,
                                                        borderRadius: '5px',
                                                        transition: 'width 0.4s ease'
                                                    }}
                                                />
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>
                        )}
                    </div>

                    {/* Tabela Resumo */}
                    <div style={{ flex: '1 1 300px' }}>
                        <h3>Resumo Numérico</h3>
                        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                            <thead>
                            <tr style={{ borderBottom: '2px solid #ddd' }}>
                                <th style={{ padding: '8px' }}>Categoria</th>
                                <th style={{ padding: '8px' }}>Valor</th>
                                <th style={{ padding: '8px' }}>%</th>
                            </tr>
                            </thead>
                            <tbody>
                            {Array.isArray(dadosGrafico) && dadosGrafico.length > 0 ? (
                                dadosGrafico.map((item, index) => {
                                    const percentual = valorTotalGeral > 0 ? ((item.value / valorTotalGeral) * 100).toFixed(1) : 0;
                                    return (
                                        <tr key={index} style={{ borderBottom: '1px solid #eee' }}>
                                            <td style={{ padding: '8px' }}>{item.name}</td>
                                            <td style={{ padding: '8px' }}>R$ {item.value.toFixed(2)}</td>
                                            <td style={{ padding: '8px' }}>{percentual}%</td>
                                        </tr>
                                    );
                                })
                            ) : (
                                <tr>
                                    <td colSpan="3" style={{ padding: '8px', textAlign: 'center' }}>
                                        Nenhum registro encontrado.
                                    </td>
                                </tr>
                            )}
                            </tbody>
                            <tfoot>
                            <tr style={{ fontWeight: 'bold', borderTop: '2px solid #ddd' }}>
                                <td style={{ padding: '8px' }}>Total</td>
                                <td style={{ padding: '8px' }}>R$ {valorTotalGeral.toFixed(2)}</td>
                                <td style={{ padding: '8px' }}>100%</td>
                            </tr>
                            </tfoot>
                        </table>
                    </div>

                </div>
            )}
        </div>
    );
}
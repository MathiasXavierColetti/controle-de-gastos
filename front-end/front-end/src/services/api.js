import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8090',
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token'); // Ou onde você guarda o token

    if (token) {
        config.headers.Authorization = `Bearer ${token}`;

        // Decodifica e exibe o CPF contido no token
        try {
            const payloadBase64 = token.split('.')[1];
            const payload = JSON.parse(atob(payloadBase64));

            console.log('--------------------------------------------------');
            console.log('>>> [FRONTEND] ROTA CHAMADA:', config.url);
            console.log('>>> [FRONTEND] CPF (sub) NO TOKEN:', payload.sub);
            console.log('>>> [FRONTEND] PAYLOAD COMPLETO DO TOKEN:', payload);
            console.log('--------------------------------------------------');
        } catch (e) {
            console.error('>>> [FRONTEND] Erro ao decodificar token JWT:', e);
        }
    } else {
        console.warn('⚠️ [FRONTEND] Requisição disparada SEM token JWT!');
    }

    return config;
});

export default api;
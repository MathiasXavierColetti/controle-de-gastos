# Etapa 1: Build do projeto Vite/React
FROM node:18-alpine AS build
WORKDIR /app

# Copia as dependências e instala
COPY package*.json ./
RUN npm install

# Copia o restante do código e faz o build de produção
COPY . .
RUN npm run build

# Etapa 2: Servir a aplicação com Node e o pacote 'serve'
FROM node:18-alpine
WORKDIR /app

# Instala um servidor estático global leve
RUN npm install -g serve

# Copia apenas os arquivos gerados no build anterior
COPY --from=build /app/dist ./dist

# O Render injeta a porta dinamicamente na variável PORT
EXPOSE 3000

# Executa o servidor apontando para a pasta dist e respeitando a porta do Render
CMD sh -c "serve -s dist -l ${PORT:-3000}"
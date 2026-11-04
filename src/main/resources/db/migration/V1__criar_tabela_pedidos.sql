CREATE TABLE pedidos(
    id UUID PRIMARY KEY,
    cliente VARCHAR(100) NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL
);
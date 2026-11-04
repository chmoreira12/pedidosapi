CREATE TABLE produtos (
    id UUID PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    preco NUMERIC(10, 2) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_produto_preco
        CHECK (preco >= 0)
);

CREATE TABLE itens_pedido (
    id UUID PRIMARY KEY,
    pedido_id UUID NOT NULL,
    produto_id UUID NOT NULL,
    quantidade INTEGER NOT NULL,
    preco_unitario NUMERIC(10, 2),

    CONSTRAINT fk_item_pedido
          FOREIGN KEY (pedido_id)
          REFERENCES pedidos(id),

    CONSTRAINT fk_item_produto
          FOREIGN KEY (produto_id)
          REFERENCES produtos(id),

    CONSTRAINT chk_item_quantidade
          CHECK (quantidade > 0),

    CONSTRAINT  chk_item_preco
          CHECK (preco_unitario >= 0)
);
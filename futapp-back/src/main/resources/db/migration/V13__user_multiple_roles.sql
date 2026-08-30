CREATE TABLE usuario_rol (
    id_usuario BIGINT NOT NULL REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    rol VARCHAR(40) NOT NULL,
    PRIMARY KEY (id_usuario, rol)
);

INSERT INTO usuario_rol (id_usuario, rol)
SELECT id_usuario, rol FROM usuario;

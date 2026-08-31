package com.mathias.coletti.controledegastos.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(JdbcTemplate jdbcTemplate) {
        return args -> {
            // 1. Inserir Pessoas (Verifica se o CPF já existe)
            jdbcTemplate.execute("""
                INSERT INTO pessoas (cpf, nome) 
                SELECT '06681252176', 'Mathias Xavier Coletti'
                WHERE NOT EXISTS (SELECT 1 FROM pessoas WHERE cpf = '06681252176');
            """);

            jdbcTemplate.execute("""
                INSERT INTO pessoas (cpf, nome) 
                SELECT '07673107139', 'Emilly Fernandes Tavares'
                WHERE NOT EXISTS (SELECT 1 FROM pessoas WHERE cpf = '07673107139');
            """);

            // 2. Inserir Usuários (Vinculando pelo CPF da pessoa)
            jdbcTemplate.execute("""
                INSERT INTO usuarios (senha, pessoa_id) 
                SELECT '$2a$10$qSw8NbxRvrTX77KRxCvsduns1diKIt8.XmMo7D.vio39q4nPsE2Ba', p.id 
                FROM pessoas p WHERE p.cpf = '06681252176' 
                AND NOT EXISTS (SELECT 1 FROM usuarios u WHERE u.pessoa_id = p.id);
            """);

            jdbcTemplate.execute("""
                INSERT INTO usuarios (senha, pessoa_id) 
                SELECT '$2a$10$feC3x/kab5SQzqTRRYFtzeCtiDHCJn860aHtUD4mUroBWyco8kqre', p.id 
                FROM pessoas p WHERE p.cpf = '07673107139' 
                AND NOT EXISTS (SELECT 1 FROM usuarios u WHERE u.pessoa_id = p.id);
            """);

            // 3. Inserir Grupos (Apenas as Famílias)
            jdbcTemplate.execute("""
                INSERT INTO tb_grupo (descricao, nome) 
                SELECT 'Colocar os Gastos da Familia Fernandes Tavares', 'FAMILIA FERNANDES TAVARES'
                WHERE NOT EXISTS (SELECT 1 FROM tb_grupo WHERE nome = 'FAMILIA FERNANDES TAVARES');
            """);

            jdbcTemplate.execute("""
                INSERT INTO tb_grupo (descricao, nome) 
                SELECT 'Colocar os Gastos da Familia Coletti', 'FAMILIA COLETTI'
                WHERE NOT EXISTS (SELECT 1 FROM tb_grupo WHERE nome = 'FAMILIA COLETTI');
            """);

            // 4. Inserir Tipos de Gasto (Mercado, Lazer, Monster e Uber)
            jdbcTemplate.execute("""
                INSERT INTO tb_tipo_de_gasto (descricao, nome) 
                SELECT 'Mercado', 'MERCADO'
                WHERE NOT EXISTS (SELECT 1 FROM tb_tipo_de_gasto WHERE nome = 'MERCADO');
            """);

            jdbcTemplate.execute("""
                INSERT INTO tb_tipo_de_gasto (descricao, nome) 
                SELECT 'Lazer', 'LAZER'
                WHERE NOT EXISTS (SELECT 1 FROM tb_tipo_de_gasto WHERE nome = 'LAZER');
            """);

            jdbcTemplate.execute("""
                INSERT INTO tb_tipo_de_gasto (descricao, nome) 
                SELECT 'Monster', 'MONSTER'
                WHERE NOT EXISTS (SELECT 1 FROM tb_tipo_de_gasto WHERE nome = 'MONSTER');
            """);

            jdbcTemplate.execute("""
                INSERT INTO tb_tipo_de_gasto (descricao, nome) 
                SELECT 'Uber', 'Uber'
                WHERE NOT EXISTS (SELECT 1 FROM tb_tipo_de_gasto WHERE nome = 'Uber');
            """);

            // 5. Inserir Relacionamentos Grupo-Usuário (buscando IDs dinamicamente)
            jdbcTemplate.execute("""
                INSERT INTO tb_grupo_usuario (grupo_id, usuario_id)
                SELECT g.id, u.id 
                FROM tb_grupo g, usuarios u 
                JOIN pessoas p ON u.pessoa_id = p.id
                WHERE g.nome = 'FAMILIA COLETTI' AND p.cpf = '06681252176'
                AND NOT EXISTS (SELECT 1 FROM tb_grupo_usuario gu WHERE gu.grupo_id = g.id AND gu.usuario_id = u.id);
            """);

            jdbcTemplate.execute("""
                INSERT INTO tb_grupo_usuario (grupo_id, usuario_id)
                SELECT g.id, u.id 
                FROM tb_grupo g, usuarios u 
                JOIN pessoas p ON u.pessoa_id = p.id
                WHERE g.nome = 'FAMILIA FERNANDES TAVARES' AND p.cpf = '06681252176'
                AND NOT EXISTS (SELECT 1 FROM tb_grupo_usuario gu WHERE gu.grupo_id = g.id AND gu.usuario_id = u.id);
            """);

            jdbcTemplate.execute("""
                INSERT INTO tb_grupo_usuario (grupo_id, usuario_id)
                SELECT g.id, u.id 
                FROM tb_grupo g, usuarios u 
                JOIN pessoas p ON u.pessoa_id = p.id
                WHERE g.nome = 'FAMILIA FERNANDES TAVARES' AND p.cpf = '07673107139'
                AND NOT EXISTS (SELECT 1 FROM tb_grupo_usuario gu WHERE gu.grupo_id = g.id AND gu.usuario_id = u.id);
            """);
        };
    }
}
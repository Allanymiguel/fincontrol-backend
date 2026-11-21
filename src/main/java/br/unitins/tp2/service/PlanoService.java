package br.unitins.tp2.service;

import java.nio.file.Path;
import java.util.List;

import br.unitins.tp2.dto.PlanoRequestDTO;
import br.unitins.tp2.model.Plano;
import jakarta.validation.Valid;

public interface PlanoService {

    Plano create(@Valid PlanoRequestDTO plano);
    void update(long id, @Valid PlanoRequestDTO plano);
    void delete(long id);
    Plano findById(long id);
    List<Plano> findAll(int page, int pageSize);
    List<Plano> findByNome(String nome, int page, int pageSize);
    long count();
    long count(String nome);

    // imagem do catalogo (uma por plano, guardada no S3): envia ou troca. arquivo = upload temporario.
    Plano enviarImagem(long id, String nomeArquivo, long tamanho, Path arquivo);

    // remove a imagem do plano (sem imagem, nao faz nada)
    void removerImagem(long id);
}

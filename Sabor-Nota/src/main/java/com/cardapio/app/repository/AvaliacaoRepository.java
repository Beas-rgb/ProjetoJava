package com.cardapio.app.repository;

import com.cardapio.app.model.Avaliacao;
import com.cardapio.app.model.Restaurante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    List<Avaliacao> findByRestauranteOrderByDataAvaliacaoDesc(Restaurante restaurante);
}
package com.cardapio.app.repository;

import com.cardapio.app.model.Cardapio;
import com.cardapio.app.model.Restaurante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CardapioRepository extends JpaRepository<Cardapio, Long> {
    List<Cardapio> findByRestaurante(Restaurante restaurante);
}
package com.cardapio.app.repository;

import com.cardapio.app.model.ItemCardapio;
import com.cardapio.app.model.Cardapio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ItemCardapioRepository extends JpaRepository<ItemCardapio, Long> {
    List<ItemCardapio> findByCardapio(Cardapio cardapio);
}
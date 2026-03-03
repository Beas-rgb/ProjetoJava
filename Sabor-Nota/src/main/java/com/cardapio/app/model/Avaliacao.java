package com.cardapio.app.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nomePrato;
    private Integer nota;
    
    @Column(length = 5000)
    private String comentario;
    
    private BigDecimal preco;
    
    @Column(columnDefinition = "TEXT")  // Aceita URLs de qualquer tamanho
    private String fotoPratoUrl;
    
    private LocalDateTime dataAvaliacao;
    
    @ManyToOne
    @JoinColumn(name = "restaurante_id")
    private Restaurante restaurante;
    
    public Avaliacao() {}
    
    @PrePersist
    protected void onCreate() {
        dataAvaliacao = LocalDateTime.now();
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNomePrato() { return nomePrato; }
    public void setNomePrato(String nomePrato) { this.nomePrato = nomePrato; }
    
    public Integer getNota() { return nota; }
    public void setNota(Integer nota) { this.nota = nota; }
    
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    
    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }
    
    public String getFotoPratoUrl() { return fotoPratoUrl; }
    public void setFotoPratoUrl(String fotoPratoUrl) { this.fotoPratoUrl = fotoPratoUrl; }
    
    public LocalDateTime getDataAvaliacao() { return dataAvaliacao; }
    public void setDataAvaliacao(LocalDateTime dataAvaliacao) { this.dataAvaliacao = dataAvaliacao; }
    
    public Restaurante getRestaurante() { return restaurante; }
    public void setRestaurante(Restaurante restaurante) { this.restaurante = restaurante; }
}
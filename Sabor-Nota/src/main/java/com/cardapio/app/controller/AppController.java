package com.cardapio.app.controller;

import com.cardapio.app.model.*;
import com.cardapio.app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Controller
public class AppController {

    @Autowired
    private RestauranteRepository restauranteRepository;

    @Autowired
    private CardapioRepository cardapioRepository;

    @Autowired
    private ItemCardapioRepository itemRepository;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @GetMapping("/")
    public String index(Model model) {
        List<Restaurante> restaurantes = restauranteRepository.findAll();
        for (Restaurante rest : restaurantes) {
            atualizarMediaAvaliacoes(rest);
        }
        model.addAttribute("restaurantes", restaurantes);
        return "index";
    }

    @GetMapping("/cadastro")
    public String cadastro() {
        return "cadastro";
    }

    @PostMapping("/salvar-completo")
    public String salvarCompleto(
            @RequestParam String nome,
            @RequestParam(required = false) String endereco,
            @RequestParam(required = false) String telefone,
            @RequestParam(required = false) String tipoCozinha,
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) String fotoUrl,
            @RequestParam(required = false) Double precoMedio,
            @RequestParam(required = false) List<String> itemNome,
            @RequestParam(required = false) List<String> itemDescricao,
            @RequestParam(required = false) List<BigDecimal> itemPreco,
            @RequestParam(required = false) List<Integer> itemNota,
            RedirectAttributes redirectAttributes) {

        try {
            if (nome == null || nome.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("erro", "O nome do restaurante é obrigatório");
                return "redirect:/cadastro";
            }

            Restaurante restaurante = new Restaurante();
            restaurante.setNome(nome.trim());
            restaurante.setEndereco(endereco != null ? endereco.trim() : null);
            restaurante.setTelefone(telefone != null ? telefone.trim() : null);
            restaurante.setTipoCozinha(tipoCozinha != null ? tipoCozinha.trim() : null);
            restaurante.setDescricao(descricao != null ? descricao.trim() : null);
            restaurante.setFotoUrl(fotoUrl != null ? fotoUrl.trim() : null);
            restaurante.setPrecoMedio(precoMedio);
            restaurante.setAvaliacaoMedia(0.0);
            restaurante.setTotalAvaliacoes(0);

            restaurante = restauranteRepository.save(restaurante);

            double somaNotas = 0.0;
            int quantidadeAvaliacoes = 0;

            if (itemNome != null && !itemNome.isEmpty()) {
                List<String> nomesValidos = new ArrayList<>();
                List<String> descricoesValidas = new ArrayList<>();
                List<BigDecimal> precosValidos = new ArrayList<>();
                List<Integer> notasValidas = new ArrayList<>();

                for (int i = 0; i < itemNome.size(); i++) {
                    String nomeItem = itemNome.get(i);
                    if (nomeItem != null && !nomeItem.trim().isEmpty()) {
                        nomesValidos.add(nomeItem.trim());
                        descricoesValidas.add(itemDescricao != null && i < itemDescricao.size()
                                ? (itemDescricao.get(i) != null ? itemDescricao.get(i).trim() : "")
                                : "");
                        precosValidos.add(
                                itemPreco != null && i < itemPreco.size() && itemPreco.get(i) != null ? itemPreco.get(i)
                                        : BigDecimal.ZERO);
                        notasValidas.add(
                                itemNota != null && i < itemNota.size() && itemNota.get(i) != null ? itemNota.get(i)
                                        : 3);
                    }
                }

                if (!nomesValidos.isEmpty()) {
                    Cardapio cardapio = new Cardapio();
                    cardapio.setNomeCardapio("Cardápio Principal");
                    cardapio.setCategoria("Principal");
                    cardapio.setRestaurante(restaurante);
                    cardapio.setAtivo(true);
                    cardapio = cardapioRepository.save(cardapio);

                    for (int i = 0; i < nomesValidos.size(); i++) {
                        ItemCardapio item = new ItemCardapio();
                        item.setNome(nomesValidos.get(i));
                        item.setDescricao(descricoesValidas.get(i));
                        item.setPreco(precosValidos.get(i));
                        item.setCardapio(cardapio);
                        item.setDisponivel(true);
                        item.setDestaque(0);
                        item = itemRepository.save(item);

                        Integer nota = notasValidas.get(i);

                        Avaliacao avaliacao = new Avaliacao();
                        avaliacao.setNomePrato(item.getNome());
                        avaliacao.setComentario(item.getDescricao());
                        avaliacao.setPreco(item.getPreco());
                        avaliacao.setNota(nota);
                        avaliacao.setRestaurante(restaurante);
                        avaliacaoRepository.save(avaliacao);

                        somaNotas += nota;
                        quantidadeAvaliacoes++;
                    }
                }
            }

            if (quantidadeAvaliacoes > 0) {
                double media = somaNotas / quantidadeAvaliacoes;
                BigDecimal bd = new BigDecimal(media).setScale(1, RoundingMode.HALF_UP);
                restaurante.setAvaliacaoMedia(bd.doubleValue());
                restaurante.setTotalAvaliacoes(quantidadeAvaliacoes);
                restauranteRepository.save(restaurante);
            }

            redirectAttributes.addFlashAttribute("sucesso", "Restaurante cadastrado com sucesso!");
            return "redirect:/restaurante/" + restaurante.getId();

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar: " + e.getMessage());
            return "redirect:/cadastro";
        }
    }

    @GetMapping("/restaurante/{id}")
    public String detalhe(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Restaurante restaurante = restauranteRepository.findById(id).orElse(null);
        if (restaurante == null) {
            redirectAttributes.addFlashAttribute("erro", "Restaurante não encontrado");
            return "redirect:/";
        }
        atualizarMediaAvaliacoes(restaurante);
        model.addAttribute("restaurante", restaurante);
        model.addAttribute("cardapios", cardapioRepository.findByRestaurante(restaurante));
        model.addAttribute("avaliacoes", avaliacaoRepository.findByRestauranteOrderByDataAvaliacaoDesc(restaurante));
        return "detalhe";
    }

    @GetMapping("/deletar/{id}")
    public String deletar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (restauranteRepository.existsById(id)) {
                restauranteRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("sucesso", "Restaurante removido com sucesso!");
            } else {
                redirectAttributes.addFlashAttribute("erro", "Restaurante não encontrado");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("erro", "Erro ao deletar: " + e.getMessage());
        }
        return "redirect:/";
    }

    private void atualizarMediaAvaliacoes(Restaurante restaurante) {
        List<Avaliacao> avaliacoes = avaliacaoRepository.findByRestauranteOrderByDataAvaliacaoDesc(restaurante);
        if (avaliacoes.isEmpty()) {
            restaurante.setAvaliacaoMedia(0.0);
            restaurante.setTotalAvaliacoes(0);
        } else {
            double soma = 0.0;
            for (Avaliacao av : avaliacoes) {
                soma += av.getNota();
            }
            double media = soma / avaliacoes.size();
            BigDecimal bd = new BigDecimal(media).setScale(1, RoundingMode.HALF_UP);
            restaurante.setAvaliacaoMedia(bd.doubleValue());
            restaurante.setTotalAvaliacoes(avaliacoes.size());
        }
        restauranteRepository.save(restaurante);
    }
}
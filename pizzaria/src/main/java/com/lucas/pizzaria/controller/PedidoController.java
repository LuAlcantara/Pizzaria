package com.lucas.pizzaria.controller;

import com.lucas.pizzaria.model.Cliente;
import com.lucas.pizzaria.model.Pedido;
import com.lucas.pizzaria.repository.ClienteRepository;
import com.lucas.pizzaria.repository.PedidoRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    private static final Logger logger = LoggerFactory.getLogger(PedidoController.class);

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @GetMapping("/novo")
    public String mostrarFormularioBusca(Model model) {
        model.addAttribute("numeroCelular", "");
        return "buscar-cliente-pedido";
    }

    @PostMapping("/buscar-cliente")
    public String buscarClienteParaPedido(@RequestParam String numeroCelular, Model model) {
        Optional<Cliente> clienteOpt = clienteRepository.findByNumeroCelular(numeroCelular);
        if (clienteOpt.isPresent()) {
            logger.info("Cliente encontrado: {}", clienteOpt.get().getNomeCompleto());
            Pedido pedido = new Pedido();
            pedido.setCliente(clienteOpt.get());
            model.addAttribute("pedido", pedido);
            return "cadastro-pedido";
        } else {
            logger.warn("Cliente não encontrado para o número: {}", numeroCelular);
            model.addAttribute("mensagem", "Cliente não encontrado!");
            return "buscar-cliente-pedido";
        }
    }

    @PostMapping("/salvar")
    public String salvarPedido(@Valid @ModelAttribute Pedido pedido, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        logger.info("Tentando salvar pedido: {}", pedido);

        Long clienteId = pedido.getCliente() != null ? pedido.getCliente().getId() : null;
        if (clienteId == null) {
            logger.error("Cliente ID é nulo no pedido: {}", pedido);
            model.addAttribute("mensagem", "Erro: Cliente não foi selecionado.");
            return "cadastro-pedido";
        }

        Optional<Cliente> clienteOpt = clienteRepository.findById(clienteId);
        if (clienteOpt.isEmpty()) {
            logger.error("Cliente não encontrado para o ID: {}", clienteId);
            model.addAttribute("mensagem", "Cliente não encontrado!");
            return "cadastro-pedido";
        }
        pedido.setCliente(clienteOpt.get());

        pedido.setDataHora(LocalDateTime.now());
        logger.info("DataHora definido: {}", pedido.getDataHora());

        if (result.hasErrors()) {
            logger.error("Erros de validação: {}", result.getAllErrors());
            return "cadastro-pedido";
        }

        pedidoRepository.save(pedido);
        logger.info("Pedido salvo com sucesso: {}", pedido.getId());
        redirectAttributes.addFlashAttribute("mensagem", "Pedido salvo com sucesso!");
        return "redirect:/pedidos/listar";
    }

    @GetMapping("/listar")
    public String listarPedidos(Model model) {
        model.addAttribute("pedidos", pedidoRepository.findAll());
        return "listar-pedidos";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicao(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(id);
        if (pedidoOpt.isPresent()) {
            model.addAttribute("pedido", pedidoOpt.get());
            return "cadastro-pedido";
        }
        redirectAttributes.addFlashAttribute("mensagem", "Pedido não encontrado!");
        return "redirect:/pedidos/listar";
    }

    @GetMapping("/excluir/{id}")
    public String excluirPedido(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        pedidoRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("mensagem", "Pedido excluído com sucesso!");
        return "redirect:/pedidos/listar";
    }
}
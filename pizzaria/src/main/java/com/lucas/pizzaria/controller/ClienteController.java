package com.lucas.pizzaria.controller;

import com.lucas.pizzaria.model.Cliente;
import com.lucas.pizzaria.repository.ClienteRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    private ClienteRepository clienteRepository;

    // Mapeamento para a raiz (/)
    @GetMapping("/")
    public String redirectToInicio() {
        return "redirect:/clientes/inicio";
    }

    // Página inicial: escolha entre "Novo Cliente" ou "Cliente Existente"
    @GetMapping("/inicio")
    public String inicio() {
        return "inicio";
    }

    // Formulário de busca por número de celular (Cliente Existente)
    @GetMapping("/buscar")
    public String mostrarFormularioBusca(Model model) {
        model.addAttribute("numeroCelular", "");
        return "buscar-cliente";
    }

    // Processar a busca por número de celular
    @PostMapping("/buscar")
    public String buscarCliente(@RequestParam String numeroCelular, Model model) {
        Optional<Cliente> clienteOpt = clienteRepository.findByNumeroCelular(numeroCelular);
        if (clienteOpt.isPresent()) {
            model.addAttribute("cliente", clienteOpt.get());
            return "detalhes-cliente";
        } else {
            model.addAttribute("mensagem", "Cliente não encontrado!");
            return "buscar-cliente";
        }
    }

    // Formulário de cadastro de novo cliente
    @GetMapping("/novo")
    public String mostrarFormularioCadastro(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "cadastro-cliente";
    }

    // Salvar um novo cliente
    @PostMapping("/salvar")
    public String salvarCliente(@Valid @ModelAttribute Cliente cliente, BindingResult result, Model model) {
        // Verificar erros de validação
        if (result.hasErrors()) {
            return "cadastro-cliente";
        }

        // Verificar se o número de celular já existe
        Optional<Cliente> clienteExistente = clienteRepository.findByNumeroCelular(cliente.getNumeroCelular());
        if (clienteExistente.isPresent() && !clienteExistente.get().getId().equals(cliente.getId())) {
            model.addAttribute("mensagem", "Número de celular já cadastrado!");
            return "cadastro-cliente";
        }

        clienteRepository.save(cliente);
        return "redirect:/clientes/listar";
    }

    // Listar todos os clientes
    @GetMapping("/listar")
    public String listarClientes(Model model) {
        model.addAttribute("clientes", clienteRepository.findAll());
        return "listar-clientes";
    }

    // Formulário de edição de cliente
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicao(@PathVariable Long id, Model model) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(id);
        if (clienteOpt.isPresent()) {
            model.addAttribute("cliente", clienteOpt.get());
            return "cadastro-cliente";
        }
        return "redirect:/clientes/listar";
    }

    // Excluir um cliente
    @GetMapping("/excluir/{id}")
    public String excluirCliente(@PathVariable Long id) {
        clienteRepository.deleteById(id);
        return "redirect:/clientes/listar";
    }
}
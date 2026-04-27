package webprak.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import webprak.DAO.ClientDAO;
import webprak.DAO.ProfileDAO;
import webprak.models.Client;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/clients")
public class ClientController {

    @Autowired
    private ClientDAO clientDAO;

    @Autowired
    private ProfileDAO profileDAO;

    @GetMapping
    public String listClients(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(defaultValue = "client_id") String sortBy,
                              @RequestParam(defaultValue = "true") boolean asc,
                              @RequestParam(defaultValue = "") String search,
                              Model model) {
        List<Object[]> clients = clientDAO.getAllClients(page, size, sortBy, asc, search);
        long total = clientDAO.countClients(search);
        int totalPages = (int) Math.ceil((double) total / size);

        model.addAttribute("clients", clients);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("asc", asc);
        model.addAttribute("search", search);
        return "clients/list";
    }

    @GetMapping("/add")
    public String addClientForm() {
        return "clients/add";
    }

    @PostMapping("/add")
    public String addClient(@RequestParam String info,
                            @RequestParam String name,
                            @RequestParam String phone,
                            @RequestParam(required = false) String other,
                            Model model) {
        Long clientId = clientDAO.createClient(info);
        Long profileId = profileDAO.createProfile(clientId, name, phone, other);
        if (profileId == null) {
            clientDAO.deleteById(clientId);
            model.addAttribute("error",
                    "Не удалось создать профиль. Возможно, телефон уже занят.");
            return "clients/add";
        }
        return "redirect:/clients/" + clientId;
    }

    @GetMapping("/{id}")
    public String editClientForm(@PathVariable Long id, Model model) {
        Client client = clientDAO.getById(id);
        if (client == null) {
            return "redirect:/clients";
        }
        model.addAttribute("client", client);
        model.addAttribute("regDate",
                client.getRegistrationDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        return "clients/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateClient(@PathVariable Long id, @RequestParam String info) {
        clientDAO.updateClientInfo(id, info);
        return "redirect:/clients/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteClient(@PathVariable Long id) {
        clientDAO.deleteById(id);
        return "redirect:/clients";
    }
}
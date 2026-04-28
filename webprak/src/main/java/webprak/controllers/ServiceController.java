package webprak.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import webprak.DAO.ServiceDAO;
import webprak.models.Service;

import java.util.List;

@Controller
@RequestMapping("/services")
public class ServiceController {

    @Autowired
    private ServiceDAO serviceDAO;

    @GetMapping
    public String listServices(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) String tagKey,
                               @RequestParam(defaultValue = "name") String sortBy,
                               @RequestParam(defaultValue = "true") boolean asc,
                               @RequestParam(defaultValue = "") String search,
                               Model model) {
        List<Object[]> services = serviceDAO.getAllServices(page, size, tagKey, sortBy, asc, search);
        long total = serviceDAO.countServices(tagKey, search);
        int totalPages = (int) Math.ceil((double) total / size);

        model.addAttribute("services", services);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("asc", asc);
        model.addAttribute("search", search);
        model.addAttribute("tagKey", tagKey);
        return "services/list";
    }

    @GetMapping("/add")
    public String addServiceForm() {
        return "services/add";
    }

    @PostMapping("/add")
    public String addService(@RequestParam String name,
                             @RequestParam String description,
                             @RequestParam(required = false) String includes,
                             @RequestParam(required = false) String other) {
        serviceDAO.createService(name, description, includes, other);
        return "redirect:/services";
    }

    @GetMapping("/{id}")
    public String editServiceForm(@PathVariable Long id, Model model) {
        Service service = serviceDAO.getById(id);
        if (service == null) {
            return "redirect:/services";
        }
        model.addAttribute("service", service);
        return "services/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateService(@PathVariable Long id,
                                @RequestParam String name,
                                @RequestParam String description,
                                @RequestParam(required = false) String includes,
                                @RequestParam(required = false) String other) {
        serviceDAO.updateService(id, name, description, includes, other);
        return "redirect:/services/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteService(@PathVariable Long id) {
        serviceDAO.deleteById(id);
        return "redirect:/services";
    }
}
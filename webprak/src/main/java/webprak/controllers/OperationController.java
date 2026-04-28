package webprak.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import webprak.DAO.OperationDAO;
import webprak.models.Operation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/operations")
public class OperationController {

    @Autowired
    private OperationDAO operationDAO;

    @GetMapping
    public String listOperations(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) Long profileId,
                                 @RequestParam(required = false) Long clientId,
                                 @RequestParam(required = false) String type,
                                 @RequestParam(required = false) Long serviceId,
                                 @RequestParam(required = false) LocalDateTime from,
                                 @RequestParam(required = false) LocalDateTime to,
                                 @RequestParam(defaultValue = "timestamp") String sortBy,
                                 @RequestParam(defaultValue = "false") boolean asc,
                                 Model model) {

        List<Object[]> operations = operationDAO.getAllOperations(page, size, profileId, clientId, type, serviceId, from, to);
        long total = operationDAO.countOperations(profileId, clientId, type, serviceId, from, to);
        int totalPages = (int) Math.ceil((double) total / size);

        StringBuilder queryString = new StringBuilder();
        if (profileId != null) queryString.append("&profileId=").append(profileId);
        if (clientId != null) queryString.append("&clientId=").append(clientId);
        if (type != null && !type.isEmpty()) queryString.append("&type=").append(type);
        if (serviceId != null) queryString.append("&serviceId=").append(serviceId);
        if (from != null) queryString.append("&from=").append(from.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        if (to != null) queryString.append("&to=").append(to.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        String baseUrl = "/operations";
        if (queryString.length() > 0) {
            baseUrl += "?" + queryString.substring(1); // pagination hack
        }

        model.addAttribute("operations", operations);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("asc", asc);
        model.addAttribute("search", "");
        model.addAttribute("baseUrl", baseUrl);

        model.addAttribute("profileId", profileId);
        model.addAttribute("clientId", clientId);
        model.addAttribute("type", type);
        model.addAttribute("serviceId", serviceId);
        model.addAttribute("from", from != null ? from.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")) : null);
        model.addAttribute("to", to != null ? to.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")) : null);

        return "operations/list";
    }

    @GetMapping("/add")
    public String addOperationForm(Model model) {
        model.addAttribute("operationTypes", Operation.OperationType.values());
        return "operations/add";
    }

    @PostMapping("/add")
    public String addOperation(@RequestParam Long profileId,
                               @RequestParam String type,
                               @RequestParam(required = false) Long serviceId,
                               @RequestParam Double balanceChange,
                               @RequestParam(required = false) String description,
                               Model model) {
        try {
            Long operationId = operationDAO.createOperation(profileId, type, serviceId, balanceChange, description);
            if (operationId == null) {
                model.addAttribute("error", "Неверные параметры операции.");
                model.addAttribute("operationTypes", Operation.OperationType.values());
                return "operations/add";
            }
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("operationTypes", Operation.OperationType.values());
            return "operations/add";
        }
        return "redirect:/operations";
    }

    @GetMapping("/{id}")
    public String viewOperation(@PathVariable Long id, Model model) {
        Operation operation = operationDAO.getById(id);
        if (operation == null) {
            return "redirect:/operations";
        }
        model.addAttribute("operation", operation);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        model.addAttribute("timestampFormatted", operation.getTimestamp().format(formatter));
        return "operations/view";
    }
}
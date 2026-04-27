package webprak.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import webprak.DAO.ProfileDAO;
import webprak.models.Profile;

import java.util.List;

@Controller
@RequestMapping("/profiles")
public class ProfileController {

    @Autowired
    private ProfileDAO profileDAO;

    @GetMapping
    public String listProfiles(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(defaultValue = "client_id") String sortBy,
                               @RequestParam(defaultValue = "true") boolean asc,
                               @RequestParam(defaultValue = "") String search,
                               Model model) {
        List<Object[]> profiles = profileDAO.getAllProfiles(page, size, sortBy, asc, search);
        long total = profileDAO.countProfiles(search);
        int totalPages = (int) Math.ceil((double) total / size);

        model.addAttribute("profiles", profiles);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("asc", asc);
        model.addAttribute("search", search);
        return "profiles/list";
    }

    @GetMapping("/add")
    public String addProfileForm() {
        return "profiles/add";
    }

    @PostMapping("/add")
    public String addProfile(@RequestParam Long clientId,
                             @RequestParam String name,
                             @RequestParam String phone,
                             @RequestParam(required = false) String other,
                             Model model) {
        Long newId = profileDAO.createProfile(clientId, name, phone, other);
        if (newId == null) {
            model.addAttribute("error",
                    "Не удалось создать профиль. Возможно, телефон уже занят или клиент не существует.");
            return "profiles/add";
        }
        return "redirect:/profiles/" + newId;
    }

    @GetMapping("/{id}")
    public String editProfileForm(@PathVariable Long id, Model model) {
        Profile profile = profileDAO.getById(id);
        if (profile == null) {
            return "redirect:/profiles";
        }
        model.addAttribute("profile", profile);
        return "profiles/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateProfile(@PathVariable Long id,
                                @RequestParam String name,
                                @RequestParam String phone,
                                @RequestParam(required = false) String other,
                                Model model) {
        try {
            profileDAO.updateProfile(id, name, phone, other);
        } catch (Exception e) {
            model.addAttribute("profile", profileDAO.getById(id));
            model.addAttribute("error", "Ошибка обновления: " + e.getMessage());
            return "profiles/edit";
        }
        return "redirect:/profiles/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteProfile(@PathVariable Long id) {
        profileDAO.deleteById(id);
        return "redirect:/profiles";
    }
}
package com.arcade.dashboard.controller;

import com.arcade.dashboard.model.Machine;
import com.arcade.dashboard.service.MachineService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.Optional;

@Controller
public class DashboardController {

    @Autowired
    private MachineService machineService;

    @GetMapping("/")
    public String dashboard(Model model) {
        Map<String, Object> stats = machineService.getDashboardStats();
        model.addAllAttributes(stats);
        model.addAttribute("recentEvents", machineService.getRecentEvents());
        return "dashboard";
    }

    @GetMapping("/machines")
    public String machines(Model model) {
        model.addAttribute("machines", machineService.getAllMachines());
        model.addAttribute("newMachine", new Machine());
        return "machines";
    }

    @PostMapping("/machines/add")
    public String addMachine(
            @Valid @ModelAttribute("newMachine") Machine machine,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("machines", machineService.getAllMachines());
            model.addAttribute("showAddModal", true);
            return "machines";
        }
        try {
            machineService.addMachine(machine);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Machine '" + machine.getName() + "' added successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/machines";
    }

    @GetMapping("/machines/edit/{id}")
    public String editMachineForm(@PathVariable Long id, Model model) {
        Optional<Machine> machine = machineService.getMachineById(id);
        if (machine.isEmpty()) return "redirect:/machines";
        model.addAttribute("machine", machine.get());
        return "edit-machine";
    }

    @PostMapping("/machines/edit/{id}")
    public String editMachine(
            @PathVariable Long id,
            @Valid @ModelAttribute("machine") Machine machine,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) return "edit-machine";
        try {
            machineService.updateMachine(id, machine);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Machine updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/machines";
    }

    @PostMapping("/machines/delete/{id}")
    public String deleteMachine(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            machineService.deleteMachine(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Machine deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Could not delete machine.");
        }
        return "redirect:/machines";
    }

    @PostMapping("/machines/reset/{id}")
    public String resetCount(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        try {
            machineService.resetCount(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Play count reset.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Could not reset count.");
        }
        return "redirect:/machines";
    }

    @GetMapping("/analytics")
    public String analytics(Model model) {
        model.addAttribute("machines", machineService.getAllMachines());
        model.addAttribute("stats", machineService.getDashboardStats());
        return "analytics";
    }
}
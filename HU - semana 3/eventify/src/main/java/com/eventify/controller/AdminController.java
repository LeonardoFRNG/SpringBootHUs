package com.eventify.controller;

import com.eventify.model.Event;
import com.eventify.model.Venue;
import com.eventify.service.EventService;
import com.eventify.service.VenueService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final EventService eventService;
    private final VenueService venueService;

    public AdminController(EventService eventService, VenueService venueService) {
        this.eventService = eventService;
        this.venueService = venueService;
    }

    /**
     * Panel administrativo visual centralizado.
     * Renderiza la vista principal con los listados de eventos y lugares.
     */
    @GetMapping({"", "/", "/events", "/venues", "/dashboard"})
    public String index(Model model) {
        model.addAttribute("events", eventService.getAll());
        model.addAttribute("venues", venueService.getAll());
        return "admin/index";
    }

    /**
     * Muestra el formulario para registrar un nuevo Evento.
     */
    @GetMapping("/events/new")
    public String newEventForm(Model model) {
        model.addAttribute("event", new Event());
        return "admin/event-form";
    }

    /**
     * Procesa la creación de un nuevo Evento utilizando el patrón Post-Redirect-Get.
     */
    @PostMapping({"/events", "/events/save"})
    public String createEvent(@ModelAttribute("event") Event event, RedirectAttributes redirectAttributes) {
        eventService.create(event);
        redirectAttributes.addFlashAttribute("successMessage", "Evento '" + event.getNombre() + "' registrado exitosamente.");
        return "redirect:/admin";
    }

    /**
     * Muestra el formulario para registrar un nuevo Lugar (Venue).
     */
    @GetMapping("/venues/new")
    public String newVenueForm(Model model) {
        model.addAttribute("venue", new Venue());
        return "admin/venue-form";
    }

    /**
     * Procesa la creación de un nuevo Lugar utilizando el patrón Post-Redirect-Get.
     */
    @PostMapping({"/venues", "/venues/save"})
    public String createVenue(@ModelAttribute("venue") Venue venue, RedirectAttributes redirectAttributes) {
        venueService.create(venue);
        redirectAttributes.addFlashAttribute("successMessage", "Lugar '" + venue.getNombre() + "' registrado exitosamente.");
        return "redirect:/admin";
    }
}

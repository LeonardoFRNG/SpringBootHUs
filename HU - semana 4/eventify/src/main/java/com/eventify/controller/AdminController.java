package com.eventify.controller;

import com.eventify.dto.EventForm;
import com.eventify.dto.EventSummaryDTO;
import com.eventify.model.Category;
import com.eventify.model.Event;
import com.eventify.model.Venue;
import com.eventify.service.CategoryService;
import com.eventify.service.EventService;
import com.eventify.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final EventService eventService;
    private final VenueService venueService;
    private final CategoryService categoryService;

    public AdminController(EventService eventService, VenueService venueService, CategoryService categoryService) {
        this.eventService = eventService;
        this.venueService = venueService;
        this.categoryService = categoryService;
    }

    /**
     * Panel administrativo visual centralizado.
     * Renderiza el catálogo optimizado mediante Slice<EventSummaryDTO> con filtros persistentes.
     */
    @GetMapping({"", "/", "/events", "/venues", "/dashboard"})
    public String index(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        // Validación de coherencia en fechas
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            model.addAttribute("errorMessage", "La fecha inicial (" + startDate + ") no puede ser posterior a la fecha final (" + endDate + ")");
        }

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        Slice<EventSummaryDTO> eventsSlice = eventService.searchEventsSummary(
                name, city, category, minCapacity, startDate, endDate, pageable
        );

        model.addAttribute("eventsSlice", eventsSlice);
        model.addAttribute("events", eventsSlice.getContent()); // Para compatibilidad
        model.addAttribute("venues", venueService.getAll());
        model.addAttribute("categories", categoryService.getAll());

        // Parámetros de filtro para persistencia en enlaces de paginación
        model.addAttribute("name", name != null ? name : "");
        model.addAttribute("city", city != null ? city : "");
        model.addAttribute("category", category != null ? category : "");
        model.addAttribute("minCapacity", minCapacity);
        model.addAttribute("startDate", startDate != null ? startDate.toString() : "");
        model.addAttribute("endDate", endDate != null ? endDate.toString() : "");
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);

        return "admin/index";
    }

    /**
     * Muestra el formulario para registrar un nuevo Evento.
     */
    @GetMapping("/events/new")
    public String newEventForm(Model model) {
        if (!model.containsAttribute("event")) {
            model.addAttribute("event", new EventForm());
        }
        populateFormDependencies(model);
        model.addAttribute("isEdit", false);
        return "admin/event-form";
    }

    /**
     * Procesa la creación de un nuevo Evento.
     * Valida campos obligatorios y la selección de sede.
     */
    @PostMapping({"/events", "/events/save"})
    public String createEvent(
            @Valid @ModelAttribute("event") EventForm eventForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            populateFormDependencies(model);
            model.addAttribute("isEdit", false);
            return "admin/event-form";
        }

        try {
            Venue venue = venueService.getById(eventForm.getVenueId());
            Set<Category> categories = (eventForm.getCategoryIds() != null && !eventForm.getCategoryIds().isEmpty())
                    ? categoryService.getByIds(new HashSet<>(eventForm.getCategoryIds()))
                    : new HashSet<>();

            Event event = eventForm.toEntity(venue, categories);
            eventService.create(event);

            redirectAttributes.addFlashAttribute("successMessage", "Evento '" + event.getNombre() + "' registrado exitosamente.");
            return "redirect:/admin";
        } catch (Exception ex) {
            populateFormDependencies(model);
            model.addAttribute("isEdit", false);
            model.addAttribute("errorMessage", ex.getMessage());
            return "admin/event-form";
        }
    }

    /**
     * Muestra el formulario para editar un evento existente.
     */
    @GetMapping("/events/{id}/edit")
    public String editEventForm(@PathVariable Long id, Model model) {
        Event event = eventService.getByIdWithRelations(id);
        model.addAttribute("event", EventForm.fromEntity(event));
        populateFormDependencies(model);
        model.addAttribute("isEdit", true);
        return "admin/event-form";
    }

    /**
     * Procesa la actualización de un evento existente.
     */
    @PostMapping("/events/{id}/edit")
    public String updateEvent(
            @PathVariable Long id,
            @Valid @ModelAttribute("event") EventForm eventForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            populateFormDependencies(model);
            model.addAttribute("isEdit", true);
            return "admin/event-form";
        }

        try {
            Venue venue = venueService.getById(eventForm.getVenueId());
            Set<Category> categories = (eventForm.getCategoryIds() != null && !eventForm.getCategoryIds().isEmpty())
                    ? categoryService.getByIds(new HashSet<>(eventForm.getCategoryIds()))
                    : new HashSet<>();

            Event event = eventForm.toEntity(venue, categories);
            eventService.update(id, event);

            redirectAttributes.addFlashAttribute("successMessage", "Evento '" + event.getNombre() + "' actualizado exitosamente.");
            return "redirect:/admin";
        } catch (Exception ex) {
            populateFormDependencies(model);
            model.addAttribute("isEdit", true);
            model.addAttribute("errorMessage", ex.getMessage());
            return "admin/event-form";
        }
    }

    /**
     * Borrado lógico: desactiva un evento sin eliminarlo físicamente.
     */
    @PostMapping({"/events/{id}/delete", "/events/{id}/deactivate"})
    public String deactivateEvent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        eventService.softDelete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Evento desactivado exitosamente (borrado lógico aplicado).");
        return "redirect:/admin";
    }

    /**
     * Muestra el formulario para registrar un nuevo Lugar (Venue).
     */
    @GetMapping("/venues/new")
    public String newVenueForm(Model model) {
        if (!model.containsAttribute("venue")) {
            model.addAttribute("venue", new Venue());
        }
        return "admin/venue-form";
    }

    /**
     * Procesa la creación de un nuevo Lugar utilizando el patrón Post-Redirect-Get.
     */
    @PostMapping({"/venues", "/venues/save"})
    public String createVenue(
            @Valid @ModelAttribute("venue") Venue venue,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "admin/venue-form";
        }
        venueService.create(venue);
        redirectAttributes.addFlashAttribute("successMessage", "Lugar '" + venue.getNombre() + "' registrado exitosamente.");
        return "redirect:/admin";
    }

    private void populateFormDependencies(Model model) {
        model.addAttribute("venues", venueService.getAll());
        model.addAttribute("categories", categoryService.getAll());
    }
}

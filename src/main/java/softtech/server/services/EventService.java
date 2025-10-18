package softtech.server.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import softtech.server.dto.EventDTO;
import softtech.server.models.Event;
import softtech.server.repositories.EventRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final NotificationService notificationService;

    public List<EventDTO> getAll() {
        return eventRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    // MỚI
    public List<EventDTO> getActive(LocalDate date) {
        LocalDate d = (date != null) ? date : LocalDate.now();
        return eventRepository.findAll().stream()
                .filter(e -> e.getDiscountStartDate() != null && e.getDiscountEndDate() != null)
                .filter(e -> !d.isBefore(e.getDiscountStartDate()) && !d.isAfter(e.getDiscountEndDate()))
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public EventDTO create(EventDTO dto) {
        validate(dto);
        Event e = Event.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .discountPercent(dto.getDiscountPercent())
                .discountStartDate(dto.getDiscountStartDate())
                .discountEndDate(dto.getDiscountEndDate())
                .build();
        Event saved = eventRepository.save(e);
        EventDTO created = toDTO(saved);
        notificationService.broadcastNewEvent(created);
        return created;
    }

    @Transactional
    public EventDTO update(String id, EventDTO dto) {
        Event e = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));
        if (dto.getName() != null) e.setName(dto.getName());
        if (dto.getDescription() != null) e.setDescription(dto.getDescription());
        if (dto.getDiscountPercent() >= 0) e.setDiscountPercent(dto.getDiscountPercent());
        if (dto.getDiscountStartDate() != null) e.setDiscountStartDate(dto.getDiscountStartDate());
        if (dto.getDiscountEndDate() != null) e.setDiscountEndDate(dto.getDiscountEndDate());
        Event saved = eventRepository.save(e);
        return toDTO(saved);
    }

    @Transactional
    public void delete(String id) {
        if (!eventRepository.existsById(id)) {
            throw new IllegalArgumentException("Event not found: " + id);
        }
        eventRepository.deleteById(id);
    }

    private EventDTO toDTO(Event e) {
        return EventDTO.builder()
                .eventId(e.getEventId())
                .name(e.getName())
                .description(e.getDescription())
                .discountPercent(e.getDiscountPercent())
                .discountStartDate(e.getDiscountStartDate())
                .discountEndDate(e.getDiscountEndDate())
                .build();
    }

    private void validate(EventDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Event name is required");
        }
        if (dto.getDiscountPercent() < 0) {
            throw new IllegalArgumentException("Discount percent must be >= 0");
        }
        if (dto.getDiscountStartDate() != null && dto.getDiscountEndDate() != null
                && dto.getDiscountEndDate().isBefore(dto.getDiscountStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        if (dto.getDiscountPercent() > 100) {
            throw new IllegalArgumentException("Discount percent must be ≤ 100");
        }
    }
}
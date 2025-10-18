package softtech.server.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import softtech.server.dto.ServiceDTO.ServiceDTO;
import softtech.server.services.ServiceService;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceService serviceService;

    @PostMapping
    public ServiceDTO createService(@RequestBody ServiceDTO dto) {
        return serviceService.createService(dto);
    }

    @GetMapping
    public List<ServiceDTO> getAllServices() {
        return serviceService.getAllServices();
    }

    @PutMapping("/{serviceId}")
    public ServiceDTO updateService(@PathVariable String serviceId, @RequestBody ServiceDTO dto) {
        return serviceService.updateService(serviceId, dto);
    }
}

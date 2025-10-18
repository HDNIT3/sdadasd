package softtech.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import softtech.server.dto.ServiceDTO.ServiceDTO;
import softtech.server.repositories.ServiceRepo;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceService {

    @Autowired
    private ServiceRepo serviceRepo;

    public ServiceDTO createService(ServiceDTO dto) {
        softtech.server.models.Service entity = softtech.server.models.Service.builder()
                .name(dto.getName())
                .price(dto.getPrice())
                .description(dto.getDescription())
                .build();

        return mapToDTO(serviceRepo.save(entity));
    }

    public List<ServiceDTO> getAllServices() {
        return serviceRepo.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public ServiceDTO updateService(String id, ServiceDTO dto) {
        softtech.server.models.Service entity = serviceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        entity.setName(dto.getName());
        entity.setPrice(dto.getPrice());
        entity.setDescription(dto.getDescription());

        return mapToDTO(serviceRepo.save(entity));
    }

    public void deleteService(String id) {
        serviceRepo.deleteById(id);
    }

    private ServiceDTO mapToDTO(softtech.server.models.Service s) {
        return ServiceDTO.builder()
                .serviceId(s.getServiceId())
                .name(s.getName())
                .price(s.getPrice())
                .description(s.getDescription())
                .build();
    }
}

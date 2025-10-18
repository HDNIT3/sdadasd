package softtech.server.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import softtech.server.dto.ServiceDTO.ServiceOrderDetailDTO;
import softtech.server.models.ServiceOrder;
import softtech.server.services.ServiceOrderService;
import softtech.server.utils.JwtUtil;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/service-orders")
@CrossOrigin(origins = "https://zzzzz-production.up.railway.app")
@RequiredArgsConstructor
public class ServiceOrderController {

    private final ServiceOrderService serviceOrderService;

    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) String phoneNumber)
    {
        try {
            ServiceOrder order = serviceOrderService.createServiceOrder(accountId, phoneNumber);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Failed to create service order: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/{orderId}/details")
    public ResponseEntity<?> addOrderDetails(
            @PathVariable String orderId,
            @RequestBody List<ServiceOrderDetailDTO> details) {
        try {
            serviceOrderService.addServiceOrderDetails(orderId, details);
            return ResponseEntity.ok(orderId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Failed to add order details: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable String orderId) {
        ServiceOrder order = serviceOrderService.getServiceOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteServiceOrder(@PathVariable String orderId) {
        try {
            serviceOrderService.deleteServiceOrder(orderId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Service order deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Failed to delete service order: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/details/{detailId}")
    public ResponseEntity<?> deleteOrderDetail(@PathVariable String detailId) {
        try {
            serviceOrderService.deleteOrderDetailById(detailId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Order detail deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Failed to delete order detail: " + e.getMessage()
            ));
        }
    }
}
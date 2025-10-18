package softtech.server.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import softtech.server.dto.StaffDTO.EmployeeDTO;
import softtech.server.services.EmployeeService;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "http://localhost:3000")
public class EmployeeController {

    @Autowired
    private EmployeeService service;

    @GetMapping
    public ResponseEntity<?> getEmployee(@RequestParam(required = false) String search) {
        List<EmployeeDTO> rs = service.search(search);
        return ResponseEntity.ok(rs);
    }

    @PostMapping
    public ResponseEntity<?> createEmployee(@RequestBody EmployeeDTO dto) {
        try {
            EmployeeDTO saved = service.create(dto);
            return ResponseEntity.ok(Map.of("success", true, "employee", saved));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{employeeId}")
    public ResponseEntity<?> updateEmployee(@PathVariable String employeeId, @RequestBody EmployeeDTO dto) {
        try {
            EmployeeDTO saved = service.update(employeeId, dto);
            return ResponseEntity.ok(Map.of("success", true, "employee", saved));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
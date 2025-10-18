package softtech.server.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import softtech.server.dto.ShiftDTO.*;
import softtech.server.dto.ShiftDTO.AssignmentResult;
import softtech.server.dto.ShiftDTO.RegisterShiftRequest;
import softtech.server.dto.ShiftDTO.RegistrationDTO;
import softtech.server.models.ShiftRegistration;
import softtech.server.repositories.EmployeeRepo;
import softtech.server.services.ShiftScheduleService;
import softtech.server.utils.JwtUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/shifts")
@CrossOrigin(origins = "https://zzzzz-production.up.railway.app")
@RequiredArgsConstructor
public class ShiftScheduleController {
    private final ShiftScheduleService shiftScheduleService;
    private final JwtUtil jwtUtil;
    private final EmployeeRepo employeeRepo;

    @PostMapping("/schedule")
    public ResponseEntity<?> generateWeeklySchedule(@RequestParam LocalDate startDate) {
        try {
            List<ShiftDTO> shifts = shiftScheduleService.generateWeeklySchedule(startDate);
            return ResponseEntity.ok(shifts);
        } catch (IllegalArgumentException e) {
            log.error("Invalid start date: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", "Invalid date",
                            "message", e.getMessage(),
                            "hint", "Start date must be Monday (DayOfWeek = MONDAY)"
                    ));
        } catch (Exception e) {
            log.error("Error generating schedule: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate schedule", "message", e.getMessage()));
        }
    }

    @GetMapping("/schedule/week")
    public ResponseEntity<?> getWeeklySchedule(@RequestParam LocalDate startDate) {
        try {
            List<ShiftDTO> schedule = shiftScheduleService.getWeeklySchedule(startDate);
            return ResponseEntity.ok(schedule);
        } catch (Exception e) {
            log.error("Error fetching weekly schedule: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch schedule", "message", e.getMessage()));
        }
    }

    @GetMapping("/created-weeks")
    public ResponseEntity<List<LocalDate>> getCreatedWeeks() {
        try {
            List<LocalDate> weeks = shiftScheduleService.getCreatedWeeks();
            return ResponseEntity.ok(weeks);
        } catch (Exception e) {
            log.error("Error getting created weeks", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/employee-shift-counts")
    public ResponseEntity<Map<String, Integer>> getEmployeeShiftCounts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        try {
            Map<String, Integer> counts = shiftScheduleService.getEmployeeShiftCountsForWeek(startDate);
            return ResponseEntity.ok(counts);
        } catch (Exception e) {
            log.error("Error getting employee shift counts for week {}", startDate, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/registered")
    public ResponseEntity<?> getMyRegisteredShifts(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String accountId = jwtUtil.extractUserId(token);

            var employee = employeeRepo.findByAccount_AccountId(accountId);
            if (employee == null) {
                employee = employeeRepo.findByAccount_Username(accountId);
            }
            if (employee == null) {
                log.warn("Employee not found by accountId/username='{}'", accountId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized", "message", "Staff not found by accountId/username"));
            }

            List<ShiftDTO> data = shiftScheduleService.getRegisteredShiftsOf(employee.getEmployeeId());
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("Error fetching registered shifts: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch registered shifts", "message", e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerShift(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody RegisterShiftRequest request
    ) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String accountId = jwtUtil.extractUserId(token);
            String shiftId = request.getShiftId();

            ShiftRegistration registration = shiftScheduleService.registerShift(accountId, shiftId);

            RegistrationDTO dto = RegistrationDTO.builder()
                    .registrationId(registration.getRegistrationId())
                    .shiftId(registration.getShift().getShiftId())
                    .employeeId(registration.getEmployee().getEmployeeId())
                    .note(registration.getNote())
                    .isAutoAssign(registration.isAutoAssigned())
                    .updated_at(registration.getUpdatedAt())
                    .created_at(registration.getCreatedAt())
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (RuntimeException e) {
            log.error("Error registering shift: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Registration failed", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    @PostMapping("/assign")
    public ResponseEntity<?> assignEmployee(@RequestBody AssignShiftRequest request) {
        try {
            String employeeId = request.getEmployeeId();
            String shiftId = request.getShiftId();

            ShiftRegistration registration = shiftScheduleService.assignEmployeeToShift(employeeId, shiftId);

            RegistrationDTO dto = RegistrationDTO.builder()
                    .registrationId(registration.getRegistrationId())
                    .shiftId(registration.getShift().getShiftId())
                    .employeeId(registration.getEmployee().getEmployeeId())
                    .note(registration.getNote())
                    .isAutoAssign(registration.isAutoAssigned())
                    .updated_at(registration.getUpdatedAt())
                    .created_at(registration.getCreatedAt())
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (RuntimeException e) {
            log.error("Error assigning employee: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Assignment failed", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/cancel/{registrationId}")
    public ResponseEntity<?> cancelRegistration(@PathVariable String registrationId) {
        try {
            shiftScheduleService.cancelRegistration(registrationId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error cancelling registration: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to cancel registration", "message", e.getMessage()));
        }
    }

    @PostMapping("/auto-assign")
    public ResponseEntity<?> autoAssignShifts(
            @RequestParam(required = false) LocalDate startDate
    ) {
        try {
            if (startDate == null) {
                startDate = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
            }

            AssignmentResult result = shiftScheduleService.autoAssignShiftsForWeek(startDate);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.error("Error auto-assigning shifts: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Auto-assignment failed", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during auto-assignment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/schedule")
    public ResponseEntity<?> deleteWeeklySchedule(@RequestParam LocalDate startDate) {
        try {
            shiftScheduleService.deleteWeek(startDate);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid start date for delete: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", "Invalid date",
                            "message", e.getMessage(),
                            "hint", "Start date must be Monday (DayOfWeek = MONDAY)"
                    ));
        } catch (Exception e) {
            log.error("Error deleting weekly schedule: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete weekly schedule", "message", e.getMessage()));
        }
    }

    @GetMapping("/current-monday")
    public ResponseEntity<Map<String, String>> getCurrentMonday() {
        LocalDate monday = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        return ResponseEntity.ok(Map.of(
                "currentMonday", monday.toString(),
                "hint", "Use this date for /schedule and /auto-assign endpoints"
        ));
    }
}

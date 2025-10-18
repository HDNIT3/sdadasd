package softtech.server.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import softtech.server.dto.ShiftDTO.AssignmentResult;
import softtech.server.dto.ShiftDTO.RegistrationDTO;
import softtech.server.dto.ShiftDTO.ShiftDTO;
import softtech.server.models.Employee;
import softtech.server.models.Shift;
import softtech.server.models.ShiftRegistration;
import softtech.server.repositories.EmployeeRepo;
import softtech.server.repositories.ShiftRegistrationRepo;
import softtech.server.repositories.ShiftRepo;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftScheduleService {
    private final ShiftRepo shiftRepo;
    private final ShiftRegistrationRepo registrationRepo;
    private final EmployeeRepo employeeRepo;

    private static final int MAX_SHIFTS_PER_WEEK = 6;
    private static final int MAX_HOURS_PER_WEEK = 40;
    private static final int MAX_CONSECUTIVE_DAYS = 5;
    private static final int MIN_REST_HOURS = 12;

    @Transactional
    public List<ShiftDTO> generateWeeklySchedule(LocalDate startDate) {
        if (startDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new IllegalArgumentException("Start date must be Monday! Provided: " + startDate.getDayOfWeek());
        }

        LocalDate endDate = startDate.plusDays(6); // Chủ nhật

        boolean exists = shiftRepo.existsByWorkDateBetween(startDate, endDate);
        if (exists) {
            log.info("Schedule already exists for week starting {}", startDate);
            return getWeeklySchedule(startDate);
        }

        List<Shift> allShifts = new ArrayList<>();

        // Tạo ca cho từng ngày trong tuần
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            boolean isWeekend = (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY);

            List<Shift> shiftsForDay = createShiftsForDay(date, isWeekend);
            allShifts.addAll(shiftsForDay);
        }

        log.info("Created {} shifts for week starting {}", allShifts.size(), startDate);

        // Chuyển đổi sang DTO
        return allShifts.stream()
                .map(shift -> ShiftDTO.builder()
                        .shiftId(shift.getShiftId())
                        .name(shift.getName())
                        .startTime(shift.getStartTime().toString())
                        .endTime(shift.getEndTime().toString())
                        .note(shift.getNote())
                        .shiftStatus(shift.getShiftStatus())
                        .isWeekend(shift.isWeekend())
                        .build())
                .collect(Collectors.toList());
    }

    private List<Shift> createShiftsForDay(LocalDate date, boolean isWeekend) {
        List<Shift> shifts = new ArrayList<>();
        String dateStr = date.toString();

        if (isWeekend) {
            // CUỐI TUẦN: 3 ca - thời gian dài hơn
            shifts.add(Shift.builder()
                    .name("Morning - " + dateStr)
                    .workDate(date)
                    .startTime(LocalTime.of(7, 0))
                    .endTime(LocalTime.of(14, 0))  // 7 giờ
                    .isWeekend(true)
                    .shiftStatus("EMPTY")
                    .note("Morning Weekend - 7 hours")
                    .build());

            shifts.add(Shift.builder()
                    .name("Afternoon - " + dateStr)
                    .workDate(date)
                    .startTime(LocalTime.of(14, 0))
                    .endTime(LocalTime.of(23, 59))  // 7 giờ
                    .isWeekend(true)
                    .shiftStatus("EMPTY")
                    .note("Afternoon Weekend - 10 hours")
                    .build());

            shifts.add(Shift.builder()
                    .name("Full Day - " + dateStr)
                    .workDate(date)
                    .startTime(LocalTime.of(8, 0))
                    .endTime(LocalTime.of(17, 0))  // 9 giờ
                    .isWeekend(true)
                    .shiftStatus("EMPTY")
                    .note("Full Day Weekend - 9 hours")
                    .build());
        } else {
            // NGÀY THƯỜNG: 5 ca - thời gian ngắn hơn
            shifts.add(Shift.builder()
                    .name("Morning Early - " + dateStr)
                    .workDate(date)
                    .startTime(LocalTime.of(6, 0))
                    .endTime(LocalTime.of(10, 0))  // 4 giờ
                    .isWeekend(false)
                    .shiftStatus("EMPTY")
                    .note("Morning Early - 4 hours")
                    .build());

            shifts.add(Shift.builder()
                    .name("Morning - " + dateStr)
                    .workDate(date)
                    .startTime(LocalTime.of(8, 0))
                    .endTime(LocalTime.of(12, 0))  // 4 giờ
                    .isWeekend(false)
                    .shiftStatus("EMPTY")
                    .note("Morning - 4 hours")
                    .build());

            shifts.add(Shift.builder()
                    .name("Noon - " + dateStr)
                    .workDate(date)
                    .startTime(LocalTime.of(12, 0))
                    .endTime(LocalTime.of(16, 0))  // 4 giờ
                    .isWeekend(false)
                    .shiftStatus("EMPTY")
                    .note("Noon - 4 hours")
                    .build());

            shifts.add(Shift.builder()
                    .name("Afternoon - " + dateStr)
                    .workDate(date)
                    .startTime(LocalTime.of(14, 0))
                    .endTime(LocalTime.of(18, 0))  // 4 giờ
                    .isWeekend(false)
                    .shiftStatus("EMPTY")
                    .note("Afternoon - 4 hours")
                    .build());

            shifts.add(Shift.builder()
                    .name("Evening - " + dateStr)
                    .workDate(date)
                    .startTime(LocalTime.of(18, 0))
                    .endTime(LocalTime.of(22, 0))  // 4 giờ
                    .isWeekend(false)
                    .shiftStatus("EMPTY")
                    .note("Evening - 4 hours")
                    .build());
        }

        return shiftRepo.saveAll(shifts);
    }

    public List<ShiftDTO> getWeeklySchedule(LocalDate startDate) {
        LocalDate endDate = startDate.plusDays(6);
        List<Shift> shifts = shiftRepo.findByWorkDateBetween(startDate, endDate);
        List<ShiftRegistration> registrations = registrationRepo.findByShiftIn(shifts);

        Map<String, List<RegistrationDTO>> registrationMap = registrations.stream()
                .collect(Collectors.groupingBy(
                        reg -> reg.getShift().getShiftId(),
                        Collectors.mapping(reg -> RegistrationDTO.builder()
                                        .registrationId(reg.getRegistrationId())
                                        .shiftId(reg.getShift().getShiftId())
                                        .employeeId(reg.getEmployee().getEmployeeId())
                                        .note(reg.getNote())
                                        .isAutoAssign(reg.isAutoAssigned())
                                        .updated_at(reg.getUpdatedAt())
                                        .created_at(reg.getCreatedAt())
                                        .build(),
                                Collectors.toList())
                ));

        return shifts.stream()
                .map(shift -> ShiftDTO.builder()
                        .shiftId(shift.getShiftId())
                        .name(shift.getName())
                        .startTime(shift.getStartTime().toString())
                        .endTime(shift.getEndTime().toString())
                        .note(shift.getNote())
                        .shiftStatus(shift.getShiftStatus())
                        .isWeekend(shift.isWeekend())
                        .registrations(registrationMap.getOrDefault(shift.getShiftId(), Collections.emptyList()))
                        .build())
                .collect(Collectors.toList());
    }

    public List<ShiftDTO> getRegisteredShiftsOf(String employeeId) {
        var regs = registrationRepo.findAllWithShiftByEmployeeId(employeeId);
        return regs.stream()
                .map(reg -> {
                    var s = reg.getShift();
                    return ShiftDTO.builder()
                            .shiftId(s.getShiftId())
                            .name(s.getName())
                            .startTime(s.getStartTime().toString())
                            .endTime(s.getEndTime().toString())
                            .note(s.getNote())
                            .shiftStatus(s.getShiftStatus())
                            .isWeekend(s.isWeekend())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<LocalDate> getCreatedWeeks() {
        List<Shift> allShifts = shiftRepo.findAll();

        // Nhóm theo tuần và lấy thứ 2 của mỗi tuần
        Set<LocalDate> mondays = allShifts.stream()
                .map(shift -> {
                    LocalDate date = shift.getWorkDate();
                    int dayOfWeek = date.getDayOfWeek().getValue();
                    return date.minusDays(dayOfWeek - 1); // 1 = Monday
                })
                .collect(Collectors.toSet());

        return mondays.stream()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    public Map<String, Integer> getEmployeeShiftCountsForWeek(LocalDate startDate) {
        LocalDate endDate = startDate.plusDays(6);

        List<Shift> shifts = shiftRepo.findByWorkDateBetween(startDate, endDate);
        if (shifts.isEmpty()) return new HashMap<>();

        List<ShiftRegistration> registrations = registrationRepo.findByShiftIn(shifts);

        Map<String, Integer> employeeShiftCounts = new HashMap<>();
        registrations.forEach(registration -> {
            if (registration.getEmployee() != null) {
                String employeeId = registration.getEmployee().getEmployeeId();
                employeeShiftCounts.merge(employeeId, 1, Integer::sum);
            }
        });

        return employeeShiftCounts;
    }

    @Transactional
    public ShiftRegistration registerShift(String accountId, String shiftId) {
        Employee employee = employeeRepo.findByAccount_AccountId(accountId);
        if (employee == null) throw new RuntimeException("Employee not found: " + accountId);

        Shift shift = shiftRepo.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found: " + shiftId));

        // Đã đăng ký ca này?
        Optional<ShiftRegistration> existing = registrationRepo.findByShiftAndEmployee(shift, employee);
        if (existing.isPresent()) throw new RuntimeException("Employee already registered for this shift");

        // ====== RÀNG BUỘC CHO TỰ ĐĂNG KÝ ======
        LocalDate weekStart = shift.getWorkDate().with(DayOfWeek.MONDAY);
        LocalDate weekEnd   = weekStart.plusDays(6);

        List<Shift> weekShifts = shiftRepo.findByWorkDateBetween(weekStart, weekEnd);
        List<ShiftRegistration> weekRegs = registrationRepo.findByShiftIn(weekShifts).stream()
                .filter(r -> r.getEmployee() != null
                        && r.getEmployee().getEmployeeId().equals(employee.getEmployeeId()))
                .collect(Collectors.toList());

        // 1) Tối đa 6 ca/tuần
        if (weekRegs.size() + 1 > MAX_SHIFTS_PER_WEEK) {
            throw new RuntimeException("Weekly shift limit exceeded");
        }

        // 2) Tối đa 40 giờ/tuần
        double currentHours = weekRegs.stream()
                .map(ShiftRegistration::getShift)
                .mapToDouble(this::calculateShiftHours)
                .sum();
        if (currentHours + calculateShiftHours(shift) > MAX_HOURS_PER_WEEK) {
            throw new RuntimeException("Weekly hour limit exceeded");
        }

        // Danh sách các ca của riêng nhân viên trong tuần
        List<Shift> myWeekShifts = weekRegs.stream()
                .map(ShiftRegistration::getShift)
                .collect(Collectors.toList());

        // 3) Không trùng giờ
        boolean conflict = myWeekShifts.stream().anyMatch(s -> shiftsOverlap(s, shift));
        if (conflict) throw new RuntimeException("Shift time conflict");

        // 4) Nghỉ tối thiểu 12h giữa 2 ca (so sánh theo chiều thời gian đúng)
        for (Shift s : myWeekShifts) {
            if (!hasEnoughRestTimeBetween(s, shift)) {
                throw new RuntimeException("Not enough rest time");
            }
        }

        // 5) Không quá 5 ngày liên tiếp
        if (exceedsConsecutiveDaysWithNew(myWeekShifts, shift)) {
            throw new RuntimeException("Consecutive day limit exceeded");
        }

        ShiftRegistration registration = ShiftRegistration.builder()
                .employee(employee)
                .shift(shift)
                .note("Self-registered")
                .isAutoAssigned(false)
                .build();

        registrationRepo.save(registration);
        updateShiftStatus(shift);

        log.info("Employee {} registered for shift {}", employee.getEmployeeId(), shift.getName());
        return registration;
    }

    @Transactional
    public void cancelRegistration(String registrationId) {
        ShiftRegistration registration = registrationRepo.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found"));

        Shift shift = registration.getShift();
        registrationRepo.delete(registration);
        updateShiftStatus(shift);

        log.info("Cancelled registration {}", registrationId);
    }

    @Transactional
    public ShiftRegistration assignEmployeeToShift(String employeeId, String shiftId) {
        Employee employee = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + employeeId));

        Shift shift = shiftRepo.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found: " + shiftId));

        Optional<ShiftRegistration> existing = registrationRepo.findByShiftAndEmployee(shift, employee);
        if (existing.isPresent()) throw new RuntimeException("Employee already assigned to this shift");

        ShiftRegistration registration = ShiftRegistration.builder()
                .employee(employee)
                .shift(shift)
                .note("Manually assigned")
                .isAutoAssigned(false)
                .build();

        registrationRepo.save(registration);
        updateShiftStatus(shift);

        log.info("Manually assigned employee {} to shift {}", employeeId, shift.getName());
        return registration;
    }

    @Transactional
    public AssignmentResult autoAssignShiftsForWeek(LocalDate startDate) {
        LocalDate endDate = startDate.plusDays(6);

        List<Shift> shifts = shiftRepo.findByWorkDateBetween(startDate, endDate);
        if (shifts.isEmpty()) throw new RuntimeException("No shifts found for week starting " + startDate);

        List<Employee> allEmployees = employeeRepo.findAll();
        if (allEmployees.isEmpty()) throw new RuntimeException("No employees available in system");

        List<ShiftRegistration> existingRegistrations = registrationRepo.findByShiftIn(shifts);

        Map<String, Set<String>> shiftAssignments = buildShiftAssignmentsMap(existingRegistrations);
        Map<String, Set<String>> employeeAssignedShifts = buildEmployeeAssignmentsMap(existingRegistrations);
        Map<String, Integer> employeeShiftCount = new HashMap<>();
        Map<String, Double> employeeHoursCount = new HashMap<>();

        calculateCurrentWorkload(allEmployees, employeeAssignedShifts, shifts, employeeShiftCount, employeeHoursCount);

        shifts.sort(Comparator.comparing(Shift::getWorkDate).thenComparing(Shift::getStartTime));

        List<ShiftRegistration> newAssignments = new ArrayList<>();
        Map<String, String> unfulfilledShifts = new HashMap<>();

        for (Shift shift : shifts) {
            processShiftAssignment(
                    shift,
                    allEmployees,
                    shiftAssignments,
                    employeeAssignedShifts,
                    employeeShiftCount,
                    employeeHoursCount,
                    shifts,
                    newAssignments,
                    unfulfilledShifts
            );
        }

        if (!newAssignments.isEmpty()) {
            registrationRepo.saveAll(newAssignments);
            log.info("Auto-assigned {} new registrations", newAssignments.size());
        }

        shifts.forEach(this::updateShiftStatus);

        return buildAssignmentResult(shifts, newAssignments, existingRegistrations,
                unfulfilledShifts, employeeShiftCount, employeeHoursCount, allEmployees);
    }

    @Transactional
    public void deleteWeek(LocalDate startDate) {
        if (startDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new IllegalArgumentException("Start date must be Monday! Provided: " + startDate.getDayOfWeek());
        }
        LocalDate endDate = startDate.plusDays(6);

        List<Shift> shifts = shiftRepo.findByWorkDateBetween(startDate, endDate);
        if (shifts.isEmpty()) {
            log.info("No shifts found to delete for week starting {}", startDate);
            return; // Không ném lỗi để FE nhận 204 kể cả khi tuần trống
        }

        List<ShiftRegistration> regs = registrationRepo.findByShiftIn(shifts);
        if (!regs.isEmpty()) {
            registrationRepo.deleteAllInBatch(regs);
        }

        shiftRepo.deleteAllInBatch(shifts);
        log.info("Deleted weekly schedule: {} - {}", startDate, endDate);
    }

    private void processShiftAssignment(
            Shift shift,
            List<Employee> allEmployees,
            Map<String, Set<String>> shiftAssignments,
            Map<String, Set<String>> employeeAssignedShifts,
            Map<String, Integer> employeeShiftCount,
            Map<String, Double> employeeHoursCount,
            List<Shift> allShifts,
            List<ShiftRegistration> newAssignments,
            Map<String, String> unfulfilledShifts
    ) {
        int requiredStaff = shift.isWeekend() ? 3 : 2;
        int currentAssigned = shiftAssignments.getOrDefault(shift.getShiftId(), Collections.emptySet()).size();
        int needed = requiredStaff - currentAssigned;

        if (needed <= 0) return;

        List<Employee> availableEmployees = findAvailableEmployees(
                allEmployees, shift, shiftAssignments, employeeAssignedShifts,
                employeeShiftCount, employeeHoursCount, allShifts
        );

        if (availableEmployees.size() < needed) {
            unfulfilledShifts.put(shift.getShiftId(),
                    String.format("Shift %s on %s (%s-%s) needs %d staff but only %d available",
                            shift.getName(), shift.getWorkDate(),
                            shift.getStartTime(), shift.getEndTime(),
                            needed, availableEmployees.size()));
            needed = availableEmployees.size();
        }

        assignEmployeesToShift(
                shift,
                availableEmployees,
                needed,
                newAssignments,
                shiftAssignments,
                employeeAssignedShifts,
                employeeShiftCount,
                employeeHoursCount
        );
    }

    private List<Employee> findAvailableEmployees(
            List<Employee> allEmployees,
            Shift shift,
            Map<String, Set<String>> shiftAssignments,
            Map<String, Set<String>> employeeAssignedShifts,
            Map<String, Integer> employeeShiftCount,
            Map<String, Double> employeeHoursCount,
            List<Shift> allShifts
    ) {
        return allEmployees.stream()
                .filter(emp -> canAssignEmployeeToShift(
                        emp, shift, shiftAssignments, employeeAssignedShifts,
                        employeeShiftCount, employeeHoursCount, allShifts))
                .sorted(Comparator.comparingInt(emp -> employeeShiftCount.getOrDefault(emp.getEmployeeId(), 0)))
                .collect(Collectors.toList());
    }

    private boolean canAssignEmployeeToShift(
            Employee emp,
            Shift shift,
            Map<String, Set<String>> shiftAssignments,
            Map<String, Set<String>> employeeAssignedShifts,
            Map<String, Integer> employeeShiftCount,
            Map<String, Double> employeeHoursCount,
            List<Shift> allShifts
    ) {
        String empId = emp.getEmployeeId();

        // đã ở shift này
        if (shiftAssignments.getOrDefault(shift.getShiftId(), Collections.emptySet()).contains(empId)) {
            return false;
        }

        // quá số ca/tuần
        if (employeeShiftCount.getOrDefault(empId, 0) >= MAX_SHIFTS_PER_WEEK) {
            return false;
        }

        // quá số giờ/tuần
        double currentHours = employeeHoursCount.getOrDefault(empId, 0.0);
        double shiftHours = calculateShiftHours(shift);
        if (currentHours + shiftHours > MAX_HOURS_PER_WEEK) {
            return false;
        }

        // trùng giờ / không đủ nghỉ / vượt số ngày liên tiếp
        if (hasConflictingShift(emp, shift, employeeAssignedShifts, allShifts)) return false;
        if (!hasEnoughRestTime(emp, shift, employeeAssignedShifts, allShifts)) return false;
        return !exceedsConsecutiveDays(emp, shift, employeeAssignedShifts, allShifts);
    }

    private void assignEmployeesToShift(
            Shift shift,
            List<Employee> employees,
            int count,
            List<ShiftRegistration> newAssignments,
            Map<String, Set<String>> shiftAssignments,
            Map<String, Set<String>> employeeAssignedShifts,
            Map<String, Integer> employeeShiftCount,
            Map<String, Double> employeeHoursCount
    ) {
        for (int i = 0; i < count && i < employees.size(); i++) {
            Employee emp = employees.get(i);

            ShiftRegistration registration = ShiftRegistration.builder()
                    .employee(emp)
                    .shift(shift)
                    .note("Auto-assigned by system")
                    .isAutoAssigned(true)
                    .build();

            newAssignments.add(registration);

            // cập nhật tracking
            shiftAssignments
                    .computeIfAbsent(shift.getShiftId(), k -> new HashSet<>())
                    .add(emp.getEmployeeId());
            employeeAssignedShifts
                    .computeIfAbsent(emp.getEmployeeId(), k -> new HashSet<>())
                    .add(shift.getShiftId());
            employeeShiftCount.merge(emp.getEmployeeId(), 1, Integer::sum);
            employeeHoursCount.merge(emp.getEmployeeId(), calculateShiftHours(shift), Double::sum);
        }
    }

    private boolean hasEnoughRestTimeOneToAnother(Shift a, Shift b) {
        long daysDiff = Math.abs(java.time.temporal.ChronoUnit.DAYS.between(a.getWorkDate(), b.getWorkDate()));
        if (daysDiff > 1) return true;

        java.time.LocalDateTime aEnd = java.time.LocalDateTime.of(a.getWorkDate(), a.getEndTime());
        java.time.LocalDateTime bStart = java.time.LocalDateTime.of(b.getWorkDate(), b.getStartTime());
        long ab = java.time.Duration.between(aEnd, bStart).toHours();

        return ab >= MIN_REST_HOURS;
    }

    private boolean hasEnoughRestTimeBetween(Shift a, Shift b) {
        java.time.LocalDateTime aStart = java.time.LocalDateTime.of(a.getWorkDate(), a.getStartTime());
        java.time.LocalDateTime aEnd   = java.time.LocalDateTime.of(a.getWorkDate(), a.getEndTime());
        java.time.LocalDateTime bStart = java.time.LocalDateTime.of(b.getWorkDate(), b.getStartTime());
        java.time.LocalDateTime bEnd   = java.time.LocalDateTime.of(b.getWorkDate(), b.getEndTime());

        if (!(aEnd.isBefore(bStart) || bEnd.isBefore(aStart))) return false;

        long gapHours = aEnd.isBefore(bStart)
                ? java.time.Duration.between(aEnd, bStart).toHours()
                : java.time.Duration.between(bEnd, aStart).toHours();

        return gapHours >= MIN_REST_HOURS;
    }

    private boolean exceedsConsecutiveDaysWithNew(List<Shift> existing, Shift newShift) {
        Set<LocalDate> dates = existing.stream().map(Shift::getWorkDate).collect(Collectors.toSet());
        dates.add(newShift.getWorkDate());

        List<LocalDate> sorted = dates.stream().sorted().collect(Collectors.toList());
        int streak = 1, maxStreak = 1;
        for (int i = 1; i < sorted.size(); i++) {
            if (sorted.get(i).equals(sorted.get(i - 1).plusDays(1))) {
                streak++;
                maxStreak = Math.max(maxStreak, streak);
            } else {
                streak = 1;
            }
        }
        return maxStreak > MAX_CONSECUTIVE_DAYS;
    }

    private boolean hasConflictingShift(Employee emp, Shift newShift,
                                        Map<String, Set<String>> employeeAssignedShifts, List<Shift> allShifts) {
        Set<String> assignedShiftIds = employeeAssignedShifts.getOrDefault(emp.getEmployeeId(), Collections.emptySet());
        if (assignedShiftIds.isEmpty()) return false;

        return allShifts.stream()
                .filter(s -> s.getWorkDate().equals(newShift.getWorkDate()))
                .filter(s -> assignedShiftIds.contains(s.getShiftId()))
                .anyMatch(existingShift -> shiftsOverlap(existingShift, newShift));
    }

    private boolean hasEnoughRestTime(Employee emp, Shift newShift,
                                      Map<String, Set<String>> employeeAssignedShifts, List<Shift> allShifts) {
        Set<String> assignedShiftIds = employeeAssignedShifts.getOrDefault(emp.getEmployeeId(), Collections.emptySet());
        if (assignedShiftIds.isEmpty()) return true;

        List<Shift> assignedShifts = allShifts.stream()
                .filter(s -> assignedShiftIds.contains(s.getShiftId()))
                .collect(Collectors.toList());

        for (Shift existingShift : assignedShifts) {
            long daysDiff = Math.abs(java.time.temporal.ChronoUnit.DAYS.between(
                    existingShift.getWorkDate(), newShift.getWorkDate()));
            if (daysDiff > 1) continue;

            java.time.LocalDateTime end1 = java.time.LocalDateTime.of(existingShift.getWorkDate(), existingShift.getEndTime());
            java.time.LocalDateTime start2 = java.time.LocalDateTime.of(newShift.getWorkDate(), newShift.getStartTime());
            long hoursBetween = java.time.Duration.between(end1, start2).toHours();

            if (Math.abs(hoursBetween) < MIN_REST_HOURS) {
                return false;
            }
        }
        return true;
    }

    private boolean exceedsConsecutiveDays(Employee emp, Shift newShift,
                                           Map<String, Set<String>> employeeAssignedShifts, List<Shift> allShifts) {
        Set<String> assignedShiftIds = employeeAssignedShifts.getOrDefault(emp.getEmployeeId(), Collections.emptySet());
        if (assignedShiftIds.isEmpty()) return false;

        Set<LocalDate> workingDates = allShifts.stream()
                .filter(s -> assignedShiftIds.contains(s.getShiftId()))
                .map(Shift::getWorkDate)
                .collect(Collectors.toSet());
        workingDates.add(newShift.getWorkDate());

        List<LocalDate> sortedDates = workingDates.stream().sorted().collect(Collectors.toList());

        int consecutiveDays = 1, maxConsecutive = 1;
        for (int i = 1; i < sortedDates.size(); i++) {
            if (sortedDates.get(i).equals(sortedDates.get(i - 1).plusDays(1))) {
                consecutiveDays++;
                maxConsecutive = Math.max(maxConsecutive, consecutiveDays);
            } else {
                consecutiveDays = 1;
            }
        }
        return maxConsecutive > MAX_CONSECUTIVE_DAYS;
    }

    private boolean shiftsOverlap(Shift s1, Shift s2) {
        if (!s1.getWorkDate().equals(s2.getWorkDate())) {
            return false;
        }

        LocalTime s1Start = s1.getStartTime();
        LocalTime s1End = s1.getEndTime();
        LocalTime s2Start = s2.getStartTime();
        LocalTime s2End = s2.getEndTime();

        return s1Start.isBefore(s2End) && s2Start.isBefore(s1End);
    }

    private double calculateShiftHours(Shift shift) {
        long minutes = java.time.Duration.between(shift.getStartTime(), shift.getEndTime()).toMinutes();
        return minutes / 60.0;
    }

    private void updateShiftStatus(Shift shift) {
        long registrationCount = registrationRepo.countByShift(shift);
        int required = shift.isWeekend() ? 3 : 2;

        if (registrationCount == 0) {
            shift.setShiftStatus("EMPTY");
        } else if (registrationCount < required) {
            shift.setShiftStatus("PARTIAL");
        } else {
            shift.setShiftStatus("FULL");
        }
        shiftRepo.save(shift);
    }

    private Map<String, Set<String>> buildShiftAssignmentsMap(List<ShiftRegistration> registrations) {
        Map<String, Set<String>> map = new HashMap<>();
        registrations.forEach(reg -> {
            if (reg.getEmployee() != null) {
                map.computeIfAbsent(reg.getShift().getShiftId(), k -> new HashSet<>())
                        .add(reg.getEmployee().getEmployeeId());
            }
        });
        return map;
    }

    private Map<String, Set<String>> buildEmployeeAssignmentsMap(List<ShiftRegistration> registrations) {
        Map<String, Set<String>> map = new HashMap<>();
        registrations.forEach(reg -> {
            if (reg.getEmployee() != null) {
                map.computeIfAbsent(reg.getEmployee().getEmployeeId(), k -> new HashSet<>())
                        .add(reg.getShift().getShiftId());
            }
        });
        return map;
    }

    private void calculateCurrentWorkload(
            List<Employee> employees,
            Map<String, Set<String>> employeeAssignedShifts,
            List<Shift> shifts,
            Map<String, Integer> shiftCount,
            Map<String, Double> hoursCount
    ) {
        employees.forEach(emp -> {
            Set<String> assignedShiftIds = employeeAssignedShifts.getOrDefault(emp.getEmployeeId(), Collections.emptySet());
            shiftCount.put(emp.getEmployeeId(), assignedShiftIds.size());

            double totalHours = shifts.stream()
                    .filter(s -> assignedShiftIds.contains(s.getShiftId()))
                    .mapToDouble(this::calculateShiftHours)
                    .sum();
            hoursCount.put(emp.getEmployeeId(), totalHours);
        });
    }

    private AssignmentResult buildAssignmentResult(
            List<Shift> shifts,
            List<ShiftRegistration> newAssignments,
            List<ShiftRegistration> existingRegistrations,
            Map<String, String> unfulfilledShifts,
            Map<String, Integer> employeeShiftCount,
            Map<String, Double> employeeHoursCount,
            List<Employee> allEmployees
    ) {
        int totalApprovedCount = (int) existingRegistrations.stream()
                .filter(r -> r.getEmployee() != null)
                .count() + newAssignments.size();

        return AssignmentResult.builder()
                .totalShifts(shifts.size())
                .assignedCount(newAssignments.size())
                .totalApprovedCount(totalApprovedCount)
                .unfulfilledShifts(unfulfilledShifts)
                .employeeWorkload(employeeShiftCount)
                .employeeHoursWorked(employeeHoursCount)
                .totalEmployees(allEmployees.size())
                .constraints(Map.of(
                        "maxShiftsPerWeek", MAX_SHIFTS_PER_WEEK,
                        "maxHoursPerWeek", MAX_HOURS_PER_WEEK,
                        "maxConsecutiveDays", MAX_CONSECUTIVE_DAYS,
                        "minRestHours", MIN_REST_HOURS
                ))
                .build();
    }
}

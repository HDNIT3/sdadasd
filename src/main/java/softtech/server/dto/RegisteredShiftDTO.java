package softtech.server.dto;
import java.time.LocalDate;
import java.time.LocalTime;

public class RegisteredShiftDTO {
    private String shiftId;
    private String name;
    private LocalDate workDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean weekend;
    private String note;
    private boolean autoAssigned;

    public RegisteredShiftDTO(String shiftId, String name, LocalDate workDate, LocalTime startTime, LocalTime endTime,
                              boolean weekend, String note, boolean autoAssigned) {
        this.shiftId = shiftId;
        this.name = name;
        this.workDate = workDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.weekend = weekend;
        this.note = note;
        this.autoAssigned = autoAssigned;
    }

    public String getShiftId() { return shiftId; }
    public String getName() { return name; }
    public LocalDate getWorkDate() { return workDate; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public boolean isWeekend() { return weekend; }
    public String getNote() { return note; }
    public boolean isAutoAssigned() { return autoAssigned; }
}

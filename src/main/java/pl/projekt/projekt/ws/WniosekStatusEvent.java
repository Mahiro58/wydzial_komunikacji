package pl.projekt.projekt.ws;

import pl.projekt.projekt.entity.StatusWniosku;

import java.time.Instant;

public class WniosekStatusEvent {
    private String type; // STATUS_CHANGED
    private Long wniosekId;
    private StatusWniosku oldStatus;
    private StatusWniosku newStatus;
    private Instant ts;

    public WniosekStatusEvent() {}

    public WniosekStatusEvent(Long wniosekId, StatusWniosku oldStatus, StatusWniosku newStatus) {
        this.type = "STATUS_CHANGED";
        this.wniosekId = wniosekId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.ts = Instant.now();
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getWniosekId() { return wniosekId; }
    public void setWniosekId(Long wniosekId) { this.wniosekId = wniosekId; }

    public StatusWniosku getOldStatus() { return oldStatus; }
    public void setOldStatus(StatusWniosku oldStatus) { this.oldStatus = oldStatus; }

    public StatusWniosku getNewStatus() { return newStatus; }
    public void setNewStatus(StatusWniosku newStatus) { this.newStatus = newStatus; }

    public Instant getTs() { return ts; }
    public void setTs(Instant ts) { this.ts = ts; }
}

package com.futapp.exception;

import java.time.LocalDateTime;
import org.slf4j.MDC;

public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String message;
    private String requestId;

    public ErrorResponse(int status, String message) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.message = message;
        this.requestId = MDC.get("requestId");
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getRequestId() { return requestId; }
}

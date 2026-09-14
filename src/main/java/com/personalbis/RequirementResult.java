package com.personalbis;

public class RequirementResult
{
    public enum Status
    {
        USABLE,
        BLOCKED,
        UNVERIFIED
    }

    private final Status status;
    private final String message;

    private RequirementResult(Status status, String message)
    {
        this.status = status;
        this.message = message;
    }

    public static RequirementResult usable(String message)
    {
        return new RequirementResult(Status.USABLE, message);
    }

    public static RequirementResult blocked(String message)
    {
        return new RequirementResult(Status.BLOCKED, message);
    }

    public static RequirementResult unverified(String message)
    {
        return new RequirementResult(Status.UNVERIFIED, message);
    }

    public Status getStatus() { return status; }
    public String getMessage() { return message; }
    public boolean isBlocked() { return status == Status.BLOCKED; }
}

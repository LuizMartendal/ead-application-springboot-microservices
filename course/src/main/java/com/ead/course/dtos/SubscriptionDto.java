package com.ead.course.dtos;

import javax.validation.constraints.NotNull;
import java.util.UUID;

public class SubscriptionDto {

    public SubscriptionDto() {}

    public SubscriptionDto(UUID userId) {
        this.userId = userId;
    }

    @NotNull
    private UUID userId;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}

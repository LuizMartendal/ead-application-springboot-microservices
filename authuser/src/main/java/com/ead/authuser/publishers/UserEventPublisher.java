package com.ead.authuser.publishers;


import com.ead.authuser.dtos.UserEventDto;
import com.ead.authuser.enums.ActionType;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class UserEventPublisher {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Value(value = "${ead.broker.exchange.userEvent}")
    private String exchangeUserEvent;

    public void publishUserEvent(UserEventDto userEventDto, ActionType actionType) {
        userEventDto.setActionType(actionType.toString());
        CompletableFuture.runAsync(() -> {
            rabbitTemplate.convertAndSend(exchangeUserEvent, "", userEventDto);
        });
    }
}

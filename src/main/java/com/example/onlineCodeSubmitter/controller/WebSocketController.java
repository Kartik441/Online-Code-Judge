package com.example.onlineCodeSubmitter.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.DestinationVariable;

@Controller
public class WebSocketController {

    @MessageMapping("/submitCodeResult")
    @SendTo("/topic/result/{userId}/{questionId}")
    public String submitCode(String result, @DestinationVariable String userId, @DestinationVariable String questionId) {
        System.out.println("Received result for userID "+userId +" questionId "+questionId +" -> "+result);
        return result;
    }
}

package com.substring.chat.controllers;

import com.substring.chat.entities.Message;
import com.substring.chat.entities.Room;
import com.substring.chat.playload.MessageRequest;
import com.substring.chat.playload.UserRoomContext;
import com.substring.chat.repositories.RoomRepository;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ChatController {

    private final RoomRepository roomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // sessionID -> UserContext mapping for abrupt disconnect tracking
    private final ConcurrentHashMap<String, UserRoomContext> activeUsers = new ConcurrentHashMap<>();

    public ChatController(RoomRepository roomRepository, SimpMessagingTemplate messagingTemplate) {
        this.roomRepository = roomRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // for sending and receiving messages
    @MessageMapping("/sendMessage/{roomId}")// /app/sendMessage/roomId
    @SendTo("/topic/room/{roomId}")//subscribe
    public Message sendMessage(
            @DestinationVariable String roomId,
            @RequestBody MessageRequest request
    ) {
        Room room = roomRepository.findByRoomId(request.getRoomId());
        Message message = new Message();
        message.setContent(request.getContent());
        message.setSender(request.getSender());
        message.setTimeStamp(LocalDateTime.now());
        
        if (room != null) {
            room.getMessages().add(message);
            roomRepository.save(room);
        } else {
            throw new RuntimeException("room not found !!");
        }

        return message;
    }

    // handle user join
    @MessageMapping("/joinRoom/{roomId}")
    public void joinRoom(@DestinationVariable String roomId, @RequestBody MessageRequest request, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        activeUsers.put(sessionId, new UserRoomContext(roomId, request.getSender()));

        // broadcast SYSTEM join message
        Message message = new Message();
        message.setSender("SYSTEM");
        message.setContent(request.getSender() + " joined the room");
        message.setTimeStamp(LocalDateTime.now());
        messagingTemplate.convertAndSend("/topic/room/" + roomId, message);

        // broadcast updated active users count
        broadcastOnlineCount(roomId);
    }

    // handle abrupt user disconnect explicitly
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        UserRoomContext context = activeUsers.remove(sessionId);

        if (context != null) {
            String roomId = context.getRoomId();
            String username = context.getUsername();

            Message message = new Message();
            message.setSender("SYSTEM");
            message.setContent(username + " left the room");
            message.setTimeStamp(LocalDateTime.now());

            messagingTemplate.convertAndSend("/topic/room/" + roomId, message);
            broadcastOnlineCount(roomId);
        }
    }

    private void broadcastOnlineCount(String roomId) {
        long count = activeUsers.values().stream().filter(c -> c.getRoomId().equals(roomId)).count();
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/users", count);
    }
}

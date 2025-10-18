package softtech.server.services;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import softtech.server.dto.EventDTO;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastNewEvent(EventDTO event) {
        // FE subscribe /topic/events để nhận thông báo mới
        messagingTemplate.convertAndSend("/topic/events", event);
    }

}
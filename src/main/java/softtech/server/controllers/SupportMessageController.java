package softtech.server.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import softtech.server.dto.AuthDTO.MesDTO;

@RestController
@RequestMapping("/api/support-messages")
@CrossOrigin(origins = "https://zzzzz-production.up.railway.app")
public class SupportMessageController {
	private List<MesDTO> messages = new ArrayList<>();
	
	@PostMapping("/receive-message")
	public List<MesDTO> receiveMessage(@RequestBody MesDTO mesDTO) {
		messages.add(mesDTO);
		return messages;
	}
	
	@GetMapping("/get-messages")
	public List<MesDTO> getMessages() {
		return messages;
	}
	
}

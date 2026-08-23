package br.edu.cpa.cpainteligente.controller;

import br.edu.cpa.cpainteligente.dto.response.BotFlowResult;
import br.edu.cpa.cpainteligente.security.HashUtil;
import br.edu.cpa.cpainteligente.service.BotFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/dev/agent")
@RequiredArgsConstructor
public class AgentTestController {

    private final BotFlowService botFlowService;
    private final HashUtil hashUtil;

    @PostMapping("/chat")
    public Map<String, String> chat(
            @RequestParam String phone,
            @RequestParam String institutionId,
            @RequestBody Map<String, String> body
    ) {
        String phoneHash = hashUtil.hash(phone);
        BotFlowResult result = botFlowService.handleIncomingMessage(
                institutionId, phoneHash, phone, body.get("message")
        );
        return Map.of("reply", result.reply() != null ? result.reply() : "");
    }
}
package br.edu.cpa.cpainteligente.controller;

import br.edu.cpa.cpainteligente.dto.response.BotFlowResult;
import br.edu.cpa.cpainteligente.security.HashUtil;
import br.edu.cpa.cpainteligente.service.BotFlowService;
import br.edu.cpa.cpainteligente.whatsapp.EvolutionClient;
import br.edu.cpa.cpainteligente.whatsapp.WebhookPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final BotFlowService botFlowService;
    private final EvolutionClient evolutionClient;
    private final HashUtil hashUtil;

    // ID da sua instituição — temporário até termos multi-tenant no webhook
    // Em produção isso viria da configuração do webhook ou de um header customizado
    @Value("${app.institution-id:}")
    private String defaultInstitutionId;

    @PostMapping("/br/edu/cpa/cpainteligente/whatsapp")
    public ResponseEntity<Void> receive(@RequestBody WebhookPayload payload) {

        // Ignora eventos que não sejam mensagens recebidas
        if (!"messages.upsert".equals(payload.event())) {
            return ResponseEntity.ok().build();
        }

        WebhookPayload.WebhookData data = payload.data();

        // Ignora mensagens enviadas pelo próprio bot
        if (data == null || data.key().fromMe()) {
            return ResponseEntity.ok().build();
        }

        // Extrai número do formato "5511999999999@s.whatsapp.net"
        String remoteJid = data.key().remoteJid();
        if (remoteJid == null || remoteJid.contains("@g.us")) {
            // Ignora mensagens de grupos
            return ResponseEntity.ok().build();
        }

        String phoneNumber = remoteJid.replace("@s.whatsapp.net", "");

        // Extrai texto da mensagem
        String messageText = data.message() != null
                ? data.message().extractText()
                : null;

        if (messageText == null || messageText.isBlank()) {
            return ResponseEntity.ok().build();
        }

        log.info("Mensagem recebida de {}: {}", phoneNumber, messageText);

        // Hash do número antes de qualquer operação (LGPD)
        String phoneHash = hashUtil.hash(phoneNumber);

        // Processa o fluxo conversacional
        BotFlowResult result = botFlowService.handleIncomingMessage(
                defaultInstitutionId,
                phoneHash,
                phoneNumber,
                messageText
        );

        // Envia resposta de volta ao aluno
        if (result.handled() && result.reply() != null) {
            evolutionClient.sendText(phoneNumber, result.reply());
        }

        // Sempre retorna 200 para a Evolution API
        // Se retornar erro, ela vai retentar o webhook
        return ResponseEntity.ok().build();
    }
}
package br.edu.cpa.cpainteligente.whatsapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// @JsonIgnoreProperties(ignoreUnknown = true) → ignora campos extras no JSON
// sem isso, qualquer campo não mapeado lança exceção
@JsonIgnoreProperties(ignoreUnknown = true)
public record WebhookPayload(
        String event,
        String instance,
        WebhookData data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WebhookData(
            MessageKey key,
            MessageContent message,
            String messageType,
            String pushName    // nome do contato no WhatsApp
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MessageKey(
            String remoteJid,  // número do remetente: "5511999999999@s.whatsapp.net"
            boolean fromMe
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MessageContent(
            String conversation,          // mensagem de texto simples
            ExtendedText extendedTextMessage // mensagem com formatação
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record ExtendedText(String text) {}

        // Extrai o texto de qualquer formato de mensagem
        public String extractText() {
            if (conversation != null) return conversation;
            if (extendedTextMessage != null) return extendedTextMessage.text();
            return null;
        }
    }
}
package br.edu.cpa.cpainteligente.whatsapp;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "ollamaChatModel"
        // Sem chatMemoryProvider — memória gerenciada pelo BotFlowService
)
public interface CpaAgent {

    @SystemMessage("""
        Você é um assistente de avaliação institucional CPA de uma universidade brasileira.
        Responda SEMPRE em português brasileiro, de forma breve e amigável.
        Nunca mencione que vai "chamar tools" ou "salvar dados" — isso é feito automaticamente.
        """)
    String generate(@UserMessage String prompt);
}
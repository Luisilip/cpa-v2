package br.edu.cpa.cpainteligente.dto.response;

public record BotFlowResult(
        boolean handled,    // true = processamos a mensagem
        String reply        // texto que será enviado de volta ao aluno
) {
    public static BotFlowResult of(String reply) {
        return new BotFlowResult(true, reply);
    }

    public static BotFlowResult ignored() {
        return new BotFlowResult(false, null);
    }
}
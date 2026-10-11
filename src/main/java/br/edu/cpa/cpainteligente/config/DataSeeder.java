package br.edu.cpa.cpainteligente.config;

import br.edu.cpa.cpainteligente.entity.*;
import br.edu.cpa.cpainteligente.entity.enums.Sentiment;
import br.edu.cpa.cpainteligente.entity.enums.SinaesAxis;
import br.edu.cpa.cpainteligente.repository.*;
import br.edu.cpa.cpainteligente.security.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

// CommandLineRunner: método run() executa uma vez, logo após o Spring Boot subir.
// Só faz algo se app.seed.enabled=true — em boot normal, fica inerte.
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final InstitutionRepository institutionRepository;
    private final StudentRepository studentRepository;
    private final QuestionnaireRepository questionnaireRepository;
    private final ResponseRepository responseRepository;
    private final AnswerRepository answerRepository;
    private final HashUtil hashUtil;

    @Value("${app.seed.enabled:false}")
    private boolean seedEnabled;

    @Value("${app.seed.student-count:300}")
    private int studentCount;

    // Marca os alunos fictícios para nunca confundir com dados reais
    private static final String SEED_MARKER = "@ficticio.cpa";
    // Seed fixa: toda vez que rodar do zero, gera os MESMOS 300 alunos (reprodutível)
    private static final Random RANDOM = new Random(42);

    private static final String[] FIRST_NAMES = {
            "Ana","Bruno","Carla","Diego","Elisa","Felipe","Gabriela","Heitor","Isabela","João",
            "Karen","Lucas","Mariana","Nicolas","Olivia","Pedro","Queila","Rafael","Sofia","Thiago"
    };
    private static final String[] LAST_NAMES = {
            "Silva","Santos","Oliveira","Souza","Rodrigues","Ferreira","Almeida","Pereira","Lima","Gomes"
    };
    private static final String[] COURSES = {
            "Engenharia de Software","Administração","Direito","Psicologia",
            "Ciência da Computação","Enfermagem","Arquitetura","Pedagogia"
    };

    private static final Map<SinaesAxis, String[]> POSITIVE_COMMENTS = Map.of(
            SinaesAxis.ENSINO, new String[]{"Professores muito qualificados e atenciosos.", "Aulas dinâmicas e bem estruturadas."},
            SinaesAxis.PESQUISA, new String[]{"Boas oportunidades de iniciação científica.", "Incentivo real à pesquisa."},
            SinaesAxis.EXTENSAO, new String[]{"Projetos de extensão relevantes para a comunidade.", "Gosto muito dos projetos sociais."},
            SinaesAxis.GESTAO, new String[]{"Coordenação sempre disponível.", "Boa organização administrativa."},
            SinaesAxis.INFRAESTRUTURA, new String[]{"Laboratórios bem equipados.", "Espaços de estudo confortáveis."}
    );
    private static final Map<SinaesAxis, String[]> NEGATIVE_COMMENTS = Map.of(
            SinaesAxis.ENSINO, new String[]{"Faltam mais aulas práticas.", "Alguns professores faltam muito."},
            SinaesAxis.PESQUISA, new String[]{"Quase não há bolsas disponíveis.", "Pouco incentivo à pesquisa."},
            SinaesAxis.EXTENSAO, new String[]{"Divulgação fraca dos projetos de extensão.", "Poucas vagas disponíveis."},
            SinaesAxis.GESTAO, new String[]{"Demora para resolver pendências.", "Comunicação institucional falha."},
            SinaesAxis.INFRAESTRUTURA, new String[]{"Salas de aula precisam de manutenção.", "Wi-Fi instável no campus."}
    );

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            return; // boot normal — não faz nada
        }

        long already = studentRepository.findAll().stream()
                .filter(s -> s.getEmail() != null && s.getEmail().endsWith(SEED_MARKER))
                .count();

        if (already >= studentCount) {
            log.info("Seed já aplicado ({} alunos fictícios). Pulando.", already);
            return;
        }

        Institution institution = institutionRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> institutionRepository.save(
                        Institution.builder().name("Instituição Demonstrativa CPA").build()));

        Questionnaire questionnaire = questionnaireRepository.findByInstitutionId(institution.getId())
                .stream().findFirst()
                .orElseGet(() -> createDefaultQuestionnaire(institution));

        log.info("Gerando {} alunos fictícios...", studentCount);

        for (int i = 0; i < studentCount; i++) {
            Student student = studentRepository.save(createFakeStudent(institution, i));

            // ~78% já responderam — simula taxa de participação realista, não 100%
            if (RANDOM.nextDouble() < 0.78) {
                simulateResponse(institution, student, questionnaire);
            }
        }

        log.info("Seed concluído: {} alunos fictícios criados.", studentCount);
    }

    private Questionnaire createDefaultQuestionnaire(Institution institution) {
        Questionnaire questionnaire = Questionnaire.builder()
                .title("Avaliação CPA 2026/1")
                .periodLabel("2026/1")
                .active(true)
                .institution(institution)
                .build();

        List<Question> questions = new ArrayList<>();
        int order = 1;
        for (SinaesAxis axis : SinaesAxis.values()) {
            questions.add(Question.builder()
                    .text("Como você avalia o eixo " + axis.getLabel() + "?")
                    .axis(axis)
                    .orderNum(order++)
                    .questionnaire(questionnaire)
                    .build());
        }
        questionnaire.setQuestions(questions);
        return questionnaireRepository.save(questionnaire);
    }

    private Student createFakeStudent(Institution institution, int index) {
        String first = FIRST_NAMES[RANDOM.nextInt(FIRST_NAMES.length)];
        String last = LAST_NAMES[RANDOM.nextInt(LAST_NAMES.length)];
        String fakePhone = "55119" + String.format("%08d", 10000000 + index);

        return Student.builder()
                .name(first + " " + last)
                .email((first + "." + last + index + SEED_MARKER).toLowerCase())
                .phoneHash(hashUtil.hash(fakePhone))
                .course(COURSES[RANDOM.nextInt(COURSES.length)])
                .className("T" + (1 + RANDOM.nextInt(4)))
                .period(String.valueOf(1 + RANDOM.nextInt(8)))
                .institution(institution)
                .build();
    }

    private void simulateResponse(Institution institution, Student student, Questionnaire questionnaire) {
        Response response = responseRepository.save(
                Response.builder()
                        .institution(institution)
                        .student(student)
                        .questionnaire(questionnaire)
                        .build()
        );

        for (Question question : questionnaire.getQuestions()) {
            int rating = weightedRandomRating();
            boolean withComment = RANDOM.nextDouble() < 0.35; // 35% deixam comentário
            String comment = null;
            Sentiment sentiment = null;

            if (withComment) {
                String[] pool = rating >= 4
                        ? POSITIVE_COMMENTS.get(question.getAxis())
                        : NEGATIVE_COMMENTS.get(question.getAxis());
                comment = pool[RANDOM.nextInt(pool.length)];
            }

            answerRepository.save(Answer.builder()
                    .response(response)
                    .question(question)
                    .rating(rating)
                    .comment(comment)
                    .build());
        }
    }

    // Distribuição enviesada para notas 3-5 — mais parecida com dados reais que uniforme 1-5
    private int weightedRandomRating() {
        double r = RANDOM.nextDouble();
        if (r < 0.05) return 1;
        if (r < 0.15) return 2;
        if (r < 0.40) return 3;
        if (r < 0.75) return 4;
        return 5;
    }
}
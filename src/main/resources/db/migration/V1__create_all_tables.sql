-- Instituições
CREATE TABLE institutions (
                              id         VARCHAR(36)  NOT NULL,
                              name       VARCHAR(255) NOT NULL,
                              created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                              updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                              CONSTRAINT pk_institutions PRIMARY KEY (id)
);

-- Usuários
CREATE TABLE users (
                       id             VARCHAR(36)  NOT NULL,
                       name           VARCHAR(255) NOT NULL,
                       email          VARCHAR(255) NOT NULL,
                       password_hash  VARCHAR(255) NOT NULL,
                       institution_id VARCHAR(36)  NOT NULL,
                       created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                       CONSTRAINT pk_users       PRIMARY KEY (id),
                       CONSTRAINT uk_users_email UNIQUE (email),
                       CONSTRAINT fk_users_inst  FOREIGN KEY (institution_id) REFERENCES institutions(id)
);

-- Alunos
CREATE TABLE students (
                          id             VARCHAR(36)  NOT NULL,
                          name           VARCHAR(255) NOT NULL,
                          email          VARCHAR(255),
                          phone_hash     VARCHAR(64),
                          course         VARCHAR(255),
                          class_name     VARCHAR(255),
                          period         VARCHAR(50),
                          institution_id VARCHAR(36)  NOT NULL,
                          created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                          CONSTRAINT pk_students      PRIMARY KEY (id),
                          CONSTRAINT fk_students_inst FOREIGN KEY (institution_id) REFERENCES institutions(id)
);

CREATE INDEX idx_students_phone_hash ON students(phone_hash);

-- Questionários
CREATE TABLE questionnaires (
                                id             VARCHAR(36)  NOT NULL,
                                title          VARCHAR(255) NOT NULL,
                                description    TEXT,
                                active         BOOLEAN      NOT NULL DEFAULT TRUE,
                                period_label   VARCHAR(50),
                                institution_id VARCHAR(36)  NOT NULL,
                                created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                                CONSTRAINT pk_questionnaires      PRIMARY KEY (id),
                                CONSTRAINT fk_questionnaires_inst FOREIGN KEY (institution_id) REFERENCES institutions(id)
);

-- Perguntas
CREATE TABLE questions (
                           id               VARCHAR(36)  NOT NULL,
                           text             TEXT         NOT NULL,
                           axis             VARCHAR(30)  NOT NULL,
                           order_num        INTEGER      NOT NULL,
                           questionnaire_id VARCHAR(36)  NOT NULL,
                           CONSTRAINT pk_questions       PRIMARY KEY (id),
                           CONSTRAINT fk_questions_quest FOREIGN KEY (questionnaire_id) REFERENCES questionnaires(id),
                           CONSTRAINT chk_questions_axis CHECK (axis IN ('ENSINO','PESQUISA','EXTENSAO','GESTAO','INFRAESTRUTURA'))
);

CREATE INDEX idx_questions_questionnaire ON questions(questionnaire_id, order_num);

-- Respostas (uma por aluno por questionário)
CREATE TABLE responses (
                           id               VARCHAR(36) NOT NULL,
                           institution_id   VARCHAR(36) NOT NULL,
                           student_id       VARCHAR(36) NOT NULL,
                           questionnaire_id VARCHAR(36) NOT NULL,
                           created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                           CONSTRAINT pk_responses         PRIMARY KEY (id),
                           CONSTRAINT uk_responses_uniq    UNIQUE (student_id, questionnaire_id),
                           CONSTRAINT fk_responses_inst    FOREIGN KEY (institution_id)   REFERENCES institutions(id),
                           CONSTRAINT fk_responses_student FOREIGN KEY (student_id)       REFERENCES students(id),
                           CONSTRAINT fk_responses_quest   FOREIGN KEY (questionnaire_id) REFERENCES questionnaires(id)
);

-- Respostas por pergunta
CREATE TABLE answers (
                         id               VARCHAR(36)       NOT NULL,
                         rating           INTEGER           NOT NULL CHECK (rating BETWEEN 1 AND 5),
                         comment          TEXT,
                         sentiment        VARCHAR(20)       CHECK (sentiment IN ('POSITIVE','NEUTRAL','NEGATIVE')),
                         sentiment_score  DOUBLE PRECISION,
                         sentiment_reason TEXT,
                         analyzed_at      TIMESTAMPTZ,
                         response_id      VARCHAR(36)       NOT NULL,
                         question_id      VARCHAR(36)       NOT NULL,
                         created_at       TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
                         updated_at       TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
                         CONSTRAINT pk_answers            PRIMARY KEY (id),
                         CONSTRAINT uk_answers_uniq       UNIQUE (response_id, question_id),
                         CONSTRAINT fk_answers_response   FOREIGN KEY (response_id) REFERENCES responses(id),
                         CONSTRAINT fk_answers_question   FOREIGN KEY (question_id) REFERENCES questions(id)
);

-- Sessões do bot WhatsApp
CREATE TABLE bot_sessions (
                              id                     VARCHAR(36)  NOT NULL,
                              status                 VARCHAR(20)  NOT NULL DEFAULT 'IN_PROGRESS',
                              current_question_order INTEGER      NOT NULL DEFAULT 1,
                              phone_hash             VARCHAR(64)  NOT NULL,
                              last_message_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                              expires_at             TIMESTAMPTZ  NOT NULL,
                              completed_at           TIMESTAMPTZ,
                              institution_id         VARCHAR(36)  NOT NULL,
                              student_id             VARCHAR(36)  NOT NULL,
                              questionnaire_id       VARCHAR(36)  NOT NULL,
                              response_id            VARCHAR(36),
                              created_at             TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                              updated_at             TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                              CONSTRAINT pk_bot_sessions  PRIMARY KEY (id),
                              CONSTRAINT fk_sess_inst     FOREIGN KEY (institution_id)   REFERENCES institutions(id),
                              CONSTRAINT fk_sess_student  FOREIGN KEY (student_id)       REFERENCES students(id),
                              CONSTRAINT fk_sess_quest    FOREIGN KEY (questionnaire_id) REFERENCES questionnaires(id),
                              CONSTRAINT fk_sess_response FOREIGN KEY (response_id)      REFERENCES responses(id),
                              CONSTRAINT chk_sess_status  CHECK (status IN ('IN_PROGRESS','PAUSED','COMPLETED','EXPIRED'))
);

CREATE INDEX idx_sessions_phone_hash ON bot_sessions(phone_hash);
CREATE INDEX idx_sessions_status     ON bot_sessions(status);
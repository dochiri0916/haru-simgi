package com.dochiri.jpa.configuration;

import com.dochiri.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = JpaKstInstantPersistenceTest.TestApplication.class,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:jpa-kst-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.jpa.hibernate.ddl-auto=create-drop"
        }
)
@ImportAutoConfiguration(JpaAutoConfiguration.class)
@Transactional
class JpaKstInstantPersistenceTest {

    private static final Instant AUDIT_NOW = Instant.parse("2026-04-17T14:07:57.248810Z");

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void Instant는_DB에_KST_LocalDateTime으로_저장하고_읽을_때_Instant로_복원한다() {
        Instant instant = Instant.parse("2026-04-15T16:00:36.345146Z");
        TestInstantEntity entity = new TestInstantEntity(instant);

        entityManager.persist(entity);
        entityManager.flush();
        entityManager.clear();

        LocalDateTime dbValue = jdbcTemplate.queryForObject(
                "select occurred_at from jpa_kst_instant_test_entities where id = ?",
                LocalDateTime.class,
                entity.id
        );
        TestInstantEntity restored = entityManager.find(TestInstantEntity.class, entity.id);

        assertThat(dbValue).isEqualTo(LocalDateTime.parse("2026-04-16T01:00:36.345146"));
        assertThat(restored.occurredAt).isEqualTo(instant);
    }

    @Test
    void BaseEntity_감사_필드는_DB에_KST_LocalDateTime으로_저장된다() {
        TestAuditEntity entity = new TestAuditEntity("audit");

        entityManager.persist(entity);
        entityManager.flush();

        LocalDateTime createdAt = jdbcTemplate.queryForObject(
                "select created_at from jpa_audit_test_entities where id = ?",
                LocalDateTime.class,
                entity.id
        );

        assertThat(entity.getCreatedAt()).isEqualTo(AUDIT_NOW);
        assertThat(entity.getUpdatedAt()).isEqualTo(AUDIT_NOW);
        assertThat(createdAt).isEqualTo(LocalDateTime.parse("2026-04-17T23:07:57.248810"));
        assertThat(entity.getCreatedBy()).isEqualTo("0");
        assertThat(entity.getUpdatedBy()).isEqualTo("0");
    }

    @SpringBootApplication
    static class TestApplication {

        @Bean
        Clock clock() {
            return Clock.fixed(AUDIT_NOW, ZoneId.of("Asia/Seoul"));
        }
    }

    @Entity
    @Table(name = "jpa_kst_instant_test_entities")
    static class TestInstantEntity extends BaseEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false)
        private Instant occurredAt;

        protected TestInstantEntity() {
        }

        private TestInstantEntity(Instant occurredAt) {
            this.occurredAt = occurredAt;
        }
    }

    @Entity
    @Table(name = "jpa_audit_test_entities")
    static class TestAuditEntity extends BaseEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false)
        private String name;

        protected TestAuditEntity() {
        }

        private TestAuditEntity(String name) {
            this.name = name;
        }
    }
}

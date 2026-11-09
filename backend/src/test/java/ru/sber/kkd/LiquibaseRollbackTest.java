package ru.sber.kkd;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class LiquibaseRollbackTest {

    private static final String JDBC_URL = "jdbc:h2:mem:kkd_migration_cycle;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";

    @Test
    @DisplayName("Проверка цикла миграций: накат -> откат -> повторный накат без ошибок")
    void testMigrationRollbackCycle() throws Exception {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, "sa", "")) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(conn));

            Liquibase liquibase = new Liquibase(
                    "db/changelog/db.changelog-master.xml",
                    new ClassLoaderResourceAccessor(),
                    database
            );

            // 1. ПЕРВИЧНЫЙ НАКАТ (001-init-schema + 002-seed-data)
            liquibase.update((String) null);

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM goals")) {
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) >= 20, "После первого наката цели должны быть загружены");
            }

            // 2. ОТКАТ changelog 002 (seed data)
            liquibase.rollback(1, (String) null);

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM kkd_issues")) {
                assertTrue(rs.next());
                assertEquals(0, rs.getInt(1), "После отката seed-данных таблица kkd_issues должна быть пустой");
            }

            // 3. ОТКАТ changelog 001 (init schema - все таблицы удаляются)
            liquibase.rollback(1, (String) null);

            // 4. ПОВТОРНЫЙ НАКАТ всех миграций
            liquibase.update((String) null);

            // 5. Проверка целостности после повторного наката
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM organizational_units")) {
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) >= 5, "После повторного наката подразделения должны быть на месте");
            }

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM goals")) {
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) >= 20, "После повторного наката цели должны быть на месте");
            }

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM kkd_runs")) {
                assertTrue(rs.next());
                assertEquals(2, rs.getInt(1), "После повторного наката прогоны должны быть на месте");
            }
        }
    }
}

package top.smartduck.ducktodo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class DatabaseInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Environment env;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String profile = env.getProperty("spring.profiles.active", "default");
        String port = env.getProperty("server.port", "8080");
        String dbUrl = env.getProperty("spring.datasource.url", "");
        String dbUser = env.getProperty("spring.datasource.username", "");
        String minioEndpoint = env.getProperty("minio.endpoint", "");
        String minioBucket = env.getProperty("minio.bucket", "");
        String minioEnsure = env.getProperty("minio.ensure-bucket", "false");
        String jwtExpire = env.getProperty("jwt.expire-seconds", "");
        log.info("[App] profile={}, port={}, datasource.url={}, datasource.username={}", profile, port, dbUrl, dbUser);
        log.info("[App] minio.endpoint={}, minio.bucket={}, minio.ensure-bucket={}", minioEndpoint, minioBucket, minioEnsure);
        log.info("[App] jwt.expire-seconds={}", jwtExpire);
        log.info("[DB-Init] Starting database schema check & init...");
        try (Connection conn = dataSource.getConnection()) {
            String schema = null;
            try (PreparedStatement ps = conn.prepareStatement("SELECT DATABASE()")) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) schema = rs.getString(1);
                }
            }
            if (schema == null || schema.trim().isEmpty()) {
                log.warn("[DB-Init] Current database name not resolved; skip init.");
                return;
            }
            log.info("[DB-Init] Current database: {}", schema);

            Map<String, String> tableToResource = new LinkedHashMap<>();
            tableToResource.put("user", "schema/user.sql");
            tableToResource.put("user_security", "schema/user_security.sql");
            tableToResource.put("user_llm_config", "schema/user_llm_config.sql");
            tableToResource.put("user_dingtalk_robot", "schema/user_dingtalk_robot.sql");
            tableToResource.put("user_tool_config", "schema/user_tool_config.sql");
            tableToResource.put("team", "schema/team.sql");
            tableToResource.put("team_user_relation", "schema/team_user_relation.sql");
            tableToResource.put("task_group", "schema/task_group.sql");
            tableToResource.put("task_group_user_relation", "schema/task_group_user_relation.sql");
            tableToResource.put("task", "schema/task.sql");
            tableToResource.put("task_user_relation", "schema/task_user_relation.sql");
            tableToResource.put("child_task", "schema/child_task.sql");
            tableToResource.put("task_file", "schema/task_file.sql");
            tableToResource.put("task_node", "schema/task_node.sql");
            tableToResource.put("task_edge", "schema/task_edge.sql");
            tableToResource.put("task_audit", "schema/task_audit.sql");

            for (Map.Entry<String, String> e : tableToResource.entrySet()) {
                String table = e.getKey();
                long cnt = 0;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?")) {
                    ps.setString(1, schema);
                    ps.setString(2, table);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) cnt = rs.getLong(1);
                    }
                }
                if (cnt > 0) {
                    log.info("[DB-Init] Table '{}' exists; skip.", table);
                    continue;
                }
                log.info("[DB-Init] Table '{}' missing; creating from resource '{}'...", table, e.getValue());
                String sql = readResource(e.getValue());
                if (sql == null || sql.trim().isEmpty()) {
                    log.warn("[DB-Init] Resource '{}' not found or empty; table '{}' not created.", e.getValue(), table);
                    continue;
                }
                try (Statement st = conn.createStatement()) {
                    st.execute(sql);
                    log.info("[DB-Init] Table '{}' created successfully.", table);
                } catch (Exception ex) {
                    log.error("[DB-Init] Failed to create table '{}': {}", table, ex.getMessage());
                }
            }

            // 执行版本升级脚本
            log.info("[DB-Init] Checking for database schema upgrades...");
            executeUpgradeScripts(conn, schema);

            log.info("[DB-Init] Database schema check & init completed.");
        } catch (Exception ex) {
            log.error("[DB-Init] Initialization failed: {}", ex.getMessage());
        }
    }

    private String readResource(String path) {
        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            byte[] buf = readAll(in);
            return new String(buf, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("[DB-Init] Read resource '{}' failed: {}", path, e.getMessage());
            return null;
        }
    }

    private static byte[] readAll(InputStream in) throws Exception {
        byte[] buffer = new byte[8192];
        int len;
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        while ((len = in.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        return bos.toByteArray();
    }

    /**
     * 执行数据库升级脚本
     *
     * @param conn 数据库连接
     * @param schema 数据库名称
     */
    private void executeUpgradeScripts(Connection conn, String schema) {
        // 升级：为 user_llm_config 表添加 llm_model_type 字段
        upgradeUserLlmConfigAddModelType(conn, schema);
    }

    /**
     * 升级：为 user_llm_config 表添加 llm_model_type 字段
     *
     * @param conn 数据库连接
     * @param schema 数据库名称
     */
    private void upgradeUserLlmConfigAddModelType(Connection conn, String schema) {
        String tableName = "user_llm_config";
        String columnName = "llm_model_type";
        
        // 检查表是否存在
        long tableExists = 0;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?")) {
            ps.setString(1, schema);
            ps.setString(2, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) tableExists = rs.getLong(1);
            }
        } catch (Exception e) {
            log.warn("[DB-Init] Failed to check table existence: {}", e.getMessage());
            return;
        }
        
        if (tableExists == 0) {
            log.info("[DB-Init] Table '{}' does not exist; skip upgrade.", tableName);
            return;
        }
        
        // 检查字段是否存在
        long columnExists = 0;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?")) {
            ps.setString(1, schema);
            ps.setString(2, tableName);
            ps.setString(3, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) columnExists = rs.getLong(1);
            }
        } catch (Exception e) {
            log.warn("[DB-Init] Failed to check column existence: {}", e.getMessage());
            return;
        }
        
        if (columnExists > 0) {
            log.info("[DB-Init] Column '{}' already exists in table '{}'; skip upgrade.", columnName, tableName);
            return;
        }
        
        // 添加字段
        try (Statement st = conn.createStatement()) {
            String sql = String.format(
                "ALTER TABLE `%s` ADD COLUMN `%s` tinyint(1) NOT NULL DEFAULT 1 COMMENT 'LLM 模型类型，1-chat模型，2-embedding模型，3-rerank模型' AFTER `llm_model_thinking`",
                tableName, columnName
            );
            st.execute(sql);
            log.info("[DB-Init] Added column '{}' to table '{}' successfully.", columnName, tableName);
        } catch (Exception ex) {
            log.error("[DB-Init] Failed to add column '{}' to table '{}': {}", columnName, tableName, ex.getMessage());
        }
    }
}

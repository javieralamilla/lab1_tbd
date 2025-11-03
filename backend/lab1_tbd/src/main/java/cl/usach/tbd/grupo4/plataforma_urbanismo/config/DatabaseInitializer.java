package cl.usach.tbd.grupo4.plataforma_urbanismo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        logger.info("=== Iniciando inicialización de la base de datos ===");

        try {
            // Ejecutar schema.sql
            logger.info("Ejecutando schema.sql...");
            executeScript("schema.sql");
            logger.info("✓ schema.sql ejecutado correctamente");

            // Ejecutar datos.sql
            logger.info("Ejecutando datos.sql...");
            executeScript("datos.sql");
            logger.info("✓ datos.sql ejecutado correctamente");

            // Ejecutar complemento.sql
            logger.info("Ejecutando complemento.sql...");
            executeScript("complemento.sql");
            logger.info("✓ complemento.sql ejecutado correctamente");

            logger.info("=== Base de datos inicializada correctamente ===");

        } catch (Exception e) {
            logger.error("Error al inicializar la base de datos: ", e);
            throw e;
        }
    }

    private void executeScript(String scriptPath) throws Exception {
        ClassPathResource resource = new ClassPathResource(scriptPath);

        if (!resource.exists()) {
            logger.warn("El archivo {} no existe", scriptPath);
            return;
        }

        String script = FileCopyUtils.copyToString(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)
        );

        // Ejecutar el script completo
        jdbcTemplate.execute(script);
    }
}
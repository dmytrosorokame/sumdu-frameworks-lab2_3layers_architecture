package sumdu.edu.ua.core.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class CoreArchitectureTest {

    @Test
    void coreShouldNotDependOnServlet() {
        JavaClasses imported = new ClassFileImporter()
                .importPackages("sumdu.edu.ua.core");

        noClasses()
                .that().resideInAPackage("..core..")
                .should().dependOnClassesThat()
                .resideInAPackage("jakarta.servlet..")
                .check(imported);
    }

    @Test
    void coreShouldNotDependOnJdbc() {
        JavaClasses imported = new ClassFileImporter()
                .importPackages("sumdu.edu.ua.core");

        noClasses()
                .that().resideInAPackage("..core..")
                .should().dependOnClassesThat()
                .resideInAPackage("java.sql..")
                .check(imported);
    }
}


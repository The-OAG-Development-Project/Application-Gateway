package org.owasp.oag.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Guards the layering of the production code of {@code org.owasp.oag}.
 */
@AnalyzeClasses(packages = PackageCycleTest.ROOT, importOptions = ImportOption.DoNotIncludeTests.class)
class LayeringTest {

    /** The package of the servlet filters. */
    static final String SERVLET_FILTERS = PackageCycleTest.ROOT + ".filters.servlet..";

    /** Servlet filters are wired by component scan, so no other package may depend on them. */
    @ArchTest
    static final ArchRule noDependencyOnServletFilters = noClasses()
            .that().resideOutsideOfPackage(SERVLET_FILTERS)
            .should().dependOnClassesThat().resideInAPackage(SERVLET_FILTERS);
}

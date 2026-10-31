package org.owasp.oag.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Guards the production code of {@code org.owasp.oag} against package cycles. Every package is its own slice.
 */
@AnalyzeClasses(packages = PackageCycleTest.ROOT, importOptions = ImportOption.DoNotIncludeTests.class)
class PackageCycleTest {

    /** The root package of OAG. */
    static final String ROOT = "org.owasp.oag";

    /** No package cycles. */
    @ArchTest
    static final ArchRule packagesAreFreeOfCycles = slices().matching(ROOT + ".(**)").should().beFreeOfCycles();

    /** The root package is not part of any slice, so no subpackage may depend on it. */
    @ArchTest
    static final ArchRule subpackagesDoNotDependOnRootPackage = noClasses()
            .that().resideOutsideOfPackage(ROOT)
            .should().dependOnClassesThat(resideInAPackage(ROOT).and(resideOutsideOfPackage(ROOT + ".*..")));
}

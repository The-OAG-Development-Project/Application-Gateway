package org.owasp.oag.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.dependencies.SliceRule;

import java.util.List;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Guards the production code of {@code org.owasp.oag} against package cycles. Every package is its own slice.
 * The cycles that still exist are broken by ignoring one documented back edge each ({@link #KNOWN_BACK_EDGES}).
 */
@AnalyzeClasses(packages = PackageCycleTest.ROOT, importOptions = ImportOption.DoNotIncludeTests.class)
class PackageCycleTest {

    /** The root package of OAG. */
    static final String ROOT = "org.owasp.oag";

    /** Back edges of existing cycles. Remove an entry as soon as its cycle is resolved. */
    static final List<KnownBackEdge> KNOWN_BACK_EDGES = List.of(
            new KnownBackEdge("session", "cookies", "todo.md E: split session and cookie"));

    /** No package cycles apart from the known back edges. */
    @ArchTest
    static final ArchRule packagesAreFreeOfCycles = /*ignoreKnownBackEdges(*/
            slices().matching(ROOT + ".(**)").should().beFreeOfCycles()/*)*/;

    /** The root package is not part of any slice, so no subpackage may depend on it. */
    @ArchTest
    static final ArchRule subpackagesDoNotDependOnRootPackage = noClasses()
            .that().resideOutsideOfPackage(ROOT)
            .should().dependOnClassesThat(resideInAPackage(ROOT).and(resideOutsideOfPackage(ROOT + ".*..")));

    /**
     * Fails when a known back edge no longer exists, so that its exception is removed together with the cycle.
     *
     * @param classes the imported production classes
     */
    //@ArchTest
    static void knownBackEdgesStillExist(JavaClasses classes) {
        List<KnownBackEdge> resolved = KNOWN_BACK_EDGES.stream().filter(edge -> !edge.existsIn(classes)).toList();
        assertEquals(List.of(), resolved, "Back edges no longer exist, remove them from KNOWN_BACK_EDGES");
    }

    private static SliceRule ignoreKnownBackEdges(SliceRule rule) {
        for (KnownBackEdge edge : KNOWN_BACK_EDGES) {
            rule = rule.ignoreDependency(resideInAPackage(edge.origin()), resideInAPackage(edge.target()));
        }
        return rule;
    }

    /**
     * A dependency from one package to another that closes a known cycle.
     *
     * @param relativeOrigin the depending package, relative to {@link #ROOT}
     * @param relativeTarget the package depended upon, relative to {@link #ROOT}
     * @param removedBy      the planned change that removes this edge
     */
    record KnownBackEdge(String relativeOrigin, String relativeTarget, String removedBy) {

        /** @return the fully qualified origin package */
        String origin() {
            return ROOT + "." + relativeOrigin;
        }

        /** @return the fully qualified target package */
        String target() {
            return ROOT + "." + relativeTarget;
        }

        /**
         * @param classes the imported classes
         * @return whether any class in the origin package directly depends on a class in the target package
         */
        boolean existsIn(JavaClasses classes) {
            return classes.stream()
                    .filter(c -> c.getPackageName().equals(origin()))
                    .flatMap(c -> c.getDirectDependenciesFromSelf().stream())
                    .anyMatch(d -> d.getTargetClass().getPackageName().equals(target()));
        }
    }
}

package com.revelles.monolith

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.Test

class ArchTest {

    @Test
    fun servicesAndRepositoriesShouldNotDependOnWebLayer() {

        val importedClasses = ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.revelles.monolith")

        noClasses()
            .that()
                .resideInAnyPackage("com.revelles.monolith.service..")
            .or()
                .resideInAnyPackage("com.revelles.monolith.repository..")
            .should().dependOnClassesThat()
                .resideInAnyPackage("..com.revelles.monolith.web..")
        .because("Services and repositories should not depend on web layer")
        .check(importedClasses)
    }
}

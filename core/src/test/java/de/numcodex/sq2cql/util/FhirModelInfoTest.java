package de.numcodex.sq2cql.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FhirModelInfoTest {

    @Nested
    class Preconditions {

        @Test
        void presenceOfXmlResource() throws Exception {
            var is = getClass().getResourceAsStream(FhirModelInfo.RESOURCE_PATH);
            assertNotNull(is, "Missing resource file @ %s".formatted(FhirModelInfo.RESOURCE_PATH));
        }

    }

    @Nested
    class IsRetrievableType {

        @Test
        void unknownType() {
            assertFalse(FhirModelInfo.isRetrievableType("???"));
        }

        @Test
        void knownUnretrievableType() {
            assertFalse(FhirModelInfo.isRetrievableType("Coding"));
        }

        @Test
        void knownRetrievableType() {
            assertTrue(FhirModelInfo.isRetrievableType("Encounter"));
        }

    }

    @Nested
    class IsRetrievableWithUnknownType {

        @Test
        void unknownSearchPath() {
            assertFalse(FhirModelInfo.isRetrievable("???", "verificationStatus"));
        }

        @Test
        void knownSearchPath() {
            assertFalse(FhirModelInfo.isRetrievable("???", "class"));
        }

    }

    @Nested
    class IsRetrievableWithUnretrievableType {

        @Test
        void unknownSearchPath() {
            assertFalse(FhirModelInfo.isRetrievable("Coding", "system"));
        }

        @Test
        void knownSearchPath() {
            assertFalse(FhirModelInfo.isRetrievable("Coding", "code"));
        }

    }

    @Nested
    class IsRetrievableWithRetrievableType {

        @Test
        void unknownSearchPath() {
            assertFalse(FhirModelInfo.isRetrievable("Encounter", "verificationStatus"));
        }

        @Test
        void knownSearchPath() {
            assertTrue(FhirModelInfo.isRetrievable("Encounter", "class"));
        }

    }

}

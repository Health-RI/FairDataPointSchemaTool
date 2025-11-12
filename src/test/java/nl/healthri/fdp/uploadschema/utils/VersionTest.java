package nl.healthri.fdp.uploadschema.utils;

import nl.healthri.fdp.uploadschema.domain.Version;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VersionTest {

    @Test
    void testConstructorWithValidString() {
        Version v = new Version("1.2.3");
        assertEquals(1, v.major());
        assertEquals(2, v.minor());
        assertEquals(3, v.patch());
    }

    @Test
    void testConstructorWithInvalidString() {
        assertThrows(IllegalArgumentException.class, () -> new Version("1.2"));
        assertThrows(IllegalArgumentException.class, () -> new Version("v1.2.3"));
        assertThrows(IllegalArgumentException.class, () -> new Version("1.2.3.4"));
        assertThrows(IllegalArgumentException.class, () -> new Version("abc"));
    }

    @Test
    void testConstructorWithInts() {
        Version v = new Version(2, 5, 9);
        assertEquals(2, v.major());
        assertEquals(5, v.minor());
        assertEquals(9, v.patch());
    }

    @Test
    void testToString() {
        Version v = new Version(3, 4, 5);
        assertEquals("3.4.5", v.toString());
    }

    @Test
    void testCompareTo() {
        Version v1 = new Version(1, 0, 0);
        Version v2 = new Version(1, 0, 1);
        Version v3 = new Version(1, 1, 0);
        Version v4 = new Version(2, 0, 0);

        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v2.compareTo(v1) > 0);
        assertTrue(v2.compareTo(v2) == 0);
        assertTrue(v3.compareTo(v2) > 0);
        assertTrue(v4.compareTo(v3) > 0);
    }

    @Test
    void testNextReturnsRequestedIfNewer() {
        Version current = new Version(1, 0, 0);
        Version requested = new Version(1, 0, 1);
        Version result = current.next(requested);
        assertEquals(requested, result);
    }

    @Test
    void testNextIncrementsPatchIfRequestedNotNewer() {
        Version current = new Version(1, 0, 5);
        Version requested = new Version(1, 0, 3); // older
        Version result = current.next(requested);
        assertEquals(new Version(1, 0, 6), result);
    }

    @Test
    void testEqualsHashCodeConsistency() {
        Version a = new Version(1, 2, 3);
        Version b = new Version("1.2.3");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testEquals() {
        Version a = new Version(1, 2, 3);
        Version b = new Version(1, 2, 3);
        Version c = new Version(1, 2, 4);
        assertNotEquals(a, "1.2.3"); // Different type
        assertEquals(a, b); // Same version, different instances
        assertNotEquals(a, c); // Different type
    }
}

package bgu.spl.mics;

import bgu.spl.mics.application.passiveObjects.Ewok;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EwokTest {
    private Ewok ewok;

    @BeforeEach
    void setUp() {
        ewok = new Ewok(0, true);
    }

    @Test
    void testAcquire() {
        assertTrue(ewok.getAvailable());
        ewok.acquire();
        assertFalse(ewok.getAvailable());
    }

    @Test
    void testRelease() {
        assertTrue(ewok.getAvailable());
        ewok.acquire();
        assertFalse(ewok.getAvailable());
        ewok.release();
        assertTrue(ewok.getAvailable());
    }
}
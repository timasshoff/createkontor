package com.timder.kontor;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MathTest {

    @Test
    void testAddition() {
        // Arrange & Act
        int result = 2 + 3;

        // Assert
        assertEquals(5, result, "2 + 3 should equal 5");
    }
}
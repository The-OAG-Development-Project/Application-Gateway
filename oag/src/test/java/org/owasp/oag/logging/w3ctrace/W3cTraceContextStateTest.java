package org.owasp.oag.logging.w3ctrace;

import org.owasp.oag.logging.TraceException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class W3cTraceContextStateTest {

    @Test
    void constructorGood() {
        String expectedTraceparent = "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01";
        String expectedTracestate = "key=value";
        String expectedTracestate2 = "key=value, key2=value2,ttt";


        W3cTraceContextState underTest = new W3cTraceContextState(expectedTraceparent, null, 128);
        assertEquals(expectedTraceparent, underTest.getTraceString());
        assertEquals(null, underTest.getSecondaryTraceInfoString());

        underTest = new W3cTraceContextState(expectedTraceparent, expectedTracestate, 128);
        assertEquals(expectedTraceparent, underTest.getTraceString());
        assertEquals(expectedTracestate, underTest.getSecondaryTraceInfoString());

        underTest = new W3cTraceContextState(expectedTraceparent, expectedTracestate2, 128);
        assertEquals(expectedTraceparent, underTest.getTraceString());
        assertEquals(expectedTracestate2.substring(0, expectedTracestate2.length() - 4), underTest.getSecondaryTraceInfoString());
    }

    @Test
    void constructorBad() {
        String expectedTraceparent = "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01";
        String badTraceparent1 = "00-00000000000000000000000000000000-00f067aa0ba902b7-01";
        String badTraceParent2 = "00-4bf92f3577b34da6a3ce929d0e0e4736-0000000000000000-01";
        String badTraceParent3 = "abcdefghijklmnopq-dasads-ssddssd-kjjjjkjkjkjkjkjkjkjkjk";
        String badTracestate = "keyvalue";
        String badTracestate2 = "key=value key2=value2";


        assertThrows(TraceException.class, () -> new W3cTraceContextState(badTraceparent1, null, 128));

        assertThrows(TraceException.class, () -> new W3cTraceContextState(badTraceParent2, null, 128));

        assertThrows(TraceException.class, () -> new W3cTraceContextState(badTraceParent3, null, 128));

        W3cTraceContextState underTest = new W3cTraceContextState(expectedTraceparent, badTracestate, 128);
        assertEquals(null, underTest.getSecondaryTraceInfoString());

        underTest = new W3cTraceContextState(expectedTraceparent, badTracestate2, 128);
        assertEquals(null, underTest.getSecondaryTraceInfoString());
    }

    @Test
    void maxTraceLength() {
        String expectedTraceparent = "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01";
        String expectedTracestate = "key=value";
        String inputTracestate = "key=value, key2=value2,ttt";

        W3cTraceContextState underTest = new W3cTraceContextState(expectedTraceparent, inputTracestate, 14);
        assertEquals(expectedTraceparent, underTest.getTraceString());
        assertEquals(expectedTracestate, underTest.getSecondaryTraceInfoString());

    }
}
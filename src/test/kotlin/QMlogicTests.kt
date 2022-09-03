import kotlin.test.Test
import kotlin.test.assertEquals

class QMlogicTests {
    val range = 0..7
    val m = range.map { MinTerm3.fromInt(it) }
    @Test
    fun m0rep() {
        assertEquals("000", m[0].toString())
    }

    @Test
    fun m4rep() {
        assertEquals("100", m[4].toString())
    }

    @Test
    fun m0m7combine() {
        assertEquals( null, m[0]?.combine(m[7]))
    }

    @Test
    fun m0m1Aeq() {
        assertEquals(true, m[0]?.A == m[1]?.A)
    }

    @Test
    fun m0m1Beq() {
        assertEquals(true, m[0]?.B == m[1]?.B)
    }

    @Test
    fun m0m1Ceq() {
        assertEquals(false, m[0]?.C == m[1]?.C)
    }

    @Test
    fun m0m1distance() {
        assertEquals(1, m[0]?.distance(m[1]))
    }

    @Test
    fun m0m1combine() {
        assertEquals(MinTerm3(Signal.Zero, Signal.Zero, Signal.Dash), m[0]?.combine(m[1]))
    }
}
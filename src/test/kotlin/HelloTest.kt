import org.example.Hello
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class HelloTest {

    @Test
    fun testHey () {
        val sut = Hello()

        assertEquals("hello!", Hello().hey())
    }

}
import kotlin.reflect.jvm.*
import kotlin.test.*

interface I {
    fun foo(): Any
}

class A : I {
    override fun foo(): Int = 0
    fun bar(): Int = 1
}

fun box(): String {
    assertEquals(javaClass<Integer>(), A::foo.returnType.javaType)
    assertNotEquals(Integer.TYPE, A::foo.returnType.javaType)

    assertNotEquals(javaClass<Integer>(), A::bar.returnType.javaType)
    assertEquals(Integer.TYPE, A::bar.returnType.javaType)

    return "OK"
}

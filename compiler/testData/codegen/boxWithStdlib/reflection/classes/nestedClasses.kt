import kotlin.reflect.KClass
import kotlin.reflect.jvm.java
import kotlin.test.assertEquals

class A {
    companion object {}
    inner class Inner
    class Nested
    private class PrivateNested
}

fun nestedNames(c: KClass<*>) = c.nestedClasses.map { it.simpleName ?: "wow: ${it.java.simpleName}" }.toSortedList()

fun box(): String {
    assertEquals(listOf("Companion", "Inner", "Nested", "PrivateNested"), nestedNames(A::class))

    // assertEquals(emptyList<String>(), nestedNames(String::class)) // TODO
    assertEquals(emptyList<String>(), nestedNames(Int::class))
    assertEquals(emptyList<String>(), nestedNames(Array<Any>::class))
    assertEquals(emptyList<String>(), nestedNames(Error::class))

    return "OK"
}

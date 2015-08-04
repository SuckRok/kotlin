import kotlin.reflect.jvm.*
import kotlin.test.assertEquals

fun foo(strings: Array<String>, integers: Array<Int>, objectArrays: Array<Array<Any>>) {}

fun box(): String {
    assertEquals(javaClass<Array<String>>(), ::foo.parameters[0].type.javaType)
    assertEquals(javaClass<Array<Int>>(), ::foo.parameters[1].type.javaType)
    assertEquals(javaClass<Array<Array<Any>>>(), ::foo.parameters[2].type.javaType)

    return "OK"
}

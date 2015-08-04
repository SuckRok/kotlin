import kotlin.reflect.*
import kotlin.reflect.jvm.*
import kotlin.platform.platformStatic as static

class A {
    private var foo: String = ""
}

object O {
    private static var bar: String = ""
}

class CounterTest {
    private var baz: String? = ""
}

fun box(): String {
    val p = A::class.memberProperties.single() as KMutableProperty1<A, String?>
    p.isAccessible = true
    try {
        p.setter.call(A(), null)
        return "Fail: exception should have been thrown"
    } catch (e: IllegalArgumentException) {}


    val o = O::class.memberProperties.single() as KMutableProperty1<O, String?>
    o.isAccessible = true
    try {
        o.setter.call(O, null)
        return "Fail: exception should have been thrown"
    } catch (e: IllegalArgumentException) {}


    val c = CounterTest::class.memberProperties.single() as KMutableProperty1<CounterTest, String?>
    c.isAccessible = true
    c.setter.call(CounterTest(), null) // Should not fail, because CounterTest::baz is nullable

    return "OK"
}

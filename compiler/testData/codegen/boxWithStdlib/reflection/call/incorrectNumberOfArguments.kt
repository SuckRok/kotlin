import kotlin.platform.platformStatic as static
import kotlin.reflect.jvm.isAccessible

var foo: String = ""

class A(private var bar: String = "") {
    fun getBar() = A::bar
}

object O {
    private static var baz: String = ""

    static fun getBaz() = O::baz.apply { isAccessible = true }
}

fun assertIAE(lambda: () -> Unit) {
    try {
        lambda()
        throw AssertionError("Fail: an IllegalArgumentException should have been thrown")
    } catch (e: IllegalArgumentException) {
        if (!e.getMessage()!!.matches("Callable expects [0-9] arguments, but [0-9] were provided\\.".toRegex())) {
            // This most probably means that we don't check number of passed arguments in reflection
            // and the default check from Java reflection yields an IllegalArgumentException, but with a not that helpful message
            throw AssertionError("Fail: an exception with an unrecognized message was thrown: \"${e.getMessage()}\"")
        }
    }
}

fun box(): String {
    assertIAE { ::box.call(null) }
    assertIAE { ::box.call("") }

    assertIAE { ::A.call() }
    assertIAE { ::A.call(null, "") }

    assertIAE { O::getBaz.call() }
    assertIAE { O::getBaz.call(null, "") }


    val f = ::foo
    assertIAE { f.call(null) }
    assertIAE { f.call(null, null) }
    assertIAE { f.call(arrayOf<Any?>(null)) }
    assertIAE { f.call("") }

    assertIAE { f.getter.call(null) }
    assertIAE { f.getter.call(null, null) }
    assertIAE { f.getter.call(arrayOf<Any?>(null)) }
    assertIAE { f.getter.call("") }

    assertIAE { f.setter.call() }
    assertIAE { f.setter.call(null, null) }
    assertIAE { f.setter.call(null, "") }


    val b = A().getBar()

    assertIAE { b.call() }
    assertIAE { b.call(null, null) }
    assertIAE { b.call("", "") }

    assertIAE { b.getter.call() }
    assertIAE { b.getter.call(null, null) }
    assertIAE { b.getter.call("", "") }

    assertIAE { b.setter.call() }
    assertIAE { b.setter.call(null) }
    assertIAE { b.setter.call("") }


    val z = O.getBaz()

    assertIAE { z.call() }
    assertIAE { z.call(null, null) }
    assertIAE { z.call("", "") }

    assertIAE { z.getter.call() }
    assertIAE { z.getter.call(null, null) }
    assertIAE { z.getter.call("", "") }

    assertIAE { z.setter.call() }
    assertIAE { z.setter.call(null) }
    assertIAE { z.setter.call("") }


    return "OK"
}

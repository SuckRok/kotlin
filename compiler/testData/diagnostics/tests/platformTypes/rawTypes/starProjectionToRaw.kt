// !DIAGNOSTICS: -UNUSED_VARIABLE
// FILE: A.java

import java.util.*;

class A<T extends CharSequence> {}

// FILE: B.java

import java.util.*;

class B<E extends B<*>> {}

// FILE: Test.java

class Test {
    static void foo(B x) {}
}

// FILE: main.kt


fun main(x: B<*>) {
    Test.foo(x)
}

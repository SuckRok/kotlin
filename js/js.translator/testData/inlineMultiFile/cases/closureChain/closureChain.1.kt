/*
 * Copy of JVM-backend test
 * Found at: compiler/testData/codegen/boxInline/complex/closureChain.1.kt
 */

package foo

fun test1(): Int {
    val inlineX = Inline()
    return inlineX.foo({ z: Int -> "" + z}, 25, {String.() -> this.length})
}

fun box(): String {
    if (test1() != 2) return "test1: ${test1()}"

    return "OK"
}
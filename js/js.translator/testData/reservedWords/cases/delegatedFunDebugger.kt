package foo

// NOTE THIS FILE IS AUTO-GENERATED by the generateTestDataForReservedWords.kt. DO NOT EDIT!

trait Trait {
    fun debugger()
}

class TraitImpl : Trait {
    override fun debugger() { debugger() }
}

class TestDelegate : Trait by TraitImpl() {
    fun test() {
        testNotRenamed("debugger", { ::debugger })
    }
}

fun box(): String {
    TestDelegate().test()

    return "OK"
}
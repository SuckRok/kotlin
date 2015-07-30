package test

import kotlin.annotation.*

open class ClassWithFlagsChanged {
    public fun main() {}
}

class ClassWithTypeParameterListChanged<U> {
    public fun main() {}
}

class ClassWithSuperTypeListChanged : java.io.Serializable {
    public fun main() {}
}

annotation class ClassWithClassAnnotationListChanged

fun<T> foo(p1: String?, p2: Any?): String {
    if (p2)
    return p_<caret>
}

// ORDER: something
// ORDER: s
// ORDER: calcSomething

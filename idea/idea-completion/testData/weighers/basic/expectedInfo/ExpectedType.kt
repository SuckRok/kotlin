fun<T> foo(p1: String?, p2: Any?, p3: Any): String {
    if (p2 is String) return p<caret>
}

// ORDER: p2
// ORDER: p1
// ORDER: p3

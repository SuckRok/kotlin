fun box(): String {
    val wc = WeirdComparator<String>().createInner()!!
    val result = wc.max({ a, b -> a.length() - b.length() }, "java", "kotlin")
    if (result != "kotlin") return "Wrong: $result"
    return "OK"
}

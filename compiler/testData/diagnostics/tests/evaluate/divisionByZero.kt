// !DIAGNOSTICS: -UNUSED_VARIABLE

val a1 = 0
val a2 = <!DIVISION_BY_ZERO!>1 / 0<!>
val a3 = <!DIVISION_BY_ZERO!>1 / a1<!>
val a4 = 1 / a2
val a5 = 2 * (<!DIVISION_BY_ZERO!>1 / 0<!>)

val a6 = <!DIVISION_BY_ZERO!>1.div(0)<!>
val a7 = <!DIVISION_BY_ZERO!>1.div(a1)<!>
val a8 = 1.div(a2)
val a9 = 2 * (<!DIVISION_BY_ZERO!>1.div(0)<!>)

val b1: Byte = <!DIVISION_BY_ZERO, TYPE_MISMATCH!>1 / 0<!>
Ann(<!DIVISION_BY_ZERO, ANNOTATION_PARAMETER_MUST_BE_CONST!>1 / 0<!>) val b2 = 1

annotation class Ann(val i : Int)
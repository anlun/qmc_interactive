import react.FC
import react.Props
import react.dom.html.InputType
import react.dom.html.ReactHTML.b
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.hr
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.tr
import react.useState

external interface QMProps : Props {
    var minTermInput: String
    var minTermList: List<Int>
    var qmStarted: Boolean
}
fun Boolean.toSymbol() : String =
    if (this) "1" else "0"
fun String.toDomainRange() : Int? {
    try {
        val v = this.toInt()
        if (v !in MinTerm4.range) return null
        return v
    } catch (e : NumberFormatException) {
        return null
    }

}
fun Int.toMinTerm4String() : String = MinTerm4.fromInt(this).toString()
fun Int.minTerm4Ones() : Int = toMinTerm4String().count { it == '1' }

val QM = FC<QMProps> { props ->
    var minTermInput by useState(props.minTermInput)
    var minTermList  by useState(props.minTermList)
    var qmStarted    by useState(props.qmStarted)
    div {
        +"f("
        b { +MinTerm4.argString }
        +") = Σ m("
        if (!qmStarted) {
            input {
                type = InputType.text
                value = minTermInput
                onChange = { event ->
                    minTermInput = event.target.value
                }
            }
            +") "
            div { }
            button {
                +"start"
                onClick = { event ->
                    minTermList = minTermInput
                        .split(",")
                        .mapNotNull { it.trim().toDomainRange() }
                        .sorted()
                        .distinct()
                    qmStarted = true
                }
            }
        } else {
            +minTermInput
            +") "
        }
        if (qmStarted) {
            hr {}
            table {
                td {
                    tr { +"N" }
                    MinTerm4.range.forEach {
                        tr {
                            +it.toString()
                        }
                    }
                }
                td {
                    tr { +"Binary N" }
                    MinTerm4.range.forEach {
                        tr {
                            +it.toMinTerm4String()
                        }
                    }
                }
                td {
                    tr { +"f(N)" }
                    MinTerm4.range.forEach {
                        tr {
                            +minTermList.contains(it).toSymbol()
                        }
                    }
                }
            }
            div {}
            hr {}
            +"MinTerms"
            val oneSortedMinTermList = minTermList.sortedBy { it.minTerm4Ones() }
            table {
                td {
                    tr { +"N" }
                    oneSortedMinTermList.forEach {
                        tr {
                            +it.toString()
                        }
                    }
                }
                td {
                    tr { +"Binary N" }
                    oneSortedMinTermList.forEach {
                        tr {
                            +it.toMinTerm4String()
                        }
                    }
                }
                td {
                    tr { +"Combine 1" }
                    val combine1list =
                        oneSortedMinTermList
                            .flatMap { i1 ->
                                oneSortedMinTermList.mapNotNull { i2 ->
                                    if (i1 >= i2) return@mapNotNull null
                                    MinTerm4.fromInt(i1)?.combine(MinTerm4.fromInt(i2))
                                }
                            }
                            .distinct()
                    combine1list.forEach {
                        tr {
                            +it.toString()
                        }
                    }
                }
            }
        }
    }
}

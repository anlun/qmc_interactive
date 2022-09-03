import react.FC
import react.Props
import react.dom.html.InputType
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.hr
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.tr
import react.useState

val domainRange = 0..7
external interface QMProps : Props {
    var minTermInput: String
    var minTermList: List<Int>
    var qmStarted: Boolean
}
fun Boolean.toSymbol() : String =
    if (this) "1" else "0"
fun String.toDomainRange() : Int? =
    when (this) {
        "0" -> 0
        "1" -> 1
        "2" -> 2
        "3" -> 3
        "4" -> 4
        "5" -> 5
        "6" -> 6
        "7" -> 7
        else -> null
    }
fun Int.toMinTerm3String() : String = MinTerm3.fromInt(this).toString()
fun Int.minTerm3Ones() : Int = toMinTerm3String().count { it == '1' }

val QM = FC<QMProps> { props ->
    var minTermInput by useState(props.minTermInput)
    var minTermList by useState(props.minTermList)
    var qmStarted by useState(props.qmStarted)
    div {
        +"f(A, B, C) = Σ m("
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
                    domainRange.forEach {
                        tr {
                            +it.toString()
                        }
                    }
                }
                td {
                    tr { +"Binary N" }
                    domainRange.forEach {
                        tr {
                            +it.toMinTerm3String()
                        }
                    }
                }
                td {
                    tr { +"f(N)" }
                    domainRange.forEach {
                        tr {
                            +minTermList.contains(it).toSymbol()
                        }
                    }
                }
            }
            div {}
            hr {}
            +"MinTerms"
            val oneSortedMinTermList = minTermList.sortedBy { it.minTerm3Ones() }
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
                            +it.toMinTerm3String()
                        }
                    }
                }
                td {
                    tr { +"Combine 1" }
                    val combine1list =
                        oneSortedMinTermList
                            .flatMap { i1 ->
                                oneSortedMinTermList.mapNotNull { i2 ->
                                    if (i1 > i2) return@mapNotNull null
                                    MinTerm3.fromInt(i1)?.combine(MinTerm3.fromInt(i2))
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

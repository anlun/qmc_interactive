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
                            +MinTerm3.fromInt(it).toString()
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
            table {
                td {
                    tr { +"N" }
                    minTermList.forEach {
                        tr {
                            +it.toString()
                        }
                    }
                }
                td {
                    tr { +"Binary N" }
                    minTermList.forEach {
                        tr {
                            +MinTerm3.fromInt(it).toString()
                        }
                    }
                }
            }
        }
    }
}

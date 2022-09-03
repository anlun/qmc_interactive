import react.FC
import react.Props
import react.dom.html.InputType
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.th
import react.dom.html.ReactHTML.tr
import react.useState

val domainRange = 1..7
external interface QMProps : Props {
    var minItemInput: String
    var minItemList: List<Int>
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
    var minItemInput by useState(props.minItemInput)
    var minItemList by useState(props.minItemList)
    var qmStarted by useState(props.qmStarted)
    div {
        +"f(A, B, C) = Σ m("
        if (!qmStarted) {
            input {
                type = InputType.text
                value = minItemInput
                onChange = { event ->
                    minItemInput = event.target.value
                }
            }
            +") "
            div { }
            button {
                +"start"
                onClick = { event ->
                    minItemList = minItemInput
                        .split(",")
                        .mapNotNull { it.trim().toDomainRange() }
                    qmStarted = true
                }
            }
        } else {
            +minItemInput
            +") "
        }
        if (qmStarted) {
            table {
                td {
                    domainRange.forEach {
                        tr {
                            +minItemList.contains(it).toSymbol()
                        }
                    }
                }
            }
        }
    }
}

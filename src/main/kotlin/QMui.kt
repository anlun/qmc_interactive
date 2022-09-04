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

sealed class QMuiState {
    object INPUT            : QMuiState()
    object SHOWED_BIN_TABLE : QMuiState()
    object SHOWED_MINTERMS  : QMuiState()
    object FINAL            : QMuiState()

    companion object {
        private val orderList = listOf(INPUT, SHOWED_BIN_TABLE, SHOWED_MINTERMS, FINAL)
    }

    fun orderIndex() : Int = orderList.indexOf(this)
    fun ge(other : QMuiState) : Boolean =
        this.orderIndex() >= other.orderIndex()
}

external interface QMprops : Props {
    var qmTable   : QMtable
    var qmUiState : QMuiState
}
val qmUI = FC<QMprops> { props ->
    var qmTable   by useState(props.qmTable)
    var qmUiState by useState(props.qmUiState)
    div {
        +"f("
        b { +MinTerm4.argString }
        +") = Σ m("
        if (qmUiState == QMuiState.INPUT) {
            input {
                type = InputType.text
                value = qmTable.minTermInput
                onChange = { event ->
                    qmTable = QMtable(event.target.value)
                }
            }
            +") "
            div { }
            button {
                +"Show truth table of f"
                onClick = {
                    qmUiState = QMuiState.SHOWED_BIN_TABLE
                }
            }
        } else {
            +qmTable.minTermInput
            +") "
        }
        if (qmUiState.ge(QMuiState.SHOWED_BIN_TABLE)) {
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
                            +qmTable.minTermList.contains(it).toSymbol()
                        }
                    }
                }
            }
        }
        if (qmUiState == QMuiState.SHOWED_BIN_TABLE) {
            button {
                +"Show minterms"
                onClick = {
                    qmUiState = QMuiState.SHOWED_MINTERMS
                }
            }
        }
        if (qmUiState.ge(QMuiState.SHOWED_MINTERMS)) {
            div {}
            hr {}
            +"MinTerms"
            table {
                td {
                    tr { +"N" }
                    qmTable.oneSortedMinTermList.forEach {
                        tr {
                            +it.toString()
                        }
                    }
                }
                td {
                    tr { +"Binary N" }
                    qmTable.oneSortedMinTermList.forEach {
                        tr {
                            +it.toMinTerm4String()
                        }
                    }
                }
                if (qmUiState == QMuiState.SHOWED_MINTERMS) {
                    button {
                        +"Show merged minterms"
                        onClick = {
                            qmUiState = QMuiState.FINAL
                        }
                    }
                }

                fun columnForCombineList(s : String, l : List<MinTerm4>) =
                    td {
                        tr { +s }
                        l.forEach {
                            tr {
                                +it.toString()
                            }
                        }
                    }
                if (qmUiState.ge(QMuiState.FINAL)) {
                    columnForCombineList("Combine 1", qmTable.combine1List)
                    columnForCombineList("Combine 2", qmTable.combine2List)
                    columnForCombineList("Combine 3", qmTable.combine3List)
                    columnForCombineList("Combine 4", qmTable.combine4List)
                }
            }
        }
    }
}
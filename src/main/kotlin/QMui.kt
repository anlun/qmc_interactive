import csstype.Padding
import csstype.px
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.InputType
import react.dom.html.ReactHTML.b
import react.dom.html.ReactHTML.br
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.hr
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.tr
import react.useState

sealed class QMuiState {
    object INPUT                    : QMuiState()
    object SHOWED_BIN_TABLE         : QMuiState()
    object SHOWED_MINTERMS          : QMuiState()
    object SHOWED_COMBINED_MINTERMS : QMuiState()
    object SHOWED_MINTERMS_REPRESENTATIVES : QMuiState()
    object SHOWED_PRIME_IMPLICANTS : QMuiState()
    object FINAL                    : QMuiState()

    companion object {
        private val orderList =
        listOf(INPUT, SHOWED_BIN_TABLE, SHOWED_MINTERMS,
               SHOWED_COMBINED_MINTERMS,
               SHOWED_MINTERMS_REPRESENTATIVES,
               SHOWED_PRIME_IMPLICANTS,
               FINAL)
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
    fun createStateButton(text : String, state: QMuiState) {
        button {
            +text
            disabled = qmUiState == state
            onClick = {
                qmUiState = state
            }
        }
        br {}
    }
    fun createInputBlock() {
        +"f("
        b { +MinTerm4.argString }
        +") = Σ m("
        input {
            type = InputType.text
            value = qmTable.minTermInput
            onChange = { event ->
                qmTable = QMtable(event.target.value)
            }
        }
        +") "
    }
    fun createBinaryPresentationBlock() {
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
    fun columnForCombineList(reprWord : String, s : String, l : List<MinTerm4>) {
        if (qmUiState.ge(QMuiState.SHOWED_MINTERMS_REPRESENTATIVES)) {
            td {
                css {
                    padding = 5.px
                }
                tr { +reprWord }
                l.forEach {
                    tr {
                        +it.toIntRepresentatives().toString()
                    }
                }
            }
        }
        td {
            css {
                padding = 5.px
            }
            tr { +s }
            l.forEach {
                tr {
                    +it.toString()
                }
            }
        }
    }
    fun createMinTermsBlock() {
        +"MinTerms"
        table {
            columnForCombineList("N","Binary N", qmTable.combine0List)
            if (qmUiState.ge(QMuiState.SHOWED_COMBINED_MINTERMS)) {
                columnForCombineList("Repr. 1", "Combine 1", qmTable.combine1List)
                columnForCombineList("Repr. 2", "Combine 2", qmTable.combine2List)
                columnForCombineList("Repr. 3", "Combine 3", qmTable.combine3List)
                columnForCombineList("Repr. 4", "Combine 4", qmTable.combine4List)
            }
        }
    }
    fun createPrimeImplicantsBlock() {
        +"Prime Implicants: "
        qmTable.primeImplicants.forEach {
            +(it.toString() + ", ")
        }
    }

    createStateButton("Step 1. Show the truth table of f", QMuiState.SHOWED_BIN_TABLE)
    createStateButton("Step 2. Show minterms", QMuiState.SHOWED_MINTERMS)
    createStateButton("Step 3. Show combined minterms", QMuiState.SHOWED_COMBINED_MINTERMS)
    createStateButton("Step 4. Show minterms representatives", QMuiState.SHOWED_MINTERMS_REPRESENTATIVES)
    createStateButton("Step 5. Show prime implicants", QMuiState.SHOWED_PRIME_IMPLICANTS)
    br {}
    createInputBlock()
    if (qmUiState.ge(QMuiState.SHOWED_BIN_TABLE)) {
        br {}
        hr {}
        createBinaryPresentationBlock()
    }
    if (qmUiState.ge(QMuiState.SHOWED_MINTERMS)) {
        br {}
        hr {}
        createMinTermsBlock()
    }
    if (qmUiState.ge(QMuiState.SHOWED_PRIME_IMPLICANTS)) {
        br {}
        hr {}
        createPrimeImplicantsBlock()
    }
}
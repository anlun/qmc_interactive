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
    object SHOWED_TRUTH_TABLE         : QMuiState()
    object SHOWED_MINTERMS          : QMuiState()
    object SHOWED_COMBINED_MINTERMS : QMuiState()
    object SHOWED_MINTERMS_REPRESENTATIVES : QMuiState()
    object SHOWED_PRIME_IMPLICANTS : QMuiState()
    object SHOWED_PRIME_IMPLICANT_TABLE : QMuiState()
    object FINAL                    : QMuiState()

    companion object {
        private val orderList =
        listOf(INPUT, SHOWED_TRUTH_TABLE, SHOWED_MINTERMS,
               SHOWED_COMBINED_MINTERMS,
               SHOWED_MINTERMS_REPRESENTATIVES,
               SHOWED_PRIME_IMPLICANTS,
               SHOWED_PRIME_IMPLICANT_TABLE,
               FINAL)
    }

    fun orderIndex() : Int = orderList.indexOf(this)
    fun ge(other : QMuiState) : Boolean =
        this.orderIndex() >= other.orderIndex()
}

//class QMuiState_new( var show_input         : Boolean = false
//                   , var show_truth_table   : Boolean = false
//                   , var show_minterms      : Boolean = false
//                   , var show_minterms_repr : Boolean = false
//                   , var show_prime_impl    : Boolean = false
//                   )
//{}

external interface QMprops : Props {
    var qmTable   : QMtable
    var qmUiState : QMuiState
//    var qmUiState_new : QMuiState_new
}
val qmUI = FC<QMprops> { props ->
    var qmTable   by useState(props.qmTable)
    var qmUiState by useState(props.qmUiState)
//    var qmUiState_new by useState(props.qmUiState_new)
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
//    fun createStateCheckbox(text : String, stateComponent : String) {
//        input {
//            type = InputType.checkbox
//            onChange = { event ->
//                print("test")
//                qmUiState_new.show_truth_table = true
//            }
//        }
//        +text
//        br {}
//    }
//    createStateCheckbox("Step 1. Show the truth table of f", "truth table")

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
    fun createTruthTableBlock() {
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
    fun createPrimeImplTableBlock() {
        table {
            td {
                tr {
                    +"Prime Minterms"
                }
                qmTable.primeImplicants.forEach {
                    tr {
                        +it.toString()
                    }
                }
            }
            td {
                tr {
                    +"Prime Implicants"
                }
                qmTable.primeImplicants.forEach {
                    tr {
                        +it.toABCD()
                    }
                }
            }
            td {
                tr {
                    +"Repr."
                }
                qmTable.primeImplicants.forEach {
                    tr {
                        +it.toIntRepresentatives().toString()
                    }
                }
            }
            qmTable.minTermList.forEach { i ->
                td {
                    tr { +"m${i.toString()}" }
                    qmTable.primeImplicants.forEach { mt ->
                        tr {
                            if (mt.toIntRepresentatives().contains(i)) {
                                +"x"
                            } else {
                                +"-"
                            }
                        }
                    }
                }
            }
        }
    }

    createStateButton("Step 1. Show the truth table of f", QMuiState.SHOWED_TRUTH_TABLE)
    createStateButton("Step 2. Show minterms", QMuiState.SHOWED_MINTERMS)
    createStateButton("Step 3. Show combined minterms", QMuiState.SHOWED_COMBINED_MINTERMS)
    createStateButton("Step 4. Show minterms representatives", QMuiState.SHOWED_MINTERMS_REPRESENTATIVES)
    createStateButton("Step 5. Show prime implicants", QMuiState.SHOWED_PRIME_IMPLICANTS)
    createStateButton("Step 6. Show prime implicant table", QMuiState.SHOWED_PRIME_IMPLICANT_TABLE)
    br {}
    createInputBlock()
    if (qmUiState.ge(QMuiState.SHOWED_TRUTH_TABLE)) {
        br {}
        hr {}
        createTruthTableBlock()
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
    if (qmUiState.ge(QMuiState.SHOWED_PRIME_IMPLICANT_TABLE)) {
        br {}
        hr {}
        createPrimeImplTableBlock()
    }
}
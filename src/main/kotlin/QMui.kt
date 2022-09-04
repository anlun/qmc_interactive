import csstype.Display
import csstype.px
import csstype.rgb
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.InputType
import react.dom.html.ReactHTML.b
import react.dom.html.ReactHTML.br
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.hr
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.table
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.tr
import react.useState


class QMuiState(var show_truth_table       : Boolean
                , var show_minterms          : Boolean
                , var show_combined_minterms : Boolean
                , var show_minterms_repr     : Boolean
                , var show_prime_impl        : Boolean
                , var show_prime_impl_table  : Boolean
                   )
{
    companion object {
        const val TRUTH_TABLE = 0
        const val MINTERMS = 1
        const val COMBINED_MINTERMS = 2
        const val MINTERMS_REPR = 3
        const val PRIME_IMPL = 4
        const val PRIME_IMPL_TABLE = 5
    }

    fun getField(i : Int) : Boolean =
        when (i) {
            TRUTH_TABLE -> show_truth_table
            MINTERMS -> show_minterms
            COMBINED_MINTERMS -> show_combined_minterms
            MINTERMS_REPR -> show_minterms_repr
            PRIME_IMPL -> show_prime_impl
            PRIME_IMPL_TABLE -> show_prime_impl_table
            else -> false
        }
    fun updateField(i : Int, new_value : Boolean) : QMuiState =
        when (i) {
            TRUTH_TABLE ->
                QMuiState(new_value, show_minterms, show_combined_minterms, show_minterms_repr, show_prime_impl, show_prime_impl_table)
            MINTERMS ->
                QMuiState(show_truth_table, new_value, show_combined_minterms, show_minterms_repr, show_prime_impl, show_prime_impl_table)
            COMBINED_MINTERMS ->
                QMuiState(show_truth_table, show_minterms, new_value, show_minterms_repr, show_prime_impl, show_prime_impl_table)
            MINTERMS_REPR ->
                QMuiState(show_truth_table, show_minterms, show_combined_minterms, new_value, show_prime_impl, show_prime_impl_table)
            PRIME_IMPL ->
                QMuiState(show_truth_table, show_minterms, show_combined_minterms, show_minterms_repr, new_value, show_prime_impl_table)
            PRIME_IMPL_TABLE ->
                QMuiState(show_truth_table, show_minterms, show_combined_minterms, show_minterms_repr, show_prime_impl, new_value)
            else -> this
        }
}

external interface QMprops : Props {
    var qmTable   : QMtable
    var qmUiState : QMuiState
}
val qmUI = FC<QMprops> { props ->
    var qmTable   by useState(props.qmTable)
    var qmUiState by useState(props.qmUiState)
//    fun createStateButton(text : String, state: QMuiState) {
//        button {
//            +text
//            disabled = qmUiState == state
//            onClick = {
//                qmUiState = state
//            }
//        }
//        br {}
//    }
    fun createStateCheckbox(text : String, stateComponent : Int) {
        input {
            type = InputType.checkbox
            checked = qmUiState.getField(stateComponent)
            onClick = { event ->
                qmUiState = qmUiState.updateField(stateComponent, event.currentTarget.checked)
            }
        }
        +text
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
        if (qmUiState.show_minterms_repr) {
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
            columnForCombineList("N", "Binary N", qmTable.combine0List)
            if (qmUiState.show_combined_minterms) {
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

    createStateCheckbox("Step 1. Show the truth table of f", QMuiState.TRUTH_TABLE)
    createStateCheckbox("Step 2. Show minterms", QMuiState.MINTERMS)
    createStateCheckbox("Step 3. Show combined minterms", QMuiState.COMBINED_MINTERMS)
    createStateCheckbox("Step 4. Show minterms representatives", QMuiState.MINTERMS_REPR)
    createStateCheckbox("Step 5. Show prime implicants", QMuiState.PRIME_IMPL)
    createStateCheckbox("Step 6. Show prime implicant table", QMuiState.PRIME_IMPL_TABLE)
    br {}
    createInputBlock()
    br {}
//    if (qmUiState.ge(QMuiState.SHOWED_TRUTH_TABLE)) {
    div {
        css {
            display = Display.flex
            backgroundColor = rgb(8, 97, 22)
        }
        if (qmUiState.show_truth_table) {
            createTruthTableBlock()
        }
        if (qmUiState.show_minterms) {
            createMinTermsBlock()
        }
    }
    if (qmUiState.show_prime_impl) {
        br {}
        hr {}
        createPrimeImplicantsBlock()
    }
    if (qmUiState.show_prime_impl_table) {
        br {}
        hr {}
        createPrimeImplTableBlock()
    }
}
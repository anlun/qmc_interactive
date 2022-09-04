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
import react.dom.html.ReactHTML.tbody
import react.dom.html.ReactHTML.td
import react.dom.html.ReactHTML.th
import react.dom.html.ReactHTML.thead
import react.dom.html.ReactHTML.tr
import react.useState


class QMuiState(paramShows : Array<Boolean> = Array(SHOWS_SIZE) {true})
{
    private var shows: Array<Boolean> =
        if (paramShows.size == SHOWS_SIZE) {
            paramShows
        } else {
            Array(SHOWS_SIZE) { true }
        }
    companion object {
        const val SHOWS_SIZE = 6

        const val TRUTH_TABLE = 0
        const val MINTERMS = 1
        const val COMBINED_MINTERMS = 2
        const val MINTERMS_REPR = 3
        const val PRIME_IMPL = 4
        const val PRIME_IMPL_TABLE = 5
    }

    fun getField(i : Int) : Boolean = shows[i]
    fun updateField(i : Int, new_value : Boolean) : QMuiState {
        val newShows = shows.copyOf()
        newShows[i] = new_value
        return QMuiState(newShows)
    }
}

external interface QMprops : Props {
    var qmTable   : QMtable
    var qmUiState : QMuiState
}
val qmUI = FC<QMprops> { props ->
    var qmTable   by useState(props.qmTable)
    var qmUiState by useState(props.qmUiState)
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
    fun createTable(header : List<String>, columns : List<List<String>>) {
        val emptyCellString = ""
        fun longestColumnSize() : Int {
            var max_size = 0
            columns.forEach { column ->
                max_size = kotlin.math.max(max_size, column.size)
            }
            return max_size
        }
        table {
            thead {
                tr {
                    header.forEach {
                        th { +it }
                    }
                }
            }
            tbody {
                (0 until longestColumnSize()).forEach { currentRow ->
                    tr {
                        columns.forEach { column ->
                            td { +(column.getOrNull(currentRow) ?: emptyCellString) }
                        }
                    }
                }
            }
        }
    }
    fun createTruthTableBlock() {
        createTable(listOf("N", "Binary N", "f(N)"),
            listOf( MinTerm4.range.map { it.toString() }
                  , MinTerm4.range.map { it.toMinTerm4String() }
                  , MinTerm4.range.map { qmTable.minTermList.contains(it).toSymbol() }
                  )
        )

    }
    fun columnForCombineList(reprWord : String, s : String, l : List<MinTerm4>) {
        if (qmUiState.getField(QMuiState.MINTERMS_REPR)) {
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
            if (qmUiState.getField(QMuiState.COMBINED_MINTERMS)) {
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
        if (qmUiState.getField(QMuiState.TRUTH_TABLE)) {
            createTruthTableBlock()
        }
        if (qmUiState.getField(QMuiState.MINTERMS)) {
            createMinTermsBlock()
        }
    }
    if (qmUiState.getField(QMuiState.PRIME_IMPL)) {
        br {}
        hr {}
        createPrimeImplicantsBlock()
    }
    if (qmUiState.getField(QMuiState.PRIME_IMPL_TABLE)) {
        br {}
        hr {}
        createPrimeImplTableBlock()
    }
}
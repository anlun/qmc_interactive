import csstype.px
import emotion.react.css
import react.ChildrenBuilder
import react.FC
import react.Props
import react.dom.html.InputType
import react.dom.html.ReactHTML.b
import react.dom.html.ReactHTML.br
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3
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
        const val SHOWS_SIZE = 7

        const val DONT_CARE = 0
        const val TRUTH_TABLE = 1
        const val MINTERMS = 2
        const val COMBINED_MINTERMS = 3
        const val MINTERMS_REPR = 4
        const val PRIME_IMPL = 5
        const val PRIME_IMPL_TABLE = 6
    }

    fun get(i : Int) : Boolean = shows[i]
    fun set(i : Int, new_value : Boolean) : QMuiState {
        val newShows = shows.copyOf()
        newShows[i] = new_value
        return QMuiState(newShows)
    }
}

external interface QMprops : Props {
    var qmTable   : QMtable
    var lastDontCareInput : String
    var qmUiState : QMuiState
}
val qmUI = FC<QMprops> { props ->
    var qmTable   by useState(props.qmTable)
    var lastDontCareInput by useState(props.lastDontCareInput)
    var qmUiState by useState(props.qmUiState)
    fun ChildrenBuilder.createStateCheckbox(text : String, stateComponent : Int) {
        input {
            type = InputType.checkbox
            checked = qmUiState.get(stateComponent)
            onClick = { event ->
                qmUiState = qmUiState.set(stateComponent, event.currentTarget.checked)
            }
        }
        +text
        br {}
    }
    fun ChildrenBuilder.createInputBlock() {
        +"f("
        b { +MinTerm4.argString }
        +") = Σ m("
        input {
            type = InputType.text
            onChange = { event ->
                qmTable = QMtable(event.target.value, qmTable.dontCareInput)
            }
            value = qmTable.minTermInput
        }
        +")"
        if (qmUiState.get(QMuiState.DONT_CARE)) {
            +" + Σ d("
            input {
                type = InputType.text
                onChange = { event ->
                    qmTable = QMtable(qmTable.minTermInput, event.target.value)
                }
                value = qmTable.dontCareInput
            }
            +") "
        }
    }
    fun ChildrenBuilder.createTable(header : List<String>, columns : List<List<String>>) {
        val emptyCellString = ""
        fun longestColumnSize() : Int {
            var maxSize = 0
            columns.forEach { column ->
                maxSize = kotlin.math.max(maxSize, column.size)
            }
            return maxSize
        }
        table {
            thead {
                tr {
                    header.forEach {
                        th {
                            css {
                                padding = 5.px
                            }
                            +it
                        }
                    }
                }
            }
            tbody {
                (0 until longestColumnSize()).forEach { currentRow ->
                    tr {
                        columns.forEach { column ->
                            td {
                                css {
                                    padding = 5.px
                                }
                                +(column.getOrNull(currentRow) ?: emptyCellString)
                            }
                        }
                    }
                }
            }
        }
    }
    fun ChildrenBuilder.createTruthTableBlock() {
        createTable(listOf("N", "Binary N", "f(N)"),
            listOf( MinTerm4.range.map { it.toString() }
                  , MinTerm4.range.map { it.toMinTerm4String() }
                  , MinTerm4.range.map {
                    if (qmTable.minTermList.contains(it)) {
                        "1"
                    } else if (qmTable.dontCareList.contains(it)) {
                        "undef"
                    } else {
                        "0"
                    }
                }
                  )
        )
    }
    fun ChildrenBuilder.createMinTermsBlock() {
        val header = listOf("N", "Binary N") +
                if (qmUiState.get(QMuiState.COMBINED_MINTERMS)) {
                    (1..4).flatMap {
                        if (qmUiState.get(QMuiState.MINTERMS_REPR)) {
                            listOf("Repr. $it")
                        } else {
                            listOf()
                        } +
                        listOf("Combine $it")
                    }
                } else listOf()
        fun reprCombineColumns(l : List<MinTerm4>) : List<List<String>> {
            return if (qmUiState.get(QMuiState.MINTERMS_REPR)) {
                listOf(l.map { it.toIntRepresentatives().toString() })
            } else {
                listOf()
            } + listOf(l.map { it.toString() })
        }
        val columns : List<List<String>> =
            listOf( qmTable.combine0List.map { it.toIntRepresentatives().toString() }
                  , qmTable.combine0List.map { it.toString() }) +
                if (qmUiState.get(QMuiState.COMBINED_MINTERMS)) {
                    reprCombineColumns(qmTable.combine1List) +
                    reprCombineColumns(qmTable.combine2List) +
                    reprCombineColumns(qmTable.combine3List) +
                    reprCombineColumns(qmTable.combine4List)
                } else {
                   listOf()
                }
//        +"MinTerms"
        createTable(header, columns)
    }
    fun ChildrenBuilder.createListBlock(title : String, l : List<String>) {
        +title
        l.forEach {
            +("$it, ")
        }
    }
    fun ChildrenBuilder.createImplChartBlock(
        header : String,
        header_col1 : String,
        header_col2 : String,
        xl : List<Int>, yl : List<MinTerm4>,
        implChart : Set<Pair<Int, MinTerm4>>
    ) {
        val headerList: List<String> =
            listOf(header_col1, header_col2, "Repr.") + xl.map { "m${it}" }
        val columns: List<List<String>> =
            listOf(yl.map { it.toString() },
                yl.map { it.toABCD() },
                yl.map { it.toIntRepresentatives().toString() }
            ) +
                    xl.map { i ->
                        yl.map { mt ->
                            if (implChart.contains(Pair(i, mt))) {
                                "x"
                            } else {
                                ""
                            }
                        }
                    }
        h3 { +header }
        createTable(headerList, columns)
    }
    fun ChildrenBuilder.createPrimeImplChartBlock() {
        val xl = qmTable.minTermList
        val yl = qmTable.primeImplicants
        createImplChartBlock("Prime implicant chart",
            "Prime Minterms", "Prime Implicants",
            xl, yl,
            qmTable.primeImplicantChart)
    }
    fun ChildrenBuilder.createNonEssentialPrimeImplChartBlock() {
        val xl = qmTable.nonEssentialPrimeImplicantMinTerms
        val yl = qmTable.nonEssentialPrimeImplicants
        createImplChartBlock("Non-Essential Prime implicant chart",
            "Non-Essential Prime Minterms", "Non-Essential Prime Implicants",
            xl, yl,
            qmTable.nonEssentialPrimeImplicantChart)
    }

    input {
        type = InputType.checkbox
        checked = qmUiState.get(QMuiState.DONT_CARE)
        onClick = { event ->
            qmUiState = qmUiState.set(QMuiState.DONT_CARE, event.currentTarget.checked)
            if (!event.currentTarget.checked) {
                lastDontCareInput = qmTable.dontCareInput
                qmTable = QMtable(qmTable.minTermInput, "")
            } else if (qmTable.dontCareInput == "") {
                qmTable = QMtable(qmTable.minTermInput, lastDontCareInput)
            }
        }
    }
    +"Use 'Don't care' values"
    br {}
    createStateCheckbox("Step 1. Show the truth table of f", QMuiState.TRUTH_TABLE)
    createStateCheckbox("Step 2. Show minterms", QMuiState.MINTERMS)
    createStateCheckbox("Step 3. Show combined minterms", QMuiState.COMBINED_MINTERMS)
    createStateCheckbox("Step 4. Show minterms representatives", QMuiState.MINTERMS_REPR)
    createStateCheckbox("Step 5. Show prime implicants", QMuiState.PRIME_IMPL)
    createStateCheckbox("Step 6. Show prime implicant chart", QMuiState.PRIME_IMPL_TABLE)
    br {}
    createInputBlock()
    br {}
    div {
//        css {
//            display = Display.flex
//        }
        if (qmUiState.get(QMuiState.TRUTH_TABLE)) {
            createTruthTableBlock()
        }
        if (qmUiState.get(QMuiState.MINTERMS)) {
            br {}
            hr {}
            h3 { +"Minterms" }
            createMinTermsBlock()
        }
    }
    if (qmUiState.get(QMuiState.PRIME_IMPL)) {
        br {}
        hr {}
        createListBlock("Prime Implicants: ", qmTable.primeImplicants.map { it.toString() })
    }
    if (qmUiState.get(QMuiState.PRIME_IMPL_TABLE)) {
        br {}
        hr {}
        createPrimeImplChartBlock()
        br {}
        hr {}
        createListBlock("Essential Prime Implicants: ",
            qmTable.essentialPrimeImplicants.map { it.toABCD() })
        br {}
        hr {}
        createNonEssentialPrimeImplChartBlock()
    }
}
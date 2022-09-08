sealed class Signal {
    object Dash : Signal()
    object One  : Signal()
    object Zero : Signal()

    companion object {
        fun fromInt(v : Int) : Signal? {
            if (v == 0) return Zero
            if (v == 1) return One
            return null
        }
    }

    fun combine(other : Signal) : Signal =
        if (this == other) { this }
        else               { Dash }

    override fun toString() : String =
        when (this) {
            is Dash -> "-"
            is One  -> "1"
            is Zero -> "0"
        }

    fun toABCD(s : String) : String =
        when (this) {
            is Dash -> ""
            is One  -> s
            is Zero -> "$s'"
        }

    fun toIntRepresentatives() : List<Int> =
        when (this) {
            is Zero -> listOf(0)
            is One  -> listOf(1)
            is Dash -> listOf(0, 1)
        }

    fun toMetricValue() : Int =
        when (this) {
            is Zero, One -> 1
            is Dash      -> 0
        }
}

fun List<Signal>.toIntRepresentatives() : List<Int> {
    if (this.isEmpty()) return emptyList()
    return this.drop(1).fold(this[0].toIntRepresentatives()) { l, s ->
        l.flatMap {
            when (s) {
                is Signal.Zero -> listOf(it * 2)
                is Signal.One  -> listOf(it * 2 + 1)
                is Signal.Dash  -> listOf(it * 2, it * 2 + 1)
            }
        }
    }
    .sorted()
}

class MinTerm4(
    val A : Signal,
    val B : Signal,
    val C : Signal,
    val D : Signal
    )
{
    companion object {
        private const val size  : Int = 16 // 2 ** 4
        val range = 0 until size
        const val argString = "a, b, c, d"

        val fullMinTerm = MinTerm4(Signal.Dash, Signal.Dash, Signal.Dash, Signal.Dash)

        fun fromInt(v : Int) : MinTerm4? {
            if (v !in range) return null
            val a = Signal.fromInt((v / 8) % 2) ?: return null
            val b = Signal.fromInt((v / 4) % 2) ?: return null
            val c = Signal.fromInt((v / 2) % 2) ?: return null
            val d = Signal.fromInt(      v % 2) ?: return null
            return MinTerm4(a, b, c, d)
        }
    }

    fun distance(other : MinTerm4?) : Int {
        if (other == null) return size
        var result = 0
        if (this.A != other.A) { result += 1 }
        if (this.B != other.B) { result += 1 }
        if (this.C != other.C) { result += 1 }
        if (this.D != other.D) { result += 1 }
        return result
    }
    fun isDistance1(other : MinTerm4?) : Boolean {
        return distance(other) == 1
    }

    fun combine(other : MinTerm4?) : MinTerm4? {
        if (other == null || !this.isDistance1(other)) return null
        return MinTerm4(this.A.combine(other.A)
                      , this.B.combine(other.B)
                      , this.C.combine(other.C)
                      , this.D.combine(other.D)
                      )
    }

    override fun toString(): String {
        return A.toString() + B.toString() + C.toString() + D.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is MinTerm4) return false
        return this.A == other.A && this.B == other.B
            && this.C == other.C && this.D == other.D
    }

    override fun hashCode(): Int {
        val hashSeed1 = 239
        val hashSeed2 = 17
        fun f(i : Int) : Int = i * hashSeed2 + hashSeed1
        val hashAB   = f(A.hashCode()) + B.hashCode()
        val hashABC  = f(hashAB      ) + C.hashCode()
        val hashABCD = f(hashABC     ) + D.hashCode()
        return f(hashABCD)
    }

    fun toIntRepresentatives() : List<Int> =
        listOf(A, B, C, D).toIntRepresentatives()

    fun toABCD() : String {
        if (this == fullMinTerm) return "<full mt>"
        return A.toABCD("a") + B.toABCD("b") + C.toABCD("c") + D.toABCD("d")
    }

    fun toMetricValue() : Int =
        A.toMetricValue() + B.toMetricValue() +
        C.toMetricValue() + D.toMetricValue()
}
fun Int.toMinTerm4String() : String = MinTerm4.fromInt(this).toString()
fun Int.minTerm4CountOnes() : Int = toMinTerm4String().count { it == '1' }
fun String.toIntInMinTerm4Range() : Int? {
    try {
        val v = this.toInt()
        if (v !in MinTerm4.range) return null
        return v
    } catch (e : NumberFormatException) {
        return null
    }
}

class QMtable(val  minTermInput : String
            , val dontCareInput : String) {
    companion object {
        private fun combineListWithItself(l : List<MinTerm4>) : List<MinTerm4> =
            l.flatMap { mt1 ->
                l.mapNotNull { mt2 ->
                    mt1.combine(mt2)
                }
            }
            .distinct()

        private fun parseListInt(s : String) : List<Int> =
            s.split(",")
                .flatMap {
                    val ranges = it.split("-").map { it.trim() }
                    if (ranges.isEmpty()) return@flatMap listOf<Int>()
                    val v0 = ranges[0].toIntInMinTerm4Range() ?: return@flatMap listOf<Int>()
                    if (ranges.size == 1) return@flatMap listOf(v0)
                    val v1 = ranges[1].toIntInMinTerm4Range() ?: return@flatMap listOf<Int>()
                    return@flatMap v0..v1
                }
                .sorted()
                .distinct()

        fun solutionValue(solution : List<MinTerm4>) : Int =
            solution.size + solution.fold(0) { a, b ->
                return@fold a + b.toMetricValue()
            }
    }

    val  minTermList : List<Int> = parseListInt(minTermInput)
    val dontCareList : List<Int> = parseListInt(dontCareInput)

    val initialMinTerms =
        minTermList
            .sortedBy { it.minTerm4CountOnes() }
            .mapNotNull { MinTerm4.fromInt(it) }
    val combine0List : List<MinTerm4> =
        (minTermList + dontCareList)
            .sortedBy { it.minTerm4CountOnes() }
            .mapNotNull { MinTerm4.fromInt(it) }
    val combine1List : List<MinTerm4> = combineListWithItself(combine0List)
    val combine2List : List<MinTerm4> = combineListWithItself(combine1List)
    val combine3List : List<MinTerm4> = combineListWithItself(combine2List)
    val combine4List : List<MinTerm4> = combineListWithItself(combine3List)

    private fun dontCombineWithOthers(l : List<MinTerm4>) : List<MinTerm4> {
        val result : MutableList<MinTerm4> = l.toMutableList()
        l.forEach { fst ->
            l.forEach { snd ->
                if (fst.combine(snd) != null) {
                    result.remove(fst)
                    result.remove(snd)
                }
            }
        }
        return result
    }
    private fun calculatePrimeImplicants() : List<MinTerm4> =
        dontCombineWithOthers(combine4List) + dontCombineWithOthers(combine3List) +
        dontCombineWithOthers(combine2List) + dontCombineWithOthers(combine1List) +
        dontCombineWithOthers(combine0List)
    val primeImplicants : List<MinTerm4> = calculatePrimeImplicants()

    private fun calculatePrimeImplicantForIndex(i : Int) : List<MinTerm4> {
        val result = mutableListOf<MinTerm4>()
        primeImplicants.forEach { mt ->
            if (mt.toIntRepresentatives().contains(i)) {
                result.add(mt)
            }
        }
        return result
    }
    private fun calculatePrimeImplicantChart() : Set<Pair<Int, MinTerm4>> {
        val set = mutableSetOf<Pair<Int, MinTerm4>>()
        minTermList.forEach { i ->
            calculatePrimeImplicantForIndex(i).forEach { mt ->
                set.add(Pair(i, mt))
            }
        }
        return set
    }
    val primeImplicantChart : Set<Pair<Int, MinTerm4>> = calculatePrimeImplicantChart()

    private fun calculateEssentialPrimeImplicants() : List<MinTerm4> {
        val result = mutableListOf<MinTerm4>()
        minTermList.forEach { i ->
            val l = calculatePrimeImplicantForIndex(i)
            if (l.size == 1) {
                result.add(l[0])
            }
        }
        return result.distinct()
    }
    val essentialPrimeImplicants : List<MinTerm4> = calculateEssentialPrimeImplicants()

    val essentialPrimeImplicantMinTerms = essentialPrimeImplicants.flatMap { mt ->
        mt.toIntRepresentatives()
    }.distinct()
    val nonEssentialPrimeImplicantMinTerms = minTermList - essentialPrimeImplicantMinTerms
    val nonEssentialPrimeImplicants = primeImplicants - essentialPrimeImplicants

    private fun calculateNonEssentialPrimeImplicantChart() : Set<Pair<Int,MinTerm4>> {
        val result = primeImplicantChart.toMutableSet()
        primeImplicantChart.forEach { p ->
            val (i, mt) = p
            if (essentialPrimeImplicants.contains(mt) || essentialPrimeImplicantMinTerms.contains(i)) {
                result.remove(p)
            }
        }
        return result
    }
    val nonEssentialPrimeImplicantChart : Set<Pair<Int,MinTerm4>> =
        calculateNonEssentialPrimeImplicantChart()

    fun calculateNonEssentialSolutions_helper(leftMinTerms : List<Int>, leftImplicants : List<MinTerm4>)
    : List<List<MinTerm4>>? {
        console.log(leftMinTerms)
        console.log(leftImplicants)
        console.log("\n")
        if (leftMinTerms  .isEmpty()) return listOf<List<MinTerm4>>()
        if (leftImplicants.isEmpty()) return null
        val newLeftImplicants = leftImplicants.drop(1)
        val head = leftImplicants[0]
        fun calculateResultWithHead() : List<List<MinTerm4>>? {
            val newLeftMinTerms = leftMinTerms - head.toIntRepresentatives()
            if (newLeftMinTerms.size == leftMinTerms.size) return null
            val recursiveResult =
              calculateNonEssentialSolutions_helper(newLeftMinTerms, newLeftImplicants)
                  ?: return null
            if (recursiveResult.isEmpty()) return listOf(listOf(head))
            return recursiveResult.map { listOf(head) + it }
        }
        val resultWithHead    = calculateResultWithHead()
        val resultWithoutHead = calculateNonEssentialSolutions_helper(leftMinTerms, newLeftImplicants)
        if (resultWithoutHead == null) return resultWithHead
        return (resultWithHead ?: listOf()) + resultWithoutHead
    }
    fun createNonEssentialSolutions() : List<List<MinTerm4>> =
        calculateNonEssentialSolutions_helper(nonEssentialPrimeImplicantMinTerms,
            nonEssentialPrimeImplicants)
            ?: listOf<List<MinTerm4>>()
    val nonEssentialSolutions : List<List<MinTerm4>> = createNonEssentialSolutions().sortedBy { solutionValue(it)  }
    val minimalNonEssentialSolution : List<MinTerm4>? = nonEssentialSolutions.getOrNull(0)
}
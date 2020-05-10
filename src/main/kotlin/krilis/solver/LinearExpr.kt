package krilis.solver

interface ProgramElement {
    operator fun plus(other: ContinuousVar): ProgramElement
    operator fun plus(other: LinearExpr): ProgramElement
    operator fun plus(other: Double): ProgramElement
    operator fun minus(other: ContinuousVar): ProgramElement
    operator fun minus(other: LinearExpr): ProgramElement
    operator fun minus(other: Double): ProgramElement
    operator fun unaryMinus(): ProgramElement
    operator fun times(other: Double): ProgramElement
}

data class SimpleTerm(val coeff: Double, val variable: ContinuousVar)

data class LinearExpr(
        val expressionVarTerms: Map<String, SimpleTerm>,
        val constantSum: Double = 0.0
) : ProgramElement {

    override operator fun plus(other: ContinuousVar): LinearExpr {
        val newMap = HashMap<String, SimpleTerm>(this.expressionVarTerms)
        val existing: SimpleTerm? = this.expressionVarTerms[other.name]
        if (existing != null) {
            val newCoeff: Double = existing.coeff + 1.0
            newMap[other.name] = SimpleTerm(newCoeff, other)
            return LinearExpr(newMap, this.constantSum)
        }
        newMap[other.name] = SimpleTerm(1.0, other)
        return LinearExpr(newMap.toMap(), this.constantSum)
    }

    override operator fun plus(other: LinearExpr): LinearExpr {
        val newMap = HashMap<String, SimpleTerm>(this.expressionVarTerms)
        newMap.putAll(this.expressionVarTerms)
        for ((key, term) in other.expressionVarTerms.entries) {
            val ourLookup: SimpleTerm? = newMap[key]
            if (ourLookup != null) {
                newMap[key] = SimpleTerm(
                    ourLookup.coeff + term.coeff,
                    ourLookup.variable
                )
            } else {
                newMap[key] = term
            }
        }

        return LinearExpr(newMap.toMap(), this.constantSum + other.constantSum)
    }

    override operator fun plus(other: Double): LinearExpr = LinearExpr(
        expressionVarTerms = this.expressionVarTerms,
        constantSum = this.constantSum + other
    )

    override operator fun minus(other: ContinuousVar): LinearExpr = this + (-other)
    override operator fun minus(other: LinearExpr): LinearExpr = this + (-other)
    override operator fun minus(other: Double): LinearExpr = this + (-other)

    override operator fun unaryMinus(): LinearExpr = LinearExpr(
        expressionVarTerms = this.expressionVarTerms.mapValues { entry ->
            SimpleTerm(-entry.value.coeff, entry.value.variable)
        },
        constantSum = -this.constantSum
    )

    override operator fun times(other: Double): LinearExpr = LinearExpr(
            expressionVarTerms = this.expressionVarTerms.mapValues { entry ->
                SimpleTerm(other * entry.value.coeff, entry.value.variable)
            },
            constantSum = other * this.constantSum
    )


    override fun toString(): String {
        var fillerString = ""
        var isFirstItem = true
        for (varTerm in this.expressionVarTerms.values) {
            if (isFirstItem) {
                isFirstItem = false
            } else {
                fillerString += " + "
            }
            fillerString += "${varTerm.coeff}*${varTerm.variable.name}"
        }
        return "LinearExpr($fillerString)"
    }
}

/**
 * Extension function for Doubles to allow adding to LinearExprs
 */
operator fun Double.plus(other: LinearExpr) = other + this

/**
 * Extension function for Doubles to allow subtracting to LinearExprs
 */
operator fun Double.minus(other: LinearExpr) = other - this

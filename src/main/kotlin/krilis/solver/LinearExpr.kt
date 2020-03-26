package krilis.solver

interface ProgramElement {
    operator fun plus(other: ContinuousVar): ProgramElement
    operator fun plus(other: LinearExpr): ProgramElement
    operator fun plus(other: Double): ProgramElement
    operator fun minus(other: ContinuousVar): ProgramElement
    operator fun minus(other: LinearExpr): ProgramElement
    operator fun minus(other: Double): ProgramElement
    operator fun unaryMinus(): ProgramElement
}

data class SimpleTerm(val coeff: Double, val variable: ContinuousVar)

data class LinearExpr(
    val expressionVars: Map<String, SimpleTerm>,
    val constantSum: Double = 0.0
) : ProgramElement {
    val size: Int
        get() = expressionVars.size

    override operator fun plus(other: ContinuousVar): LinearExpr {
        val newMap = HashMap<String, SimpleTerm>(this.expressionVars)
        val existing: SimpleTerm? = this.expressionVars[other.name]
        if (existing != null) {
            val newCoeff: Double = existing.coeff + 1.0
            newMap[other.name] = SimpleTerm(newCoeff, other)
            return LinearExpr(newMap, this.constantSum)
        }
        newMap[other.name] = SimpleTerm(1.0, other)
        return LinearExpr(newMap.toMap(), this.constantSum)
    }

    override operator fun plus(other: LinearExpr): LinearExpr {
        val newMap = HashMap<String, SimpleTerm>(this.expressionVars)
        for (key in other.expressionVars.keys) {
            val ourLookup: SimpleTerm? = this.expressionVars[key]
            if (ourLookup != null) {
                val theirLookup: SimpleTerm = other.expressionVars[key]!!
                newMap[key] = SimpleTerm(
                    ourLookup.coeff + theirLookup.coeff,
                    ourLookup.variable
                )
            }
        }
        return LinearExpr(newMap.toMap(), this.constantSum + other.constantSum)
    }

    override operator fun plus(other: Double): LinearExpr = LinearExpr(
        this.expressionVars,
        this.constantSum + other
    )

    override operator fun minus(other: ContinuousVar): LinearExpr = this + (-other)
    override operator fun minus(other: LinearExpr): LinearExpr = this + (-other)
    override operator fun minus(other: Double): LinearExpr = this + (-other)

    override operator fun unaryMinus(): LinearExpr = LinearExpr(
        expressionVars = this.expressionVars.mapValues { entry ->
            SimpleTerm(-entry.value.coeff, entry.value.variable)
        },
        constantSum = -this.constantSum
    )


    override fun toString(): String {
        var fillerString = ""
        var isFirstItem = true
        for (varTerm in this.expressionVars.values) {
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

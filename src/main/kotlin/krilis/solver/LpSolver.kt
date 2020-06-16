package krilis.solver

import java.math.BigDecimal
import java.math.RoundingMode

import krilis.solver.utils.withinAbsTolerance

enum class PivotCellResultType {
    VALUE,
    COMPLETE,
    INFEASIBLE,
    UNBOUNDED,
}
data class PivotCellResult(val type: PivotCellResultType, val rowIdx: Int, val colIdx: Int)

private fun printMatrix(matrix: Array<DoubleArray>) {
    matrix.forEach { i ->
        i.forEach { j ->
            val out: BigDecimal = BigDecimal(j).setScale(4, RoundingMode.HALF_EVEN)
            if (j >= 0.0) {
                print("+$out    ")
            } else {
                print("$out    ")
            }
        }
        print("\n")
    }
}

class LpSolver(val maximize: LinearExpr, val subjectTo: Collection<LinearConstraint>) {
    private val lpVarNames: ArrayList<String> = arrayListOf()

    init {
        val varNameSet = mutableSetOf<String>()
        for (cons in this.subjectTo) {
            varNameSet.addAll(cons.lhs.expressionVarTerms.keys)
        }
        varNameSet.addAll(maximize.expressionVarTerms.keys)
        this.lpVarNames.addAll(varNameSet)
    }

    fun simplexSolve(): ProblemSolution {
        val tableau: Array<DoubleArray> = this.tableau()
        var pivotCell: PivotCellResult = this.getPivotCellIndices(tableau)
        while (true) {
            when (pivotCell.type) {
                PivotCellResultType.COMPLETE -> {
                    return OptimalSolution(
                            this.createVariableMapping(tableau),
                            this.getOptimizationValue(tableau)
                    )
                }
                PivotCellResultType.INFEASIBLE -> return InfeasibleSolution
                PivotCellResultType.UNBOUNDED -> return UnboundedSolution
                PivotCellResultType.VALUE -> {
                    this.pivot(tableau, pivotCell)
                    pivotCell = this.getPivotCellIndices(tableau)
                }
            }
        }
    }

    /**
     * Produces a Canonical Tableau from the linear program.
     *
     * See Also: https://people.richland.edu/james/ictcm/2006/simplex.html
     */
    private fun tableau(): Array<DoubleArray> {
        val numSlackVars: Int = this.subjectTo.size
        val numRows: Int = this.subjectTo.size + 1
        val numCols: Int = 2 * this.lpVarNames.size + numSlackVars + 2
        val entries: Array<DoubleArray> = Array(numRows) { DoubleArray(numCols) { 0.0 } }
        val artificialVariableIndices: ArrayList<Int> = arrayListOf()
        for ((r: Int, rawConstraint: LinearConstraint) in this.subjectTo.withIndex()) {
            val constraint: LinearConstraint = if (rawConstraint.lhs.constantSum > 0.0) {
                rawConstraint.negate()
            } else {
                rawConstraint
            }
            for (c: Int in 0 until this.lpVarNames.size) {
                val varName: String = this.lpVarNames[c]
                val coeff: Double = constraint.lhs.expressionVarTerms[varName]?.coeff ?: 0.0
                entries[r][2 * c] = coeff
                entries[r][2 * c + 1] = -coeff
            }
            // Set the slack variable.
            val slackIdx: Int = 2 * this.lpVarNames.size + r
            when (constraint) {
                is GteConstraint -> {
                    entries[r][slackIdx] = -1.0
                }
                is LteConstraint -> {
                    entries[r][slackIdx] = 1.0
                }
                else -> {
                    entries[r][slackIdx] = 1.0
                    artificialVariableIndices.add(slackIdx)
                }
            }
            // Set the RHS
            entries[r][numCols - 1] = -constraint.lhs.constantSum
        }
        val lastRow = numRows - 1
        for ((c: Int, varName: String) in this.lpVarNames.withIndex()) {
            val coeff: Double = -(this.maximize.expressionVarTerms[varName]?.coeff ?: 0.0)
            entries[lastRow][2 * c] = coeff
            entries[lastRow][2 * c + 1] = -coeff
        }
        for (c: Int in artificialVariableIndices) {
            entries[lastRow][c] = 1.0
        }
        // Set the optimisation variable
        entries[lastRow][numCols - 2] = 1.0
        return entries
    }

    /**
     * Returns the index pairing in the format (row, column).
     */
    private fun getPivotCellIndices(matrix: Array<DoubleArray>): PivotCellResult {
        val pivotCol: Int = getPivotCol(matrix) ?: return PivotCellResult(
                PivotCellResultType.COMPLETE,
                -1,
                -1
        )
        val pivotRow: Int = getPivotRow(matrix, pivotCol) ?: return PivotCellResult(
                PivotCellResultType.INFEASIBLE,
                -1,
                pivotCol
        )
        return PivotCellResult(PivotCellResultType.VALUE, pivotRow, pivotCol)
    }

    private fun getPivotCol(matrix: Array<DoubleArray>): Int? {
        var currentMinValueCol = -0.0
        var currentMinValueColIdx: Int? = null
        for ((idx, entry) in matrix.last().withIndex()) {
            if (entry < currentMinValueCol) {
                currentMinValueCol = entry
                currentMinValueColIdx = idx
            }
        }
        return currentMinValueColIdx
    }

    private fun getPivotRow(matrix: Array<DoubleArray>, pivotColumnIdx: Int): Int? {
        var smallestNonNegativeRatio = Double.POSITIVE_INFINITY
        var smallestNonNegativeRatioIdx: Int? = null
        for ((rowIdx, row) in matrix.withIndex()) {
            val newRatio: Double = row.last() / row[pivotColumnIdx]
            if (newRatio > 0.0 && newRatio < smallestNonNegativeRatio) {
                smallestNonNegativeRatio = newRatio
                smallestNonNegativeRatioIdx = rowIdx
            }
        }
        return smallestNonNegativeRatioIdx
    }

    private fun pivot(tableau: Array<DoubleArray>, pivotCellResult: PivotCellResult): Array<DoubleArray> {
        val pRowIdx: Int = pivotCellResult.rowIdx
        val pColIdx: Int = pivotCellResult.colIdx
        // Divide out row.
        val amount: Double = tableau[pRowIdx][pColIdx]
        for ((colIdx, cellValue) in tableau[pRowIdx].withIndex()) {
            tableau[pRowIdx][colIdx] = cellValue / amount
        }
        assert (withinAbsTolerance(tableau[pRowIdx][pColIdx], 1.0))
        // Zero out the non-basics
        for ((rowIdx, row) in tableau.withIndex()) {
            if (rowIdx == pRowIdx) {
                continue
            }
            if (withinAbsTolerance(row[pColIdx], 0.0)) {
                continue
            }
            rowSubtractionOp(
                    minuend = row,
                    subtrahend = tableau[pRowIdx],
                    colIdx = pColIdx
            )
        }
        return tableau
    }

    private fun rowSubtractionOp(minuend: DoubleArray, subtrahend: DoubleArray, colIdx: Int) {
        val amount: Double = minuend[colIdx]
        for ((idx, it) in minuend.withIndex()) {
            minuend[idx] = it - (subtrahend[idx] * amount)
        }
    }

    private fun createVariableMapping(matrix: Array<DoubleArray>): Map<String, Double> {
        val map: HashMap<String, Double> = HashMap()
        for (varIdx: Int in 0 until lpVarNames.size) {
            val colIdx: Int = varIdx * 2  // Recall that variable names map to two separate alpha/beta values.
            var rowIdx = 0
            // We don't need the last row, since that won't contain anything except the optimisation expression.
            while (rowIdx < matrix.size - 1) {
                if (withinAbsTolerance(matrix[rowIdx][colIdx], 1.0)
                        || withinAbsTolerance(matrix[rowIdx][colIdx + 1], 1.0)) {
                    break
                }
                rowIdx++
            }
            map[lpVarNames[varIdx]] = matrix[rowIdx].last()
        }
        return map
    }

    /**
     * Return the optimization value of the provided matrix tableau.
     */
    private fun getOptimizationValue(matrix: Array<DoubleArray>): Double {
        return matrix.last().last()
    }
}
package krilis.solver

sealed class ProblemSolution

object InfeasibleSolution : ProblemSolution()

data class FeasibleSolution(val valueMappings: Map<String, Double>,
                            val optimizationFunctionValue: Double): ProblemSolution() {
    fun valueOf(element: ContinuousVar): Double = valueMappings[element.name] ?:
        throw IllegalArgumentException("Variable ${element.name} not found in solution.")
}
package org.xodium.vanillaplus.engines

import org.mariuszgromada.math.mxparser.Argument
import org.mariuszgromada.math.mxparser.Expression

/**
 * A mathematical expression engine that safely evaluates string expressions using mXparser.
 * @see org.mariuszgromada.math.mxparser.Expression
 * @see org.mariuszgromada.math.mxparser.Argument
 */
object ExpressionEngine {
    /**
     * Evaluates a mathematical expression with provided variable context.
     * @param expression the mathematical expression to evaluate (e.g., "speed * 10 + jump * 5").
     * @param context a map of variable names to their values for expression substitution.
     * @param allowedVariables the whitelist of variable names permitted in the expression.
     * @return the computed numerical result of the expression.
     * @throws IllegalArgumentException if:
     * - Expression contains forbidden characters (`;`, `{`, `}`, `[`, `]`, `"`) :cite[1]
     * - Expression syntax is invalid
     * - Result is not a valid number (NaN or infinite)
     */
    fun evaluate(
        expression: String,
        context: Map<String, Double>,
        allowedVariables: Iterable<String>,
    ): Double {
        if (expression.contains(Regex("""[;{}\[\]"]"""))) {
            throw IllegalArgumentException("Expression contains forbidden characters")
        }

        val expr = Expression(expression)

        context.forEach { (key, value) -> if (key in allowedVariables) expr.addArguments(Argument("$key = $value")) }

        val result = expr.calculate()

        if (expr.checkSyntax()) {
            if (result.isNaN() || result.isInfinite()) {
                throw IllegalArgumentException("Expression result is not a valid number")
            }
            return result
        } else {
            throw IllegalArgumentException("Syntax error in expression: ${expr.errorMessage}")
        }
    }
}

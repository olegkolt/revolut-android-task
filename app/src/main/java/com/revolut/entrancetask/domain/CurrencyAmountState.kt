package com.revolut.entrancetask.domain

import java.math.BigDecimal

/**
 * State of amount field
 */
sealed class CurrencyAmountState

data class CurrencyAmountOutcomeState(
    /**
     * Null if no need to update EditText
     */
    val outcomeAmount: BigDecimal?
) : CurrencyAmountState()

data class CurrencyAmountIncomeState(
    val incomeAmount: BigDecimal
) : CurrencyAmountState()

object CurrencyAmountLoading : CurrencyAmountState()
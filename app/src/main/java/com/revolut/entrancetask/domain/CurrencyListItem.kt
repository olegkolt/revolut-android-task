package com.revolut.entrancetask.domain

import java.math.BigDecimal
import java.util.*

data class CurrencyListItem(
    val currency: Currency,
    val currencyFlagUrl: String,
    /**
     * Null if amount has not calculated jet
     */
    var amount: BigDecimal?
)
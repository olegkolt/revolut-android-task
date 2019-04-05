package com.revolut.entrancetask.presentation

import java.math.BigDecimal
import java.util.*

data class CurrencyClickEvent(
    val amount: BigDecimal,
    val currency: Currency
)
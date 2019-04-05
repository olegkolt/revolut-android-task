package com.revolut.entrancetask.domain

import java.math.BigDecimal
import java.util.*

/**
 * Currency rates relative to base currency
 */
class CurrencyRelativeRates(
    val base: Currency,
    val rates: List<CurrencyRate>
)

class CurrencyRate(
    val currency: Currency,
    val rate: BigDecimal
)
package com.revolut.entrancetask.network

import java.math.BigDecimal

data class CurrencyRatesResponseDto(
    val base: String,
    var rates: Map<String, BigDecimal>
)
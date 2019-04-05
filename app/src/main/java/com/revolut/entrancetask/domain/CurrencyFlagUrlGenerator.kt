package com.revolut.entrancetask.domain

import java.util.*

class CurrencyFlagUrlGenerator(private val pattern: String) {
    fun generate(currency: Currency): String {
        return pattern.format(currency.currencyCode.toLowerCase())
    }
}
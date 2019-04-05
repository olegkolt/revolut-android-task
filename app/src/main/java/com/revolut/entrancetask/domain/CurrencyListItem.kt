package com.revolut.entrancetask.domain

import java.util.*

/**
 * Элемент списка валют
 */
data class CurrencyListItem(
    val currency: Currency,
    val currencyFlagUrl: String,
    val amountState: CurrencyAmountState
)
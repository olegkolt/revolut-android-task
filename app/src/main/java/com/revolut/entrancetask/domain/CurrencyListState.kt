package com.revolut.entrancetask.domain

import java.math.BigDecimal
import java.util.*

sealed class CurrencyListState

class CurrencyListLoadedState(
    val list: List<CurrencyListItem>
) : CurrencyListState()

class CurrencyListCurrencyMovedTopState(
    val currency: Currency
) : CurrencyListState()

class CurrencyListUpdateValues(
    val newValues: Map<Currency, BigDecimal>
) : CurrencyListState()

/**
 * Hide income values before new rates have been loaded
 */
object CurrencyListHideIncomeValues : CurrencyListState()

object CurrencyListLoadingState : CurrencyListState()

object CurrencyListErrorState : CurrencyListState()
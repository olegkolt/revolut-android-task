package com.revolut.entrancetask.domain

sealed class CurrencyListState

/**
 * Состояние списка валют
 */
class CurrencyListLoadedState(
    val list: List<CurrencyListItem>,
    val focusOutcome: Boolean
) : CurrencyListState()

//class CurrencyListCurrencyMovedTopState

object CurrencyListLoadingState : CurrencyListState()

object CurrencyListErrorState : CurrencyListState()
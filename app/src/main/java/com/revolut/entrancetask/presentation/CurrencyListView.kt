package com.revolut.entrancetask.presentation

import com.revolut.entrancetask.domain.CurrencyListState
import io.reactivex.Observable
import java.math.BigDecimal
import java.util.*

interface CurrencyListView {
    val outcomeAmount: Observable<String>
    val currencySelection: Observable<CurrencyClickEvent>
    fun showList(listState: CurrencyListState)
}
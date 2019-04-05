package com.revolut.entrancetask.domain

import com.revolut.entrancetask.infrastructure.AppSchedulers
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.math.BigDecimal
import java.util.*

/**
 * Keep state of outcomeAmount and outcomeCurrency
 */
class CurrencyRatesUseCase(
    private var outcomeAmount: BigDecimal,
    private var outcomeCurrency: Currency,
    private val ratesRepository: CurrencyRatesRepository,
    private val urlGenerator: CurrencyFlagUrlGenerator,
    private val schedulers: AppSchedulers
) {
    companion object {
        private fun generateState(
            currencyRates: CurrencyRelativeRates?,
            updateOutcomeAmount: Boolean,
            isCurrencyChanged: Boolean,
            outcomeAmount: BigDecimal,
            outcomeCurrency: Currency,
            urlGenerator: CurrencyFlagUrlGenerator
        ) : CurrencyListState {
            val isRatesCorrect = currencyRates?.base == outcomeCurrency

            return when {
                currencyRates == null -> CurrencyListLoadingState
                currencyRates.rates.isEmpty() -> CurrencyListErrorState
                else -> CurrencyListLoadedState(
                    list = listOf(
                        CurrencyListItem(
                            currencyRates.base,
                            urlGenerator.generate(currencyRates.base),
                            CurrencyAmountOutcomeState(if (updateOutcomeAmount) outcomeAmount else null)
                        )
                    ) + currencyRates.rates
                        .filter { it.currency != outcomeCurrency }
                        .map { rate ->
                        CurrencyListItem(
                            rate.currency,
                            urlGenerator.generate(rate.currency),
                            if (!isRatesCorrect)
                                CurrencyAmountLoading
                            else
                                CurrencyAmountIncomeState(
                                    rate.rate.multiply(outcomeAmount).apply {
                                        setScale(2, BigDecimal.ROUND_HALF_UP)
                                    }
                                )
                        )
                    },
                    focusOutcome = isCurrencyChanged
                )
            }
    }
    }

    private val requestSubject: Subject<Currency> = PublishSubject.create()
    private val stateSubject: Subject<CurrencyListState> = PublishSubject.create()
    private var loadedRates: CurrencyRelativeRates? = null

    init {
        requestSubject.hide()
            .switchMap {
                ratesRepository
                    .loadRates(it)
                    .subscribeOn(schedulers.io)
                    .toObservable()
            }.map { ratesResult ->
                loadedRates = ratesResult
                generateState(
                    currencyRates = loadedRates,
                    updateOutcomeAmount = true,
                    isCurrencyChanged = false,
                    outcomeAmount = outcomeAmount,
                    outcomeCurrency = outcomeCurrency,
                    urlGenerator = urlGenerator
                )
            }
            .observeOn(schedulers.ui)
            .subscribe(stateSubject)
    }

    val listState: Observable<CurrencyListState> = stateSubject.hide()

    fun load() {
        requestSubject.onNext(outcomeCurrency)
    }

    fun updateOutcomeAmount(newOutcomeAmount: BigDecimal) {
        outcomeAmount = newOutcomeAmount
        stateSubject.onNext(
            generateState(
                currencyRates = loadedRates,
                updateOutcomeAmount = false,
                isCurrencyChanged = false,
                outcomeAmount = outcomeAmount,
                outcomeCurrency = outcomeCurrency,
                urlGenerator = urlGenerator
            )
        )
    }

    fun updateOutcomeCurrency(newOutcomeAmount: BigDecimal, newOutcomeCurrency: Currency) {
        outcomeAmount = newOutcomeAmount
        outcomeCurrency = newOutcomeCurrency
        stateSubject.onNext(
            generateState(
                currencyRates = loadedRates,
                updateOutcomeAmount = true,
                isCurrencyChanged = true,
                outcomeAmount = outcomeAmount,
                outcomeCurrency = outcomeCurrency,
                urlGenerator = urlGenerator
            )
        )
        requestSubject.onNext(outcomeCurrency)
    }
}
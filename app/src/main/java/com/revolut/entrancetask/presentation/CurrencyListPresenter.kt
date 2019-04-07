package com.revolut.entrancetask.presentation

import com.revolut.entrancetask.domain.CurrencyRatesUseCase
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal

class CurrencyListPresenter(
    private val useCase: CurrencyRatesUseCase
) {
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var view: CurrencyListView? = null

    fun onStart() {
        view!!.let { view ->
            compositeDisposable.add(
                useCase.listState
                    .subscribe {
                        view.showList(it)
                    }
            )

            compositeDisposable.add(
                view.outcomeAmount
                    .subscribe {
                        val amount = try {
                            BigDecimal(it.replace(',', '.'))
                        } catch (ex: NumberFormatException) {
                            BigDecimal.ZERO
                        }
                        useCase.updateOutcomeAmount(amount)
                    }
            )

            compositeDisposable.add(
                view.currencySelection
                    .subscribe {
                        useCase.updateOutcomeCurrency(it.amount, it.currency)
                    }
            )

            useCase.load()
        }
    }

    fun onStop() {
        compositeDisposable.clear()
    }

    fun bindView(view: CurrencyListView) {
        this.view = view
    }
}
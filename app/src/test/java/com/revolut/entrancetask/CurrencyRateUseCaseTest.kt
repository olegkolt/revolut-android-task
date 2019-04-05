package com.revolut.entrancetask

import com.revolut.entrancetask.domain.*
import com.revolut.entrancetask.infrastructure.AppSchedulers
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Test

import org.junit.Assert.*
import java.math.BigDecimal
import java.util.*


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class CurrencyRateUseCaseTest {

    companion object {
        val RUB = Currency.getInstance("RUB")!!
        val USD = Currency.getInstance("USD")!!
        val EUR = Currency.getInstance("EUR")!!
    }

    private val repo: CurrencyRatesRepository = object : CurrencyRatesRepository {
        override fun loadRates(base: Currency): Single<CurrencyRelativeRates> {

            val rates = when (base) {
                USD -> listOf(
                    CurrencyRate(RUB, BigDecimal(70)),
                    CurrencyRate(EUR, BigDecimal(0.9))
                )
                EUR -> listOf(
                    CurrencyRate(RUB, BigDecimal(80)),
                    CurrencyRate(USD, BigDecimal(1.1))
                )
                else -> emptyList()
            }

            return Single.just(CurrencyRelativeRates(
                base,
                rates
            ))
        }
    }

    @Test
    fun successLoaded_isCorrect() {

        val useCase = CurrencyRatesUseCase(
            BigDecimal(10),
            EUR,
            repo,
            CurrencyFlagUrlGenerator(""),
            AppSchedulers(Schedulers.trampoline(), Schedulers.trampoline())
        )

        var loadedState: CurrencyListLoadedState? = null

        useCase.listState
            .subscribe { state ->
                loadedState = state as CurrencyListLoadedState
            }

        useCase.load()

        assertNotNull(loadedState)
        val list = loadedState!!.list

        assertEquals(3, list.size)

        assertCurrencyListItem(CurrencyListItem(EUR, "", CurrencyAmountOutcomeState(BigDecimal(10))),list.first())
        assertCurrencyListItem(CurrencyListItem(RUB, "", CurrencyAmountIncomeState(BigDecimal(800))), list[1])
        assertCurrencyListItem(CurrencyListItem(USD, "", CurrencyAmountIncomeState(BigDecimal(11))), list.last())
    }

    private fun assertCurrencyListItem(itemExpected: CurrencyListItem, itemActual: CurrencyListItem) {
        assertEquals(itemExpected.currency, itemActual.currency)

        when {
            itemExpected.amountState is CurrencyAmountOutcomeState -> {
                assertTrue(itemActual.amountState is CurrencyAmountOutcomeState)
                assertEqualBigDecimal(
                    (itemActual.amountState as CurrencyAmountOutcomeState).outcomeAmount,
                    (itemExpected.amountState as CurrencyAmountOutcomeState).outcomeAmount
                )
            }
            itemExpected.amountState is CurrencyAmountIncomeState -> {
                assertTrue(itemActual.amountState is CurrencyAmountIncomeState)
                assertEqualBigDecimal(
                    (itemActual.amountState as CurrencyAmountIncomeState).incomeAmount,
                    (itemExpected.amountState as CurrencyAmountIncomeState).incomeAmount
                )
            }
            itemExpected.amountState is CurrencyAmountLoading -> {
                assertTrue(itemActual.amountState is CurrencyAmountLoading)
            }
        }
    }

    private fun assertEqualBigDecimal(expected: BigDecimal, actual: BigDecimal) {
        assertEquals(-1, expected.minus(actual). abs().compareTo(BigDecimal(0.01)))
    }
}

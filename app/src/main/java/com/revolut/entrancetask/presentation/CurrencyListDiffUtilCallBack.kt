package com.revolut.entrancetask.presentation

import androidx.recyclerview.widget.DiffUtil
import com.revolut.entrancetask.domain.CurrencyAmountIncomeState
import com.revolut.entrancetask.domain.CurrencyAmountOutcomeState
import com.revolut.entrancetask.domain.CurrencyListItem

class CurrencyListDiffUtilCallBack(
    private val oldList: List<CurrencyListItem>,
    private val newList: List<CurrencyListItem>
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].currency == newList[newItemPosition].currency
    }

    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]

        if (old.amountState is CurrencyAmountOutcomeState &&
            new.amountState is CurrencyAmountOutcomeState &&
            new.amountState.outcomeAmount == null) {
            return true // don't change editText value
        }

        return old.amountState == new.amountState
    }
}
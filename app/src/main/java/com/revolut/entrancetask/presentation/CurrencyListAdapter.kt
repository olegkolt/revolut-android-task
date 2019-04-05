package com.revolut.entrancetask.presentation

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.revolut.entrancetask.R
import com.revolut.entrancetask.domain.CurrencyListItem
import com.revolut.entrancetask.domain.CurrencyAmountIncomeState
import com.revolut.entrancetask.domain.CurrencyAmountLoading
import com.revolut.entrancetask.domain.CurrencyAmountOutcomeState
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.currency_list_item.view.*
import java.math.BigDecimal
import java.util.*
import java.text.DecimalFormat


class CurrencyListAdapter(
    private val list: List<CurrencyListItem>
) : RecyclerView.Adapter<CurrencyListAdapter.ViewHolder>() {

    companion object {
        private const val TYPE_OUTCOME = 0
        private const val TYPE_INCOME = 1

        fun formatCurrency(currency: Currency, amount: BigDecimal): String {
            val df = DecimalFormat().apply {
                maximumFractionDigits = currency.defaultFractionDigits
                minimumFractionDigits = currency.defaultFractionDigits
                isGroupingUsed = false
            }

            return df.format(amount)
        }
    }

    private val outcomeAmountSubject: Subject<String> = PublishSubject.create()
    private val selectCurrencySubject: Subject<CurrencyClickEvent> = PublishSubject.create()

    private val outcomeTextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            outcomeAmountSubject.onNext(s.toString())
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
    }

    private val onItemClickListener = View.OnClickListener { view ->
        val event: CurrencyClickEvent? = view.getTag(R.id.currency_click_event_tag) as CurrencyClickEvent?

        if (event != null) {
            selectCurrencySubject.onNext(event)
        }
    }

    val outcomeAmount: Observable<String> = outcomeAmountSubject.hide()
    val currencySelection: Observable<CurrencyClickEvent> = selectCurrencySubject.hide()

    override fun getItemViewType(position: Int): Int {
        val item = list[position]

        return if (item.amountState is CurrencyAmountOutcomeState) {
            TYPE_OUTCOME
        } else {
            TYPE_INCOME
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.currency_list_item, parent, false) as View
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.currencyName.text = item.currency.displayName
        holder.code.text = item.currency.currencyCode

        Glide.with(holder.itemView.context)
            .load(item.currencyFlagUrl)
            .apply(RequestOptions.circleCropTransform())
            .into(holder.flagImage)

        holder.itemView.setOnClickListener(onItemClickListener)

        when {
            item.amountState is CurrencyAmountOutcomeState -> {
                holder.amount.removeTextChangedListener(outcomeTextWatcher)
                item.amountState.outcomeAmount?.let {
                    holder.amount.text = formatCurrency(item.currency, it)
                }
                holder.amount.isEnabled = true
                holder.amount.requestFocus()
                holder.amount.addTextChangedListener(outcomeTextWatcher)
            }
            item.amountState is CurrencyAmountIncomeState -> {
                holder.amount.text = formatCurrency(item.currency, item.amountState.incomeAmount)
                holder.amount.isEnabled = false

                holder.amount.removeTextChangedListener(outcomeTextWatcher)
                holder.itemView.setTag(
                    R.id.currency_click_event_tag, CurrencyClickEvent(item.amountState.incomeAmount, item.currency)
                )
            }
            item.amountState is CurrencyAmountLoading -> {
                holder.amount.text = "__" // @todo
                holder.amount.isEnabled = false

                holder.amount.removeTextChangedListener(outcomeTextWatcher)
                holder.itemView.setTag(R.id.currency_click_event_tag, null)
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val amount: TextView = view.amount
        val flagImage: ImageView = view.flagImage
        val code: TextView = view.code
        val currencyName: TextView = view.name
    }
}
package com.example.focusquest

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.focusquest.databinding.ItemQuoteSlideBinding

data class QuoteSlide(
    val quote: String,
    val imageResId: Int
)

class QuoteSliderAdapter(
    private val slides: List<QuoteSlide>
) : RecyclerView.Adapter<QuoteSliderAdapter.QuoteViewHolder>() {

    class QuoteViewHolder(val binding: ItemQuoteSlideBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteViewHolder {
        val binding = ItemQuoteSlideBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        val slide = slides[position]
        holder.binding.ivQuoteBackground.setImageResource(slide.imageResId)
        holder.binding.tvQuoteText.text = slide.quote
    }

    override fun getItemCount(): Int = slides.size
}

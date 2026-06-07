package com.example.focusquest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.focusquest.viewmodel.ChatMessage

class ChatAdapter(private val items: MutableList<ChatMessage> = mutableListOf()) :
    RecyclerView.Adapter<ChatAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvUser: TextView = view.findViewById(R.id.tvUserMessage)
        val tvAi: TextView   = view.findViewById(R.id.tvAiMessage)
        val bubbleUser: View = view.findViewById(R.id.bubbleUser)
        val bubbleAi: View   = view.findViewById(R.id.bubbleAi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val msg = items[position]
        if (msg.isUser) {
            holder.bubbleUser.visibility = View.VISIBLE
            holder.bubbleAi.visibility  = View.GONE
            holder.tvUser.text = msg.text
        } else {
            holder.bubbleAi.visibility  = View.VISIBLE
            holder.bubbleUser.visibility = View.GONE
            holder.tvAi.text = msg.text
        }
    }

    fun setMessages(newItems: List<ChatMessage>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}

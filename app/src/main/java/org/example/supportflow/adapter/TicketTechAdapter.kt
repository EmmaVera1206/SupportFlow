package org.example.supportflow.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.example.supportflow.R
import org.example.supportflow.model.Ticket

class TicketTechAdapter(
    private val onClick: (Ticket) -> Unit
) : RecyclerView.Adapter<TicketTechAdapter.VH>() {

    private val data = mutableListOf<Ticket>()

    fun submit(newData: List<Ticket>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_ticket, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val t = data[position]
        holder.tvTitle.text = t.title
        holder.tvCategory.text = "Categoría: ${t.category}"
        holder.tvStatus.text = t.status
        holder.tvPriority.text = t.priority

        holder.itemView.setOnClickListener { onClick(t) }
    }

    override fun getItemCount() = data.size

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvPriority: TextView = itemView.findViewById(R.id.tvPriority)
    }
}
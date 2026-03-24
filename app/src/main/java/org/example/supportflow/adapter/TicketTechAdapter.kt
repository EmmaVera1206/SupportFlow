package org.example.supportflow.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.example.supportflow.R
import org.example.supportflow.model.Ticket
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

        holder.tvTitle.text = valorSeguro(t.title)
        holder.tvDescription.text = valorSeguro(t.description)

        holder.tvCategory.text = "Categoría:"
        holder.tvCategoryValue.text = valorSeguro(t.category)

        holder.tvStatus.text = formatearEstado(t.status)
        holder.tvPriority.text = formatearPrioridad(t.priority)

        if (t.createdAt > 0) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            holder.tvDate.text = sdf.format(Date(t.createdAt))
        } else {
            holder.tvDate.text = "--/--/----"
        }

        aplicarEstiloStatus(holder.tvStatus, t.status)
        aplicarEstiloPrioridad(holder.tvPriority, t.priority)

        holder.itemView.setOnClickListener { onClick(t) }
    }

    override fun getItemCount() = data.size

    private fun valorSeguro(value: String?): String {
        return if (value.isNullOrBlank()) "-" else value
    }

    private fun formatearEstado(status: String?): String {
        return when (status) {
            "OPEN" -> "Abierto"
            "IN_PROGRESS" -> "En proceso"
            "RESOLVED" -> "Resuelto"
            else -> status ?: "Estado"
        }
    }

    private fun formatearPrioridad(priority: String?): String {
        return when (priority) {
            "LOW" -> "Baja"
            "MEDIUM" -> "Media"
            "HIGH" -> "Alta"
            "CRITICAL" -> "Crítica"
            else -> priority ?: "Prioridad"
        }
    }

    private fun aplicarEstiloStatus(tv: TextView, status: String?) {
        when (status) {
            "OPEN" -> tv.setBackgroundColor(Color.parseColor("#7C6DFF"))
            "IN_PROGRESS" -> tv.setBackgroundColor(Color.parseColor("#4CAF50"))
            "RESOLVED" -> tv.setBackgroundColor(Color.parseColor("#FF9800"))
            else -> tv.setBackgroundColor(Color.parseColor("#7C6DFF"))
        }
    }

    private fun aplicarEstiloPrioridad(tv: TextView, priority: String?) {
        when (priority) {
            "LOW" -> tv.setBackgroundColor(Color.parseColor("#BDBDBD"))
            "MEDIUM" -> tv.setBackgroundColor(Color.parseColor("#9FA8DA"))
            "HIGH" -> tv.setBackgroundColor(Color.parseColor("#E6D34C"))
            "CRITICAL" -> tv.setBackgroundColor(Color.parseColor("#E53935"))
            else -> tv.setBackgroundColor(Color.parseColor("#A4D96C"))
        }
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvCategoryValue: TextView = itemView.findViewById(R.id.tvCategoryValue)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvPriority: TextView = itemView.findViewById(R.id.tvPriority)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
    }
}
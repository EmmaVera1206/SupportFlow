package org.example.supportflow.ui.tecnico

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import org.example.supportflow.R
import org.example.supportflow.adapter.TicketTechAdapter
import org.example.supportflow.model.Ticket
import org.example.supportflow.ui.auth.LoginActivity

class TecnicoAsignadosActivity : AppCompatActivity() {

    private lateinit var adapter: TicketTechAdapter
    private val db = FirebaseFirestore.getInstance()
    private var ticketsListener: ListenerRegistration? = null

    private var todosLosTickets = mutableListOf<Ticket>()

    private lateinit var tvEmpty: TextView
    private lateinit var layoutFiltros: LinearLayout
    private lateinit var spFilterCategory: Spinner
    private lateinit var spFilterStatus: Spinner
    private lateinit var spFilterPriority: Spinner

    private var filtroCategoria = "Todas"
    private var filtroEstado = "Todos"
    private var filtroPrioridad = "Todas"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tecnico_asignados)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            irALogin()
            return
        }
        val uid = user.uid

        val rv = findViewById<RecyclerView>(R.id.rvTicketsAsignados)
        rv.layoutManager = LinearLayoutManager(this)

        adapter = TicketTechAdapter { ticket ->
            val i = Intent(this, TecnicoDetalleActivity::class.java)
            i.putExtra("TICKET_ID", ticket.id)
            startActivity(i)
        }
        rv.adapter = adapter

        tvEmpty = findViewById(R.id.tvEmpty)
        layoutFiltros = findViewById(R.id.layoutFiltrosTecnico)
        spFilterCategory = findViewById(R.id.spFilterCategory)
        spFilterStatus = findViewById(R.id.spFilterStatus)
        spFilterPriority = findViewById(R.id.spFilterPriority)

        configurarSpinnersFiltros()
        configurarEventosFiltros()

        findViewById<ImageButton>(R.id.btnBackTecnico).setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.btnFilterTecnico).setOnClickListener {
            layoutFiltros.visibility =
                if (layoutFiltros.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        findViewById<Button>(R.id.btnClearFiltersTecnico).setOnClickListener {
            limpiarFiltros()
        }

        findViewById<Button>(R.id.btnLogoutTecnico).setOnClickListener {
            hacerLogoutSeguro()
        }

        ticketsListener = db.collection("tickets")
            .whereEqualTo("assignedTo", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    Toast.makeText(this, "Error cargando: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val list = mutableListOf<Ticket>()
                snap?.documents?.forEach { doc ->
                    val t = doc.toObject(Ticket::class.java)
                    if (t != null) {
                        t.id = doc.id
                        list.add(t)
                    }
                }

                todosLosTickets = list
                aplicarFiltros()
            }
    }

    private fun configurarSpinnersFiltros() {
        val categoryAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.ticket_filter_categories_tecnico,
            android.R.layout.simple_spinner_item
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spFilterCategory.adapter = categoryAdapter

        val statusAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.ticket_filter_status_tecnico,
            android.R.layout.simple_spinner_item
        )
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spFilterStatus.adapter = statusAdapter

        val priorityAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.ticket_filter_priorities_tecnico,
            android.R.layout.simple_spinner_item
        )
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spFilterPriority.adapter = priorityAdapter
    }

    private fun configurarEventosFiltros() {
        spFilterCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filtroCategoria = spFilterCategory.selectedItem?.toString() ?: "Todas"
                aplicarFiltros()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spFilterStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filtroEstado = spFilterStatus.selectedItem?.toString() ?: "Todos"
                aplicarFiltros()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spFilterPriority.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filtroPrioridad = spFilterPriority.selectedItem?.toString() ?: "Todas"
                aplicarFiltros()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun aplicarFiltros() {
        val filtrados = todosLosTickets.filter { ticket ->
            val coincideCategoria =
                filtroCategoria == "Todas" || (ticket.category ?: "") == filtroCategoria

            val coincideEstado =
                filtroEstado == "Todos" || (ticket.status ?: "") == filtroEstado

            val coincidePrioridad =
                filtroPrioridad == "Todas" || (ticket.priority ?: "") == filtroPrioridad

            coincideCategoria && coincideEstado && coincidePrioridad
        }

        adapter.submit(filtrados)
        tvEmpty.visibility = if (filtrados.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun limpiarFiltros() {
        spFilterCategory.setSelection(0)
        spFilterStatus.setSelection(0)
        spFilterPriority.setSelection(0)

        filtroCategoria = "Todas"
        filtroEstado = "Todos"
        filtroPrioridad = "Todas"

        aplicarFiltros()
    }

    private fun removerListeners() {
        ticketsListener?.remove()
        ticketsListener = null
    }

    private fun hacerLogoutSeguro() {
        removerListeners()
        FirebaseAuth.getInstance().signOut()
        irALogin()
    }

    private fun irALogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        removerListeners()
        super.onDestroy()
    }
}
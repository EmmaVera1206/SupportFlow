package org.example.supportflow.ui.tecnico

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ScrollView
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
    private lateinit var layoutFiltros: ScrollView

    private lateinit var spFilterCategory: Spinner
    private lateinit var spFilterStatus: Spinner
    private lateinit var spFilterPriority: Spinner
    private lateinit var spFilterDate: Spinner
    private lateinit var spFilterUser: Spinner

    private var filtroCategoria = "Todas"
    private var filtroEstado = "Todos"
    private var filtroPrioridad = "Todas"
    private var filtroFecha = "Cualquiera"
    private var filtroUsuario = "Todos"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tecnico_asignados)

        val user = FirebaseAuth.getInstance().currentUser ?: return irALogin()
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
        spFilterDate = findViewById(R.id.spFilterDate)
        spFilterUser = findViewById(R.id.spFilterUser)

        configurarSpinnersFiltros()
        configurarEventosFiltros()

        findViewById<ImageButton>(R.id.btnBackTecnico).setOnClickListener { finish() }
        findViewById<ImageButton>(R.id.btnFilterTecnico).setOnClickListener {
            layoutFiltros.visibility = if (layoutFiltros.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        findViewById<Button>(R.id.btnClearFiltersTecnico).setOnClickListener { limpiarFiltros() }
        findViewById<Button>(R.id.btnLogoutTecnico).setOnClickListener { hacerLogoutSeguro() }

        cargarTicketsDesdeFirebase(uid)
    }

    private fun cargarTicketsDesdeFirebase(uid: String) {
        ticketsListener = db.collection("tickets")
            .whereEqualTo("assignedTo", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                val list = mutableListOf<Ticket>()
                snap?.documents?.forEach { doc ->
                    doc.toObject(Ticket::class.java)?.let {
                        it.id = doc.id
                        list.add(it)
                    }
                }
                todosLosTickets = list
                aplicarFiltros()
            }
    }

    private fun configurarSpinnersFiltros() {
        vincularSpinner(spFilterCategory, R.array.ticket_filter_categories_tecnico)
        vincularSpinner(spFilterStatus, R.array.ticket_filter_status_tecnico)
        vincularSpinner(spFilterPriority, R.array.ticket_filter_priorities_tecnico)
        vincularSpinner(spFilterDate, R.array.filter_date_options)
        cargarUsuariosDinamicos()
    }

    private fun cargarUsuariosDinamicos() {
        db.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                val listaNombres = mutableListOf("Todos")
                for (doc in documents) {
                    val nombre = doc.getString("name")
                    if (!nombre.isNullOrEmpty()) listaNombres.add(nombre)
                }
                val userAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listaNombres)
                userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spFilterUser.adapter = userAdapter
            }
    }

    private fun vincularSpinner(spinner: Spinner, arrayRes: Int) {
        val adapter = ArrayAdapter.createFromResource(this, arrayRes, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun configurarEventosFiltros() {
        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                filtroCategoria = spFilterCategory.selectedItem.toString()
                filtroEstado = spFilterStatus.selectedItem.toString()
                filtroPrioridad = spFilterPriority.selectedItem.toString()
                filtroFecha = spFilterDate.selectedItem.toString()
                filtroUsuario = spFilterUser.selectedItem?.toString() ?: "Todos"
                aplicarFiltros()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        spFilterCategory.onItemSelectedListener = listener
        spFilterStatus.onItemSelectedListener = listener
        spFilterPriority.onItemSelectedListener = listener
        spFilterDate.onItemSelectedListener = listener
        spFilterUser.onItemSelectedListener = listener
    }

    private fun aplicarFiltros() {
        val ahora = System.currentTimeMillis()
        val unDia = 24 * 60 * 60 * 1000L

        val filtrados = todosLosTickets.filter { t ->
            val matchesCat = filtroCategoria == "Todas" || t.category == filtroCategoria
            val matchesEst = filtroEstado == "Todos" || t.status == filtroEstado
            val matchesPri = filtroPrioridad == "Todas" || t.priority == filtroPrioridad

            val fechaTicket = t.createdAt
            val matchesFecha = when(filtroFecha) {
                "Hoy" -> (ahora - fechaTicket) <= unDia
                "Semana" -> (ahora - fechaTicket) <= (unDia * 7)
                "Mes" -> (ahora - fechaTicket) <= (unDia * 30)
                else -> true
            }

            val matchesUser = filtroUsuario == "Todos" || t.assignedToName == filtroUsuario

            matchesCat && matchesEst && matchesPri && matchesFecha && matchesUser
        }

        adapter.submit(filtrados)
        tvEmpty.visibility = if (filtrados.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun limpiarFiltros() {
        spFilterCategory.setSelection(0)
        spFilterStatus.setSelection(0)
        spFilterPriority.setSelection(0)
        spFilterDate.setSelection(0)
        spFilterUser.setSelection(0)
        aplicarFiltros()
    }

    private fun irALogin() {
        startActivity(Intent(this, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        finish()
    }

    private fun hacerLogoutSeguro() {
        ticketsListener?.remove()
        FirebaseAuth.getInstance().signOut()
        irALogin()
    }

    override fun onDestroy() {
        ticketsListener?.remove()
        super.onDestroy()
    }
}
package org.example.supportflow.ui.tecnico

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.example.supportflow.R
import org.example.supportflow.model.Ticket
import org.example.supportflow.ui.auth.LoginActivity
import org.example.supportflow.adapter.TicketTechAdapter

class TecnicoAsignadosActivity : AppCompatActivity() {

    private lateinit var adapter: TicketTechAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tecnico_asignados)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
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


        val btnLogout = findViewById<Button>(R.id.btnLogoutTecnico)
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }


        val tvEmpty = findViewById<TextView?>(R.id.tvEmpty)

        // Query tickets asignados
        db.collection("tickets")
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

                adapter.submit(list)
                tvEmpty?.visibility = if (list.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            }
    }
}
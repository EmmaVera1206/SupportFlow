package org.example.supportflow.ui.tecnico

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.example.supportflow.R
import org.example.supportflow.model.Ticket


class TecnicoDetalleActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var ticketId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tecnico_detalle)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) { finish(); return }

        ticketId = intent.getStringExtra("TICKET_ID") ?: run {
            Toast.makeText(this, "Ticket inválido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val tvDesc = findViewById<TextView>(R.id.tvDesc)

        val spinnerEstado = findViewById<Spinner>(R.id.spEstadoTicket)
        val opciones = arrayOf("OPEN", "IN_PROGRESS", "RESOLVED")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opciones)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEstado.adapter = adapter

        val btnResuelto = findViewById<Button>(R.id.btnMarcarResuelto)
        val btnSendComment = findViewById<Button>(R.id.btnSendComment)
        val etComment = findViewById<EditText>(R.id.etComment)

        // Escucha ticket
        db.collection("tickets").document(ticketId)
            .addSnapshotListener { doc, e ->
                if (e != null) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (doc != null && doc.exists()) {
                    val t = doc.toObject(Ticket::class.java)
                    if (t != null) {
                        tvTitle.text = t.title
                        tvDesc.text = t.description ?: ""

                        // set spinner a estado actual
                        val idx = opciones.indexOf(t.status ?: "OPEN")
                        if (idx >= 0) spinnerEstado.setSelection(idx)
                    }
                }
            }

        spinnerEstado.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                val nuevoEstado = opciones[position]

                val update = hashMapOf<String, Any>(
                    "status" to nuevoEstado
                )

                // si cambió a RESOLVED, guarda cuando se resolvió
                if (nuevoEstado == "RESOLVED") {
                    update["closedAt"] = System.currentTimeMillis()
                }

                db.collection("tickets").document(ticketId)
                    .update(update)
                    .addOnFailureListener { err ->
                        Toast.makeText(this@TecnicoDetalleActivity, "Error actualizando: ${err.message}", Toast.LENGTH_SHORT).show()
                    }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Botón marcar resuelto
        btnResuelto.setOnClickListener {
            spinnerEstado.setSelection(2) // RESOLVED
            Toast.makeText(this, "Ticket marcado como resuelto ✅", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.btnBackTecnicoDetalle).setOnClickListener { finish() }

        // Comentario técnico
        btnSendComment.setOnClickListener {
            val msg = etComment.text.toString().trim()
            if (msg.isEmpty()) {
                Toast.makeText(this, "Escribe una nota", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val comment = hashMapOf<String, Any>(
                "message" to msg,
                "authorId" to user.uid,
                "authorName" to (user.email ?: "Técnico"),
                "createdAt" to System.currentTimeMillis()
            )

            db.collection("tickets").document(ticketId)
                .collection("comments")
                .add(comment)
                .addOnSuccessListener {
                    etComment.setText("")
                    Toast.makeText(this, "Comentario enviado ✅", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { err ->
                    Toast.makeText(this, "Error: ${err.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

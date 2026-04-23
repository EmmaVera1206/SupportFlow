package org.example.supportflow.ui.tecnico

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import org.example.supportflow.R
import org.example.supportflow.model.Ticket
import org.example.supportflow.ui.auth.LoginActivity

class TecnicoDetalleActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var ticketId: String
    private var ticketListener: ListenerRegistration? = null

    private var cargandoEstadoInicial = true
    private var ultimoEstadoCargado: String? = null
    private var currentUserName: String = "Técnico"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tecnico_detalle)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            irALogin()
            return
        }

        db.collection("users").document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name")
                if (!name.isNullOrBlank()) {
                    currentUserName = name.trim()
                }
            }

        ticketId = intent.getStringExtra("TICKET_ID") ?: run {
            Toast.makeText(this, "Ticket inválido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val tvDesc = findViewById<TextView>(R.id.tvDesc)

        val spinnerEstado = findViewById<Spinner>(R.id.spEstadoTicket)
        val opciones = arrayOf("OPEN", "IN_PROGRESS", "RESOLVED")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opciones)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEstado.adapter = spinnerAdapter

        val btnResuelto = findViewById<Button>(R.id.btnMarcarResuelto)
        val btnSendComment = findViewById<Button>(R.id.btnSendComment)
        val etComment = findViewById<EditText>(R.id.etComment)

        findViewById<View>(R.id.btnBackTecnicoDetalle).setOnClickListener { finish() }

        val btnLogout = findViewById<Button?>(R.id.btnLogout)
        btnLogout?.setOnClickListener { hacerLogoutSeguro() }

        ticketListener = db.collection("tickets").document(ticketId)
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

                        val estadoActual = t.status ?: "OPEN"
                        ultimoEstadoCargado = estadoActual

                        val idx = opciones.indexOf(estadoActual)
                        if (idx >= 0 && spinnerEstado.selectedItemPosition != idx) {
                            cargandoEstadoInicial = true
                            spinnerEstado.setSelection(idx)
                        } else {
                            cargandoEstadoInicial = false
                        }
                    }
                }
            }

        spinnerEstado.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val nuevoEstado = opciones[position]

                if (cargandoEstadoInicial) {
                    cargandoEstadoInicial = false
                    return
                }

                if (nuevoEstado == ultimoEstadoCargado) {
                    return
                }

                val update = hashMapOf<String, Any>(
                    "status" to nuevoEstado
                )

                if (nuevoEstado == "RESOLVED") {
                    update["closedAt"] = System.currentTimeMillis()
                }

                db.collection("tickets").document(ticketId)
                    .update(update)
                    .addOnSuccessListener {
                        ultimoEstadoCargado = nuevoEstado
                    }
                    .addOnFailureListener { err ->
                        Toast.makeText(
                            this@TecnicoDetalleActivity,
                            "Error actualizando: ${err.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        btnResuelto.setOnClickListener {
            spinnerEstado.setSelection(2)
            Toast.makeText(this, "Ticket marcado como resuelto ✅", Toast.LENGTH_SHORT).show()
        }

        btnSendComment.setOnClickListener {
            val msg = etComment.text.toString().trim()
            if (msg.isEmpty()) {
                Toast.makeText(this, "Escribe una nota", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val comment = hashMapOf<String, Any>(
                "message" to msg,
                "authorId" to user.uid,
                "authorName" to currentUserName,
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

    private fun removerListeners() {
        ticketListener?.remove()
        ticketListener = null
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

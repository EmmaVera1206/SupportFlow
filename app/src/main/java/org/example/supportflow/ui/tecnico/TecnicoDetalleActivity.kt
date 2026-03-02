package org.example.supportflow.ui.tecnico

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.example.supportflow.R

class TecnicoDetalleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tecnico_detalle)


        val spinnerEstado = findViewById<Spinner>(R.id.spEstadoTicket)
        val opciones = arrayOf("PENDIENTE", "EN PROCESO", "RESUELTO")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opciones)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEstado.adapter = adapter


        val btnResuelto = findViewById<Button>(R.id.btnMarcarResuelto)

        btnResuelto.setOnClickListener {

            spinnerEstado.setSelection(2)

            Toast.makeText(this, "¡Excelente! Ticket marcado como resuelto", Toast.LENGTH_LONG).show()
        }
    }
}
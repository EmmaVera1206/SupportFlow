package org.example.supportflow.ui.tecnico

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.example.supportflow.R

class TecnicoAsignadosActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_tecnico_asignados)


        val btnLogout = findViewById<Button>(R.id.btnLogoutTecnico)

        btnLogout.setOnClickListener {

            Toast.makeText(this, "Cerrando sesión de técnico...", Toast.LENGTH_SHORT).show()

            finish()
        }
    }
}
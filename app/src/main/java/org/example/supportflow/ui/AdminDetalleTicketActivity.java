package org.example.supportflow.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;
import org.example.supportflow.R;
import org.example.supportflow.model.Ticket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminDetalleTicketActivity extends AppCompatActivity {

    public static final String EXTRA_TICKET_ID = "ticket_id";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_DESCRIPTION = "description";
    public static final String EXTRA_CATEGORY = "category";
    public static final String EXTRA_PRIORITY = "priority";
    public static final String EXTRA_STATUS = "status";
    public static final String EXTRA_ASSIGNED_TO = "assignedTo";
    public static final String EXTRA_CREATED_AT = "createdAt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_detalle_ticket);

        // Botón regresar
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Recibir datos
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        String description = getIntent().getStringExtra(EXTRA_DESCRIPTION);
        String category = getIntent().getStringExtra(EXTRA_CATEGORY);
        String priority = getIntent().getStringExtra(EXTRA_PRIORITY);
        String status = getIntent().getStringExtra(EXTRA_STATUS);
        String assignedTo = getIntent().getStringExtra(EXTRA_ASSIGNED_TO);
        long createdAt = getIntent().getLongExtra(EXTRA_CREATED_AT, 0);

        // Mostrar datos
        ((TextView) findViewById(R.id.tvTitulo)).setText(title);
        ((TextView) findViewById(R.id.tvDescripcion)).setText(description);
        ((TextView) findViewById(R.id.tvCategoria)).setText("Categoría: " + category);
        ((TextView) findViewById(R.id.tvPrioridad)).setText("Prioridad: " + priority);
        ((TextView) findViewById(R.id.tvEstatus)).setText("Estatus: " + status);
        ((TextView) findViewById(R.id.tvAsignadoA)).setText(
                assignedTo != null && !assignedTo.isEmpty()
                        ? "Asignado a: " + assignedTo
                        : "Sin técnico asignado"
        );

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        ((TextView) findViewById(R.id.tvFecha)).setText("Fecha: " + sdf.format(new Date(createdAt)));
    }
}
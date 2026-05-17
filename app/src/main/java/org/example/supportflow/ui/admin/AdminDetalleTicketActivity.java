package org.example.supportflow.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.example.supportflow.R;

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
    public static final String EXTRA_ASSIGNED_TO_NAME = "assignedToName";
    public static final String EXTRA_CREATED_AT = "createdAt";
    public static final String EXTRA_IMAGE_URL = "image_url";
    public static final String EXTRA_CREATED_BY_NAME = "createdByName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_detalle_ticket);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        String description = getIntent().getStringExtra(EXTRA_DESCRIPTION);
        String category = getIntent().getStringExtra(EXTRA_CATEGORY);
        String priority = getIntent().getStringExtra(EXTRA_PRIORITY);
        String status = getIntent().getStringExtra(EXTRA_STATUS);
        String assignedToName = getIntent().getStringExtra(EXTRA_ASSIGNED_TO_NAME);
        String imageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);
        String createdByName = getIntent().getStringExtra(EXTRA_CREATED_BY_NAME);
        long createdAt = getIntent().getLongExtra(EXTRA_CREATED_AT, 0);

        ImageView ivEvidencia = findViewById(R.id.ivEvidencia);

        ((TextView) findViewById(R.id.tvTitulo)).setText(title != null ? title : "-");
        ((TextView) findViewById(R.id.tvDescripcion)).setText(description != null ? description : "-");
        ((TextView) findViewById(R.id.tvCategoria)).setText("Categoría: " + (category != null ? category : "-"));
        ((TextView) findViewById(R.id.tvPrioridad)).setText("Prioridad: " + (priority != null ? priority : "-"));
        ((TextView) findViewById(R.id.tvEstatus)).setText("Estatus: " + (status != null ? status : "-"));
        ((TextView) findViewById(R.id.tvAsignadoA)).setText(
                assignedToName != null && !assignedToName.trim().isEmpty()
                        ? "Asignado a: " + assignedToName
                        : "Sin técnico asignado"
        );
        ((TextView) findViewById(R.id.tvCreadoPor)).setText(
                createdByName != null && !createdByName.trim().isEmpty()
                        ? "Creado por: " + createdByName
                        : "Creado por: -"
        );

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        ((TextView) findViewById(R.id.tvFecha)).setText("Fecha: " + sdf.format(new Date(createdAt)));

        if (imageUrl != null && !imageUrl.isEmpty()) {
            ivEvidencia.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(imageUrl)
                    .into(ivEvidencia);
        } else {
            ivEvidencia.setVisibility(View.GONE);
        }
    }
}
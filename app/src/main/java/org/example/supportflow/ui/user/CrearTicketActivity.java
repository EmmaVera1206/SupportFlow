package org.example.supportflow.ui.user;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import org.example.supportflow.R;
import org.example.supportflow.data.TicketRepository;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CrearTicketActivity extends AppCompatActivity {

    private final TicketRepository repo = new TicketRepository();
    private Bitmap imagenCapturada = null;
    private String currentUserName = "Usuario";

    private final ActivityResultLauncher<Void> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(),
            bitmap -> {
                if (bitmap != null) {
                    imagenCapturada = bitmap;
                    ((ImageView) findViewById(R.id.iv_preview)).setImageBitmap(bitmap);
                }
            }
    );

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    takePictureLauncher.launch(null);
                } else {
                    Toast.makeText(this, "Permiso de cámara necesario", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_ticket);

        try {
            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", "divqqxy4r");
            config.put("secure", true);
            MediaManager.init(getApplicationContext(), config);
        } catch (Exception e) {
            // Ya inicializado
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Sesión no válida.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = user.getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        if (name != null && !name.trim().isEmpty()) {
                            currentUserName = name.trim();
                        }
                    }
                });

        EditText etTitle = findViewById(R.id.etTitle);
        EditText etDesc = findViewById(R.id.etDescription);
        Spinner spCategory = findViewById(R.id.spCategory);
        Spinner spPriority = findViewById(R.id.spPriority);
        Button btnCrear = findViewById(R.id.btnCrear);
        ImageButton btnTomarFoto = findViewById(R.id.btn_tomar_foto);

        ArrayAdapter<CharSequence> adapterCat = ArrayAdapter.createFromResource(
                this, R.array.ticket_categories, android.R.layout.simple_spinner_item
        );
        adapterCat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapterCat);

        ArrayAdapter<CharSequence> adapterPri = ArrayAdapter.createFromResource(
                this, R.array.ticket_priorities, android.R.layout.simple_spinner_item
        );
        adapterPri.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPriority.setAdapter(adapterPri);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnTomarFoto.setOnClickListener(v -> {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                takePictureLauncher.launch(null);
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        btnCrear.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String cat = spCategory.getSelectedItem() != null ? spCategory.getSelectedItem().toString().trim() : "";
            String pri = spPriority.getSelectedItem() != null ? spPriority.getSelectedItem().toString().trim().toUpperCase() : "";

            if (title.isEmpty() || desc.isEmpty() || cat.isEmpty() || pri.isEmpty()) {
                Toast.makeText(this, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            btnCrear.setEnabled(false);

            if (imagenCapturada != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imagenCapturada.compress(Bitmap.CompressFormat.JPEG, 90, baos);
                byte[] data = baos.toByteArray();

                MediaManager.get().upload(data)
                        .unsigned("ticket_final")
                        .callback(new UploadCallback() {
                            @Override
                            public void onStart(String requestId) {
                            }

                            @Override
                            public void onProgress(String requestId, long bytes, long totalBytes) {
                            }

                            @Override
                            public void onSuccess(String requestId, Map resultData) {
                                String url = resultData.get("secure_url") != null
                                        ? resultData.get("secure_url").toString()
                                        : "";

                                enviarTicketAFirestore(title, desc, cat, pri, uid, url);
                            }

                            @Override
                            public void onError(String requestId, ErrorInfo error) {
                                btnCrear.setEnabled(true);
                                Toast.makeText(
                                        CrearTicketActivity.this,
                                        "Error Cloudinary: " + error.getDescription(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }

                            @Override
                            public void onReschedule(String requestId, ErrorInfo error) {
                            }
                        })
                        .dispatch();

            } else {
                enviarTicketAFirestore(title, desc, cat, pri, uid, "");
            }
        });
    }

    private void enviarTicketAFirestore(String title, String desc, String cat, String pri, String uid, String url) {
        repo.crearTicket(title, desc, cat, pri, uid, currentUserName, url, new TicketRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(CrearTicketActivity.this, "Ticket creado ✅", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(Exception e) {
                findViewById(R.id.btnCrear).setEnabled(true);
                Toast.makeText(CrearTicketActivity.this, "Falla en DB: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
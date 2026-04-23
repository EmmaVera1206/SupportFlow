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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.example.supportflow.R;
import org.example.supportflow.data.TicketRepository;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class CrearTicketActivity extends AppCompatActivity {

    private final TicketRepository repo = new TicketRepository();
    private Bitmap imagenCapturada = null;
    private String currentUserName = "Usuario";

    private final ActivityResultLauncher<Void> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(),
            bitmap -> {
                if (bitmap != null) {
                    imagenCapturada = bitmap;
                    ImageView ivPreview = findViewById(R.id.iv_preview);
                    ivPreview.setImageBitmap(bitmap);
                }
            }
    );

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    takePictureLauncher.launch(null);
                } else {
                    Toast.makeText(this, "Permiso de cámara necesario para adjuntar foto", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_ticket);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Sesión no válida.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        if (name != null && !name.trim().isEmpty()) {
                            currentUserName = name.trim();
                        }
                    }
                })
                .addOnFailureListener(e -> currentUserName = "Usuario");

        EditText etTitle = findViewById(R.id.etTitle);
        EditText etDesc = findViewById(R.id.etDescription);
        Spinner spCategory = findViewById(R.id.spCategory);
        Spinner spPriority = findViewById(R.id.spPriority);
        Button btnCrear = findViewById(R.id.btnCrear);
        ImageButton btnTomarFoto = findViewById(R.id.btn_tomar_foto);

        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.ticket_categories,
                android.R.layout.simple_spinner_item
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(categoryAdapter);

        ArrayAdapter<CharSequence> priorityAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.ticket_priorities,
                android.R.layout.simple_spinner_item
        );
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPriority.setAdapter(priorityAdapter);

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
            String cat = spCategory.getSelectedItem() != null
                    ? spCategory.getSelectedItem().toString().trim()
                    : "";
            String pri = spPriority.getSelectedItem() != null
                    ? spPriority.getSelectedItem().toString().trim().toUpperCase()
                    : "";

            if (title.isEmpty() || desc.isEmpty() || cat.isEmpty() || pri.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            btnCrear.setEnabled(false);

            if (imagenCapturada != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imagenCapturada.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                StorageReference storageRef = FirebaseStorage.getInstance()
                        .getReference()
                        .child("tickets/" + UUID.randomUUID() + ".jpg");

                storageRef.putBytes(data)
                        .addOnSuccessListener(taskSnapshot ->
                                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    String downloadUrl = uri.toString();
                                    repo.crearTicket(
                                            title,
                                            desc,
                                            cat,
                                            pri,
                                            uid,
                                            currentUserName,
                                            downloadUrl,
                                            new TicketRepository.SimpleCallback() {
                                                @Override
                                                public void onSuccess() {
                                                    Toast.makeText(CrearTicketActivity.this, "Ticket creado con foto ✅", Toast.LENGTH_SHORT).show();
                                                    setResult(RESULT_OK);
                                                    finish();
                                                }

                                                @Override
                                                public void onError(Exception e) {
                                                    btnCrear.setEnabled(true);
                                                    Toast.makeText(CrearTicketActivity.this, "Error al crear: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                    );
                                })
                        )
                        .addOnFailureListener(e -> {
                            btnCrear.setEnabled(true);
                            Toast.makeText(this, "Error al subir foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });

            } else {
                repo.crearTicket(
                        title,
                        desc,
                        cat,
                        pri,
                        uid,
                        currentUserName,
                        "",
                        new TicketRepository.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(CrearTicketActivity.this, "Ticket creado sin foto ✅", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            }

                            @Override
                            public void onError(Exception e) {
                                btnCrear.setEnabled(true);
                                Toast.makeText(CrearTicketActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            }
        });
    }
}
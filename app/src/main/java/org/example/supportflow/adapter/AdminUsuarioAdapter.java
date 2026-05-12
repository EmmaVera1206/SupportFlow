package org.example.supportflow.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.example.supportflow.R;

import java.util.List;

public class AdminUsuarioAdapter extends RecyclerView.Adapter<AdminUsuarioAdapter.ViewHolder> {

    public interface OnUserActionListener {
        void onCambiarRol(UsuarioAdminItem usuario, String nuevoRol);
    }

    public static class UsuarioAdminItem {
        public String uid;
        public String name;
        public String email;
        public String role;

        public UsuarioAdminItem(String uid, String name, String email, String role) {
            this.uid = uid;
            this.name = name;
            this.email = email;
            this.role = role;
        }
    }

    private List<UsuarioAdminItem> usuarios;
    private final String currentAdminUid;
    private final OnUserActionListener listener;

    public AdminUsuarioAdapter(List<UsuarioAdminItem> usuarios, String currentAdminUid, OnUserActionListener listener) {
        this.usuarios = usuarios;
        this.currentAdminUid = currentAdminUid;
        this.listener = listener;
    }

    public void setUsuarios(List<UsuarioAdminItem> usuarios) {
        this.usuarios = usuarios;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usuario_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UsuarioAdminItem usuario = usuarios.get(position);

        holder.tvNombreUsuario.setText(usuario.name);
        holder.tvCorreoUsuario.setText(usuario.email);
        holder.tvRolActual.setText("Rol actual: " + usuario.role);

        String[] roles = {"USER", "TECH"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                holder.itemView.getContext(),
                android.R.layout.simple_spinner_item,
                roles
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spRolUsuario.setAdapter(spinnerAdapter);

        int selectedIndex = 0;
        for (int i = 0; i < roles.length; i++) {
            if (roles[i].equals(usuario.role)) {
                selectedIndex = i;
                break;
            }
        }
        holder.spRolUsuario.setSelection(selectedIndex);

        boolean esAdminActual = usuario.uid.equals(currentAdminUid);

        holder.spRolUsuario.setEnabled(!esAdminActual);
        holder.btnGuardarRol.setEnabled(!esAdminActual);

        if (esAdminActual) {
            holder.btnGuardarRol.setText("Tu cuenta");
        } else {
            holder.btnGuardarRol.setText("Guardar rol");
        }

        holder.btnGuardarRol.setOnClickListener(v -> {
            String nuevoRol = holder.spRolUsuario.getSelectedItem().toString();
            if (!nuevoRol.equals(usuario.role)) {
                listener.onCambiarRol(usuario, nuevoRol);
            }
        });
    }

    @Override
    public int getItemCount() {
        return usuarios != null ? usuarios.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreUsuario, tvCorreoUsuario, tvRolActual;
        Spinner spRolUsuario;
        Button btnGuardarRol;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreUsuario = itemView.findViewById(R.id.tvNombreUsuario);
            tvCorreoUsuario = itemView.findViewById(R.id.tvCorreoUsuario);
            tvRolActual = itemView.findViewById(R.id.tvRolActual);
            spRolUsuario = itemView.findViewById(R.id.spRolUsuario);
            btnGuardarRol = itemView.findViewById(R.id.btnGuardarRol);
        }
    }
}
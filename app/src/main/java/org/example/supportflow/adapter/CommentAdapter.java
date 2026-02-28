package org.example.supportflow.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.example.supportflow.R;
import org.example.supportflow.model.Comment;

import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.VH> {

    private final List<Comment> data = new ArrayList<>();

    public void submit(List<Comment> newData) {
        data.clear();
        data.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Comment c = data.get(position);
        String author = (c.getAuthorName() != null && !c.getAuthorName().isEmpty()) ? c.getAuthorName() : "Usuario";
        h.tvAuthor.setText(author);
        h.tvMessage.setText(c.getMessage());
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvAuthor, tvMessage;
        VH(@NonNull View itemView) {
            super(itemView);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
    }
}
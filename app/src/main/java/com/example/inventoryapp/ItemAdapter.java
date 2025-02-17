package com.example.inventoryapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
    private ArrayList<String> itemsList;
    private DatabaseHelper dbHelper;
    private int selectedItemPosition = RecyclerView.NO_POSITION;

    public ItemAdapter(ArrayList<String> itemsList, DatabaseHelper dbHelper) {
        this.itemsList = itemsList;
        this.dbHelper = dbHelper;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        String item = itemsList.get(position);
        holder.textViewItem.setText(item);

        holder.itemView.setOnClickListener(v -> {
            selectedItemPosition = holder.getBindingAdapterPosition();
            notifyDataSetChanged();
        });

        holder.itemView.setBackgroundColor(selectedItemPosition == position ?
                ContextCompat.getColor(holder.itemView.getContext(), R.color.selectedItemColor) :
                ContextCompat.getColor(holder.itemView.getContext(), android.R.color.transparent));
    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }

    public int getSelectedItemPosition() {
        return selectedItemPosition;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView textViewItem;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewItem = itemView.findViewById(R.id.textViewItem);
        }
    }
}
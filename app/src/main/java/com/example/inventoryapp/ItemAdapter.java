package com.example.inventoryapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<String> itemsList;
    private Context context;
    private int selectedPosition = -1; // موقع العنصر المختار

    public ItemAdapter(Context context, List<String> itemsList) {
        this.context = context;
        this.itemsList = itemsList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        String item = itemsList.get(position);
        String[] parts = item.split(" - ");

        if (parts.length >= 3) { // تأكد من وجود ID واسم الصنف والعدد
            holder.className.setText(parts[1]); // اسم الصنف
            holder.quantity.setText(parts[2]); // العدد
        }

        // تحديد العنصر المختار
        holder.itemView.setActivated(position == selectedPosition);

        // عند النقر على العنصر
        holder.itemView.setOnClickListener(v -> {
            selectedPosition = position; // تحديث الموقع المختار
            notifyDataSetChanged(); // تحديث القائمة
        });
    }

    @Override
    public int getItemCount() {
        return itemsList.size(); // عدد العناصر في القائمة
    }

    // الحصول على موقع العنصر المختار
    public int getSelectedItemPosition() {
        return selectedPosition;
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView className, quantity;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            className = itemView.findViewById(R.id.textViewItemName); // اسم الصنف
            quantity = itemView.findViewById(R.id.textViewItemQuantity); // العدد
        }
    }
}
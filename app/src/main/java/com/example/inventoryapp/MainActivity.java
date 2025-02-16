package com.example.inventoryapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText editTextClassName, editTextQuantity;
    private RecyclerView recyclerViewItems; // استخدم RecyclerView
    private ArrayList<String> itemsList;
    private ItemAdapter adapter; // Adapter الخاص بـ RecyclerView
    private DatabaseHelper dbHelper;
    private Button buttonSave, buttonClearAll, buttonUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Views
        editTextClassName = findViewById(R.id.editTextClassName);
        editTextQuantity = findViewById(R.id.editTextQuantity);
        buttonSave = findViewById(R.id.buttonSave);
        buttonClearAll = findViewById(R.id.buttonClearAll);
        buttonUpdate = findViewById(R.id.buttonUpdate);
        recyclerViewItems = findViewById(R.id.recyclerViewItems); // RecyclerView

        // Initialize List and Adapter
        itemsList = new ArrayList<>();
        adapter = new ItemAdapter(this, itemsList);
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this)); // ضبط الاتجاه العمودي
        recyclerViewItems.setAdapter(adapter); // ربط adapter بالـ RecyclerView

        // Initialize Database Helper
        dbHelper = new DatabaseHelper(this);

        // Load saved data from database
        loadItemsFromDatabase(); // هذا الكود يحمل البيانات المخزنة

        // Save Button Listener
        buttonSave.setOnClickListener(v -> saveItemToDatabase());

        // Update Button Listener
        buttonUpdate.setOnClickListener(v -> {
            int selectedItemPosition = adapter.getSelectedItemPosition(); // الحصول على الموقع المحدد

            if (selectedItemPosition != -1) { // تأكد من اختيار العنصر
                String selectedItem = itemsList.get(selectedItemPosition);
                String[] parts = selectedItem.split(" - ");

                if (parts.length >= 3) { // تأكد من وجود ID واسم الصنف والعدد
                    int itemId = Integer.parseInt(parts[0]);
                    String currentClassName = parts[1];
                    int currentQuantity = Integer.parseInt(parts[2]);

                    // إنشاء حوار تعديل
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    View dialogView = getLayoutInflater().inflate(R.layout.update_dialog, null);

                    EditText editClassName = dialogView.findViewById(R.id.editTextUpdateClassName);
                    EditText editQuantity = dialogView.findViewById(R.id.editTextUpdateQuantity);
                    Button btnUpdate = dialogView.findViewById(R.id.buttonUpdate);

                    // تعيين القيم الحالية
                    editClassName.setText(currentClassName);
                    editQuantity.setText(String.valueOf(currentQuantity));

                    builder.setView(dialogView);
                    AlertDialog dialog = builder.create();

                    btnUpdate.setOnClickListener(innerV -> {
                        String newClassName = editClassName.getText().toString().trim();
                        String newQuantityStr = editQuantity.getText().toString().trim();

                        if (!newClassName.isEmpty() && !newQuantityStr.isEmpty()) {
                            try {
                                int newQuantity = Integer.parseInt(newQuantityStr);
                                updateItem(newClassName, newQuantityStr, itemId); // تحديث السجل
                                dialog.dismiss(); // إغلاق الحوار بعد التعديل
                            } catch (NumberFormatException e) {
                                Toast.makeText(MainActivity.this, "يرجى إدخال عدد صحيح", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "يرجى إدخال جميع البيانات", Toast.LENGTH_SHORT).show();
                        }
                    });

                    dialog.show();
                } else {
                    Toast.makeText(this, "خطأ في البيانات المخزنة", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "اختر عنصرًا لتعدíله", Toast.LENGTH_SHORT).show();
            }
        });

        // Clear All Button Listener
        buttonClearAll.setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("تأكيد المسح")
                    .setMessage("هل أنت متأكد من أنك تريد مسح جميع الصنوف؟")
                    .setPositiveButton("نعم", (dialog, which) -> {
                        dbHelper.clearAllItems();
                        itemsList.clear();
                        adapter.notifyDataSetChanged();
                        Toast.makeText(MainActivity.this, "تم مسح جميع الصنوف", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("لا", null)
                    .show();
        });
    }

    /**
     * Method to Save Item to Database
     */
    private void saveItemToDatabase() {
        String className = editTextClassName.getText().toString().trim();
        String quantityStr = editTextQuantity.getText().toString().trim();

        if (!className.isEmpty() && !quantityStr.isEmpty()) {
            try {
                int quantity = Integer.parseInt(quantityStr);
                boolean isInserted = dbHelper.insertItem(className, quantityStr);

                if (isInserted) {
                    Toast.makeText(this, "تم الحفظ بنجاح", Toast.LENGTH_SHORT).show();
                    loadItemsFromDatabase(); // Reload data after insertion
                } else {
                    Toast.makeText(this, "فشل الحفظ", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "يرجى إدخال عدد صحيح", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "يرجى إدخال جميع البيانات", Toast.LENGTH_SHORT).show();
        }

        // Clear input fields
        editTextClassName.setText("");
        editTextQuantity.setText("");
    }

    /**
     * Method to Load Items from Database
     */
    private void loadItemsFromDatabase() {
        Cursor cursor = dbHelper.getAllItems();
        itemsList.clear();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                    String className = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLASS_NAME));
                    int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_QUANTITY));

                    // إضافة عنصر بصيغة "ID - ClassName - Quantity"
                    itemsList.add(id + " - " + className + " - " + quantity);
                } catch (IllegalArgumentException e) {
                    System.err.println("Error: Missing column in database");
                }
            } while (cursor.moveToNext());

            cursor.close(); // دائمًا إغلاق المؤشر بعد الاستخدام
        }

        adapter.notifyDataSetChanged(); // تحديث RecyclerView
    }

    /**
     * Method to Update an Item in the Database
     */
    private void updateItem(String className, String quantity, int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CLASS_NAME, className);
        values.put(DatabaseHelper.COLUMN_QUANTITY, Integer.parseInt(quantity));

        long result = db.update(DatabaseHelper.TABLE_ITEMS, values, DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        if (result != -1) {
            Toast.makeText(this, "تم التعديل بنجاح", Toast.LENGTH_SHORT).show();
            loadItemsFromDatabase(); // Reload data after update
        } else {
            Toast.makeText(this, "فشل التعديل", Toast.LENGTH_SHORT).show();
        }
    }
}
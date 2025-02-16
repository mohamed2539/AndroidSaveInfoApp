package com.example.inventoryapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText editTextClassName, editTextQuantity;
    private Button buttonSave, buttonClearAll, buttonUpdate;
    private ListView listViewItems;
    private ArrayList<String> itemsList;
    private ArrayAdapter<String> adapter;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // تهيئة العناصر
        editTextClassName = findViewById(R.id.editTextClassName);
        editTextQuantity = findViewById(R.id.editTextQuantity);
        buttonSave = findViewById(R.id.buttonSave);
        buttonClearAll = findViewById(R.id.buttonClearAll);
        buttonUpdate = findViewById(R.id.buttonUpdate);
        listViewItems = findViewById(R.id.listViewItems);

        // تهيئة القائمة و Adapter
        itemsList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itemsList);
        listViewItems.setAdapter(adapter);

        // تهيئة قاعدة البيانات
        dbHelper = new DatabaseHelper(this);

        // تحميل البيانات عند بدء التشغيل
        loadItemsFromDatabase();

        // Save Button Listener
        buttonSave.setOnClickListener(v -> saveItemToDatabase());

        // Update Button Listener
        buttonUpdate.setOnClickListener(v -> {
            int selectedItemPosition = listViewItems.getCheckedItemPosition();

            if (selectedItemPosition != ListView.INVALID_POSITION) { // تأكد من اختيار العنصر
                String selectedItem = itemsList.get(selectedItemPosition);
                String[] parts = selectedItem.split(" - ");

                if (parts.length >= 3) { // تأكد من وجود ID واسم الصنف والعدد
                    int itemId = Integer.parseInt(parts[0]);
                    String currentClassName = parts[1];
                    int currentQuantity = Integer.parseInt(parts[2]);

                    // إنشاء حوار التعديل
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    View dialogView = getLayoutInflater().inflate(R.layout.update_dialog, null);

                    EditText editClassName = dialogView.findViewById(R.id.editTextUpdateClassName);
                    EditText editQuantity = dialogView.findViewById(R.id.editTextUpdateQuantity);
                    Button btnUpdateDialog = dialogView.findViewById(R.id.buttonUpdateDialog);

                    // تعيين القيم الحالية
                    editClassName.setText(currentClassName);
                    editQuantity.setText(String.valueOf(currentQuantity));

                    builder.setView(dialogView);
                    AlertDialog dialog = builder.create();

                    btnUpdateDialog.setOnClickListener(innerV -> {
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
                Toast.makeText(this, "اختر عنصرًا لتعديله", Toast.LENGTH_SHORT).show();
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
                        adapter.notifyDataSetChanged(); // تحديث ListView
                        Toast.makeText(MainActivity.this, "تم مسح جميع الصنوف", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("لا", null)
                    .show();
        });
    }

    /**
     * دالة لحفظ العنصر في قاعدة البيانات
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
                    loadItemsFromDatabase(); // إعادة تحميل البيانات
                } else {
                    Toast.makeText(this, "فشل الحفظ", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "يرجى إدخال عدد صحيح", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "يرجى إدخال جميع البيانات", Toast.LENGTH_SHORT).show();
        }

        // مسح الحقول بعد الحفظ
        editTextClassName.setText("");
        editTextQuantity.setText("");
    }

    /**
     * دالة لتحميل العناصر من قاعدة البيانات
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

            cursor.close(); // إغلاق المؤشر بعد الاستخدام
        }

        adapter.notifyDataSetChanged(); // تحديث ListView
    }

    /**
     * دالة لتحديث العنصر في قاعدة البيانات
     */
    private void updateItem(String className, String quantity, int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CLASS_NAME, className);
        values.put(DatabaseHelper.COLUMN_QUANTITY, Integer.parseInt(quantity));

        long result = db.update(DatabaseHelper.TABLE_ITEMS, values, DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        if (result != 0) {
            Toast.makeText(this, "تم التعديل بنجاح", Toast.LENGTH_SHORT).show();
            loadItemsFromDatabase(); // إعادة تحميل البيانات
        } else {
            Toast.makeText(this, "فشل التعديل", Toast.LENGTH_SHORT).show();
        }
    }
}
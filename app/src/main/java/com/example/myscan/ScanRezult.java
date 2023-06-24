package com.example.myscan;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.myscan.Model.Modul;
import com.example.myscan.Model.Warehouse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ScanRezult extends AppCompatActivity {
    private static final int SCAN_REQUEST_CODE = 1;
    private static final int PICK_IMAGE_REQUEST = 2;

    ImageView deviceImageView;
    EditText deviceNameEditText;
    EditText deviceSNEditText;
    EditText devicePNEditText;
    EditText deviceDataEditText;

    private DatabaseReference warehousesRef;
    private Spinner warehouseSpinner;
    private List<Warehouse> warehouseList;
    private ArrayAdapter<Warehouse> warehouseAdapter;
    ImageView back_btn;

    DatabaseReference modulesRef;

    Uri imageUri; // Сохраняем URI выбранной пользователем картинки
    private String warehouses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_rezult);

        deviceImageView = findViewById(R.id.device_image);
        deviceNameEditText = findViewById(R.id.device_name);
        deviceSNEditText = findViewById(R.id.device_sn);
        devicePNEditText = findViewById(R.id.device_pn);
        deviceDataEditText = findViewById(R.id.device_data);
        back_btn = findViewById(R.id.back_btn);

        warehousesRef = FirebaseDatabase.getInstance().getReference("warehouses");
        warehouseList = new ArrayList<>();
        warehouseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, warehouseList);
        warehouseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        warehouseSpinner = findViewById(R.id.device_sklad);
        warehouseSpinner.setAdapter(warehouseAdapter);

        // Получение данных о складах из базы данных Firebase
        loadWarehouses();

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent backIntent = new Intent(ScanRezult.this, KnopkaScan.class);
                startActivity(backIntent);
            }
        });

        modulesRef = FirebaseDatabase.getInstance().getReference("moduli");

        Intent intent = getIntent();
        String scannedCode = intent.getStringExtra("scan_result");

        deviceSNEditText.setText(scannedCode);

        checkModuleExistence(scannedCode);

        Button addModuleButton = findViewById(R.id.add_module_button);
        addModuleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveModuleData(); // Вызываем метод сохранения данных модуля
            }
        });

        deviceImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickerDialog();
            }
        });
    }

    private void loadWarehouses() {
        warehouseList.clear();
        warehouseList.add(Warehouse.PLACEHOLDER);
        warehouseList.addAll(Warehouse.getSampleWarehouses());

        warehouseAdapter.notifyDataSetChanged();

        warehousesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot warehouseSnapshot : snapshot.getChildren()) {
                    Warehouse warehouse = warehouseSnapshot.getValue(Warehouse.class);
                    warehouseList.add(warehouse);
                }

                warehouseAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ScanRezult.this, "Ошибка при чтении данных о складах", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void checkModuleExistence(String serialNumber) {
        Query query = modulesRef.orderByChild("serialNumber").equalTo(serialNumber);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(ScanRezult.this, "Модуль уже существует в базе данных", Toast.LENGTH_SHORT).show();

                    for (DataSnapshot moduleSnapshot : snapshot.getChildren()) {
                        Modul module = moduleSnapshot.getValue(Modul.class);

                        deviceNameEditText.setText(module.getModel());
                        deviceSNEditText.setText(module.getSerialNumber());
                        devicePNEditText.setText(module.getPartNumber());
                        deviceDataEditText.setText(module.getManufactureDate());

                        String imageUri = module.getImageUri();
                        if (imageUri != null) {
                            loadAndDisplayImage(imageUri);
                        } else {
                            // TODO: Загрузка и отображение изображения
                        }

                        break;
                    }
                } else {
                    Toast.makeText(ScanRezult.this, "Модуль не найден в базе данных", Toast.LENGTH_SHORT).show();
                }
            }

            private void loadAndDisplayImage(String imageUri) {
                RequestOptions requestOptions = new RequestOptions()
                        .placeholder(R.drawable.placeholder_image) // Показываем заглушку во время загрузки
                        .error(R.drawable.errors); // Показываем ошибку, если загрузка не удалась

                Glide.with(ScanRezult.this)
                        .load(imageUri)
                        .apply(requestOptions)
                        .into(deviceImageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ScanRezult.this, "Ошибка при чтении данных из базы данных", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showImagePickerDialog() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                imageUri = data.getData(); // Сохраняем выбранный пользователем URI изображения

                // Отображаем выбранное изображение в ImageView
                deviceImageView.setImageURI(imageUri);
            }
        }
    }

    private void saveModuleData() {
        if (imageUri != null) {
            // Создание ссылки на Firebase Storage
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            // Создание имени файла в Storage (используется уникальный идентификатор)
            String filename = UUID.randomUUID().toString();

            // Создание ссылки на место сохранения файла
            StorageReference imageRef = storageRef.child("images/" + filename);

            // Загрузка изображения в Firebase Storage
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Получение URL загруженного изображения
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();

                            // Сохранение модуля с URL изображения в базе данных
                            saveModuleDataWithImage(imageUrl);
                        });
                    })
                    .addOnFailureListener(e -> {
                        // Обработка ошибки при загрузке изображения
                        Toast.makeText(ScanRezult.this, "Ошибка при загрузке изображения", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Если пользователь не выбрал изображение, сохраняем модуль без изображения
            saveModuleDataWithImage(null);
        }
    }

    private void saveModuleDataWithImage(String imageUrl) {
        String model = deviceNameEditText.getText().toString().trim();
        String serialNumber = deviceSNEditText.getText().toString().trim();
        String partNumber = devicePNEditText.getText().toString().trim();
        String manufactureDate = deviceDataEditText.getText().toString().trim();
        String warehouseName = warehouseSpinner.getSelectedItem().toString();

        if (warehouseName.equals("Выберите склад")) {
            Toast.makeText(this, "Пожалуйста, выберите склад", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(model)) {
            Toast.makeText(this, "Введите модель модуля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(serialNumber)) {
            Toast.makeText(this, "Введите серийный номер модуля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(partNumber)) {
            Toast.makeText(this, "Введите номер детали модуля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(manufactureDate)) {
            Toast.makeText(this, "Введите дату производства модуля", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String moduleId = modulesRef.push().getKey();
        String warehouseId = warehousesRef.push().getKey();
        Warehouse warehouse = new Warehouse(warehouseId, warehouseName);

        warehousesRef.child(warehouseId).setValue(warehouse)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ScanRezult.this, "Склад успешно добавлен", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ScanRezult.this, "Ошибка при добавлении склада", Toast.LENGTH_SHORT).show();
                    }
                });

        Modul module = new Modul(moduleId, model, serialNumber, partNumber, manufactureDate, imageUrl, warehouses);

        if (moduleId != null) {
            modulesRef.child(moduleId).setValue(module)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            modulesRef.child(moduleId).child("warehouses").setValue(warehouse)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(ScanRezult.this, "Модуль успешно добавлен", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(ScanRezult.this, "Ошибка при добавлении склада в модуль", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ScanRezult.this, "Ошибка при добавлении модуля", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
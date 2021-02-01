package com.example.realmexample;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.realmexample.adapter.OnClickUserItemListener;
import com.example.realmexample.adapter.UserAdapter;
import com.example.realmexample.db.DbService;
import com.example.realmexample.model.UserModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, OnClickUserItemListener {

    private RecyclerView recyclerView;
    private ImageView img;
    private TextView nameEt;
    private TextView phoneEt;
    private Button deleteBtn;

    private UserAdapter adapter;

    private List<UserModel> models = new ArrayList<>();

    private BottomSheetDialog dialog;

    private final int CAMERA_REQUEST_CODE = 100;
    private final int STORAGE_REQUEST_CODE = 200;
    private final int CAMERA_PICK_CODE = 300;
    private final int GALLERY_PICK_CODE = 400;

    private final String[] cameraPermissions = {Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private final String[] storagePermission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private Uri imageUri = null;

    private byte[] bytesImg = null;

    private int id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler);
        adapter = new UserAdapter(this);
        dialog = new BottomSheetDialog(this);

        initRecyclerView();
        showUsers();
        createBottomSheetDialog();

        dialog.setOnCancelListener(dialog -> {
            closeDialog();
        });
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.add: {
                this.id = 0;
                deleteBtn.setVisibility(View.GONE);
                dialog.show();
            } break;
            case R.id.delete: {
                DbService.deleteAll();
                closeDialog();
                showUsers();
            } break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createBottomSheetDialog() {
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.add_update_layout, null, false);

        img = view.findViewById(R.id.img);
        nameEt = view.findViewById(R.id.nameEt);
        phoneEt = view.findViewById(R.id.phoneEt);
        Button doneBtn = view.findViewById(R.id.doneBtn);
        deleteBtn = view.findViewById(R.id.deleteBtn);

        phoneEt.addTextChangedListener(new CurrencyTextWatcher());

        dialog.setContentView(view);

        doneBtn.setOnClickListener(this);
        img.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.doneBtn: insert(); break;
            case R.id.img: showChooser(); break;
            case R.id.deleteBtn: deleteUser(); break;
        }
    }

    private void deleteUser() {
        DbService.delete(id);
        closeDialog();
        showUsers();
    }

    private void insert() {
        String name = nameEt.getText().toString().trim();
        String phone = phoneEt.getText().toString().trim();

        if (name.equals("")) {
            nameEt.setError("Name is empty");
            nameEt.requestFocus();
            return;
        }
        if (phone.equals("")) {
            nameEt.setError("Phone is empty");
            nameEt.requestFocus();
            return;
        }

        UserModel model = new UserModel();

        getImageBytes();

        model.setName(name);
        model.setPhone(phone);
        model.setImage(bytesImg);

        if (id == 0) {
            DbService.insert(model);
        }
        else {
            DbService.update(id, name, phone, bytesImg);
            deleteBtn.setVisibility(View.GONE);
        }
        closeDialog();
        showUsers();
    }

    private void showUsers() {
        models = DbService.getAll();
        adapter.setUsers(models);
    }

    private void showChooser() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Options");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: {
                    if (checkCameraPermission()) {
                        openCamera();
                    }
                    else {
                        requestCameraPermissions();
                    }
                } break;
                case 1: {
                    if (checkStoragePermission()) {
                        openGallery();
                    }
                    else {
                        requestStoragePermissions();
                    }
                }
            }
        });

        builder.create().show();
    }

    private void openCamera() {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "New Image");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "From camera");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAMERA_PICK_CODE);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_PICK_CODE);
    }

    private void getImageBytes() {
        if (imageUri != null) {
            Drawable drawable = img.getDrawable();
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 70, stream);
            bytesImg =  stream.toByteArray();
        }
    }

    private boolean checkCameraPermission() {
        boolean b1 = ContextCompat
                .checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;

        boolean b2 = ContextCompat
                .checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;

        return b1 && b2;
    }

    private void requestCameraPermissions() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermissions() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CAMERA_PICK_CODE: {
                img.setImageURI(imageUri);
            } break;
            case GALLERY_PICK_CODE: {
                assert data != null;
                imageUri = data.getData();
                img.setImageURI(imageUri);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0) {
            switch (requestCode) {
                case CAMERA_REQUEST_CODE: {
                    boolean b1 = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean b2 = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (b1 && b2) {
                        openCamera();
                    }
                } break;
                case STORAGE_REQUEST_CODE: {
                    boolean b1 = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (b1) {
                        openGallery();
                    }
                } break;
            }
        }
    }

    public void closeDialog() {
        id = 0;
        dialog.cancel();
        img.setImageResource(R.drawable.ic_baseline_add_a_photo_24);
        nameEt.setText("");
        phoneEt.setText("");
    }

    @Override
    public void onClickItem(int position) {
        deleteBtn.setVisibility(View.VISIBLE);
        deleteBtn.setOnClickListener(this);
        dialog.show();
        UserModel model = models.get(position);

        id = model.getId();

        bytesImg = model.getImage();

        if (bytesImg != null) {
            Bitmap userImg = BitmapFactory.decodeByteArray(bytesImg, 0, bytesImg.length);
            img.setImageBitmap(userImg);
        }
        else {
            img.setImageResource(R.drawable.ic_baseline_add_a_photo_24);
        }

        nameEt.setText(model.getName());
        phoneEt.setText(model.getPhone());
    }

    private static class CurrencyTextWatcher implements TextWatcher {

        StringBuilder sb = new StringBuilder();
        boolean ignore;

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (!ignore) {
                removeFormat(editable.toString());
                applyFormat(sb.toString());
                ignore = true;
                editable.replace(0, editable.length(), sb.toString());
                ignore = false;
            }
        }

        private void removeFormat(String text) {
            sb.setLength(0);
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (isNumberChar(c)) {
                    sb.append(c);
                }
            }
        }

        private void applyFormat(String text) {
            String template = getTemplate(text);
            sb.setLength(0);
            for (int i = 0, textIndex = 0; i < template.length() && textIndex < text.length(); i++) {
                char numPlace = 'X';
                if (template.charAt(i) == numPlace) {
                    sb.append(text.charAt(textIndex));
                    textIndex++;
                } else {
                    sb.append(template.charAt(i));
                }
            }
        }

        private boolean isNumberChar(char c) {
            return c >= '0' && c <= '9';
        }

        private String getTemplate(String text) {
            if (text.startsWith("998")) {
                return "+XXX (XX) XXX-XX-XX";
            }
            if (text.startsWith("7")) {
                return "+X (XXX) XXX-XX-XX";
            }
            return  "+XXX (XXX) XX-XX-XX";
        }
    }
}
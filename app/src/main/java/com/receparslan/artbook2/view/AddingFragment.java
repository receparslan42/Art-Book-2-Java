package com.receparslan.artbook2.view;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;
import com.receparslan.artbook2.R;
import com.receparslan.artbook2.databinding.FragmentAddingBinding;
import com.receparslan.artbook2.model.Art;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;


public class AddingFragment extends Fragment {

    FragmentAddingBinding binding; // View binding

    // Smaller image for save the database
    private Bitmap smallerImage;

    private Art art; // New art

    // Activity result launcher for gallery
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            Intent intentFromResult = result.getData();
            if (intentFromResult != null) {
                Uri imageUri = intentFromResult.getData();
                if (imageUri != null) {
                    try {
                        // Check the sdk version for ImageDecoder
                        if (Build.VERSION.SDK_INT >= 28) {
                            // Return the image uri to the image bitmap with decoder
                            ImageDecoder.Source source = ImageDecoder.createSource(binding.getRoot().getContext().getContentResolver(), imageUri);
                            art.setImage(ImageDecoder.decodeBitmap(source));
                            binding.artImageView.setImageBitmap(art.getImage());
                        } else {
                            // Return the image uri to the image bitmap with old way
                            ContentResolver contentResolver = binding.getRoot().getContext().getContentResolver();
                            try (InputStream inputStream = contentResolver.openInputStream(imageUri)) {
                                art.setImage(BitmapFactory.decodeStream(inputStream));
                                binding.artImageView.setImageBitmap(art.getImage());
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
    });
    // Request permission launcher
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        // Check the permission
        if (isGranted) {
            // Permission granted and open the gallery
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intentToGallery);
        } else {
            // Permission denied and show a toast message
            Toast.makeText(binding.getRoot().getContext(), "Permission needed to access the gallery", Toast.LENGTH_SHORT).show();
        }
    });

    public AddingFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_adding, container, false);

        binding = FragmentAddingBinding.bind(viewGroup); // Initialize view binding

        return viewGroup;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String key;
        int artID;

        art = new Art(); // Initialize the new art

        if (getArguments() != null) {
            key = AddingFragmentArgs.fromBundle(getArguments()).getKey();
            artID = AddingFragmentArgs.fromBundle(getArguments()).getArtID();
        } else {
            key = "add";
            artID = -1;
        }

        // Set the art details when the edit button is clicked
        if (key.equals("edit")) {
            try (SQLiteDatabase database = view.getContext().openOrCreateDatabase("Arts", MODE_PRIVATE, null);
                 Cursor cursor = database.rawQuery("SELECT * FROM arts WHERE id = ? ", new String[]{String.valueOf(artID)})) {

                // Get the indexes
                int artNameIdx = cursor.getColumnIndex("name");
                int artistNameIdx = cursor.getColumnIndex("artist");
                int dateIdx = cursor.getColumnIndex("date");
                int imageIdx = cursor.getColumnIndex("image");

                // Set the art details
                if (cursor.moveToNext()) {
                    art.setId(artID);
                    art.setName(cursor.getString(artNameIdx));
                    art.setArtistName(cursor.getString(artistNameIdx));
                    art.setDate(cursor.getString(dateIdx));
                    art.setImage(BitmapFactory.decodeByteArray(cursor.getBlob(imageIdx), 0, cursor.getBlob(imageIdx).length));

                    binding.artNameEditText.setText(art.getName());
                    binding.artistEditText.setText(art.getArtistName());
                    binding.artTimeEditText.setText(art.getDate());
                    binding.artImageView.setImageBitmap(art.getImage());
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        // Open the gallery when the art image is clicked and the permission is granted
        binding.artImageView.setOnClickListener(v -> {
            // Check the sdk version for permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermission(v, Manifest.permission.READ_MEDIA_IMAGES);
            } else {
                requestPermission(v, Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        });

        final String finalKey = key; // Final key for lambda expression

        // Save the art to the database and go back to the home page when the save button is clicked
        binding.saveButton.setOnClickListener(v -> {
            // Set the art properties from the inputs
            art.setName(binding.artNameEditText.getText().toString());
            art.setArtistName(binding.artistEditText.getText().toString());
            art.setDate(binding.artTimeEditText.getText().toString());

            // Check the image is selected
            if (art.getImage() == null) {
                binding.artImageView.callOnClick();
            } else {
                // Make the selected image smaller and compress it
                ByteArrayOutputStream compressedImage = new ByteArrayOutputStream();
                smallerImage = makeSmallerImage(art.getImage(), 300);
                smallerImage.compress(Bitmap.CompressFormat.PNG, 50, compressedImage);

                String query; // Query for database

                // Set the query for the database
                if (finalKey.equals("edit"))
                    query = "UPDATE  arts SET name = ?, artist = ?, date = ?, image = ? WHERE id = ?";
                else
                    query = "INSERT INTO arts (name, artist, date, image) VALUES (?,?,?,?)";

                // Save the art to the database
                try (SQLiteDatabase database = view.getContext().openOrCreateDatabase("Arts", MODE_PRIVATE, null)) {
                    SQLiteStatement sqLiteStatement = database.compileStatement(query);
                    sqLiteStatement.bindString(1, art.getName());
                    sqLiteStatement.bindString(2, art.getArtistName());
                    sqLiteStatement.bindString(3, art.getDate());
                    sqLiteStatement.bindBlob(4, compressedImage.toByteArray());
                    if (finalKey.equals("edit"))
                        sqLiteStatement.bindLong(5, art.getId());
                    sqLiteStatement.execute();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

                // Go back to the home page
                NavDirections action = AddingFragmentDirections.actionAddingFragmentToShowFragment();
                Navigation.findNavController(v).navigate(action);
            }
        });
    }

    // Request permission
    public void requestPermission(View view, String permission) {
        // Check the permission
        if (ContextCompat.checkSelfPermission(view.getContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            // Permission granted
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intentToGallery);
        } else if (getActivity() != null && ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission)) {
            // Permission denied
            Snackbar.make(view, "Permission needed to access the gallery", Snackbar.LENGTH_INDEFINITE).setAction("Allow", v -> requestPermissionLauncher.launch(permission)).show();
        } else {
            // Request permission
            requestPermissionLauncher.launch(permission);
        }
    }

    // Make the image smaller
    public Bitmap makeSmallerImage(@NonNull Bitmap image, int maximumSize) {
        int width = image.getWidth(); // Image width
        int height = image.getHeight(); // Image height
        float bitmapRatio = (float) width / (float) height; // Image ratio

        // Check the image orientation
        if (bitmapRatio > 1) {
            //Landscape Image
            width = maximumSize;
            height = (int) (width / bitmapRatio);
        } else {
            //Portrait Image
            height = maximumSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
}
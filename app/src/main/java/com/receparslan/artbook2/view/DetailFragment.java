package com.receparslan.artbook2.view;

import static android.content.Context.MODE_PRIVATE;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.receparslan.artbook2.R;
import com.receparslan.artbook2.databinding.FragmentDetailBinding;
import com.receparslan.artbook2.model.Art;

public class DetailFragment extends Fragment {

    FragmentDetailBinding binding; // View binding

    Art art; // Selected art

    public DetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_detail, container, false);

        binding = FragmentDetailBinding.bind(viewGroup); // Initialize view binding

        return viewGroup;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the selected art
        art = new Art();
        if (getArguments() != null)
            art.setId(DetailFragmentArgs.fromBundle(getArguments()).getArtID());

        // Get the art details
        try (SQLiteDatabase database = view.getContext().openOrCreateDatabase("Arts", MODE_PRIVATE, null)) {
            try (Cursor cursor = database.rawQuery("SELECT * FROM arts WHERE id = ? ", new String[]{String.valueOf(art.getId())})) {
                // Get the indexes
                int artNameIdx = cursor.getColumnIndex("name");
                int artistNameIdx = cursor.getColumnIndex("artist");
                int dateIdx = cursor.getColumnIndex("date");
                int imageIdx = cursor.getColumnIndex("image");

                // Set the art details
                if (cursor.moveToNext()) {
                    art.setName(cursor.getString(artNameIdx));
                    art.setArtistName(cursor.getString(artistNameIdx));
                    art.setDate(cursor.getString(dateIdx));
                    art.setImage(BitmapFactory.decodeByteArray(cursor.getBlob(imageIdx), 0, cursor.getBlob(imageIdx).length));
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // Set the art details
        binding.artImageView.setImageBitmap(art.getImage());
        binding.artNameTextView.setText(art.getName());
        binding.artistTextView.setText(art.getArtistName());
        binding.artTimeTextView.setText(art.getDate());

        // Edit the selected art
        binding.editButton.setOnClickListener(v -> {
            DetailFragmentDirections.ActionDetailFragmentToAddingFragment action = DetailFragmentDirections.actionDetailFragmentToAddingFragment("edit");
            action.setArtID(art.getId());
            Navigation.findNavController(v).navigate(action);
        });

        // Delete the selected art
        binding.deleteButton.setOnClickListener(v -> {
            AlertDialog.Builder confirmDialog = new AlertDialog.Builder(view.getContext());
            confirmDialog.setMessage("Are you sure want to delete?");
            confirmDialog.setPositiveButton("Yes", (dialogInterface, i) -> {
                String query = "DELETE FROM arts WHERE id = ?";

                // Delete the art
                try (SQLiteDatabase database = view.getContext().openOrCreateDatabase("Arts", MODE_PRIVATE, null);
                     SQLiteStatement statement = database.compileStatement(query)) {
                    statement.bindLong(1, art.getId());
                    statement.execute();
                }

                // Go to the home page
                NavDirections action = DetailFragmentDirections.actionDetailFragmentToShowFragment();
                Navigation.findNavController(v).navigate(action);
            });
            confirmDialog.setNegativeButton("No", null);
            confirmDialog.show();
        });
    }
}
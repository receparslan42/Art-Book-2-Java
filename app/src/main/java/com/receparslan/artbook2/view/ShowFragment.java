package com.receparslan.artbook2.view;

import static android.content.Context.MODE_PRIVATE;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.receparslan.artbook2.R;
import com.receparslan.artbook2.adapter.RecyclerAdapter;
import com.receparslan.artbook2.databinding.FragmentShowBinding;
import com.receparslan.artbook2.model.Art;

import java.util.ArrayList;

public class ShowFragment extends Fragment {

    FragmentShowBinding binding; // View binding

    RecyclerView recyclerView; // Recycler view

    ArrayList<Art> artList; // List of arts

    public ShowFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_show, container, false);

        binding = FragmentShowBinding.bind(viewGroup); // Initialize view binding

        return viewGroup;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the arts from the database
        try (SQLiteDatabase database = view.getContext().openOrCreateDatabase("Arts", MODE_PRIVATE, null)) {
            database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, name VARCHAR, artist VARCHAR, date VARCHAR, image BLOB)");

            try (Cursor cursor = database.rawQuery("SELECT * FROM arts", null)) {
                // Get the indexes
                int artIDIdx = cursor.getColumnIndex("id");
                int artNameIdx = cursor.getColumnIndex("name");
                int artistNameIdx = cursor.getColumnIndex("artist");
                int dateIdx = cursor.getColumnIndex("date");
                int imageIdx = cursor.getColumnIndex("image");

                // Initialize the list of arts
                artList = new ArrayList<>();
                while (cursor.moveToNext()) {
                    Art art = new Art();
                    art.setId(cursor.getInt(artIDIdx));
                    art.setName(cursor.getString(artNameIdx));
                    art.setArtistName(cursor.getString(artistNameIdx));
                    art.setDate(cursor.getString(dateIdx));
                    art.setImage(BitmapFactory.decodeByteArray(cursor.getBlob(imageIdx), 0, cursor.getBlob(imageIdx).length));
                    artList.add(art);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // Set the recycler view
        recyclerView = binding.recyclerView;
        recyclerView.setAdapter(new RecyclerAdapter(artList));
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        binding.addFAB.setOnClickListener(v -> {
            NavDirections action = ShowFragmentDirections.actionShowFragmentToAddingFragment("add");
            Navigation.findNavController(v).navigate(action);
        });
    }
}
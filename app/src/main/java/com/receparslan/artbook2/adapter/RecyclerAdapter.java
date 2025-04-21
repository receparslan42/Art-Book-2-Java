package com.receparslan.artbook2.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.receparslan.artbook2.databinding.RecyclerRowBinding;
import com.receparslan.artbook2.model.Art;
import com.receparslan.artbook2.view.ShowFragmentDirections;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    final ArrayList<Art> artList;

    public RecyclerAdapter(ArrayList<Art> artList) {
        this.artList = artList;
    }

    @NonNull
    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapter.ViewHolder holder, int position) {
        holder.recyclerRowBinding.artIdTextView.setText(String.valueOf(position + 1));
        holder.recyclerRowBinding.artNameTextView.setText(artList.get(position).getName());
        holder.itemView.setOnClickListener(view -> {
            NavDirections action = ShowFragmentDirections.actionShowFragmentToDetailFragment(artList.get(position).getId());
            Navigation.findNavController(view).navigate(action);
        });
    }

    @Override
    public int getItemCount() {
        return artList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final RecyclerRowBinding recyclerRowBinding;

        public ViewHolder(@NonNull RecyclerRowBinding recyclerRowBinding) {
            super(recyclerRowBinding.getRoot());
            this.recyclerRowBinding = recyclerRowBinding;
        }
    }
}

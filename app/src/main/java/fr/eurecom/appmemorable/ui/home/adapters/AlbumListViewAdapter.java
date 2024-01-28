package fr.eurecom.appmemorable.ui.home.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import fr.eurecom.appmemorable.NodesActivity;
import fr.eurecom.appmemorable.R;
import fr.eurecom.appmemorable.databinding.AlbumItemBinding;
import fr.eurecom.appmemorable.models.Album;
import fr.eurecom.appmemorable.models.ConcreteAlbum;
import fr.eurecom.appmemorable.models.User;

public class AlbumListViewAdapter extends ArrayAdapter<Album> {
    private boolean mIsAllFabsVisible = false;
    private List<Album> mAlbums = new ArrayList<>();
    private List<Album> mFilteredAlbums = new ArrayList<>();
    private String titleFilter = "";
    private String dateFilter = "";
    private String userFilter = "";
    private boolean ownedFilter = false;

    public void setFavoriteFilter(boolean favoriteFilter) {
        this.favoriteFilter = favoriteFilter;
    }

    private boolean favoriteFilter = false;
    private boolean filterByTitle, filterByDate, filterByUser;
    private AlbumItemBinding albumItemBinding;

    public AlbumListViewAdapter(@NonNull Context context, List<Album> albums) {
        super(context, 0, albums);
        mAlbums.addAll(albums);
        mFilteredAlbums.addAll(albums);
    }

    @Override
    public int getCount() {
        return mFilteredAlbums.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Album album = mFilteredAlbums.get(position);

        albumItemBinding = AlbumItemBinding.inflate(LayoutInflater.from(getContext()));
        albumItemBinding.textViewAlbumTitle.setText(album.getTitle());
        albumItemBinding.albumDate.setText(album.getTimeOfCreation());
        if (album.isFavorite()) {
            albumItemBinding.albumFavorite.setImageResource(R.drawable.star_svgrepo_com_fill);
        } else {
            albumItemBinding.albumFavorite.setImageResource(R.drawable.star_svgrepo_com);
        }
        //If owned set owned icon visible
        if (album.getOwner().getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            albumItemBinding.ownedIcon.setVisibility(View.VISIBLE);
        } else {
            albumItemBinding.ownedIcon.setVisibility(View.INVISIBLE);
        }


        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("cover/" + album.getAlbumCoverUrl());
        if(storageRef != null) {

            Glide
                    .with(this.getContext())
                    .load(storageRef)
                    .centerCrop()
                    .into(albumItemBinding.imageViewAlbum);

        }



        //Handle the open album button to start a new NodesActivity
        initOpenAlbumButton(album);
        //Handle deleting the album
        initDeleteAlbumButton(album);
        //
        initFavoriteButton(album);
        convertView = albumItemBinding.getRoot();
        return convertView;
    }

    private void initFavoriteButton(Album album) {
        albumItemBinding.albumFavorite.setOnClickListener(v -> {
            album.setFavorite(!album.isFavorite());
            notifyDataSetChanged();
            //Upload in firebaseDatabase corresponding userAlbums (the idea is to have different values of favorites based on the user
            FirebaseDatabase.getInstance().getReference("userAlbums/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + album.getId()).setValue(new ConcreteAlbum(album));
            Log.e("favorite", "userAlbums/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + album.getId());
            Log.e("favorite", album.isFavorite() + "");
        });
    }

    private void initDeleteAlbumButton(Album album) {
        albumItemBinding.albumDelete.setOnClickListener(v -> {
            showDeleteConfirmationDialog(album);
        });
    }

    private void initOpenAlbumButton(Album album) {
        albumItemBinding.albumItem.setOnClickListener(v -> {
            String valueToPass = album.getId();

            // Create an Intent
            Intent intent = new Intent(this.getContext(), NodesActivity.class);

            // Put the value as an extra in the Intent
            intent.putExtra("albumKey", valueToPass);
            intent.putExtra("albumName", album.getTitle());
            intent.putExtra("albumOwner", album.getOwner().getUid());

            // Start the SecondActivity
            this.getContext().startActivity(intent);
        });
    }
    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Album> filteredAlbums = new ArrayList<>();
                for (Album album : mAlbums) {
                    if (album.getTitle().toLowerCase().contains(titleFilter.toString().toLowerCase()) &&
                            (!ownedFilter || album.getOwner().getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) &&
                            (!favoriteFilter || album.isFavorite())) {
                        filteredAlbums.add(album);
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filteredAlbums;
                results.count = filteredAlbums.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mFilteredAlbums.clear();
                mFilteredAlbums.addAll((List<Album>) results.values);
                notifyDataSetChanged();
            }
        };
    }

    private void showDeleteConfirmationDialog(Album albumToDelete) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete Album")
                .setMessage("Are you sure you want to delete this album?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // User confirmed deletion, proceed with deletion
                    deleteAlbum(albumToDelete);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // User canceled the deletion, do nothing
                })
                .show();
    }
    private void deleteAlbum(Album album) {
        //Delete from storage all elements relatives to the album.
        FirebaseStorage.getInstance().getReference().child("" + album.getId() + "/").listAll().addOnSuccessListener(listResult -> {
            for (StorageReference ref : listResult.getItems()) {
                Log.e("deleteAlbum", ref.toString());
                ref.delete();
            }
        });
        //Delete from albums
        FirebaseDatabase.getInstance().getReference("albums/" + album.getId()).removeValue();
        Log.e("deleteAlbum", "" + album.getUsers().size());
        //Delete from userAlbums
        for (User usr : album.getUsers()) {
            String path = "userAlbums/" + usr.getUid() + "/" + album.getId();
            Log.e("deleteUserAlbums", path);
            FirebaseDatabase.getInstance().getReference(path).removeValue();
        }
    }
    public List<Album> getmAlbums() {
        return mAlbums;
    }

    public void setmAlbums(List<Album> mAlbums) {
        this.mAlbums = mAlbums;
    }

    public String getTitleFilter() {
        return titleFilter;
    }

    public void setTitleFilter(String titleFilter) {
        this.titleFilter = titleFilter;
    }

    public void setOwnedFilter(boolean isChecked) {
        this.ownedFilter = isChecked;
    }

    public void sortAlbums(Comparator<Album> comparator) {
        mFilteredAlbums.sort(comparator);
        Log.e("albums", mFilteredAlbums.stream().map(Album::getTitle).collect(Collectors.toList()).toString());
        notifyDataSetChanged();
    }
}


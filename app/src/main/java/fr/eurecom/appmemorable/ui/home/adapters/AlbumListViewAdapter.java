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

import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import fr.eurecom.appmemorable.NodesActivity;
import fr.eurecom.appmemorable.R;
import fr.eurecom.appmemorable.databinding.AlbumItemBinding;
import fr.eurecom.appmemorable.models.Album;
import fr.eurecom.appmemorable.models.User;

public class AlbumListViewAdapter extends ArrayAdapter<Album> {
    private boolean mIsAllFabsVisible = false;
    private List<Album> mAlbums = new ArrayList<>();
    private List<Album> mFilteredAlbums = new ArrayList<>();
    private String titleFilter = "";
    private String dateFilter = "";
    private String userFilter = "";
    private boolean filterByTitle, filterByDate, filterByUser;
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

        AlbumItemBinding albumItemBinding = AlbumItemBinding.inflate(LayoutInflater.from(getContext()));
        albumItemBinding.textViewAlbumTitle.setText(album.getTitle());
        albumItemBinding.albumDate.setText(album.getTimeOfCreation());
        albumItemBinding.albumItem.setOnClickListener(v -> {
            String valueToPass = album.getId();

            // Create an Intent
            Intent intent = new Intent(this.getContext(), NodesActivity.class);

            // Put the value as an extra in the Intent
            intent.putExtra("albumKey", valueToPass);
            intent.putExtra("albumName", album.getTitle());

            // Start the SecondActivity
            this.getContext().startActivity(intent);
        });
        //Handle popup menu for editing/deleting the album
        ImageButton imageViewSettings = albumItemBinding.albumSettings;

        imageViewSettings.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), v);
            popupMenu.inflate(R.menu.album_settings_menu); // Create a menu resource file (res/menu/album_settings_menu.xml)

            // Handle menu item clicks
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_item_edit){
                    return true;
                }
                else if (item.getItemId() == R.id.menu_item_delete){
                    showDeleteConfirmationDialog(album);
                    return true;
                }
                return false;
            });

            // Show the popup menu
            popupMenu.show();
        });
        convertView = albumItemBinding.getRoot();
        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Album> filteredAlbums = new ArrayList<>();
                for(Album album : mAlbums){
                    if(album.getTitle().toLowerCase().contains(titleFilter.toString().toLowerCase())){
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
        //Delete from albums
        FirebaseDatabase.getInstance().getReference("albums/"+album.getId()).removeValue();
        Log.e("deleteAlbum", ""+album.getUsers().size());
        //Delete from userAlbums
        for(User usr : album.getUsers()){
            String path = "userAlbums/"+usr.getUid()+"/"+album.getId();
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
}


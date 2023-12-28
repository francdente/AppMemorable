package fr.eurecom.appmemorable.ui.home.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.PopupMenu;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import fr.eurecom.appmemorable.NodesActivity;
import fr.eurecom.appmemorable.R;
import fr.eurecom.appmemorable.databinding.AlbumItemBinding;
import fr.eurecom.appmemorable.databinding.ImageNodeBinding;
import fr.eurecom.appmemorable.databinding.TextNodeBinding;
import fr.eurecom.appmemorable.models.Album;
import fr.eurecom.appmemorable.models.ConcreteAlbum;
import fr.eurecom.appmemorable.models.ConcreteNode;
import fr.eurecom.appmemorable.models.ContentNode;
import fr.eurecom.appmemorable.models.ImageNode;
import fr.eurecom.appmemorable.models.TextNode;
import fr.eurecom.appmemorable.models.User;

public class AlbumListViewAdapter extends ArrayAdapter<Album> {
    private boolean mIsAllFabsVisible = false;
    public AlbumListViewAdapter(@NonNull Context context, List<Album> albums) {
        super(context, 0, albums);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Album album = getItem(position);
        AlbumItemBinding albumItemBinding = AlbumItemBinding.inflate(LayoutInflater.from(getContext()));
        albumItemBinding.textViewAlbumTitle.setText(album.getTitle());
        albumItemBinding.albumDate.setText(album.getTimeFromCreation(LocalDateTime.now()));
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

}


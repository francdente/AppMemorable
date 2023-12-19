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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
        albumItemBinding.textViewAlbumTitle.setOnClickListener(v -> {
            String valueToPass = album.getId();

            // Create an Intent
            Intent intent = new Intent(this.getContext(), NodesActivity.class);

            // Put the value as an extra in the Intent
            intent.putExtra("albumKey", valueToPass);
            intent.putExtra("albumName", album.getTitle());

            // Start the SecondActivity
            this.getContext().startActivity(intent);
        });
        convertView = albumItemBinding.getRoot();
        return convertView;
    }

    private void deleteNode(String albumId, String nodeId){
        DatabaseReference node = FirebaseDatabase.getInstance().getReference("albumNodes/"+albumId+"/"+nodeId);
        node.removeValue();
    }

}


package fr.eurecom.appmemorable.ui.home.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import fr.eurecom.appmemorable.databinding.FriendItemBinding;
import fr.eurecom.appmemorable.databinding.FriendRequestItemBinding;
import fr.eurecom.appmemorable.models.Album;
import fr.eurecom.appmemorable.models.ConcreteAlbum;
import fr.eurecom.appmemorable.models.User;

public class FriendListViewAdapter extends ArrayAdapter<User> {

    List<User> friends;
    FriendItemBinding friendItemBinding;

    public FriendListViewAdapter(@NonNull Context context, List<User> friends) {
        super(context, 0, friends);
        this.friends = friends;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        friendItemBinding = FriendItemBinding.inflate(LayoutInflater.from(getContext()));
        User user = getItem(position);
        friendItemBinding.textViewFriendName.setText(user.getEmail());
        friendItemBinding.buttonRemoveFriend.setOnClickListener(v -> {
            showDeleteConfirmationDialog(user);
        });
        convertView = friendItemBinding.getRoot();
        return convertView;
    }

    private void showDeleteConfirmationDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Remove Friend")
                .setMessage("Are you sure you want to remove " + user.getEmail() + " from your friends list?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    // User confirmed deletion, proceed with deletion
                    FirebaseDatabase.getInstance().getReference("userAlbums/" + FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot userAlbumSnapshot) {
                                    List<Album> albums = new ArrayList<>();
                                    for (DataSnapshot albumSnapshot : userAlbumSnapshot.getChildren()) {
                                        ConcreteAlbum concreteAlbum = albumSnapshot.getValue(ConcreteAlbum.class);
                                        albums.add(concreteAlbum.IntoAlbum());
                                    }
                                    if (albums.stream().flatMap(a -> a.getUsers().stream()).anyMatch(u -> u.getUid().equals(user.getUid()))){
                                        //Don't delete friend if they are in any of the user's albums
                                        new AlertDialog.Builder(getContext())
                                                .setTitle("Error")
                                                .setMessage("Cannot remove friend because they are in one of your albums")
                                                .setPositiveButton("Ok", (dialog, which) -> {
                                                    // User confirmed deletion, proceed with deletion
                                                })
                                                .show();
                                    } else {
                                        deleteFriend(user);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // User canceled the deletion, do nothing
                })
                .show();
    }

    private void deleteFriend(User user) {
        FirebaseDatabase.getInstance().getReference("friends/" + FirebaseAuth.getInstance().getCurrentUser().getUid()).child(user.getUid()).removeValue();
        FirebaseDatabase.getInstance().getReference("friends/" + user.getUid()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
    }

}

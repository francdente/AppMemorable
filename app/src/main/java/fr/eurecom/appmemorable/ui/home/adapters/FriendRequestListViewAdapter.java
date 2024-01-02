package fr.eurecom.appmemorable.ui.home.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import fr.eurecom.appmemorable.databinding.AlbumItemBinding;
import fr.eurecom.appmemorable.databinding.FriendRequestItemBinding;
import fr.eurecom.appmemorable.models.User;

public class FriendRequestListViewAdapter extends ArrayAdapter<User> {

    FriendRequestItemBinding friendRequestItemBinding;
    public FriendRequestListViewAdapter(@NonNull Context context, List<User> friendRequests) {
        super(context, 0, friendRequests);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        friendRequestItemBinding = FriendRequestItemBinding.inflate(LayoutInflater.from(getContext()));
        User user = getItem(position);
        friendRequestItemBinding.textViewFriendName.setText(user.getEmail());
        friendRequestItemBinding.buttonAccept.setOnClickListener(v -> {
            addFriend(user);
            deleteFriendRequest(user);
        });
        friendRequestItemBinding.buttonCancel.setOnClickListener(v -> {
            deleteFriendRequest(user);
        });
        convertView = friendRequestItemBinding.getRoot();
        return convertView;

    }

    private void deleteFriendRequest(User user) {
        FirebaseDatabase.getInstance().getReference("friendRequests/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()).child(user.getUid()).removeValue();
    }
    private void addFriend(User user) {
        FirebaseDatabase.getInstance().getReference("friends/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()+"/"+user.getUid()).setValue(user);
        FirebaseDatabase.getInstance().getReference("friends/"+user.getUid()+"/"+FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(new User(FirebaseAuth.getInstance().getCurrentUser().getEmail(), FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), FirebaseAuth.getInstance().getCurrentUser().getUid()));
    }
}

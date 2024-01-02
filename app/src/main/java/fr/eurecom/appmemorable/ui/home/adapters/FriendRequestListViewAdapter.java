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

public class FriendRequestListViewAdapter extends ArrayAdapter<String> {

    FriendRequestItemBinding friendRequestItemBinding;
    public FriendRequestListViewAdapter(@NonNull Context context, List<String> friendRequests) {
        super(context, 0, friendRequests);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        friendRequestItemBinding = FriendRequestItemBinding.inflate(LayoutInflater.from(getContext()));
        String userEmail = getItem(position);
        friendRequestItemBinding.textViewFriendName.setText(userEmail);
        friendRequestItemBinding.buttonAccept.setOnClickListener(v -> {
            addFriend(userEmail);
        });
        friendRequestItemBinding.buttonCancel.setOnClickListener(v -> {
            FirebaseDatabase.getInstance().getReference("friendRequests/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()).child(userEmail).removeValue();
        });
        convertView = friendRequestItemBinding.getRoot();
        return convertView;

    }

    private void addFriend(String email) {
        FirebaseDatabase.getInstance().getReference("friends/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()).push().setValue(email);
    }
}

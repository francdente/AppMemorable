package fr.eurecom.appmemorable.ui.friends;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import fr.eurecom.appmemorable.databinding.FragmentFriendsBinding;
import fr.eurecom.appmemorable.models.Album;
import fr.eurecom.appmemorable.models.ConcreteAlbum;
import fr.eurecom.appmemorable.models.User;
import fr.eurecom.appmemorable.ui.home.adapters.FriendListViewAdapter;
import fr.eurecom.appmemorable.ui.home.adapters.FriendRequestListViewAdapter;

public class FriendsFragment extends Fragment {

private FragmentFriendsBinding binding;
private FriendRequestListViewAdapter friendRequestListViewAdapter;
private FriendListViewAdapter friendListViewAdapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentFriendsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFriendSearch();
        initFriendRequestListView();
        initFriendListView();

    }

    private void initFriendListView(){
        friendListViewAdapter = new FriendListViewAdapter(getContext(), new ArrayList<>());
        FirebaseDatabase.getInstance().getReference("friends/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<User> newUsers = new ArrayList<>();
                // Check if snapshot exists and has children
                if (snapshot.exists() && snapshot.hasChildren()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null) {
                            newUsers.add(user);
                        }
                    }
                }
                friendListViewAdapter.clear();
                friendListViewAdapter.addAll(newUsers);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        binding.listViewFriends.setAdapter(friendListViewAdapter);
    }

    private void initFriendRequestListView() {
        friendRequestListViewAdapter = new FriendRequestListViewAdapter(getContext(), new ArrayList<>());
        FirebaseDatabase.getInstance().getReference("friendRequests/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<User> newUsers = new ArrayList<>();
                // Check if snapshot exists and has children
                if (snapshot.exists() && snapshot.hasChildren()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null) {
                            newUsers.add(user);
                        }
                    }
                }
                friendRequestListViewAdapter.clear();
                friendRequestListViewAdapter.addAll(newUsers);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        binding.listViewFriendRequests.setAdapter(friendRequestListViewAdapter);
    }

    private void initFriendSearch() {
        AutoCompleteTextView autoCompleteTextView = binding.autoCompleteTextViewFriendSearch;
        // Create adapter and set it to AutoCompleteTextView
        ArrayAdapter<User> dropDownAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        autoCompleteTextView.setAdapter(dropDownAdapter);
        // Set threshold to start showing suggestions after a certain number of characters
        autoCompleteTextView.setThreshold(1);

        FirebaseDatabase.getInstance().getReference("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<User> newUsers = new ArrayList<>();
                // Check if snapshot exists and has children
                if (snapshot.exists() && snapshot.hasChildren()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null) {
                            newUsers.add(user);
                        }
                    }
                }
                dropDownAdapter.clear();
                //TODO: fix this
                dropDownAdapter.addAll(newUsers.stream().filter(user -> !user.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())).collect(Collectors.toList()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Handle item click in the AutoCompleteTextView
        autoCompleteTextView.setOnItemClickListener((parent, v1, p, id) -> {
            User selectedUser = (User) parent.getItemAtPosition(p);
            showConfirmationDialog(selectedUser);
        });
    }

    private void showConfirmationDialog(User selectedUser) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Send friend request")
                .setMessage("Do you want to send a friend request to " + selectedUser.getEmail() + "?")
                .setPositiveButton("Send", (dialog, which) -> {
                    // User confirmed deletion, proceed with deletion
                    addFriendRequest(selectedUser);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // User canceled the deletion, do nothing
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void addFriendRequest(User user) {
        String currUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String currEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String currUsername = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        User currentUser = new User(currEmail, currUsername, currUid);
        FirebaseDatabase.getInstance().getReference("friendRequests/"+ user.getUid() + "/"+ currentUser.getUid()).setValue(currentUser);
    }


}
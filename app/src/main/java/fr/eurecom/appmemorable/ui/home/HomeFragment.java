package fr.eurecom.appmemorable.ui.home;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import fr.eurecom.appmemorable.R;
import fr.eurecom.appmemorable.databinding.FragmentHomeBinding;
import fr.eurecom.appmemorable.models.Album;
import fr.eurecom.appmemorable.models.ConcreteAlbum;
import fr.eurecom.appmemorable.models.ContentNode;
import fr.eurecom.appmemorable.models.User;
import fr.eurecom.appmemorable.ui.home.adapters.AlbumListViewAdapter;

public class HomeFragment extends Fragment {
    //private ViewPagerAdapter pagerAdapter;
    private AlbumListViewAdapter albumListViewAdapter;
    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        albumListViewAdapter = new AlbumListViewAdapter(getContext(), new ArrayList<>());
        FirebaseDatabase.getInstance().getReference("userAlbums/"+ FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot userAlbumSnapshot) {
                        List<Album> albums = new ArrayList<>();
                        for (DataSnapshot albumSnapshot : userAlbumSnapshot.getChildren()) {
                            ConcreteAlbum concreteAlbum = albumSnapshot.getValue(ConcreteAlbum.class);
                            albums.add(concreteAlbum.IntoAlbum());
                        }
                        albumListViewAdapter.clear();
                        albumListViewAdapter.addAll(albums);
                        albumListViewAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        //Set adapter for the pager
        binding.listView.setAdapter(albumListViewAdapter);

        //Set on click listener to add a new album
        binding.addAlbumButton.setOnClickListener(v -> {
            Dialog dialog = new Dialog(getContext());
            dialog.setContentView(R.layout.add_album);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.setCancelable(true);
            dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;

            AutoCompleteTextView autoCompleteTextView = dialog.findViewById(R.id.autoCompleteTextView);
            ListView listView = dialog.findViewById(R.id.listView);

            ArrayList<User> selectedUsers = new ArrayList<>();
            FirebaseUser usr = FirebaseAuth.getInstance().getCurrentUser();
            selectedUsers.add(new User(usr.getEmail(), usr.getDisplayName(), usr.getUid()));
            ArrayAdapter<User> userListAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, selectedUsers);
            listView.setAdapter(userListAdapter);

            // Create adapter and set it to AutoCompleteTextView
            ArrayAdapter<User> dropDownAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>());

            autoCompleteTextView.setAdapter(dropDownAdapter);
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
            // Set threshold to start showing suggestions after a certain number of characters
            autoCompleteTextView.setThreshold(1);

            // Handle item click in the AutoCompleteTextView
            autoCompleteTextView.setOnItemClickListener((parent, v1, p, id) -> {
                User selectedUser = (User) parent.getItemAtPosition(p);
                autoCompleteTextView.setText(""); // Clear the AutoCompleteTextView
                selectedUsers.add(selectedUser);
                dropDownAdapter.remove(selectedUser);
                dropDownAdapter.notifyDataSetChanged();
                userListAdapter.notifyDataSetChanged();
            });

            dialog.findViewById(R.id.btnInsert).setOnClickListener(v1 -> {
                String albumName = ((TextView)dialog.findViewById(R.id.editAlbumName)).getText().toString();
                this.addAlbum(new Album(albumName, new HashMap<>()), selectedUsers);
                dialog.dismiss();
            });
            dialog.findViewById(R.id.btnCancel).setOnClickListener(v1 -> {
                dialog.dismiss();
            });
            dialog.show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void addAlbum(Album album, List<User> users){
        DatabaseReference userAlbums = FirebaseDatabase.getInstance().getReference("userAlbums");
        DatabaseReference first_ref = userAlbums.child(users.get(0).getUid()).push();
        album.setId(first_ref.getKey());
        first_ref.setValue(new ConcreteAlbum(album));
        for (int i = 1; i < users.size(); i++) {
            DatabaseReference ref = userAlbums.child(users.get(i).getUid()).push();
            ref.setValue(new ConcreteAlbum(album));
        }
    }
}
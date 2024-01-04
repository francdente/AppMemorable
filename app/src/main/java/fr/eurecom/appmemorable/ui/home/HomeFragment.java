package fr.eurecom.appmemorable.ui.home;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.eurecom.appmemorable.NodesActivity;
import fr.eurecom.appmemorable.R;
import fr.eurecom.appmemorable.databinding.FragmentHomeBinding;
import fr.eurecom.appmemorable.models.Album;
import fr.eurecom.appmemorable.models.ConcreteAlbum;
import fr.eurecom.appmemorable.models.ContentNode;
import fr.eurecom.appmemorable.models.ImageNode;
import fr.eurecom.appmemorable.models.User;
import fr.eurecom.appmemorable.ui.home.adapters.AlbumListViewAdapter;
import fr.eurecom.appmemorable.ui.home.adapters.SelectedUsersAdapter;

public class HomeFragment extends Fragment {
    //private ViewPagerAdapter pagerAdapter;
    private AlbumListViewAdapter albumListViewAdapter;
    private FragmentHomeBinding binding;
    private boolean filterShown = false;
    ActivityResultLauncher<Intent> activityResultLauncher;
    Uri albumCover;
    ImageView albumCoverImageView;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initToggleFilterButton();
        initAlbumListView();
        initFilterAlbum();
        initAddAlbumButton();

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_OK){
                    if(result.getData() != null){
                        albumCover = result.getData().getData();
                        albumCoverImageView.setImageURI(albumCover);
                        albumCoverImageView.setVisibility(View.VISIBLE);
                    }
                }

            }
        });

    }

    private void initToggleFilterButton(){
        binding.toggleFilterButton.setOnClickListener(v -> {
            if(filterShown){
                binding.searchView.setVisibility(View.GONE);
                binding.filteringOptions.setVisibility(View.GONE);
                binding.filteringOptionsText.setVisibility(View.GONE);
            }else{
                binding.searchView.setVisibility(View.VISIBLE);
                binding.filteringOptions.setVisibility(View.VISIBLE);
                binding.filteringOptionsText.setVisibility(View.VISIBLE);
            }
            filterShown = !filterShown;
        });
    }
    private void initFilterAlbum(){
        SearchView filterAlbum = binding.searchView;
        CheckBox ownedFilter = binding.ownedFilter;
        Spinner sortSpinner = binding.spinnerSortBy;

        sortSpinner.setAdapter(new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{"Date", "Title"}));
        sortSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Comparator<Album> comparator;
                if (position == 0) {
                    Log.e("sort", "by date");
                    //Comparator using LocalDateTime parsed from creationDate of album
                    comparator = Comparator.comparing(Album::getCreationDate);
                } else {
                    Log.e("sort", "by title");
                    //Don't count uppercase and lowercase
                    comparator = Comparator.comparing(Album::getTitle, String.CASE_INSENSITIVE_ORDER);
                }
                albumListViewAdapter.sortAlbums(comparator);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ownedFilter.setOnCheckedChangeListener((buttonView, isChecked) -> {
            albumListViewAdapter.setOwnedFilter(isChecked);
            albumListViewAdapter.getFilter().filter("");
        });
        filterAlbum.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                albumListViewAdapter.setTitleFilter(query);
                albumListViewAdapter.getFilter().filter("");
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                albumListViewAdapter.setTitleFilter(newText);
                albumListViewAdapter.getFilter().filter("");
                return true;
            }
        });
    }
    private void initAlbumListView(){
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
                        //THIS IS VERY IMPORTANT FOR THE FILTERING TO WORK!!
                        albumListViewAdapter.setmAlbums(albums);
                        albumListViewAdapter.getFilter().filter("");
                        albumListViewAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        //Set adapter for the pager
        binding.listView.setAdapter(albumListViewAdapter);
    }

    private void initAddAlbumButton(){
        //Set on click listener to add a new album
        binding.addAlbumButton.setOnClickListener(v -> {
            Dialog dialog = new Dialog(getContext());
            dialog.setContentView(R.layout.add_album);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.setCancelable(true);
            dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;

            AutoCompleteTextView autoCompleteTextView = dialog.findViewById(R.id.autoCompleteTextView);
            ListView listView = dialog.findViewById(R.id.listView);

            // Create adapter and set it to AutoCompleteTextView
            ArrayAdapter<User> dropDownAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
            autoCompleteTextView.setAdapter(dropDownAdapter);
            // Set threshold to start showing suggestions after a certain number of characters
            autoCompleteTextView.setThreshold(1);

            //Create adapter for the selected users
            ArrayList<User> selectedUsers = new ArrayList<>();
            SelectedUsersAdapter userListAdapter = new SelectedUsersAdapter(getContext(), selectedUsers, dropDownAdapter);
            listView.setAdapter(userListAdapter);


            FirebaseDatabase.getInstance().getReference("friends/"+FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
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
                autoCompleteTextView.setText(""); // Clear the AutoCompleteTextView
                selectedUsers.add(selectedUser);
                dropDownAdapter.remove(selectedUser);
                dropDownAdapter.notifyDataSetChanged();
                userListAdapter.notifyDataSetChanged();
            });

            dialog.findViewById(R.id.btnInsert).setOnClickListener(v1 -> {
                String albumName = ((TextView)dialog.findViewById(R.id.editAlbumName)).getText().toString();
                FirebaseUser usr = FirebaseAuth.getInstance().getCurrentUser();
                //Add the current users to the selected users that will share the album
                User owner = new User(usr.getEmail(), usr.getDisplayName(), usr.getUid());
                selectedUsers.add(owner);
                String albumCoverUrl = UUID.randomUUID().toString();
                Album newAlbum = new Album(albumName, new HashMap<>(), selectedUsers, LocalDateTime.now(), owner, albumCoverUrl);
                this.addAlbumCover(newAlbum,selectedUsers);
                //this.addAlbum(newAlbum, selectedUsers);
                dialog.dismiss();
            });
            dialog.findViewById(R.id.btnCancel).setOnClickListener(v1 -> {
                dialog.dismiss();
            });

            dialog.findViewById(R.id.btnAddAlbumCover).setOnClickListener(v1 -> {

                albumCoverImageView = dialog.findViewById(R.id.imageView);
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                activityResultLauncher.launch(intent);
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
        Log.e("addAlbum", album.getOwner().getEmail());
        DatabaseReference userAlbums = FirebaseDatabase.getInstance().getReference("userAlbums");
        DatabaseReference first_ref = userAlbums.child(users.get(0).getUid()).push();
        album.setId(first_ref.getKey());
        first_ref.setValue(new ConcreteAlbum(album));
        for (int i = 1; i < users.size(); i++) {
            DatabaseReference ref = userAlbums.child(users.get(i).getUid()).child(album.getId());
            ref.setValue(new ConcreteAlbum(album));
        }
    }

    private void addAlbumCover(Album album, List<User> users){
        Log.e("addAlbum", album.getOwner().getEmail());
        StorageReference albumCoverRef = FirebaseStorage.getInstance().getReference().child("cover/"+album.getAlbumCoverUrl());;

        albumCoverRef.putFile(albumCover).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                addAlbum(album, users);
                Toast.makeText(getContext(), "Cover uploaded successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
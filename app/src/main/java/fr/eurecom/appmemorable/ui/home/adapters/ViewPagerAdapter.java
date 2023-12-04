package fr.eurecom.appmemorable.ui.home.adapters;

import android.app.Dialog;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

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

import fr.eurecom.appmemorable.R;
import fr.eurecom.appmemorable.databinding.ViewPagerItemBinding;
import fr.eurecom.appmemorable.models.Album;
import fr.eurecom.appmemorable.models.ConcreteAlbum;
import fr.eurecom.appmemorable.models.ContentNode;
import fr.eurecom.appmemorable.models.TextNode;
import fr.eurecom.appmemorable.models.User;
import fr.eurecom.appmemorable.models.ConcreteNode;
import fr.eurecom.appmemorable.repository.MemorableRepository;

public class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.ViewHolder> {
    private final Fragment fragment;
    private List<Album> albums;
    private RecyclerViewAdapter recyclerViewAdapter;

    private boolean mIsAllFabsVisible;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            this.view = view;
        }
        public View getView(){
            return view;
        }
    }

    /**
     * Initialize the dataset of the Adapter
     *
     * @param albums List<Album> containing the data to populate views to be used
     * by RecyclerView
     */
    public ViewPagerAdapter(Fragment fragment, List<Album> albums) {
        this.albums = albums;
        this.fragment = fragment;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        ViewPagerItemBinding binding = ViewPagerItemBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        View view = binding.getRoot();

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        ViewPagerItemBinding binding = ViewPagerItemBinding.bind(viewHolder.getView());
        recyclerViewAdapter = new RecyclerViewAdapter(new ArrayList<>());
        Log.e("ViewPagerAdapter", "onBindViewHolder: "+albums.get(position).getId()+" binding for album"+albums.get(position).getTitle());
        FirebaseDatabase.getInstance().getReference("albumNodes/"+albums.get(position).getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ContentNode> nodes = new ArrayList<>();
                // Check if snapshot exists and has children
                if (snapshot.exists() && snapshot.hasChildren()) {
                    for (DataSnapshot nodeSnapshot : snapshot.getChildren()) {
                        ConcreteNode concreteNode = nodeSnapshot.getValue(ConcreteNode.class);
                        if (concreteNode != null) {
                            ContentNode node = concreteNode.IntoContentNode();
                            node.setId(nodeSnapshot.getKey());
                            nodes.add(node);
                        }
                    }
                }
                recyclerViewAdapter.setNodes(nodes);
                recyclerViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        setFloatingButton(binding.getRoot(), position);
        binding.recyclerView.setAdapter(recyclerViewAdapter);
        binding.titleButton.setText(albums.get(position).getTitle());

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return albums.size();
    }

    private void setFloatingButton(View view, int position) {
        ViewPagerItemBinding binding = ViewPagerItemBinding.bind(view);

        binding.addAudio.setVisibility(View.GONE);
        binding.addImage.setVisibility(View.GONE);
        binding.addText.setVisibility(View.GONE);
        binding.addAlbum.setVisibility(View.GONE);

        mIsAllFabsVisible = false;

        binding.addFab.setOnClickListener(v -> {
            if (!mIsAllFabsVisible) {
                binding.addAudio.show();
                binding.addImage.show();
                binding.addText.show();
                binding.addAlbum.show();

                mIsAllFabsVisible = true;
            } else {
                binding.addAudio.setVisibility(View.GONE);
                binding.addImage.setVisibility(View.GONE);
                binding.addText.setVisibility(View.GONE);
                binding.addAlbum.setVisibility(View.GONE);

                mIsAllFabsVisible = false;
            }
        });
        binding.addAudio.setOnClickListener(
                v -> {
                    binding.addAudio.setVisibility(View.GONE);
                    binding.addImage.setVisibility(View.GONE);
                    binding.addText.setVisibility(View.GONE);
                    binding.addAlbum.setVisibility(View.GONE);

                    mIsAllFabsVisible = false;
                    Toast.makeText(fragment.getContext(),"Audio added", Toast.LENGTH_SHORT).show();
                });
        binding.addImage.setOnClickListener(
                v -> {
                    binding.addAudio.setVisibility(View.GONE);
                    binding.addImage.setVisibility(View.GONE);
                    binding.addText.setVisibility(View.GONE);
                    binding.addAlbum.setVisibility(View.GONE);

                    mIsAllFabsVisible = false;
                    Toast.makeText(fragment.getContext(), "Image Added", Toast.LENGTH_SHORT).show();
                });
        binding.addText.setOnClickListener(
                v -> {
                    binding.addAudio.setVisibility(View.GONE);
                    binding.addImage.setVisibility(View.GONE);
                    binding.addText.setVisibility(View.GONE);
                    binding.addAlbum.setVisibility(View.GONE);

                    mIsAllFabsVisible = false;
                    Dialog dialog = new Dialog(this.fragment.getContext());
                    dialog.setContentView(R.layout.add_node);
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.setCancelable(false);
                    dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
                    dialog.findViewById(R.id.btnInsert).setOnClickListener(v1 -> {
                        String text = ((TextView)dialog.findViewById(R.id.editText)).getText().toString();
                        this.addNodeToAlbum(new TextNode(albums.get(position).getId(), "1", null, text), albums.get(position).getId());
                        dialog.dismiss();
                    });
                    dialog.findViewById(R.id.btnCancel).setOnClickListener(v1 -> dialog.dismiss());
                    dialog.show();
                });
        binding.addAlbum.setOnClickListener(
                v -> {
                    binding.addAudio.setVisibility(View.GONE);
                    binding.addImage.setVisibility(View.GONE);
                    binding.addText.setVisibility(View.GONE);
                    binding.addAlbum.setVisibility(View.GONE);

                    mIsAllFabsVisible = false;
                    Dialog dialog = new Dialog(this.fragment.getContext());
                    dialog.setContentView(R.layout.add_album);
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.setCancelable(false);
                    dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;

                    AutoCompleteTextView autoCompleteTextView = dialog.findViewById(R.id.autoCompleteTextView);
                    ListView listView = dialog.findViewById(R.id.listView);

                    ArrayList<User> selectedUsers = new ArrayList<>();
                    FirebaseUser usr = FirebaseAuth.getInstance().getCurrentUser();
                    selectedUsers.add(new User(usr.getEmail(), usr.getDisplayName(), usr.getUid()));
                    ArrayAdapter<User> userListAdapter = new ArrayAdapter<>(fragment.getContext(), android.R.layout.simple_dropdown_item_1line, selectedUsers);
                    listView.setAdapter(userListAdapter);

                    // Create adapter and set it to AutoCompleteTextView
                    ArrayAdapter<User> dropDownAdapter = new ArrayAdapter<>(fragment.getContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>());

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
                            dropDownAdapter.addAll(newUsers.stream().filter(user -> !selectedUsers.contains(user.getEmail())).collect(Collectors.toList()));
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
    private void addAlbum(Album album, List<User> users){
        DatabaseReference nodes = FirebaseDatabase.getInstance().getReference("albums");
        String key = nodes.push().getKey();
        nodes.child(key).setValue(new ConcreteAlbum(album));
        DatabaseReference userAlbums = FirebaseDatabase.getInstance().getReference("userAlbums");
        for (User user : users) {
            userAlbums.child(user.getUid()).push().setValue(key);
        }
    }

    public void addNodeToAlbum(ContentNode node, String albumKey) {
        DatabaseReference nodes = FirebaseDatabase.getInstance().getReference("albumNodes/"+albumKey);
        String key = nodes.push().getKey();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        node.setUser(new User(user.getEmail(), user.getDisplayName(), user.getUid()));
        nodes.child(key).setValue(new ConcreteNode(node));
    }

    public void setAlbums(List<Album> albums){
        this.albums = albums;
    }
}


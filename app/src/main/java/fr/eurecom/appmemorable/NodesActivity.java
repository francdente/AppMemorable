package fr.eurecom.appmemorable;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.eurecom.appmemorable.databinding.ActivityNodesBinding;
import fr.eurecom.appmemorable.models.Album;
import fr.eurecom.appmemorable.models.ConcreteNode;
import fr.eurecom.appmemorable.models.ContentNode;
import fr.eurecom.appmemorable.models.ImageNode;
import fr.eurecom.appmemorable.models.TextNode;
import fr.eurecom.appmemorable.models.User;
import fr.eurecom.appmemorable.ui.home.adapters.NodeListViewAdapter;

public class NodesActivity extends AppCompatActivity {
    ActivityNodesBinding binding;
    NodeListViewAdapter nodeListViewAdapter;
    private boolean mIsAllFabsVisible = false;
    ActivityResultLauncher<Intent> activityResultLauncher;

    LinearProgressIndicator progress = null;
    Uri image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        binding = ActivityNodesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        String albumKey = getIntent().getStringExtra("albumKey");
        String albumName = getIntent().getStringExtra("albumName");
        nodeListViewAdapter = new NodeListViewAdapter(this, new ArrayList<>());
        binding.albumNameText.setText(albumName);

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_OK){
                    if(result.getData() != null){
                        image = result.getData().getData();
                        StorageReference storageRef = FirebaseStorage.getInstance("gs://appmemorable-bb5f9.appspot.com").getReference();
                        StorageReference imageRef = storageRef.child(""+albumKey+"/"+ UUID.randomUUID().toString());
                        imageRef.putFile(image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Toast.makeText(NodesActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(NodesActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            }
                        });

                        Toast.makeText(NodesActivity.this, "Image Added", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(NodesActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
                }

            }
        });
        FirebaseDatabase.getInstance().getReference("albumNodes/"+albumKey).addValueEventListener(new ValueEventListener() {
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
                            Log.e("NodesActivity", "onDataChange: "+((TextNode)node).getText());
                            nodes.add(node);
                        }
                    }
                }
                nodeListViewAdapter.clear();
                nodeListViewAdapter.addAll(nodes);
                nodeListViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        binding.nodesListView.setAdapter(nodeListViewAdapter);
        setFloatingButton(binding.getRoot(), albumKey);
    }

    private void setFloatingButton(View view, String albumKey) {
        binding.addAudio.setVisibility(View.GONE);
        binding.addImage.setVisibility(View.GONE);
        binding.addText.setVisibility(View.GONE);

        mIsAllFabsVisible = false;

        binding.addFab.setOnClickListener(v -> {
            if (!mIsAllFabsVisible) {
                binding.addAudio.show();
                binding.addImage.show();
                binding.addText.show();

                mIsAllFabsVisible = true;
            } else {
                binding.addAudio.setVisibility(View.GONE);
                binding.addImage.setVisibility(View.GONE);
                binding.addText.setVisibility(View.GONE);

                mIsAllFabsVisible = false;
            }
        });
        binding.addAudio.setOnClickListener(
                v -> {
                    binding.addAudio.setVisibility(View.GONE);
                    binding.addImage.setVisibility(View.GONE);
                    binding.addText.setVisibility(View.GONE);

                    mIsAllFabsVisible = false;
                    Toast.makeText(this,"Audio added", Toast.LENGTH_SHORT).show();
                });
        binding.addImage.setOnClickListener(
                v -> {
                    binding.addAudio.setVisibility(View.GONE);
                    binding.addImage.setVisibility(View.GONE);
                    binding.addText.setVisibility(View.GONE);

                    mIsAllFabsVisible = false;

                    Dialog dialog = new Dialog(this);
                    dialog.setContentView(R.layout.add_node);
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.setCancelable(false);
                    dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
                    dialog.findViewById(R.id.btnInsert).setOnClickListener(v1 -> {
                        String text = ((TextView)dialog.findViewById(R.id.editText)).getText().toString();
                        this.addNodeToAlbum(new ImageNode(albumKey, "1", null, text, null), albumKey);
                        dialog.dismiss();
                    });
                    dialog.findViewById(R.id.btnCancel).setOnClickListener(v1 -> dialog.dismiss());
                    dialog.show();


                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    activityResultLauncher.launch(intent);

                });
        binding.addText.setOnClickListener(
                v -> {
                    binding.addAudio.setVisibility(View.GONE);
                    binding.addImage.setVisibility(View.GONE);
                    binding.addText.setVisibility(View.GONE);

                    mIsAllFabsVisible = false;
                    Dialog dialog = new Dialog(this);
                    dialog.setContentView(R.layout.add_node);
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.setCancelable(false);
                    dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
                    dialog.findViewById(R.id.btnInsert).setOnClickListener(v1 -> {
                        String text = ((TextView)dialog.findViewById(R.id.editText)).getText().toString();
                        this.addNodeToAlbum(new TextNode(albumKey, "1", null, text), albumKey);
                        dialog.dismiss();
                    });
                    dialog.findViewById(R.id.btnCancel).setOnClickListener(v1 -> dialog.dismiss());
                    dialog.show();
                });
    }

    private void addNodeToAlbum(ContentNode node, String albumKey) {
        DatabaseReference nodes = FirebaseDatabase.getInstance().getReference("albumNodes/"+albumKey);
        Log.e("ViewPagerAdapter", "addNodeToAlbum: "+albumKey);
        String key = nodes.push().getKey();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        node.setUser(new User(user.getEmail(), user.getDisplayName(), user.getUid()));
        nodes.child(key).setValue(new ConcreteNode(node));
    }
}
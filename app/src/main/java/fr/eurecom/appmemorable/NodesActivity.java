package fr.eurecom.appmemorable;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.io.File;

import fr.eurecom.appmemorable.databinding.ActivityNodesBinding;
import fr.eurecom.appmemorable.models.AudioNode;
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

    MediaRecorder mediaRecorder;
    String audioFilePath;
    File audioFile = null;
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
                        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                        String randomUrl = UUID.randomUUID().toString();
                        StorageReference imageRef = storageRef.child(""+albumKey+"/"+randomUrl);
                        imageRef.putFile(image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Dialog dialog = new Dialog(NodesActivity.this);
                                dialog.setContentView(R.layout.add_node);
                                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                dialog.setCancelable(false);
                                dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
                                dialog.findViewById(R.id.btnInsert).setOnClickListener(v1 -> {
                                    String text = ((TextView) dialog.findViewById(R.id.editText)).getText().toString();
                                    NodesActivity.this.addNodeToAlbum(new ImageNode(albumKey, "1", null, text, randomUrl), albumKey);
                                    dialog.dismiss();

                                });
                                dialog.findViewById(R.id.btnCancel).setOnClickListener(v1 -> {
                                    NodesActivity.this.addNodeToAlbum(new ImageNode(albumKey, "1", null, null, randomUrl), albumKey);
                                    dialog.dismiss();
                                });
                                dialog.show();

                                Toast.makeText(NodesActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(NodesActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            }
                        });

                        //Toast.makeText(NodesActivity.this, "Image Added", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
        FirebaseDatabase.getInstance().getReference("albums/"+albumKey).addValueEventListener(new ValueEventListener() {
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

                    Dialog dialog = new Dialog(this);
                    dialog.setContentView(R.layout.add_audio_node);
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.setCancelable(false);
                    dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;

                    Chronometer chronometerRecording = dialog.findViewById(R.id.chronometerRecording);
                    Button btnInsert = dialog.findViewById(R.id.btnInsert);
                    Button btnCancel = dialog.findViewById(R.id.btnCancel);
                    Button btnStartRecording = dialog.findViewById(R.id.btnStartRecording);
                    Button btnStopRecording = dialog.findViewById(R.id.btnStopRecording);
                    Button btnDeleteRecording = dialog.findViewById(R.id.btnDeleteRecording);

                    chronometerRecording.setBase(SystemClock.elapsedRealtime());
                    chronometerRecording.setText("00:00");
                    String audioUrl = UUID.randomUUID().toString();
                    audioFilePath = getExternalCacheDir().getAbsolutePath() + audioUrl;
                    Log.e("NodesActivity", "audiofilePath: "+audioFilePath);
                    final String[] duration = {null};

                    btnInsert.setOnClickListener(v1 -> {
                        if(audioFile != null){



                            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                            StorageReference audioRef = storageRef.child(""+albumKey+"/"+audioUrl);

                            audioRef.putFile(Uri.fromFile(audioFile)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                    NodesActivity.this.addNodeToAlbum(new AudioNode(albumKey, LocalDateTime.now().toString(), null, null, audioUrl, duration[0]), albumKey);
                                    Toast.makeText(NodesActivity.this,"Audio added", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast.makeText(NodesActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                }
                            });

                            dialog.dismiss();
                        }else{
                            Toast.makeText(NodesActivity.this, "No audio source to insert!", Toast.LENGTH_SHORT).show();
                        }


                    });

                    btnCancel.setOnClickListener(v1 -> {
                        // Add logic to handle delete button click
                        // You can dismiss the dialog or perform any other action
                        deleteRecording();
                        dialog.dismiss();
                    });

                    btnStartRecording.setOnClickListener(v1 -> {
                        // Add logic to handle start recording button click
                        // You can start recording audio here



                        chronometerRecording.start();
                        startRecording();
                        btnStartRecording.setVisibility(View.GONE);
                        btnStopRecording.setVisibility(View.VISIBLE);
                        btnDeleteRecording.setVisibility(View.GONE);
                    });

                    btnStopRecording.setOnClickListener(v1 -> {
                        // Add logic to handle stop recording button click
                        // You can stop recording audio here
                        stopRecording();
                        chronometerRecording.stop();
                        long elapsedSeconds = (SystemClock.elapsedRealtime() - chronometerRecording.getBase())/ 1000;
                        String time = String.format(Locale.getDefault(), "%02d:%02d", (elapsedSeconds % 3600) / 60, elapsedSeconds % 60);
                        duration[0] = time;

                        btnStartRecording.setVisibility(View.GONE);
                        btnStopRecording.setVisibility(View.GONE);
                        btnDeleteRecording.setVisibility(View.VISIBLE);
                    });

                    btnDeleteRecording.setOnClickListener(v1 -> {
                        // Add logic to handle delete recording button click
                        // You can delete the recorded audio file here
                        deleteRecording();
                        chronometerRecording.setBase(SystemClock.elapsedRealtime());
                        chronometerRecording.setText("00:00");
                        btnStartRecording.setVisibility(View.VISIBLE);
                        btnStopRecording.setVisibility(View.GONE);
                        btnDeleteRecording.setVisibility(View.GONE);
                    });

                    dialog.show();

                    mIsAllFabsVisible = false;

                });
        binding.addImage.setOnClickListener(
                v -> {
                    binding.addAudio.setVisibility(View.GONE);
                    binding.addImage.setVisibility(View.GONE);
                    binding.addText.setVisibility(View.GONE);

                    mIsAllFabsVisible = false;

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
                        this.addNodeToAlbum(new TextNode(albumKey, LocalDateTime.now().toString(), null, text), albumKey);
                        dialog.dismiss();
                    });
                    dialog.findViewById(R.id.btnCancel).setOnClickListener(v1 -> dialog.dismiss());
                    dialog.show();
                });
    }

    private void addNodeToAlbum(ContentNode node, String albumKey) {
        DatabaseReference nodes = FirebaseDatabase.getInstance().getReference("albums/"+albumKey);
        Log.e("ViewPagerAdapter", "addNodeToAlbum: "+albumKey);
        String key = nodes.push().getKey();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        node.setUser(new User(user.getEmail(), user.getDisplayName(), user.getUid()));
        nodes.child(key).setValue(new ConcreteNode(node));
    }

    // Update the startRecording() method
    private void startRecording() {
        try {
            mediaRecorder = new MediaRecorder();
            //audioFilePath = getExternalCacheDir().getAbsolutePath() + "/recorded_audio.3gp";
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setOutputFile(audioFilePath);

            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Update the stopRecording() method to release MediaRecorder after stopping
    private void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
        audioFile = new File(audioFilePath);
    }

    private void deleteRecording() {

        if (audioFile != null) {
            audioFile = null;
        }
    }
}
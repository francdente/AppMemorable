package fr.eurecom.appmemorable;

import static android.widget.RelativeLayout.TRUE;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.io.File;
import java.util.stream.Collectors;

import fr.eurecom.appmemorable.databinding.ActivityNodesBinding;
import fr.eurecom.appmemorable.databinding.AddImageNodeBinding;
import fr.eurecom.appmemorable.databinding.AddNodeBinding;
import fr.eurecom.appmemorable.models.Album;
import fr.eurecom.appmemorable.models.AudioNode;
import fr.eurecom.appmemorable.models.ConcreteAlbum;
import fr.eurecom.appmemorable.models.ConcreteNode;
import fr.eurecom.appmemorable.models.ContentNode;
import fr.eurecom.appmemorable.models.ImageNode;
import fr.eurecom.appmemorable.models.TextNode;
import fr.eurecom.appmemorable.models.User;
import fr.eurecom.appmemorable.ui.home.adapters.NodeListViewAdapter;
import fr.eurecom.appmemorable.ui.home.adapters.SelectedUsersAdapter;

public class NodesActivity extends AppCompatActivity {
    ActivityNodesBinding binding;
    NodeListViewAdapter nodeListViewAdapter;
    private boolean mIsAllFabsVisible = false;
    ActivityResultLauncher<Intent> activityResultLauncher, activityResultLauncherPhoto;
    MediaRecorder mediaRecorder;
    String audioFilePath;
    File audioFile = null;

    Uri image, croppedImage;
    ImageView imageView;
    String albumKey, albumName, owner, creationDate;

    AddImageNodeBinding bindingImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        binding = ActivityNodesBinding.inflate(getLayoutInflater());
        bindingImage = AddImageNodeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        albumKey = getIntent().getStringExtra("albumKey");
        albumName = getIntent().getStringExtra("albumName");
        owner = getIntent().getStringExtra("albumOwner");

        nodeListViewAdapter = new NodeListViewAdapter(this, new ArrayList<>());
        binding.albumNameText.setText(albumName);

        initActivityResultLaunchers();

        if (owner.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            binding.editAlbumIcon.setVisibility(View.VISIBLE);
            initEditAlbumButton();
        } else {
            binding.editAlbumIcon.setVisibility(View.INVISIBLE);
        }

        FirebaseDatabase.getInstance().getReference("albums/" + albumKey).addValueEventListener(new ValueEventListener() {
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

    private void initActivityResultLaunchers() {
        ActivityResultLauncher<CropImageContractOptions> cropImage = registerForActivityResult(new CropImageContract(), result -> {
            if (result.isSuccessful()) {
                croppedImage = result.getUriContent();
                imageView.setImageURI(croppedImage);
                imageView.setVisibility(View.VISIBLE);
            }

        });


        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        image = result.getData().getData();

                        bindingImage.cropImageView.setImageUriAsync(image);

                        CropImageOptions cropImageOptions = new CropImageOptions();
                        cropImageOptions.imageSourceIncludeGallery = false;
                        cropImageOptions.imageSourceIncludeCamera = true;
                        cropImageOptions.cropShape = CropImageView.CropShape.RECTANGLE;
                        int fixedCropSizeInDp = 350;
                        int fixedCropSizeInPixels = (int) (fixedCropSizeInDp * getResources().getDisplayMetrics().density);
                        cropImageOptions.fixAspectRatio = true;
                        cropImageOptions.aspectRatioX = cropImageOptions.aspectRatioY = fixedCropSizeInPixels;

                        CropImageContractOptions cropImageContractOptions = new CropImageContractOptions(image, cropImageOptions);
                        cropImage.launch(cropImageContractOptions);


                        //Toast.makeText(NodesActivity.this, "Image Added", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        activityResultLauncherPhoto = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        image = result.getData().getData();

                        bindingImage.cropImageView.setImageUriAsync(image);

                        CropImageOptions cropImageOptions = new CropImageOptions();
                        cropImageOptions.imageSourceIncludeGallery = false;
                        cropImageOptions.imageSourceIncludeCamera = true;
                        cropImageOptions.cropShape = CropImageView.CropShape.RECTANGLE;
                        int fixedCropSizeInDp = 350;
                        int fixedCropSizeInPixels = (int) (fixedCropSizeInDp * getResources().getDisplayMetrics().density);
                        cropImageOptions.fixAspectRatio = true;
                        cropImageOptions.aspectRatioX = cropImageOptions.aspectRatioY = fixedCropSizeInPixels;

                        CropImageContractOptions cropImageContractOptions = new CropImageContractOptions(image, cropImageOptions);
                        cropImage.launch(cropImageContractOptions);


                        //Toast.makeText(NodesActivity.this, "Image Added", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
    }

    private void initEditAlbumButton() {
        imageView = findViewById(R.id.imageView);
        binding.editAlbumIcon.setOnClickListener(v -> {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.edit_album);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.setCancelable(true);
            dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;

            AutoCompleteTextView autoCompleteTextView = dialog.findViewById(R.id.autoCompleteTextView);
            ListView listView = dialog.findViewById(R.id.listView);

            // Create adapter and set it to AutoCompleteTextView
            ArrayAdapter<User> dropDownAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
            autoCompleteTextView.setAdapter(dropDownAdapter);
            // Set threshold to start showing suggestions after a certain number of characters
            autoCompleteTextView.setThreshold(1);

            //Create adapter for the selected users
            ArrayList<User> selectedUsers = new ArrayList<>();
            SelectedUsersAdapter userListAdapter = new SelectedUsersAdapter(this, selectedUsers, dropDownAdapter);
            listView.setAdapter(userListAdapter);

            FirebaseDatabase.getInstance().getReference("userAlbums/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + albumKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Album album = snapshot.getValue(ConcreteAlbum.class).IntoAlbum();
                    ((TextView) dialog.findViewById(R.id.editAlbumName)).setText(album.getTitle());
                    selectedUsers.clear();
                    selectedUsers.addAll(album.getUsers().stream().filter(user -> !user.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())).collect(Collectors.toList()));
                    userListAdapter.notifyDataSetChanged();

                    //Enable the edit button when when we have consistent data with the firebase db
                    dialog.findViewById(R.id.btnInsert).setEnabled(true);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            FirebaseDatabase.getInstance().getReference("friends/" + FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
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
                String albumName = ((TextView) dialog.findViewById(R.id.editAlbumName)).getText().toString();
                FirebaseUser usr = FirebaseAuth.getInstance().getCurrentUser();
                //Add the current users to the selected users that will share the album
                User owner = new User(usr.getEmail(), usr.getDisplayName(), usr.getUid());
                selectedUsers.add(owner);
                String albumCoverUrl = UUID.randomUUID().toString();
                Album newAlbum = new Album(albumName, new HashMap<>(), selectedUsers, LocalDateTime.now(), owner, albumCoverUrl);
                this.editAlbum(newAlbum, selectedUsers);
                //this.addAlbum(newAlbum, selectedUsers);
                dialog.dismiss();
            });
            dialog.findViewById(R.id.btnCancel).setOnClickListener(v1 -> {
                dialog.dismiss();
            });

            dialog.show();
        });
    }

    private void editAlbum(Album album, List<User> users) {
        FirebaseDatabase.getInstance().getReference("userAlbums/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + albumKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Album curr_album = snapshot.getValue(ConcreteAlbum.class).IntoAlbum();
                deleteAlbumSoft(curr_album);
                DatabaseReference userAlbums = FirebaseDatabase.getInstance().getReference("userAlbums");
                Log.e("editAlbum", "editAlbum: " + album.getId());
                Log.e("users", "editAlbum: " + users.size());
                album.setId(curr_album.getId());
                album.setAlbumCoverUrl(curr_album.getAlbumCoverUrl());
                album.setCreationDate(curr_album.getCreationDate());
                DatabaseReference first_ref = userAlbums.child(users.get(0).getUid()).child(album.getId());
                first_ref.setValue(new ConcreteAlbum(album));
                for (int i = 1; i < users.size(); i++) {
                    DatabaseReference ref = userAlbums.child(users.get(i).getUid()).child(album.getId());
                    ref.setValue(new ConcreteAlbum(album));
                }
                //TODO: THIS IS REALLY BAD TO DO
                binding.albumNameText.setText(album.getTitle());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void deleteAlbumSoft(Album album) {
        //Delete from userAlbums
        for (User usr : album.getUsers()) {
            String path = "userAlbums/" + usr.getUid() + "/" + album.getId();
            Log.e("deleteUserAlbums", path);
            FirebaseDatabase.getInstance().getReference(path).removeValue();
        }
    }

    private void setFloatingButton(View view, String albumKey) {
        binding.addAudio.setVisibility(View.GONE);
        binding.addImage.setVisibility(View.GONE);
        binding.addText.setVisibility(View.GONE);
        binding.addPhoto.setVisibility(View.GONE);

        mIsAllFabsVisible = false;

        binding.addFab.setOnClickListener(v -> {
            if (!mIsAllFabsVisible) {
                binding.addAudio.show();
                binding.addImage.show();
                binding.addText.show();
                binding.addPhoto.show();

                mIsAllFabsVisible = true;
            } else {
                binding.addAudio.setVisibility(View.GONE);
                binding.addImage.setVisibility(View.GONE);
                binding.addText.setVisibility(View.GONE);
                binding.addPhoto.setVisibility(View.GONE);

                mIsAllFabsVisible = false;
            }
        });
        binding.addAudio.setOnClickListener(
                v -> {

                    binding.addAudio.setVisibility(View.GONE);
                    binding.addImage.setVisibility(View.GONE);
                    binding.addText.setVisibility(View.GONE);
                    binding.addPhoto.setVisibility(View.GONE);

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
                    Log.e("NodesActivity", "audiofilePath: " + audioFilePath);
                    final String[] duration = {null};

                    btnInsert.setOnClickListener(v1 -> {
                        if (audioFile != null) {


                            String text = ((TextView) dialog.findViewById(R.id.editText)).getText().toString();
                            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                            StorageReference audioRef = storageRef.child("" + albumKey + "/" + audioUrl);
                            binding.progressBar.setVisibility(View.VISIBLE);

                            audioRef.putFile(Uri.fromFile(audioFile)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                    NodesActivity.this.addNodeToAlbum(new AudioNode(albumKey, LocalDateTime.now().toString(), null, text, audioUrl, duration[0]), albumKey);
                                    binding.progressBar.setVisibility(View.GONE);
                                    Toast.makeText(NodesActivity.this, "Audio added", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    binding.progressBar.setVisibility(View.GONE);
                                    Toast.makeText(NodesActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                }
                            });

                            dialog.dismiss();
                        } else {
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

                        //chronometerRecording.setBase(SystemClock.elapsedRealtime());
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
                        long elapsedSeconds = (SystemClock.elapsedRealtime() - chronometerRecording.getBase()) / 1000;
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
                    binding.addPhoto.setVisibility(View.GONE);

                    mIsAllFabsVisible = false;

                    Dialog dialog = new Dialog(this);
                    dialog.setContentView(R.layout.add_image_node);
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.setCancelable(false);
                    dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;

                    Button btnInsert = dialog.findViewById(R.id.btnInsert);
                    Button btnCancel = dialog.findViewById(R.id.btnCancel);
                    Button btnAddImage = dialog.findViewById(R.id.btnAddImage);
                    imageView = dialog.findViewById(R.id.imageView);

                    btnInsert.setOnClickListener(v1 -> {

                        if (croppedImage != null) {

                            dialog.dismiss();
                            binding.progressBar.setVisibility(View.VISIBLE);

                            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                            String randomUrl = UUID.randomUUID().toString();
                            StorageReference imageRef = storageRef.child("" + albumKey + "/" + randomUrl);
                            String text = ((TextView) dialog.findViewById(R.id.editDescription)).getText().toString();


                            imageRef.putFile(croppedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    binding.progressBar.setVisibility(View.GONE);

                                    NodesActivity.this.addNodeToAlbum(new ImageNode(albumKey, LocalDateTime.now().toString(), null, text, randomUrl), albumKey);
                                    croppedImage = null;
                                    image = null;

                                    Toast.makeText(NodesActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    binding.progressBar.setVisibility(View.GONE);
                                    //dialog.dismiss();
                                    Toast.makeText(NodesActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            Toast.makeText(NodesActivity.this, "Please select an image!", Toast.LENGTH_SHORT).show();
                        }

                    });

                    btnCancel.setOnClickListener(v1 -> {
                        dialog.dismiss();
                    });

                    btnAddImage.setOnClickListener(v1 -> {

                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        activityResultLauncher.launch(intent);


                    });


                    dialog.show();


                });
        binding.addText.setOnClickListener(
                v -> {
                    binding.addAudio.setVisibility(View.GONE);
                    binding.addImage.setVisibility(View.GONE);
                    binding.addText.setVisibility(View.GONE);
                    binding.addPhoto.setVisibility(View.GONE);

                    mIsAllFabsVisible = false;
                    Dialog dialog = new Dialog(this);
                    dialog.setContentView(R.layout.add_node);
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.setCancelable(false);
                    dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
                    dialog.findViewById(R.id.btnInsert).setOnClickListener(v1 -> {
                        binding.progressBar.setVisibility(View.VISIBLE);
                        String text = ((TextView) dialog.findViewById(R.id.editText)).getText().toString();
                        this.addNodeToAlbum(new TextNode(albumKey, LocalDateTime.now().toString(), null, text), albumKey);
                        dialog.dismiss();
                        binding.progressBar.setVisibility(View.GONE);
                    });
                    dialog.findViewById(R.id.btnCancel).setOnClickListener(v1 -> dialog.dismiss());
                    dialog.show();
                });

        binding.addPhoto.setOnClickListener(
                v -> {
                    binding.addAudio.setVisibility(View.GONE);
                    binding.addImage.setVisibility(View.GONE);
                    binding.addText.setVisibility(View.GONE);
                    binding.addPhoto.setVisibility(View.GONE);

                    mIsAllFabsVisible = false;

                    Dialog dialog = new Dialog(this);
                    dialog.setContentView(R.layout.add_image_node);
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.setCancelable(false);
                    dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;

                    Button btnInsert = dialog.findViewById(R.id.btnInsert);
                    Button btnCancel = dialog.findViewById(R.id.btnCancel);
                    Button btnAddImage = dialog.findViewById(R.id.btnAddImage);
                    imageView = dialog.findViewById(R.id.imageView);

                    btnInsert.setOnClickListener(v1 -> {

                        if (croppedImage != null) {

                            dialog.dismiss();
                            binding.progressBar.setVisibility(View.VISIBLE);

                            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                            String randomUrl = UUID.randomUUID().toString();
                            StorageReference imageRef = storageRef.child("" + albumKey + "/" + randomUrl);
                            String text = ((TextView) dialog.findViewById(R.id.editDescription)).getText().toString();


                            imageRef.putFile(croppedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    binding.progressBar.setVisibility(View.GONE);

                                    NodesActivity.this.addNodeToAlbum(new ImageNode(albumKey, LocalDateTime.now().toString(), null, text, randomUrl), albumKey);
                                    /*ContentValues values = new ContentValues();
                                    ContentResolver cr = getContentResolver();
                                    values.put(MediaStore.MediaColumns.DISPLAY_NAME, "image");
                                    values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg"); // You may need to adjust the MIME type based on your image type
                                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES+"/"+"Memorable" );
                                    Uri uri = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                                    try {
                                        OutputStream out = cr.openOutputStream(uri);
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                                    } catch (FileNotFoundException e) {
                                        throw new RuntimeException(e);
                                    }*/
                                    croppedImage = null;
                                    image = null;

                                    Toast.makeText(NodesActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    binding.progressBar.setVisibility(View.GONE);
                                    //dialog.dismiss();
                                    Toast.makeText(NodesActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            Toast.makeText(NodesActivity.this, "Please take a photo!", Toast.LENGTH_SHORT).show();
                        }

                    });

                    btnCancel.setOnClickListener(v1 -> {
                        dialog.dismiss();
                    });

                    btnAddImage.setOnClickListener(v1 -> {

                        activityResultLauncherPhoto.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));

                    });


                    dialog.show();


                });


    }

    private void addNodeToAlbum(ContentNode node, String albumKey) {
        DatabaseReference nodes = FirebaseDatabase.getInstance().getReference("albums/" + albumKey);
        Log.e("ViewPagerAdapter", "addNodeToAlbum: " + albumKey);
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

        if (mediaRecorder!=null){
            mediaRecorder.stop();
            mediaRecorder.release();
        }
        mediaRecorder = null;

        if (audioFile != null) {
            audioFile = null;
        }
    }


}
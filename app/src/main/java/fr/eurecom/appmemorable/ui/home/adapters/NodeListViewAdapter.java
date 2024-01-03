package fr.eurecom.appmemorable.ui.home.adapters;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.PopupMenu;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.List;

import fr.eurecom.appmemorable.R;
import fr.eurecom.appmemorable.databinding.AudioNodeBinding;
import fr.eurecom.appmemorable.databinding.ImageNodeBinding;
import fr.eurecom.appmemorable.databinding.TextNodeBinding;
import fr.eurecom.appmemorable.models.Album;
import fr.eurecom.appmemorable.models.AudioNode;
import fr.eurecom.appmemorable.models.ContentNode;
import fr.eurecom.appmemorable.models.ImageNode;
import fr.eurecom.appmemorable.models.TextNode;

public class NodeListViewAdapter extends ArrayAdapter<ContentNode> {

    public NodeListViewAdapter(@NonNull Context context, List<ContentNode> contentNodes) {
        super(context, 0, contentNodes);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ContentNode node = getItem(position);
        if (node instanceof TextNode) {
            TextNode textNode = (TextNode) node;
            TextNodeBinding textNodeBinding = TextNodeBinding.inflate(LayoutInflater.from(getContext()));
            initializeTextNode(textNode, textNodeBinding);
            convertView = textNodeBinding.getRoot();
        } else if (node instanceof ImageNode) {
            ImageNode imageNode = (ImageNode) node;
            ImageNodeBinding imageNodeBinding = ImageNodeBinding.inflate(LayoutInflater.from(getContext()));

            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("" + imageNode.getAlbum() + "/" + imageNode.getImage());
            //Log.e("ImageLoading", "StorageRef path: " + storageRef.getPath());
            Glide
                    .with(this.getContext())
                    .load(storageRef)
                    .centerCrop()
                    .into(imageNodeBinding.imageView);
            initializeImageNode(imageNode, imageNodeBinding);
            convertView = imageNodeBinding.getRoot();

        } else if (node instanceof AudioNode) {
            AudioNode audioNode = (AudioNode) node;
            AudioNodeBinding audioNodeBinding = AudioNodeBinding.inflate(LayoutInflater.from(getContext()));
            initializeAudioNode(audioNode, audioNodeBinding);
            convertView = audioNodeBinding.getRoot();
        }
        return convertView;
    }

    private void initializeImageNode(ImageNode imageNode, ImageNodeBinding imageNodeBinding) {
        imageNodeBinding.author.setText(imageNode.getUser().getName());
        imageNodeBinding.textView.setText(imageNode.getText());
        if (imageNode.getUser().getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            imageNodeBinding.imageView.setOnLongClickListener(v1 -> {
                PopupMenu popupMenu = new PopupMenu(getContext(), v1);
                popupMenu.inflate(R.menu.album_settings_menu); // Create a menu resource file (res/menu/album_settings_menu.xml)
                // Handle menu item clicks
                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.menu_item_edit) {
                        return true;
                    } else if (item.getItemId() == R.id.menu_item_delete) {
                        showDeleteConfirmationDialog(imageNode.getAlbum(), imageNode);
                        return true;
                    }
                    return false;
                });

                // Show the popup menu
                popupMenu.show();
                return false;
            });
        }
    }

    private void initializeTextNode(TextNode textNode, TextNodeBinding textNodeBinding) {
        textNodeBinding.textView.setText(textNode.getText());
        textNodeBinding.author.setText(textNode.getUser().getName());
        textNodeBinding.messageDate.setText(textNode.getMessageDate());
        if (textNode.getUser().getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            textNodeBinding.textView.setOnLongClickListener(v1 -> {
                PopupMenu popupMenu = new PopupMenu(getContext(), v1);
                popupMenu.inflate(R.menu.album_settings_menu); // Create a menu resource file (res/menu/album_settings_menu.xml)
                // Handle menu item clicks
                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.menu_item_edit) {
                        return true;
                    } else if (item.getItemId() == R.id.menu_item_delete) {
                        showDeleteConfirmationDialog(textNode.getAlbum(), textNode);
                        return true;
                    }
                    return false;
                });

                // Show the popup menu
                popupMenu.show();
                return false;
            });
        }
    }

    private void initializeAudioNode(AudioNode audioNode, AudioNodeBinding audioNodeBinding) {
        audioNodeBinding.audioDurationTextView.setText(audioNode.getDuration());
        audioNodeBinding.textView.setText(audioNode.getText());
        audioNodeBinding.author.setText(audioNode.getUser().getName());
        audioNodeBinding.messageDate.setText(audioNode.getMessageDate());

        // Create a handler to update the SeekBar progress

        Handler handler = new Handler();


        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("" + audioNode.getAlbum() + "/" + audioNode.getAudioUrl());
        File audioFile = new File( getContext().getExternalCacheDir().getAbsolutePath() + audioNode.getAudioUrl() );


        storageRef.getFile(audioFile).addOnSuccessListener(taskSnapshot -> {
            // File downloaded successfully, now set up MediaPlayer with the local file
            try {

                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(audioFile.getAbsolutePath());
                mediaPlayer.prepare();
                final int[] currentPosition = {0};

                Runnable updateProgressRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            // Get the current position and duration
                            currentPosition[0] += 500;
                            int totalDuration = mediaPlayer.getDuration();

                            // Calculate the progress percentage
                            int progress = (int) (((float) currentPosition[0] / totalDuration) * 100);


                            // Update the SeekBar progress
                            audioNodeBinding.progressBar.setProgress(progress);

                            // Schedule the next update after a delay (e.g., 500 milliseconds)
                            handler.postDelayed(this, 500);
                        }
                    }
                };

                mediaPlayer.setOnCompletionListener(mp -> {
                    // Update UI when playback is completed
                    handler.removeCallbacks(updateProgressRunnable);
                    currentPosition[0] = 0;
                    audioNodeBinding.playPauseButton.setImageResource(R.drawable.play_button_svgrepo_com);
                    audioNodeBinding.progressBar.setProgress(0);
                });


                audioNodeBinding.playPauseButton.setOnClickListener(v -> {
                    if (mediaPlayer.isPlaying()) {
                        // If MediaPlayer is playing, pause it
                        mediaPlayer.pause();
                        handler.removeCallbacks(updateProgressRunnable);

                        audioNodeBinding.playPauseButton.setImageResource(R.drawable.play_button_svgrepo_com);
                    } else {
                        // If MediaPlayer is not playing, start playing
                        mediaPlayer.start();

                        // Start updating the SeekBar progress
                        handler.post(updateProgressRunnable);
                        audioNodeBinding.playPauseButton.setImageResource(R.drawable.pause_button_svgrepo_com);
                    }
                });


                Log.e("AudioNode", "File download success");
                // Log.e("AudioNode", "initializeAudioNode : " + audioNode.getDuration());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).addOnFailureListener(exception -> {
            // Handle failed download
            Log.e("AudioNode", "File download failed: " + exception.getMessage());
        });



        //Log.e("AudioNode", "initializeAudioNode : " + audioNode.getDuration());
        if (audioNode.getUser().getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            audioNodeBinding.textView.setOnLongClickListener(v1 -> {
                PopupMenu popupMenu = new PopupMenu(getContext(), v1);
                popupMenu.inflate(R.menu.album_settings_menu); // Create a menu resource file (res/menu/album_settings_menu.xml)
                // Handle menu item clicks
                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.menu_item_edit) {
                        return true;
                    } else if (item.getItemId() == R.id.menu_item_delete) {
                        showDeleteConfirmationDialog(audioNode.getAlbum(), audioNode);
                        return true;
                    }
                    return false;
                });

                // Show the popup menu
                popupMenu.show();
                return false;
            });
        }
    }

    private void deleteNode(String albumId, ContentNode contentNode) {
        //TODO: delete the image/audio from cloud storage if it is an image/audio node
        if (contentNode instanceof ImageNode) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("" + albumId + "/" + ((ImageNode) contentNode).getImage());
            storageRef.delete();
        } else if (contentNode instanceof AudioNode) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("" + albumId + "/" + ((AudioNode) contentNode).getAudioUrl());
            storageRef.delete();
        }
        DatabaseReference node = FirebaseDatabase.getInstance().getReference("albums/" + albumId + "/" + contentNode.getId());
        node.removeValue();
    }

    private void showDeleteConfirmationDialog(String albumId, ContentNode contentNode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete Node")
                .setMessage("Are you sure you want to delete this node?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // User confirmed deletion, proceed with deletion
                    deleteNode(albumId, contentNode);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // User canceled the deletion, do nothing
                })
                .show();
    }
}

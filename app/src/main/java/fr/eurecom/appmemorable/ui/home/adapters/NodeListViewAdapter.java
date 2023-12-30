package fr.eurecom.appmemorable.ui.home.adapters;

import android.content.Context;
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

import java.util.List;

import fr.eurecom.appmemorable.R;
import fr.eurecom.appmemorable.databinding.ImageNodeBinding;
import fr.eurecom.appmemorable.databinding.TextNodeBinding;
import fr.eurecom.appmemorable.models.Album;
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
                        showDeleteConfirmationDialog(imageNode.getAlbum(), imageNode.getId());
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
                        showDeleteConfirmationDialog(textNode.getAlbum(), textNode.getId());
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

    private void deleteNode(String albumId, String nodeId) {
        //TODO: delete the image/audio from cloud storage if it is an image/audio node
        DatabaseReference node = FirebaseDatabase.getInstance().getReference("albums/" + albumId + "/" + nodeId);
        node.removeValue();
    }

    private void showDeleteConfirmationDialog(String albumId, String nodeId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete Node")
                .setMessage("Are you sure you want to delete this node?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // User confirmed deletion, proceed with deletion
                    deleteNode(albumId, nodeId);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // User canceled the deletion, do nothing
                })
                .show();
    }
}

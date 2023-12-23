package fr.eurecom.appmemorable.ui.home.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import fr.eurecom.appmemorable.databinding.ImageNodeBinding;
import fr.eurecom.appmemorable.databinding.TextNodeBinding;
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
        Log.e("NodeListViewAdapter", "getView: "+((TextNode) node).getText());
        if (node instanceof TextNode){
            TextNode textNode = (TextNode) node;
            TextNodeBinding textNodeBinding = TextNodeBinding.inflate(LayoutInflater.from(getContext()));
            textNodeBinding.textView.setText(textNode.getText());
            textNodeBinding.author.setText(textNode.getUser().getName());
            //TODO: add a dialog to ask for confirmation
            textNodeBinding.deleteButton.setOnClickListener(v1 -> this.deleteNode(node.getAlbum(), node.getId()));
            convertView = textNodeBinding.getRoot();
        }
        else if (node instanceof ImageNode){
            ImageNode imageNode = (ImageNode) node;
            ImageNodeBinding imageNodeBinding = ImageNodeBinding.inflate(LayoutInflater.from(getContext()));
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(""+ imageNode.getAlbum().toString() +"/"+ imageNode.getImage());
            //imageNodeBinding.imageView.setImageDrawable(AppCompatResources.getDrawable(getContext(),imageNode.getImage()));
            imageNodeBinding.author.setText(imageNode.getUser().getName());
            imageNodeBinding.textView.setText(imageNode.getText());
            imageNodeBinding.deleteButton.setOnClickListener(v1 -> this.deleteNode("albums/"+node.getAlbum()+"/"+node.getId(), node.getId()));
            convertView = imageNodeBinding.getRoot();

        }
        return convertView;
    }

    private void deleteNode(String albumId, String nodeId){
        DatabaseReference node = FirebaseDatabase.getInstance().getReference("albumNodes/"+albumId+"/"+nodeId);
        node.removeValue();
    }
}

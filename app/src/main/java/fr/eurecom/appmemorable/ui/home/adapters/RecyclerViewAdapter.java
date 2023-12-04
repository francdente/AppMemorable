package fr.eurecom.appmemorable.ui.home.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import fr.eurecom.appmemorable.R;
import fr.eurecom.appmemorable.databinding.ImageNodeBinding;
import fr.eurecom.appmemorable.databinding.RecyclerItemBinding;
import fr.eurecom.appmemorable.databinding.TextNodeBinding;
import fr.eurecom.appmemorable.models.ContentNode;
import fr.eurecom.appmemorable.models.ImageNode;
import fr.eurecom.appmemorable.models.TextNode;
import fr.eurecom.appmemorable.repository.MemorableRepository;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private List<ContentNode> nodes;
    private RecyclerItemBinding binding;

    public List<ContentNode> getNodes() {
        return nodes;
    }
    public void setNodes(List<ContentNode> nodes) {
        this.nodes = nodes;
        notifyDataSetChanged();
    }



    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout view;
        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            this.view = view.findViewById(R.id.linear_layout);
        }
        public void fillContent(ContentNode node){
            view.removeAllViews();
            View v = null;
            if (node instanceof TextNode){
                TextNode textNode = (TextNode) node;
                TextNodeBinding textNodeBinding = TextNodeBinding.inflate(LayoutInflater.from(itemView.getContext()));
                textNodeBinding.textView.setText(textNode.getText());
                textNodeBinding.author.setText(textNode.getUser().getName());
                //TODO: add a dialog to ask for confirmation
                textNodeBinding.deleteButton.setOnClickListener(v1 -> this.deleteNode(node.getAlbum(), node.getId()));
                v = textNodeBinding.getRoot();
            }
            else if (node instanceof ImageNode){
                ImageNode imageNode = (ImageNode) node;
                ImageNodeBinding imageNodeBinding = ImageNodeBinding.inflate(LayoutInflater.from(itemView.getContext()));
                imageNodeBinding.imageView.setImageDrawable(AppCompatResources.getDrawable(itemView.getContext(),imageNode.getImage()));
                imageNodeBinding.author.setText(imageNode.getUser().getName());
                imageNodeBinding.textView.setText(imageNode.getText());
                imageNodeBinding.deleteButton.setOnClickListener(v1 -> this.deleteNode("albums/"+node.getAlbum()+"/"+node.getId(), node.getId()));
                v = imageNodeBinding.getRoot();
            }
            view.addView(v);
        }

        private void deleteNode(String albumId, String nodeId){
            DatabaseReference node = FirebaseDatabase.getInstance().getReference("albumNodes/"+albumId+"/"+nodeId);
            node.removeValue();
        }
    }

    /**
     * Initialize the dataset of the Adapter
     *
     * @param nodes HashMap<String, ContentNode> containing the data to populate views to be used
     * by RecyclerView
     */
    public RecyclerViewAdapter(List<ContentNode> nodes) {
        this.nodes = nodes;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        binding = RecyclerItemBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        View view = binding.getRoot();
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        ContentNode node = nodes.get(position);
        viewHolder.fillContent(node);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return nodes.size();
    }
}

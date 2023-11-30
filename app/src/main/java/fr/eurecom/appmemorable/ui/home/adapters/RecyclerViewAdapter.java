package fr.eurecom.appmemorable.ui.home.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fr.eurecom.appmemorable.R;
import fr.eurecom.appmemorable.databinding.ImageNodeBinding;
import fr.eurecom.appmemorable.databinding.RecyclerItemBinding;
import fr.eurecom.appmemorable.databinding.TextNodeBinding;
import fr.eurecom.appmemorable.models.ContentNode;
import fr.eurecom.appmemorable.models.ImageNode;
import fr.eurecom.appmemorable.models.TextNode;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private final List<ContentNode> nodes;
    private RecyclerItemBinding binding;

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
                textNodeBinding.author.setText(textNode.getAuthor());
                v = textNodeBinding.getRoot();
            }
            else if (node instanceof ImageNode){
                ImageNode imageNode = (ImageNode) node;
                ImageNodeBinding imageNodeBinding = ImageNodeBinding.inflate(LayoutInflater.from(itemView.getContext()));
                imageNodeBinding.imageView.setImageDrawable(AppCompatResources.getDrawable(itemView.getContext(),imageNode.getImage()));
                imageNodeBinding.author.setText(imageNode.getAuthor());
                imageNodeBinding.textView.setText(imageNode.getText());
                v = imageNodeBinding.getRoot();
            }
            view.addView(v);
        }
    }

    /**
     * Initialize the dataset of the Adapter
     *
     * @param nodes List<ContentNode> containing the data to populate views to be used
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

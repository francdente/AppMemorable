package fr.eurecom.appmemorable.ui.home.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fr.eurecom.appmemorable.databinding.ViewPagerItemBinding;
import fr.eurecom.appmemorable.models.Album;
import fr.eurecom.appmemorable.models.TextNode;

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

        recyclerViewAdapter = new RecyclerViewAdapter(albums.get(position).getNodes());

        setFloatingButton(binding.getRoot(), position);
        binding.recyclerView.setAdapter(recyclerViewAdapter);

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
                    Toast.makeText(fragment.getContext(),"Audio added", Toast.LENGTH_SHORT).show();
                });
        binding.addImage.setOnClickListener(
                v -> Toast.makeText(fragment.getContext(), "Image Added", Toast.LENGTH_SHORT).show());
        binding.addText.setOnClickListener(
                v -> Toast.makeText(fragment.getContext(),"Text added", Toast.LENGTH_SHORT).show());
    }

    public void setAlbums(List<Album> albums){
        this.albums = albums;
    }
}


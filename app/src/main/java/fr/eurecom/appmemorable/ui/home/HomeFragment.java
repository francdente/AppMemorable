package fr.eurecom.appmemorable.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

import fr.eurecom.appmemorable.databinding.FragmentHomeBinding;
import fr.eurecom.appmemorable.models.Album;
import fr.eurecom.appmemorable.repository.MemorableRepository;
import fr.eurecom.appmemorable.ui.home.adapters.ViewPagerAdapter;

public class HomeFragment extends Fragment {
    MemorableRepository repository;
    private ViewPagerAdapter pagerAdapter;
    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e("HomeFragment", "called on create view");
        //Initialize data from viewModel and instantiate pagerAdapter
        repository = MemorableRepository.getInstance();
        pagerAdapter = new ViewPagerAdapter(this, repository.getAlbums().getValue());
        repository.getAlbums().observe(getViewLifecycleOwner(), new Observer<List<Album>>() {
            @Override
            public void onChanged(List<Album> albums) {
                pagerAdapter.setAlbums(albums);
                pagerAdapter.notifyDataSetChanged();
            }
        });

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //Set adapter for the pager
        binding.pager.setAdapter(pagerAdapter);



        return root;
    }

@Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
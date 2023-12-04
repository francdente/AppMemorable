package fr.eurecom.appmemorable.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import fr.eurecom.appmemorable.databinding.FragmentHomeBinding;
import fr.eurecom.appmemorable.models.Album;
import fr.eurecom.appmemorable.models.ConcreteAlbum;
import fr.eurecom.appmemorable.repository.MemorableRepository;
import fr.eurecom.appmemorable.ui.home.adapters.ViewPagerAdapter;

public class HomeFragment extends Fragment {
    private ViewPagerAdapter pagerAdapter;
    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pagerAdapter = new ViewPagerAdapter(this, new ArrayList<>());
        FirebaseDatabase.getInstance().getReference("userAlbums/"+ FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot userAlbumSnapshot) {
                        List<Album> albums = new ArrayList<>();
                        for (DataSnapshot albumSnapshot : userAlbumSnapshot.getChildren()) {
                            String albumId = albumSnapshot.getValue(String.class);
                            FirebaseDatabase.getInstance().getReference("albums/"+albumId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot albumSnapshot) {
                                    ConcreteAlbum concreteAlbum = albumSnapshot.getValue(ConcreteAlbum.class);
                                    if (concreteAlbum != null) {
                                        Album album = concreteAlbum.IntoAlbum();
                                        album.setId(albumSnapshot.getKey());
                                        albums.add(album);
                                    }
                                    pagerAdapter.setAlbums(albums);
                                    pagerAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    // Handle errors if needed
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        //Set adapter for the pager
        binding.pager.setAdapter(pagerAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
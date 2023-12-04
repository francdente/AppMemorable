package fr.eurecom.appmemorable.ui.friends;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;

import fr.eurecom.appmemorable.R;
import fr.eurecom.appmemorable.SignInActivity;
import fr.eurecom.appmemorable.databinding.FragmentFriendsBinding;
import fr.eurecom.appmemorable.models.Album;
import fr.eurecom.appmemorable.repository.MemorableRepository;

public class FriendsFragment extends Fragment {

private FragmentFriendsBinding binding;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentFriendsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        return root;
    }

@Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
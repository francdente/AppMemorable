package fr.eurecom.appmemorable.ui.Settings;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

import fr.eurecom.appmemorable.SignInActivity;
import fr.eurecom.appmemorable.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

private FragmentSettingsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        binding.loggedInAsText.setText("Logged in as: " + FirebaseAuth.getInstance().getCurrentUser().getEmail());
        binding.logoutButton.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("authPreferences", MODE_PRIVATE);
            sharedPreferences.edit().putBoolean("rememberMe", false).apply();
            sharedPreferences.edit().putString("email", "").apply();
            sharedPreferences.edit().putString("password", "").apply();
            Intent intent = new Intent(getActivity(), SignInActivity.class);
            //Clean all the stack of activities
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        });

        View root = binding.getRoot();
        return root;
    }

@Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
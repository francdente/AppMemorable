package fr.eurecom.appmemorable;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import fr.eurecom.appmemorable.databinding.ActivitySignInBinding;
import fr.eurecom.appmemorable.models.User;
import fr.eurecom.appmemorable.repository.MemorableRepository;

public class SignInActivity extends AppCompatActivity {

    MutableLiveData<Boolean> isSignedIn = new MutableLiveData<>();

    private void signIn(String email, String password) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        isSignedIn.setValue(true);
                    } else {
                        // If sign in fails, display a message to the user.
                    }
                });
    }
    private void signUp(String email, String password, String username) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        isSignedIn.setValue(true);
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
                        FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdates);
                        DatabaseReference users = FirebaseDatabase.getInstance().getReference("users");
                        String key = users.push().getKey();
                        users.child(key).setValue(new User(email, username, FirebaseAuth.getInstance().getCurrentUser().getUid()));

                    } else {
                        // If sign in fails, display a message to the user.
                    }
                });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySignInBinding binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        isSignedIn.setValue(false);

        binding.signUp.setOnClickListener(v -> {
            String email = ((TextView)findViewById(R.id.email)).getText().toString();
            String password = ((TextView)findViewById(R.id.password)).getText().toString();
            String username = ((TextView)findViewById(R.id.username)).getText().toString();
            this.signUp(email, password, username);
        });

        binding.signIn.setOnClickListener(v -> {
            String email = ((TextView)findViewById(R.id.email)).getText().toString();
            String password = ((TextView)findViewById(R.id.password)).getText().toString();
            this.signIn(email, password);
        });

        binding.toggleLoginMode.setOnClickListener(v -> {
            if (binding.toggleLoginMode.getText().toString().equals("Create a new account")) {
                binding.toggleLoginMode.setText("Login with existing account");
                binding.signUp.setVisibility(View.VISIBLE);
                binding.signIn.setVisibility(View.INVISIBLE);
                binding.username.setVisibility(View.VISIBLE);
            } else {
                binding.toggleLoginMode.setText("Create a new account");
                binding.signUp.setVisibility(View.INVISIBLE);
                binding.signIn.setVisibility(View.VISIBLE);
                binding.username.setVisibility(View.GONE);
            }
        });

        isSignedIn.observe(this, isSignedIn -> {
            if (isSignedIn) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {

            }
        });
    }
}

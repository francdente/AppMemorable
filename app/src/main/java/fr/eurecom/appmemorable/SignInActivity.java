package fr.eurecom.appmemorable;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;


import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import fr.eurecom.appmemorable.databinding.ActivitySignInBinding;
import fr.eurecom.appmemorable.models.User;

public class SignInActivity extends AppCompatActivity {

    MutableLiveData<Boolean> isSignedIn = new MutableLiveData<>();
    TextInputLayout email, password, username, confirmPassword;
    ActivitySignInBinding binding;
    private void signIn(String email, String password) {
        if (!validateEmail()) {
            return;
        }
        binding.progressBar.setVisibility(View.VISIBLE);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        isSignedIn.setValue(true);
                        this.password.setError(null);
                        this.email.setError(null);
                    } else {
                        // If sign in fails, display a message to the user.
                        binding.progressBar.setVisibility(View.GONE);
                        Exception exception = task.getException();
                        if (exception != null) {
                            if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                                this.password.setError("Wrong credentials");
                                this.password.setError("Wrong credentials");
                            }
                            else {
                                this.email.setError("Unknown error");
                                this.password.setError("Unknown error");
                            }
                        }
                    }
                });
    }
    private void signUp(String email, String password, String username, String s) {
        if (!validateEmail() | !validatePassword() | !validateUsername() | !validateConfirmPassword()) {
            return;
        }
        binding.progressBar.setVisibility(View.VISIBLE);
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
                        this.email.setError(null);
                        this.password.setError(null);
                        this.username.setError(null);
                    } else {
                        // If sign in fails, display a message to the user.
                        binding.progressBar.setVisibility(View.GONE);
                        Exception exception = task.getException();
                        if (exception != null) {
                            if (exception instanceof FirebaseAuthWeakPasswordException) {
                                this.password.setError(((FirebaseAuthWeakPasswordException) exception).getReason());
                            } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                                this.email.setError("Invalid email address");
                            } else if (exception instanceof FirebaseAuthUserCollisionException) {
                                this.email.setError("Email address already in use");
                            } else {
                                this.email.setError("Unknown error");
                                this.password.setError("Unknown error");
                                this.username.setError("Unknown error");
                            }
                        }
                    }
                });
    }

    private boolean validateEmail() {
        String emailInput = email.getEditText().getText().toString();
        if (emailInput.isEmpty()) {
            email.setError("Field can't be empty");
            return false;
        }
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            email.setError("Please enter a valid email address");
            return false;
        }
        else {
            email.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String passwordInput = password.getEditText().getText().toString();
        if (passwordInput.isEmpty()) {
            password.setError("Field can't be empty");
            return false;
        }
        else if (passwordInput.length() < 8) {
            password.setError("Password must be at least 8 characters");
            return false;
        }
        else if (!passwordInput.matches(".*[0-9].*")) {
            password.setError("Password must contain at least one digit");
            return false;
        }
        else if (!passwordInput.matches(".*[a-z].*")) {
            password.setError("Password must contain at least one lowercase letter");
            return false;
        }
        else if (!passwordInput.matches(".*[A-Z].*")) {
            password.setError("Password must contain at least one uppercase letter");
            return false;
        }
        else if (!passwordInput.matches(".*[!@#$%^&*()_+=|<>?{}\\[\\]~-].*")) {
            password.setError("Password must contain at least one special character");
            return false;
        }
        else {
            password.setError(null);
            return true;
        }
    }

    private boolean validateUsername() {
        String usernameInput = username.getEditText().getText().toString();
        if (usernameInput.isEmpty()) {
            username.setError("Field can't be empty");
            return false;
        }
        else if (usernameInput.length() < 4) {
            username.setError("Username must be at least 4 characters");
            return false;
        }
        else if (usernameInput.length() > 20) {
            username.setError("Username must be at most 20 characters");
            return false;
        }
        else if (usernameInput.matches(".*[!@#$%^&*()_+=|<>?{}\\[\\]~-].*")) {
            username.setError("Username must not contain any special character");
            return false;
        }
        else if (usernameInput.matches(".*[\\s].*")) {
            username.setError("Username must not contain any whitespace");
            return false;
        }
        else if (usernameInput.matches(".*[\\s].*")) {
            username.setError("Username must not contain any whitespace");
            return false;
        }
        else {
            username.setError(null);
            return true;
        }
    }

    private boolean validateConfirmPassword() {
        String confirmPasswordInput = confirmPassword.getEditText().getText().toString();
        String passwordInput = password.getEditText().getText().toString();
        if (confirmPasswordInput.isEmpty()) {
            confirmPassword.setError("Field can't be empty");
            return false;
        }
        else if (!confirmPasswordInput.equals(passwordInput)) {
            confirmPassword.setError("Passwords do not match");
            return false;
        }
        else {
            confirmPassword.setError(null);
            return true;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        isSignedIn.setValue(false);

        binding.signButton.setOnClickListener(v -> {
            if(binding.signButton.getText().toString().equals("Sign In")) {
                email = findViewById(R.id.email);
                password = findViewById(R.id.password);
                this.signIn(email.getEditText().getText().toString(), password.getEditText().getText().toString());
            } else {
                email = findViewById(R.id.email);
                password = findViewById(R.id.password);
                username = findViewById(R.id.username);
                confirmPassword = findViewById(R.id.confirm_password);
                this.signUp(email.getEditText().getText().toString(), password.getEditText().getText().toString(), username.getEditText().getText().toString(), confirmPassword.getEditText().getText().toString());
            }
        });

        binding.toggleLoginMode.setOnClickListener(v -> {
            if (binding.toggleLoginMode.getText().toString().equals("Sign Up")) {
                binding.toggleLoginModeText.setText("Already have an account?");
                binding.toggleLoginMode.setText("Sign In");
                binding.signButton.setText("Sign Up");
                binding.signTitle.setText("Sign Up");
                binding.username.setVisibility(View.VISIBLE);
                binding.username.getEditText().setText("");
                binding.email.getEditText().setText("");
                binding.password.getEditText().setText("");
                binding.confirmPassword.setVisibility(View.VISIBLE);
                binding.confirmPassword.getEditText().setText("");
                binding.username.setError(null);
                binding.email.setError(null);
                binding.password.setError(null);
                binding.confirmPassword.setError(null);
            } else {
                binding.toggleLoginModeText.setText("Don't have an account?");
                binding.toggleLoginMode.setText("Sign Up");
                binding.signButton.setText("Sign In");
                binding.signTitle.setText("Sign In");
                binding.username.setVisibility(View.GONE);
                binding.username.getEditText().setText("");
                binding.email.getEditText().setText("");
                binding.password.getEditText().setText("");
                binding.confirmPassword.setVisibility(View.GONE);
                binding.confirmPassword.getEditText().setText("");
                binding.username.setError(null);
                binding.email.setError(null);
                binding.password.setError(null);
                binding.confirmPassword.setError(null);
            }
        });

        isSignedIn.observe(this, isSignedIn -> {
            if (isSignedIn) {
                binding.progressBar.setVisibility(View.GONE);
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
            }
        });
    }
}

package fr.eurecom.appmemorable;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import fr.eurecom.appmemorable.databinding.ActivityForgotPasswordBinding;
import fr.eurecom.appmemorable.databinding.ActivitySignInBinding;

public class ForgotPasswordActivity extends AppCompatActivity {
    ActivityForgotPasswordBinding binding;
    TextInputLayout email;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        email = binding.email;
        binding.sendEmailButton.setOnClickListener(v -> {
            if (!validateEmail()) {
                return;
            }
            FirebaseAuth.getInstance().sendPasswordResetEmail(email.getEditText().getText().toString(), null)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            email.setError(null);
                            email.setErrorEnabled(false);
                            email.getEditText().setText("");
                            //Make toast
                            Toast.makeText(this, "Email sent", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(this, "Email not sent", Toast.LENGTH_SHORT).show();
                        }
                    });
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
}

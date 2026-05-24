package mher.minasyan.lexplain;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

public class SettingsFragment extends Fragment {

    private static final String PREFS_NAME = "lexplain_prefs";
    private static final String KEY_APP_LANG = "app_lang";

    private String[] languageNames;
    private String[] languageCodes;
    private String selectedLanguageCode;

    private FirebaseAuth mAuth;
    private EditText emailEdit, passwordEdit, emailinputreg, passwordinputreg;
    private Button registerBtn, loginBtn, btnLogout;
    private TextView tvUserEmail;

    // Карточки состояний интерфейса
    private CardView cardLogin;
    private CardView cardRegister;
    private CardView cardProfile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        languageNames = getResources().getStringArray(R.array.settings_language_names);
        languageCodes = getResources().getStringArray(R.array.settings_language_codes);

        mAuth = FirebaseAuth.getInstance();

        // Находим элементы управления языком
        Button btnChangeLang = view.findViewById(R.id.btnChangeLang);
        updateLanguageButtonText(btnChangeLang);
        btnChangeLang.setOnClickListener(v -> showLanguagePicker(btnChangeLang));

        // Находим карточки состояний
        cardLogin = view.findViewById(R.id.CardLogin);
        cardRegister = view.findViewById(R.id.CardReagistration);
        cardProfile = view.findViewById(R.id.CardProfile);

        // Элементы формы регистрации
        emailinputreg = view.findViewById(R.id.EmailInputReg);
        passwordinputreg = view.findViewById(R.id.PasswordInputReg);
        registerBtn = view.findViewById(R.id.btnRegister);

        // Элементы формы входа
        emailEdit = view.findViewById(R.id.emailInput);
        passwordEdit = view.findViewById(R.id.passwordInput);
        loginBtn = view.findViewById(R.id.btnLogin);

        // Элементы экрана профиля авторизованного пользователя
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Переключатели экранов внутри блоков авторизации
        TextView tvSwitchRegistration = view.findViewById(R.id.tvSwitchRegistration);
        TextView tvSwitchLogin = view.findViewById(R.id.tvSwitchLogin);

        tvSwitchRegistration.setOnClickListener(v -> {
            if (cardLogin.getVisibility() == View.VISIBLE) {
                cardLogin.setVisibility(View.GONE);
                cardRegister.setVisibility(View.VISIBLE);
            }
        });

        tvSwitchLogin.setOnClickListener(v -> {
            if (cardRegister.getVisibility() == View.VISIBLE) {
                cardLogin.setVisibility(View.VISIBLE);
                cardRegister.setVisibility(View.GONE);
            }
        });

        // Логика очистки истории
        view.findViewById(R.id.btnClearHistory).setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.settings_clear_history_title)
                        .setMessage(R.string.settings_clear_history_confirm)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(R.string.settings_clear_history_confirm_button, (dialog, which) -> {
                            clearAllHistory();
                            Toast.makeText(getContext(),
                                    R.string.settings_history_cleared,
                                    Toast.LENGTH_SHORT).show();
                        })
                        .show()
        );

        // Логика кнопки регистрации
        registerBtn.setOnClickListener(v -> {
            String email = emailinputreg.getText().toString().trim();
            String password = passwordinputreg.getText().toString().trim();
            Boolean isSavingAllowed = Boolean.TRUE;
            String accountstatus = "Standard";

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), R.string.settings_fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(requireContext(), R.string.settings_password_min_length, Toast.LENGTH_SHORT).show();
                return;
            }

            registerUser(email, password, isSavingAllowed, accountstatus);
        });

        // Логика кнопки входа
        loginBtn.setOnClickListener(v -> {
            String email = emailEdit.getText().toString().trim();
            String password = passwordEdit.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), R.string.settings_enter_email_password, Toast.LENGTH_SHORT).show();
                return;
            }

            loginUser(email, password);
        });

        // Логика кнопки выхода из аккаунта
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(getContext(), R.string.settings_logged_out, Toast.LENGTH_SHORT).show();
            updateUIState();
        });

        // Инициализируем начальное состояние экрана в зависимости от авторизации
        updateUIState();

        return view;
    }

    // Динамическое переключение видимости карточек
    private void updateUIState() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null && user.isEmailVerified()) {
            // Пользователь залогинен и верифицирован
            cardLogin.setVisibility(View.GONE);
            cardRegister.setVisibility(View.GONE);
            cardProfile.setVisibility(View.VISIBLE);

            tvUserEmail.setText(user.getEmail());
        } else {
            // Пользователь — гость
            cardProfile.setVisibility(View.GONE);
            cardLogin.setVisibility(View.VISIBLE);
            cardRegister.setVisibility(View.GONE);

            // Сбрасываем текст в полях, чтобы при выходе ничего не оставалось
            if (emailEdit != null) emailEdit.setText("");
            if (passwordEdit != null) passwordEdit.setText("");
            if (emailinputreg != null) emailinputreg.setText("");
            if (passwordinputreg != null) passwordinputreg.setText("");
        }
    }

    private void registerUser(String email, String password, Boolean isSavingAllowed, String accountstatus) {
        registerBtn.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        sendVerificationEmail(email, isSavingAllowed, accountstatus);
                    } else {
                        registerBtn.setEnabled(true);
                        String errorMessage = getString(R.string.settings_register_error);
                        if (task.getException() != null) {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                errorMessage = getString(R.string.settings_email_already_registered);
                            } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                errorMessage = getString(R.string.settings_invalid_email_format);
                            } else {
                                errorMessage = task.getException().getMessage();
                            }
                        }
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sendVerificationEmail(String email, Boolean isSavingAllowed, String accountstatus) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    registerBtn.setEnabled(true);
                    if (task.isSuccessful()) {
                        saveUserToFirestore(email, isSavingAllowed, accountstatus);
                        showVerificationDialog(email);
                    } else {
                        String error = task.getException() != null
                                ? task.getException().getMessage()
                                : getString(R.string.settings_verification_send_failed);
                        Toast.makeText(getContext(),
                                getString(R.string.settings_verification_error, error),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showVerificationDialog(String email) {
        if (getActivity() == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.settings_confirm_email_title)
                .setMessage(getString(R.string.settings_confirm_email_message, email))
                .setCancelable(false)
                .setPositiveButton(R.string.settings_confirm_email_done, (dialog, which) -> {
                    checkEmailVerificationStatus();
                })
                .setNegativeButton(R.string.settings_cancel_registration, (dialog, which) -> {
                    mAuth.signOut();
                    updateUIState();
                    Toast.makeText(getContext(), R.string.settings_registration_cancelled, Toast.LENGTH_LONG).show();
                })
                .show();
    }

    private void loginUser(String email, String password) {
        loginBtn.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        checkEmailVerificationStatus();
                    } else {
                        loginBtn.setEnabled(true);
                        String errorMessage = getString(R.string.settings_auth_error);
                        if (task.getException() != null) {
                            if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                errorMessage = getString(R.string.settings_user_not_found);
                            } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                errorMessage = getString(R.string.settings_wrong_password);
                            } else {
                                errorMessage = task.getException().getMessage();
                            }
                        }
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkEmailVerificationStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            loginBtn.setEnabled(true);
            return;
        }
        user.reload().addOnCompleteListener(task -> {
            loginBtn.setEnabled(true);

            if (task.isSuccessful()) {
                if (user.isEmailVerified()) {
                    Toast.makeText(getContext(), R.string.settings_login_success, Toast.LENGTH_SHORT).show();
                    // Меняем состояние интерфейса на "Авторизован" на лету
                    updateUIState();
                } else {
                    showResendVerificationDialog(user);
                }
            } else {
                Toast.makeText(getContext(), R.string.settings_account_status_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showResendVerificationDialog(FirebaseUser user) {
        mAuth.signOut();
        updateUIState();
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.settings_email_not_verified_title)
                .setMessage(R.string.settings_email_not_verified_message)
                .setPositiveButton(R.string.settings_resend_email, (dialog, which) -> {
                    user.sendEmailVerification().addOnCompleteListener(resendTask -> {
                        Toast.makeText(getContext(), R.string.settings_email_resent, Toast.LENGTH_SHORT).show();
                    });
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void saveUserToFirestore(String email, Boolean isSavingAllowed, String accountstatus) {
        String uid = mAuth.getUid();
        if (uid == null) return;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        java.util.Map<String, Object> user = new java.util.HashMap<>();
        user.put("email", email);
        user.put("isSavingAllowed", isSavingAllowed);
        user.put("accountStatus", accountstatus);
        user.put("createdAt", FieldValue.serverTimestamp());
        db.collection("users")
                .document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "User profile saved"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error saving user profile", e));
    }

    @Override
    public void onResume() {
        super.onResume();
        View view = getView();
        if (view != null) {
            Button btnChangeLang = view.findViewById(R.id.btnChangeLang);
            updateLanguageButtonText(btnChangeLang);
        }
    }

    private void showLanguagePicker(Button btnChangeLang) {
        int selectedIndex = getSelectedLanguageIndex();
        selectedLanguageCode = languageCodes[selectedIndex];

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.settings_language_dialog_title)
                .setSingleChoiceItems(languageNames, selectedIndex, (dialog, which) -> {
                    selectedLanguageCode = languageCodes[which];
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    if (selectedLanguageCode != null) {
                        saveLanguage(selectedLanguageCode);
                        applyLanguage(selectedLanguageCode);
                        updateLanguageButtonText(btnChangeLang);
                    }
                })
                .show();
    }

    private int getSelectedLanguageIndex() {
        String currentCode = getSavedLanguageCode();
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(currentCode)) return i;
        }
        return 0;
    }

    private String getSavedLanguageCode() {
        SharedPreferences preferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_APP_LANG, "ru");
    }

    private void saveLanguage(String languageCode) {
        SharedPreferences preferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putString(KEY_APP_LANG, languageCode).apply();
    }

    private void applyLanguage(String languageCode) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCode));
    }

    private void updateLanguageButtonText(Button button) {
        int index = getSelectedLanguageIndex();
        button.setText(getString(R.string.settings_language_button, languageNames[index]));
    }

    private void clearAllHistory() {
        ContractStorage.clear(requireContext());
    }
}
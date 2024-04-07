package com.ntlln.agame;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class AGAME extends AppCompatActivity {
    private GameDictionary gameDictionary;
    private String userUID, currentWord;
    private List<String> anagrams;
    private ImageButton backBTN;
    private TextView levelCounter;
    private int currentLevel = 1;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private DocumentReference userDocumentRef;
    private boolean gameJustLaunched = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agame);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        backBTN = findViewById(R.id.backButton);
        levelCounter = findViewById(R.id.levelCounter);

        backBTN.setOnClickListener(v -> {
            Intent intent = new Intent(AGAME.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        try {
            InputStream inputStream = getAssets().open("words.txt");
            gameDictionary = new GameDictionary(new InputStreamReader(inputStream));
        } catch (IOException e) {
            Toast.makeText(this, "Could not load dictionary", Toast.LENGTH_SHORT).show();
        }

        final EditText editText = findViewById(R.id.editTextText);
        editText.setRawInputType(InputType.TYPE_CLASS_TEXT);
        editText.setImeOptions(EditorInfo.IME_ACTION_GO);

        editText.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;

            if (actionId == EditorInfo.IME_ACTION_GO || (actionId == EditorInfo.IME_NULL && event != null && event.getAction() == KeyEvent.ACTION_DOWN)) {
                processWord(editText);
                handled = true;
            }
            return handled;
        });

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            userUID = currentUser.getUid();
            userDocumentRef = firestore.collection("users").document(userUID);
        }
        loadSavedLevel();
        defaultAction(findViewById(R.id.nextButton));
    }

    private void loadSavedLevel() {
        if (userDocumentRef != null) {
            userDocumentRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Long savedLevel = documentSnapshot.getLong("level");
                    if (savedLevel != null) {
                        currentLevel = savedLevel.intValue();
                        levelCounter.setText(getString(R.string.level_counter, currentLevel));
                        if (!gameJustLaunched) {
                            defaultAction(findViewById(R.id.nextButton));
                        }
                    }
                } else {
                    currentLevel = 1;
                    levelCounter.setText(getString(R.string.level_counter, currentLevel));

                    if (!gameJustLaunched) {
                        defaultAction(findViewById(R.id.nextButton));
                    }
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(AGAME.this, "Error loading level from Firestore", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void processWord(EditText editText) {
        TextView resultView = (TextView) findViewById(R.id.resultView);
        String word = editText.getText().toString().trim().toLowerCase();
        if (word.length() == 0) {
            return;
        }
        String color = "#CC0029";
        if (gameDictionary.isGoodWord(word, currentWord) && anagrams.contains(word)) {
            anagrams.remove(word);
            color = "#00AA29";
        } else {
            word = "X " + word;
        }
        resultView.append(Html.fromHtml(String.format("<font color=%s>%s</font><BR>", color, word)));
        editText.setText("");
    }

    public boolean defaultAction(View view) {
        TextView gameStatus = findViewById(R.id.anagramQuestion);
        EditText editText = findViewById(R.id.editTextText);
        TextView resultView = findViewById(R.id.resultView);

        if (currentWord == null) {
            currentWord = gameDictionary.pickGoodStarterWord();
            anagrams = gameDictionary.getAnagramsWithOneMoreLetter(currentWord);
            gameStatus.setText("Find as many words as possible that can be formed by adding one letter to " + currentWord.toUpperCase());
            resultView.setText(" ");
            editText.setText(" ");
            editText.setEnabled(true);
            editText.requestFocus();

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);

            if (userDocumentRef != null && currentWord == null) {
                userDocumentRef.update("level", currentLevel);
            }
        } else {
            editText.setText(currentWord);
            editText.setEnabled(false);
            currentWord = null;
            resultView.append(TextUtils.join(" , ", anagrams));
            currentLevel++;
            levelCounter.setText(getString(R.string.level_counter, currentLevel));

            if (userDocumentRef != null) {
                userDocumentRef.update("level", currentLevel);
            }
        }
        return true;
    }
}

package com.quartzy.redditclient;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.quartzy.redditclient.repo.RedditPost;
import com.quartzy.redditclient.repo.RedditService;
import com.quartzy.redditclient.ui.PostAdapter;
import com.quartzy.redditclient.ui.PostComparator;
import com.quartzy.redditclient.ui.SubredditViewModel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final String[] PARAM_ARRAY = new String[]{Param.HOT.name, Param.NEW.name, Param.TOP_DAY.name, Param.TOP_WEEK.name, Param.TOP_MONTH.name, Param.TOP_YEAR.name, Param.TOP_ALL.name};
    public static MainActivity INSTANCE;
    private static final Pattern pattern = Pattern.compile("[^\\d\\w]+", Pattern.DOTALL);

    private PostAdapter postAdapter;
    private boolean first = true;

    private AlertDialog currentDialog;

    private View contextView;

    public View getContextView() {
        return contextView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        INSTANCE = this;

        FloatingActionButton fabSearchSub = findViewById(R.id.fabSearch);
        FloatingActionButton fabSort = findViewById(R.id.fabSort);
        FloatingActionButton fabSettings = findViewById(R.id.fabSettings);

        RedditService redditService = new RedditService();

        SubredditViewModel model = new ViewModelProvider(this).get(SubredditViewModel.class);
        model.init(redditService);

        postAdapter = new PostAdapter(new PostComparator());
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        contextView = recyclerView;

        recyclerView.setHasFixedSize(false);
        recyclerView.setAdapter(postAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setEnabled(false);


        fabSearchSub.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setTitle("Enter the name of the subreddit");

            LinearLayout linearLayout = new LinearLayout(view.getContext());
            linearLayout.setOrientation(LinearLayout.VERTICAL);

            // Set up the input
            final EditText input = new EditText(linearLayout.getContext());
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            input.setImeOptions(EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING | EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_GO);

            linearLayout.addView(input);

            final ListView listView = new ListView(linearLayout.getContext());
            listView.post(() -> {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(listView.getLayoutParams());
                params.setMargins(10, 10, 10, 10);
                listView.setLayoutParams(params);
            });

            BookmarkManager.BookmarkAdapter adapter = new BookmarkManager.BookmarkAdapter(listView.getContext());
            listView.setAdapter(adapter);


            listView.setOnItemClickListener((adapterView, v, pos, id) -> {
                input.setText(String.valueOf(adapterView.getItemAtPosition(pos)));
            });
            listView.setOnItemLongClickListener((adapterView, v, pos, id) -> {
                Button button = new Button(linearLayout.getContext());
                final String s = String.valueOf(adapterView.getItemAtPosition(pos));
                button.setText("Remove " + s + "?");
                button.setOnClickListener(view1 -> {
                    BookmarkManager.INSTANCE.removeBookmark(linearLayout.getContext(), s);
                    adapter.refresh();
                    adapter.notifyDataSetChanged();
                    linearLayout.removeView(button);
                });
                linearLayout.addView(button);
                return true;
            });

            linearLayout.addView(listView);

            LinearLayout horizontalLayout = new LinearLayout(linearLayout.getContext());
            horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

            Button button = new Button(horizontalLayout.getContext());
            button.setText("Add");

            EditText editText = new EditText(horizontalLayout.getContext());
            button.setOnClickListener(view1 -> {
                BookmarkManager.INSTANCE.addBookmark(horizontalLayout.getContext(), editText.getText().toString());
                adapter.refresh();
                adapter.notifyDataSetChanged();
            });

            horizontalLayout.addView(button);
            horizontalLayout.addView(editText);

            linearLayout.post(() -> {
                ViewGroup.LayoutParams layoutParams = horizontalLayout.getLayoutParams();
                layoutParams.width = linearLayout.getWidth();
                horizontalLayout.setLayoutParams(layoutParams);
            });

            linearLayout.addView(horizontalLayout);


            builder.setView(linearLayout);

            // Set up the buttons
            builder.setPositiveButton("OK", (dialog, which) -> {
                String trim = input.getText().toString().trim();

                final Matcher matcher = pattern.matcher(trim);
                if (matcher.find()) {
                    Toast.makeText(fabSearchSub.getContext(), "Invalid subreddit name", Toast.LENGTH_LONG).show();
                    dialog.cancel();
                }
                StateHandler.CURRENT_SUBREDDIT = trim;
                recyclerView.setEnabled(true);
                if (first){
                    first = false;
                    model.getPosts().observe(this, pagingData ->
                            postAdapter.submitData(getLifecycle(), pagingData));
                }else{
//                    model.clear();
                    postAdapter.refresh();
                }
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            currentDialog = builder.show();
        });

        fabSort.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setTitle("Enter the name of the subreddit");



            final Spinner spinner = new Spinner(view.getContext());

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, PARAM_ARRAY);

            spinner.setAdapter(adapter);
            spinner.setSelection(StateHandler.CURRENT_PARAM.ordinal());
            builder.setView(spinner);

            // Set up the buttons
            builder.setPositiveButton("OK", (dialog, which) -> {
                Param value = Param.values()[spinner.getSelectedItemPosition()];
                if (value!=StateHandler.CURRENT_PARAM){
                    StateHandler.CURRENT_PARAM = value;
                    postAdapter.refresh();
                }
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            currentDialog = builder.show();
        });

        fabSettings.setOnClickListener(view -> {
            Intent intent = new Intent(this, ActivitySettings.class);
            startActivity(intent);
        });
    }

    public RedditPost getItemAt(int i){
        if (i<0) return null;
        if (i>=postAdapter.getItemCount())return null;
        return postAdapter.getItemAt(i);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (currentDialog!=null){
            currentDialog.dismiss();
            currentDialog = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (currentDialog!=null){
            currentDialog.dismiss();
            currentDialog = null;
        }
    }
}
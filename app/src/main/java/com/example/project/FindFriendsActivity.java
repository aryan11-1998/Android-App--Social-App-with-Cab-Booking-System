package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
//import android.widget.Toolbar;


public class FindFriendsActivity extends AppCompatActivity {

   private Toolbar mToolbar;
   private ImageButton SearchButton;
   private EditText SearchInputText; //msearchFireld
   private RecyclerView SearchResultList;

   SearchAdapter adapter;

   private DatabaseReference searchRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        searchRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar = (Toolbar) findViewById(R.id.find_friends_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Find Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SearchResultList = (RecyclerView) findViewById(R.id.search_result_list);
       // SearchResultList.setHasFixedSize(true);
        SearchResultList.setLayoutManager(new LinearLayoutManager(this));

        SearchButton = (ImageButton)findViewById(R.id.search_people_friends_button);
        SearchInputText = (EditText) findViewById(R.id.search_box_input);

        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String searchBoxInput = SearchInputText.getText().toString();
                SearchPeopleAndFriends(searchBoxInput);
            }
        });


    }//On create



    private void SearchPeopleAndFriends(String searchBoxInput) {
        Toast.makeText(this, "Searching...", Toast.LENGTH_SHORT).show();
        Query searchPeopleandFriendsQuery = searchRef.orderByChild("fullname").startAt(searchBoxInput).endAt(searchBoxInput +"\uf8ff");
        FirebaseRecyclerOptions<FindFriends> options =
                new FirebaseRecyclerOptions.Builder<FindFriends>()
                .setQuery(searchPeopleandFriendsQuery,FindFriends.class)
                .build();
        adapter = new SearchAdapter(options);
        adapter.startListening();
        SearchResultList.setAdapter(adapter);
    }





} // class FindFriendsActivity
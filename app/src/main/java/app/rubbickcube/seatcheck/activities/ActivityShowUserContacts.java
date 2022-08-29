package app.rubbickcube.seatcheck.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import app.rubbickcube.seatcheck.R;
import app.rubbickcube.seatcheck.contacts.Contact;
import app.rubbickcube.seatcheck.contacts.ContactFetcher;
import app.rubbickcube.seatcheck.contacts.ContactsAdapter;

public class ActivityShowUserContacts extends AppCompatActivity {


    ArrayList<Contact> listContacts;
    ArrayList<Contact> filterLitNotNull;
    ListView lvContacts;
    EditText _et_search;
    ImageView _search_cancel;
     ContactsAdapter adapterContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_user_contacts);

        getSupportActionBar().hide();



        TextView tv = findViewById(R.id.appbar_title);
        tv.setText("SeatCheck Contacts");
        ImageView img_back =findViewById(R.id.appbar_back);
        ImageView img_ico =findViewById(R.id.appbar_img);

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        img_ico.setVisibility(View.INVISIBLE);


        _et_search = findViewById(R.id.et_search);
        _search_cancel = findViewById(R.id.search_cancel);
        listContacts = new ContactFetcher(this).fetchAll();
        lvContacts = (ListView) findViewById(R.id.lvContacts);

        _search_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _et_search.setText("");
            }
        });

        filterLitNotNull = new ArrayList<>();

        for(Contact con : listContacts) {

            if(!con.name.equals("null")) {
               filterLitNotNull.add(con);
            }
        }

       adapterContacts = new ContactsAdapter(this, filterLitNotNull);
        lvContacts.setAdapter(adapterContacts);


        _et_search.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Call back the Adapter with current character to Filter

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                filterdList(s.toString());
            }
        });
    }

    private void filterdList(String charText) {
        ArrayList<Contact> filterList  = new ArrayList<>();
        for(Contact cn : filterLitNotNull) {

            if(cn.name.toLowerCase(Locale.getDefault()).startsWith(charText)) {
                filterList.add(cn);
            }
        }
        adapterContacts.filterdList(filterList);

    }





}

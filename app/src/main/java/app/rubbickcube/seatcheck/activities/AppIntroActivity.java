package app.rubbickcube.seatcheck.activities;

import android.content.Intent;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.pixplicity.easyprefs.library.Prefs;

import app.rubbickcube.seatcheck.MainActivity;
import app.rubbickcube.seatcheck.R;
import app.rubbickcube.seatcheck.fragments.AppIntroFragmentFive;
import app.rubbickcube.seatcheck.fragments.AppIntroFragmentFour;
import app.rubbickcube.seatcheck.fragments.AppIntroFragmentOne;
import app.rubbickcube.seatcheck.fragments.AppIntroFragmentSix;
import app.rubbickcube.seatcheck.fragments.AppIntroFragmentThree;
import app.rubbickcube.seatcheck.fragments.AppIntroFragmentTwo;
import me.relex.circleindicator.CircleIndicator;

public class AppIntroActivity extends AppCompatActivity {


    private ViewPager pager;
    private CircleIndicator indicator;
    private Boolean comingFromSetting = Boolean.FALSE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_app_intro);
        getSupportActionBar().hide();


        setupComponents();

    }

    private void setupComponents(){
        pager = (ViewPager) findViewById(R.id.pager);
        indicator = (CircleIndicator)findViewById(R.id.indicator);
        pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        indicator.setViewPager(pager);

        findViewById(R.id.btn_skip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(AppIntroActivity.this, MainActivity.class));
                finish();
                Prefs.putBoolean("introDone",true);

            }
        });
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            switch(pos) {

                case 0: return new AppIntroFragmentOne();
                case 1: return new AppIntroFragmentTwo();
                case 2: return new AppIntroFragmentThree();
                case 3: return new AppIntroFragmentFour();
                case 4: return new AppIntroFragmentFive();
                case 5: return new AppIntroFragmentSix();
                default: return new AppIntroFragmentOne();
            }
        }

        @Override
        public int getCount() {
            return 6;
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        startActivity(new Intent(AppIntroActivity.this, MainActivity.class));
        finish();
        Prefs.putBoolean("introDone",true);
    }
}

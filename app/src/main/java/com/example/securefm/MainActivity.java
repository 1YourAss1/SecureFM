package com.example.securefm;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        Bundle bundle = new Bundle();
        bundle.putString("PASSWORD", getIntent().getStringExtra("PASSWORD"));
        BrowseFragment bf = new BrowseFragment();
        bf.setArguments(bundle);
        adapter.addFragment(bf, "Browser");
        HomeFragment hf = new HomeFragment();
        hf.setArguments(bundle);
        adapter.addFragment(hf, "Home");
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, ChooseHomeDirActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    //Обработка "назад"
    @Override
    public void onBackPressed() {
        /*int cont = getSupportFragmentManager().getBackStackEntryCount();
        if (cont == 0) {
            super.onBackPressed();
        } else {
            getSupportFragmentManager().popBackStack();
        }*/
    }

}

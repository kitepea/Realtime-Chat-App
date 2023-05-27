package com.floki.chatapp;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;

import com.floki.chatapp.Adapter.MyViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        init();
        setUpViewPager();
    }
    private void setUpViewPager() {
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tabDots);

        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(new MyViewPagerAdapter(getSupportFragmentManager(), new Lifecycle() {
            @Override
            public void addObserver(@NonNull LifecycleObserver observer) {

            }
            @Override
            public void removeObserver(@NonNull LifecycleObserver observer) {

            }
            @NonNull
            @Override
            public State getCurrentState() {
                return null;
            }
        }));


        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0){
                tab.setText("Chat");
            }
            else
                tab.setText("People");
        }).attach();


    }
    private void init(){
        
    }

}
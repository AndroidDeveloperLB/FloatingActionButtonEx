package com.github.clans.fab.sample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.github.fab.sample.R;

import java.util.ArrayList;
import java.util.List;

public class FloatingMenusActivity extends AppCompatActivity {
    private Handler mUiHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.floating_menus_activity);

        final List<Pair<String, String>> items = new ArrayList<>();
        items.add(new Pair<>("https://github.com/AndroidDeveloperLB/FloatingActionButtonEx", "Repository website"));
        items.add(new Pair<>("https://github.com/Clans/FloatingActionButton", "Base Repository website"));
        items.add(new Pair<>("https://play.google.com/store/apps/developer?id=AndroidDeveloperLB", "All my apps"));
        items.add(new Pair<>("https://github.com/AndroidDeveloperLB?tab=repositories", "All my repositories"));
        final FloatingActionMenu floatingActionMenu = (FloatingActionMenu) findViewById(R.id.menu1);

        for (final Pair<String, String> item : items) {
            final FloatingActionButton menuItem = new FloatingActionButton(this);
            menuItem.setColorNormalResId(R.color.app_primary);
            menuItem.setColorPressedResId(R.color.app_primary_dark);
            menuItem.setColorRippleResId(R.color.app_primary_dark);
            menuItem.setImageResource(R.drawable.ic_star_border_black_24px);
            menuItem.setButtonSize(FloatingActionButton.SIZE_MINI);
            menuItem.setLabelText(item.second);
            menuItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    floatingActionMenu.close(true);
                    final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.first));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    startActivity(intent);
                }
            });
            floatingActionMenu.addMenuButton(menuItem);
        }
        floatingActionMenu.setOnMenuButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingActionMenu.toggle(true);
            }
        });
        findViewById(R.id.button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (floatingActionMenu.isMenuButtonHidden())
                    floatingActionMenu.showMenuButton(true);
                else floatingActionMenu.hideMenuButton(true);
            }
        });
        floatingActionMenu.setClosedOnTouchOutside(true);
    }

}

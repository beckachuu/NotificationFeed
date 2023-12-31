package com.beckachu.androidfeed.ui.home;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.beckachu.androidfeed.R;
import com.beckachu.androidfeed.data.SharedPrefsManager;
import com.beckachu.androidfeed.data.entities.NotiEntity;
import com.beckachu.androidfeed.data.models.NotiModel;
import com.beckachu.androidfeed.data.repositories.NotiRepository;
import com.beckachu.androidfeed.misc.Const;
import com.beckachu.androidfeed.misc.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NotiListAdapter extends RecyclerView.Adapter<NotiListViewHolder> {

    public static final HashMap<String, Drawable> iconCache = new HashMap<>();
    private Activity context;
    private final ArrayList<NotiModel> data = new ArrayList<>();

    private static String lastDate = "";
    private static NotiModel newestNoti = null;
    private boolean shouldLoadMore = true;
    private final String packageName;

    private final NotiRepository notiRepository;
    private final SharedPreferences sharedPrefs;

    NotiListAdapter(Activity context, String packageName) {
        this.context = context;

        this.notiRepository = new NotiRepository(context.getApplicationContext());
        this.sharedPrefs = context.getApplicationContext().getSharedPreferences(SharedPrefsManager.DEFAULT_NAME, Context.MODE_PRIVATE);

        this.packageName = packageName;

        loadMoreBeforeId(Const.NEGATIVE);
    }

    @NonNull
    @Override
    public NotiListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        /*
        Incorrect context: Make sure that the context variable passed to the Intent
        constructor is an instance of an Activity. Using a non-activity context such as
        the application context can cause issues when starting an activity or using startActivityForResult.

        Missing activity declaration: Make sure that the DetailsActivity is declared
        in your app’s AndroidManifest.xml file with the appropriate intent filters.

        Null or incorrect ID: Check if the id variable retrieved from the view’s tag
        is not null and contains a valid ID for your data. You can use logging or breakpoints to verify this.

        Unhandled exceptions: There may be unhandled exceptions occurring when you click
        on a RecyclerView item. Check your logcat output for any error messages or stack traces that could provide more information.

        */
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.noti_item, parent, false);
        NotiListViewHolder viewHolder = new NotiListViewHolder(view);
        viewHolder.item.setOnClickListener(v -> {
            if (viewHolder.extended) {
                viewHolder.extended = false;
                v.setActivated(false);
                viewHolder.title.setMaxLines(1);
                viewHolder.text.setVisibility(View.VISIBLE);
                viewHolder.preview.setVisibility(View.GONE);
            } else {
                viewHolder.extended = true;
                v.setActivated(true);
                viewHolder.title.setMaxLines(Integer.MAX_VALUE);
                viewHolder.text.setVisibility(View.GONE);
                viewHolder.preview.setVisibility(View.VISIBLE);
            }


//            String id = (String) v.getTag();
//            if (id != null) {
//                Intent intent = new Intent(context, DetailsActivity.class);
//                intent.putExtra(DetailsActivity.EXTRA_ID, id);
//                Pair<View, String> p1 = Pair.create(viewHolder.icon, "icon");
//                @SuppressWarnings("unchecked") ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(context, p1);
//                context.startActivityForResult(intent, 1, options.toBundle());
//            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull NotiListViewHolder viewHolder, int position) {

        if (position == 0) {
            SharedPrefsManager.putInt(sharedPrefs, SharedPrefsManager.UNREAD_COUNT, 0);
        }

        NotiModel notiModel = data.get(position);

        if (iconCache.containsKey(notiModel.getPackageName()) && iconCache.get(notiModel.getPackageName()) != null) {
            viewHolder.icon.setImageDrawable(iconCache.get(notiModel.getPackageName()));
        } else {
            viewHolder.icon.setImageResource(R.mipmap.ic_launcher);
        }

        viewHolder.item.setTag("" + notiModel.getId());
        viewHolder.title.setText(notiModel.getTitle());

        viewHolder.text.setVisibility(View.VISIBLE);
        viewHolder.text.setText(notiModel.getText());
        viewHolder.preview.setText(notiModel.getText());

//        if (notiModel.getPreview().length() == 0) {
//        } else {
//            viewHolder.text.setVisibility(View.GONE);
//            viewHolder.preview.setVisibility(View.VISIBLE);
//            viewHolder.preview.setText(notiModel.getPreview());
//        }

        if (notiModel.shouldShowDate()) {
            viewHolder.date.setVisibility(View.VISIBLE);
            viewHolder.date.setText(notiModel.getDate());
        } else {
            viewHolder.date.setVisibility(View.GONE);
        }

        if (position == getItemCount() - 1) {
            loadMoreBeforeId(notiModel.getId());
//            if (Const.DEBUG) System.out.println("Loading more at position " + position);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private void loadMoreBeforeId(long id) {
        if (!shouldLoadMore) {
            return;
        }

        int before = getItemCount();
        try {
            List<NotiEntity> olderNotis = notiRepository.getAllNotisOlderThanId(id, this.packageName);

            for (int i = 0; i < olderNotis.size(); i++) {
                NotiEntity notiEntity = olderNotis.get(i);
                NotiModel notiModel = new NotiModel(context, notiEntity, iconCache, Util.format, lastDate);
                lastDate = notiModel.getDate();
                data.add(notiModel);
            }

        } catch (Exception e) {
            if (Const.DEBUG) e.printStackTrace();
        }
        int after = getItemCount();

        if (before == after) {
            if (Const.DEBUG) System.out.println("Loaded all " + getItemCount() + " items");
            shouldLoadMore = false;
        }

        // TODO: what does this do?
        new Handler(Looper.getMainLooper()).post(this::notifyDataSetChanged);
    }

    public static NotiModel getNewestNoti() {
        return newestNoti;
    }

    public static void setNewestNoti(NotiModel newNotiModel) {
        newestNoti = newNotiModel;
    }

    public void addNewestNotiToAdapter() {
        if (newestNoti.getPackageName().equals(this.packageName) || this.packageName.equals(Const.ALL_NOTI)) {
            data.add(0, getNewestNoti());
            notifyItemInserted(0);
        }
    }

    public void setContext(Activity context) {
        this.context = context;
    }

    public static String getLastDate() {
        return lastDate;
    }

    public static void setLastDate(String newLastDate) {
        lastDate = newLastDate;
    }

    public static HashMap<String, Drawable> getIconCache() {
        return iconCache;
    }

}

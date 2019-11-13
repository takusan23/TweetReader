package io.github.takusan23.tweetreader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.net.Uri;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import twitter4j.MediaEntity;
import twitter4j.Status;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class StatusRecyclerViewAdapter extends RecyclerView.Adapter<StatusRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Status> itemList;
    private SharedPreferences pref_setting;

    private int imageViewWidth = 400;

    public StatusRecyclerViewAdapter(ArrayList<Status> list) {
        itemList = list;
    }

    @NonNull
    @Override
    public StatusRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.status_recycler_view_adapter_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view, parent.getContext());
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final StatusRecyclerViewAdapter.ViewHolder holder, int position) {
        //View設定
        final Status status = itemList.get(position);

        //TextViewに入れる
        String status_text = status.getText();
        String screenName = status.getUser().getScreenName();
        String name = status.getUser().getName();
        String profile_url = status.getUser().getProfileImageURLHttps();
        //時間
        Date time = status.getCreatedAt();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        String createAt = calendar.get(Calendar.MONTH) + 1 + "月" + calendar.get(Calendar.DATE) + "日" + calendar.get(Calendar.HOUR) + "時" + calendar.get(Calendar.MINUTE) + "分";

        holder.status_TextView.setText(status_text);
        holder.name_TextView.setText(name + "/ @" + screenName);
        holder.time_TextView.setText(createAt);


        //画像設定
        Glide.with(holder.profile_ImageView)
                .load(profile_url)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(30)))  //角を丸く
                .into(holder.profile_ImageView);

        //ImageViewに入れる幅を取得
        holder.media_LinearLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //取り出す
                imageViewWidth = holder.media_LinearLayout.getWidth() / 4;
            }
        });
        //添付メディア
        setMedia(holder, status);

        //ツイートのページを表示させる
        holder.open_status_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://twitter.com/" + status.getUser().getScreenName() + "/status/" + status.getId();
                lunchBrowser(url, holder.open_status_ImageView.getContext());
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView status_TextView;
        public TextView name_TextView;
        public TextView time_TextView;
        public ImageView profile_ImageView;
        public LinearLayout media_LinearLayout;
        public LinearLayout media_type_LinearLayout;
        public ImageView open_status_ImageView;

        public ViewHolder(@NonNull View itemView, Context context) {
            super(itemView);

            name_TextView = itemView.findViewById(R.id.recycler_view_name_textview);
            status_TextView = itemView.findViewById(R.id.recycler_view_status_textview);
            time_TextView = itemView.findViewById(R.id.recycler_view_time_textview);
            profile_ImageView = itemView.findViewById(R.id.recycler_view_profile_image_imageview);
            media_LinearLayout = itemView.findViewById(R.id.recycler_view_media_linearlayout);
            media_type_LinearLayout = itemView.findViewById(R.id.recycler_view_media_type_linearlayout);
            open_status_ImageView = itemView.findViewById(R.id.recycler_view_open_status_link);
        }
    }

    private void setMedia(final ViewHolder holder, final Status status) {
        //添付画像
        holder.media_LinearLayout.removeAllViews();
        holder.media_type_LinearLayout.removeAllViews();
        final Context context = holder.media_LinearLayout.getContext();
        ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(imageViewWidth, 400);
        //拡張for
        int pos = 0;
        for (final MediaEntity url : status.getMediaEntities()) {
            final ImageView imageView = new ImageView(context);
            imageView.setLayoutParams(layoutParams);
            //アニメーションできるように
            imageView.setTransitionName("photo");

            Glide.with(context)
                    .load(url.getMediaURLHttps())
                    .into(imageView);

            holder.media_LinearLayout.addView(imageView);

            //メディアタイプを表示できるように
            ImageView mediaTypeImageView = new ImageView(context);
            mediaTypeImageView.setPadding(5, 5, 5, 5);
            if (url.getType().contains("photo")) {
                mediaTypeImageView.setImageDrawable(context.getDrawable(R.drawable.ic_image));
            } else if (url.getType().contains("video")) {
                mediaTypeImageView.setImageDrawable(context.getDrawable(R.drawable.ic_movie));
            }
            holder.media_type_LinearLayout.addView(mediaTypeImageView);

            //画像を押した時に画像表示用ビューを出す。
            final int finalPos = pos;
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //クリック
                    if (url.getType().contains("photo")) {
                        //画像
                        Intent intent = new Intent(context, ImageViewActivity.class);
                        //画像のうらると位置を
                        ArrayList<String> imageLink = new ArrayList<>();
                        for (final MediaEntity url : status.getMediaEntities()) {
                            imageLink.add(url.getMediaURLHttps());
                        }
                        intent.putExtra("pos", finalPos);
                        intent.putStringArrayListExtra("images", imageLink);
                        intent.putExtra("status", status.getText());

                        ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation((AppCompatActivity) context, imageView, imageView.getTransitionName());
                        context.startActivity(intent, compat.toBundle());

                    } else if (url.getType().contains("video")) {
                        //動画
                        Intent intent = new Intent(context, VideoViewActivity.class);
                        intent.putExtra("url", url.getVideoVariants()[0].getUrl());
                        intent.putExtra("status", status.getText());
                        ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation((AppCompatActivity) context, imageView, imageView.getTransitionName());
                        context.startActivity(intent, compat.toBundle());
                    }
                }
            });
            pos++;
        }
    }

    private void lunchBrowser(String url, Context context) {
        //CustomTab起動
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setShowTitle(true);
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(context, Uri.parse(url));

    }

}

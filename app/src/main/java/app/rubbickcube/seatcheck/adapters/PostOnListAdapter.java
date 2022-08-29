package app.rubbickcube.seatcheck.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import app.rubbickcube.seatcheck.AppClass;
import app.rubbickcube.seatcheck.Helpers.Utils;
import app.rubbickcube.seatcheck.R;
import app.rubbickcube.seatcheck.activities.OtherUserProfileActivity;
import app.rubbickcube.seatcheck.activities.ProfileActivity;
import app.rubbickcube.seatcheck.model.Post;
import app.rubbickcube.seatcheck.model.Vicinity;


/**
 * Created by hp on 10/29/2016.
 */

public class PostOnListAdapter extends BaseAdapter {


    private Context context;
    private LayoutInflater inflater = null;
    public static List<Post> postList;
    BackendlessUser backendlessUser;

    RequestOptions options =  new RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.avatar)
            .error(R.drawable.avatar)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.HIGH);

    public PostOnListAdapter(Context context, List<Post> _post, BackendlessUser backendlessUser) {

        this.context = context;
        inflater = LayoutInflater.from(context);
        this.postList = _post;
        this.backendlessUser = backendlessUser;

        //AppClass.Companion.getAppComponent().inject(this);


    }
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    private static Date currentDate() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }

    public static String getTimeAgo(Date date) {
        long time = date.getTime();
        if (time < 1000000000000L) {
            time *= 1000;
        }

        long now = currentDate().getTime();
        if (time > now || time <= 0) {
            return "in the future";
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "moments ago";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 60 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 2 * HOUR_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }
    @Override
    public int getCount() {
        return postList.size();
    }

    @Override
    public Object getItem(int i) {
        return postList.size();
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View convertview, ViewGroup viewGroup) {


        ViewHolder holder;
        if(convertview == null) {

            convertview =inflater.inflate(R.layout.available_seat_list_item,null);
            holder = new ViewHolder();
            holder._postaddress = convertview.findViewById(R.id.post_location_desc);
            holder._postlocation = convertview.findViewById(R.id.post_location_name);
            holder._postseat = convertview.findViewById(R.id.post_seats);
            holder._post_name = convertview.findViewById(R.id.post_name);
            holder._user_dp = convertview.findViewById(R.id.user_dp);
            holder._img_restaurant = convertview.findViewById(R.id.img_restaurant);
            holder._img_time = convertview.findViewById(R.id.img_time);
            holder._intrestedoption = convertview.findViewById(R.id.optionintrested);
            holder._timecreated = convertview.findViewById(R.id.timecreated);
            convertview.setTag(holder);

        } else {
            holder = (ViewHolder) convertview.getTag();
        }



        if(postList.get(i).getShouldGoLive().equals("yes")) {
            holder._postseat.setText(postList.get(i).getQuantity() + " Seat(s)");

            if (postList.get(i).getInterestOption() != null){
                holder._timecreated.setText(getTimeAgo(postList.get(i).getCreated()));
            }

            if (postList.get(i).getCreated() != null){
                holder._intrestedoption.setText(postList.get(i).getInterestOption());
            }

            if(postList.get(i).getResturantName() != null) {
                holder._postlocation.setText(postList.get(i).getResturantName());
            }

            if(postList.get(i).getResturantAddress() != null) {
                holder._postaddress.setText(postList.get(i).getResturantAddress());


            }
            if(postList.get(i).getUser().getProperties().get("name") != null) {
                holder._post_name.setText(postList.get(i).getUser().getProperties().get("name").toString());

            }
            if(postList.get(i).getUser().getProperties().get("profileImage") != null) {

                Glide.with(context).load(postList.get(i).getUser().getProperties().get("profileImage")).apply(options).into(holder._user_dp);

            }

            Glide.with(context).load(R.drawable.nd_restaurant_location).apply(options).into(holder._img_restaurant);
            Glide.with(context).load(R.drawable.nd_time_duration).apply(options).into(holder._img_time);


        }



        holder._user_dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(postList.get(i).getUser().getObjectId().equals(backendlessUser.getObjectId())) {
                    AppClass.Companion.setReviewForOwner(true);

                    context.startActivity(new Intent(context, ProfileActivity.class));

                }else {
                    AppClass.Companion.setReviewForOwner(false);
                    Intent intent = new Intent(context, OtherUserProfileActivity.class);
                    if(postList.get(i).getUser().getProperties().get("profileImage") != null) {
                        intent.putExtra("otherUserProfileImage",postList.get(i).getUser().getProperties().get("profileImage").toString());
                    }
                    intent.putExtra("otherUserId",postList.get(i).getUser().getObjectId());
                    intent.putExtra("otherUserName",postList.get(i).getUser().getProperties().get("name").toString());
                    context.startActivity(intent);
                }


            }
        });




        return convertview;
    }

        public class ViewHolder {

        TextView _postaddress;
        TextView _postlocation;
        TextView _postseat;
        TextView _post_name;
        ImageView _user_dp;
        ImageView _img_time;
        ImageView _img_restaurant;
        TextView _intrestedoption;
        TextView _timecreated;

        }


}

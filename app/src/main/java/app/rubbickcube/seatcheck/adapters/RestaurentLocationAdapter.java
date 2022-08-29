package app.rubbickcube.seatcheck.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import app.rubbickcube.seatcheck.R;
import app.rubbickcube.seatcheck.model.Vicinity;


/**
 * Created by hp on 10/29/2016.
 */

public class RestaurentLocationAdapter extends BaseAdapter {


    private Context context;
    private LayoutInflater inflater = null;
    public static List<Vicinity> vicinityList;
    public static List<Vicinity> vicinityListFiltered;

    public RestaurentLocationAdapter(Context context, List<Vicinity> _vicinity) {

        this.context = context;
        inflater = LayoutInflater.from(context);
        this.vicinityList = _vicinity;


    }

    @Override
    public int getCount() {
        return vicinityList.size();
    }

    @Override
    public Object getItem(int i) {
        return vicinityList.size();
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertview, ViewGroup viewGroup) {


        Vicinity vicinity = vicinityList.get(i);
        ViewHolder holder;
        if(convertview == null) {

            convertview =inflater.inflate(R.layout.restaurent_list_item,null);
            holder = new ViewHolder();
            holder._bankname = (TextView) convertview.findViewById(R.id.name);
            holder._bankaddress = (TextView) convertview.findViewById(R.id.name_details);

            convertview.setTag(holder);

        } else {
            holder = (ViewHolder) convertview.getTag();
        }


          holder._bankname.setText(vicinity.getName());
            holder._bankaddress.setText(vicinity.getVicinity());

        return convertview;
    }

        public class ViewHolder {

            TextView _bankname;
            TextView _bankaddress;
        }
}

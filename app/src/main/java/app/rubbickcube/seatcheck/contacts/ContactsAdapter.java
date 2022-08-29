package app.rubbickcube.seatcheck.contacts;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import app.rubbickcube.seatcheck.R;
import app.rubbickcube.seatcheck.adapters.PostOnListAdapter;
import app.rubbickcube.seatcheck.model.Post;

public class ContactsAdapter extends BaseAdapter {


	private Context mContext;
	private LayoutInflater inflater;
	public static ArrayList<Contact> contactArrayList;
	//public static ArrayList<Contact> filteredList;

	public ContactsAdapter(Context context, ArrayList<Contact> contacts) {

		contactArrayList = contacts;
		//filteredList = contacts;
		mContext = context;
		inflater = LayoutInflater.from(mContext);
	}

	@Override
	public int getCount() {
		return contactArrayList.size();
	}

	@Override
	public Object getItem(int i) {
		return i;
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(int position, View convertview, ViewGroup parent) {

		final Contact contact = contactArrayList.get(position);

		final ViewHolder holder;
		if(convertview == null) {

			convertview =inflater.inflate(R.layout.adapter_contact_item,null);
			holder = new ViewHolder();
			 holder._tvName = (TextView) convertview.findViewById(R.id.tvName);
			holder._tvEmail = (TextView) convertview.findViewById(R.id.tvEmail);
			holder._tvPhone = (TextView) convertview.findViewById(R.id.tvPhone);
			holder._invite = (Button) convertview.findViewById(R.id.btn_invites);

			convertview.setTag(holder);

		} else {
			holder = (ViewHolder) convertview.getTag();
		}


		holder._tvName.setText(contact.name);
		holder._tvEmail.setText("");
		holder._tvPhone.setText("");
		if (contact.emails.size() > 0 && contact.emails.get(0) != null) {
			holder._tvEmail.setText(contact.emails.get(0).address);
		}
		if (contact.numbers.size() > 0 && contact.numbers.get(0) != null) {
			holder._tvPhone.setText(contact.numbers.get(0).number);
		}

		holder._invite.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				if (contact.numbers.size() > 0 && contact.numbers.get(0) != null) {
					String phone = contact.numbers.get(0).number;
					Intent smsIntent = new Intent(Intent.ACTION_VIEW);
					smsIntent.setType("vnd.android-dir/mms-sms");
					smsIntent.putExtra("address", phone);
					smsIntent.putExtra("sms_body","Hi! check out Seatcheck app ,its a great app to use. https://play.google.com/store/apps/details?id=app.rubbickcube.seatcheck");
					mContext.startActivity(smsIntent);
				}
			}
		});



		return convertview;
	}

//	@Override
//	public Filter getFilter() {
//		final Filter filter = new Filter() {
//
//			@SuppressWarnings("unchecked")
//			@Override
//			protected void publishResults(CharSequence constraint,FilterResults results) {
//
//				filteredList = (ArrayList<Contact>) results.values; // has the filtered values
//				notifyDataSetChanged();  // notifies the data with new filtered values
//			}
//
//			@Override
//			protected FilterResults performFiltering(CharSequence constraint) {
//				FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
//				ArrayList<Contact> FilteredArrList = new ArrayList<Contact>();
//
//				if (contactArrayList == null) {
//					contactArrayList = new ArrayList<Contact>(filteredList); // saves the original data in mOriginalValues
//				}
//
//				/********
//				 *
//				 *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
//				 *  else does the Filtering and returns FilteredArrList(Filtered)
//				 *
//				 ********/
//				if (constraint == null || constraint.length() == 0) {
//
//					// set the Original result to return
//					results.count = contactArrayList.size();
//					results.values = contactArrayList;
//				} else {
//					constraint = constraint.toString().toLowerCase();
//					for (int i = 0; i < contactArrayList.size(); i++) {
//						String data = contactArrayList.get(i).name;
//						if (data.toLowerCase().contains(constraint.toString())) {
//							FilteredArrList.add(new Contact(contactArrayList.get(i).name,contactArrayList.get(i).name));
//						}
//					}
//					// set the Filtered result to return
//					results.count = FilteredArrList.size();
//					results.values = FilteredArrList;
//				}
//				return results;
//			}
//		};
//		return filter;
//	}

	public class ViewHolder {

		TextView _tvName;
		TextView _tvEmail;
		TextView _tvPhone;
		Button _invite;


	}





	// Filter Class
	public void filterdList(ArrayList<Contact> _list) {
		contactArrayList = _list;
		notifyDataSetChanged();
	}



}

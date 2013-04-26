package charlie.marshall.pfsense;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ServicesArrayAdapter extends BaseAdapter
{
	private static ArrayList<Services> services;
	private LayoutInflater mInflater;
 
	public ServicesArrayAdapter(Context context, ArrayList<Services> services)
	{
		mInflater = LayoutInflater.from(context);
		this.services = services;
	}
	
	@Override
	public int getCount() {
		return services.size();
	}

	@Override
	public Object getItem(int position) {
		return services.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
 
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_services, null);
			holder = new ViewHolder();
			holder.txtTitle = (TextView) convertView.findViewById(R.id.label);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if(services.get(position).getStatus().equals("Running"))
			holder.txtTitle.setTextColor(Color.parseColor("green"));
		else
			holder.txtTitle.setTextColor(Color.parseColor("red"));
		holder.txtTitle.setText(services.get(position).getName());

		return convertView;
	}

	static class ViewHolder {
		TextView txtTitle;

	}
}

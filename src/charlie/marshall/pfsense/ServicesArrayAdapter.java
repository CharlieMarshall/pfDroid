package charlie.marshall.pfsense;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class ServicesArrayAdapter extends BaseAdapter
{
	private ArrayList<Services> services;
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
			
			holder.serviceTitle = (TextView) convertView.findViewById(R.id.serviceName);
			holder.serviceDesc = (TextView) convertView.findViewById(R.id.serviceDesc);
			holder.serviceStop = (Button) convertView.findViewById(R.id.stopService);
			holder.serviceStart = (Button) convertView.findViewById(R.id.startService);

			// uncomment to make the listview clickable
			
			// holder.serviceStop.setFocusable(false);
			// holder.serviceStop.setClickable(false);
			// holder.serviceStart.setFocusable(false);
			// holder.serviceStart.setClickable(false);

			convertView.setTag(holder);
		}
		else
			holder = (ViewHolder) convertView.getTag();

		if(services.get(position).getStatus().equals("Running"))
		{
			holder.serviceTitle.setTextColor(Color.parseColor("green"));
			holder.serviceStop.setVisibility(View.VISIBLE);
			holder.serviceStart.setText("Restart");
		}
		else
		{
			holder.serviceStart.setText("Start");
			holder.serviceTitle.setTextColor(Color.parseColor("red"));
		}

		holder.serviceTitle.setText(services.get(position).getName());
		holder.serviceDesc.setText(services.get(position).getDesc());

		return convertView;
	}

	static class ViewHolder {
		TextView serviceTitle;
		TextView serviceDesc;
		Button serviceStop;
		Button serviceStart;
	}
}

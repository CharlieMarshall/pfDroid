package charlie.marshall.pfsense;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ArpArrayAdapter extends BaseAdapter
{
	private ArrayList<Arp> arps;
	private LayoutInflater mInflater;

	public ArpArrayAdapter(Context context, ArrayList<Arp> arps)
	{
		mInflater = LayoutInflater.from(context);
		this.arps = arps;
	}

	@Override
	public int getCount() {
		return arps.size();
	}

	@Override
	public Object getItem(int position) {
		return arps.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_arps, null);
			holder = new ViewHolder();

			holder.arpIp = (TextView) convertView.findViewById(R.id.arpIp);
			holder.arpInterface = (TextView) convertView.findViewById(R.id.arpInterface);
			holder.arpMac = (TextView) convertView.findViewById(R.id.arpMac);
			holder.arpHostName = (TextView) convertView.findViewById(R.id.arpHostName);

			convertView.setTag(holder);
		}
		else
			holder = (ViewHolder) convertView.getTag();

		holder.arpIp.setText(arps.get(position).getIP());
		holder.arpInterface.setText(arps.get(position).getInterface());
		holder.arpMac.setText(arps.get(position).getMac());
		holder.arpHostName.setText(arps.get(position).getHostname());

		return convertView;
	}

	static class ViewHolder {
		TextView arpIp;
		TextView arpInterface;
		TextView arpMac;
		TextView arpHostName;
	}
}

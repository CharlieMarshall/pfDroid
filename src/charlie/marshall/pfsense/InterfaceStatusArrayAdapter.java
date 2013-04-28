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

public class InterfaceStatusArrayAdapter extends BaseAdapter
{
	private ArrayList<InterfaceStatusInfo> interfaces;
	private LayoutInflater mInflater;

	public InterfaceStatusArrayAdapter(Context context, ArrayList<InterfaceStatusInfo> interfaces)
	{
		mInflater = LayoutInflater.from(context);
		this.interfaces = interfaces;
	}

	@Override
	public int getCount() {
		return interfaces.size();
	}

	@Override
	public Object getItem(int position) {
		return interfaces.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_interface_status, null);
			holder = new ViewHolder();
			
			holder.interfaceName = (TextView) convertView.findViewById(R.id.interfaceName);
			holder.interfaceStatus = (TextView) convertView.findViewById(R.id.interfaceStatus);
			holder.interfaceStatusBtn = (Button) convertView.findViewById(R.id.interfaceStatusBtn);

			// uncomment to make the listview clickable
			
			// holder.serviceStop.setFocusable(false);
			// holder.serviceStop.setClickable(false);
			// holder.serviceStart.setFocusable(false);
			// holder.serviceStart.setClickable(false);

			convertView.setTag(holder);
		}
		else
			holder = (ViewHolder) convertView.getTag();
		
		if(interfaces.get(position).getStatus()==true)
			holder.interfaceName.setTextColor(Color.parseColor("green"));			
		else
			holder.interfaceName.setTextColor(Color.parseColor("red"));

		if(interfaces.get(position).hasButton()==true)
		{			
			if(interfaces.get(position).getTypeValue().equals("up"))
				holder.interfaceStatusBtn.setText("Disconnect");
			else if(interfaces.get(position).getTypeValue().equals("down"))
				holder.interfaceStatusBtn.setText("Connect");
		}
		else
			holder.interfaceStatusBtn.setVisibility(View.INVISIBLE);

		holder.interfaceName.setText(interfaces.get(position).getHeader());
		holder.interfaceStatus.setText(interfaces.get(position).getStatusStr());

		return convertView;
	}

	static class ViewHolder {
		TextView interfaceName;
		TextView interfaceStatus;
		Button interfaceStatusBtn;
	}
}

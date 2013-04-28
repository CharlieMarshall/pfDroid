package charlie.marshall.pfsense;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class ViewInterfaceStatusActivity extends CustomActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_interfaces);

		// get data from intent
		Intent i = getIntent();
		InterfaceStatusInfo interfaceInfo = (InterfaceStatusInfo)i.getSerializableExtra("interface");
		
		setTitle(interfaceInfo.getHeader());
		
		drawInterface(interfaceInfo);
	}


	public void drawInterface(InterfaceStatusInfo interfaceInfo)
	{
		// TODO need to improve UI
		// Currently ISP DNS servers are going onto new lines
		
		TextView nameTv = (TextView) findViewById(R.id.name);
		TextView valueTv = (TextView) findViewById(R.id.value);
		
		String name = "";
		String value = "";
		
		for(int i=0; i<interfaceInfo.getSize(); i++)
		{
			name += interfaceInfo.getHeader(i) + "\n";
			value += interfaceInfo.getValue(i) + "\n";
		}
		
		nameTv.setText(name);
		valueTv.setText(value);
	}

}

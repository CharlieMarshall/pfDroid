package charlie.marshall.pfsense;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

// TODO check MAC addresses are correctly formatted
// TODO check descriptions are not empty - we cant have an empty client

public class ViewArpActivity extends CustomActivity
{
	private Arp arp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_arp);

		Intent i = getIntent();
		arp = (Arp)i.getSerializableExtra("arp");
		draw(arp);
	}

	public void draw(Arp arp)
	{
		TextView ip = (TextView) findViewById(R.id.ip);
		TextView mac = (TextView) findViewById(R.id.mac);
		TextView host = (TextView) findViewById(R.id.host);
		TextView interf = (TextView) findViewById(R.id.interf);
		
		ip.setText(arp.getIP());
		mac.setText(arp.getMac());
		host.setText(arp.getHostname());
		interf.setText(arp.getInterface());
	}

}
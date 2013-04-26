package charlie.marshall.pfsense;

import java.net.URL;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class InterfacesActivity extends CustomActivity
{
	private Pfsense pf;
	private SubDrop sd;
	private int menu, subDrop;

	// variables scraped from page
	private boolean interfaceStatus = false;
	private int selectedType, selectedSpeed;
	private ArrayList<String> typeStore, speedStore; 
	private String description = "";
	private String mac = "";
	private String mtu = "";
	private String mss = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_interfaces);

		// get data from intent
		Intent i = getIntent();
		pf = (Pfsense)i.getSerializableExtra("pf");
		menu = i.getIntExtra("menu", 0);
		subDrop = i.getIntExtra("subDrop", 0);

		sd = pf.getSubDrops(menu);
		new PfFetch().execute(sd.getURL(subDrop));
	}

	/*
	 * Method to draw the interface
	 */

	public void drawInterface()
	{				

		CheckBox interf = (CheckBox) findViewById(R.id.enableInterface);

		if(interfaceStatus == true)
		{
			interf.setChecked(true); // tick the checkbox if interface is enabled

			// get reference to the general config elements
			Spinner typeSpinner = (Spinner) findViewById(R.id.type);  			
			Spinner speedSpinner = (Spinner) findViewById(R.id.speed);  

			EditText descET = (EditText) findViewById(R.id.desc);
			EditText macET = (EditText) findViewById(R.id.macAddress);
			EditText mtuET = (EditText) findViewById(R.id.mtu);
			EditText mssET = (EditText) findViewById(R.id.mss);

			TextView descLabel = (TextView) findViewById(R.id.descLabel);
			TextView typeLabel = (TextView) findViewById(R.id.typeLabel);
			TextView macLabel = (TextView) findViewById(R.id.macLabel);
			TextView mtuLabel = (TextView) findViewById(R.id.mtuLabel);
			TextView mssLabel = (TextView) findViewById(R.id.mssLabel);
			TextView speedLabel = (TextView) findViewById(R.id.speedLabel);

			CheckBox privNet = (CheckBox) findViewById(R.id.privateNetworks);
			CheckBox bogonNet = (CheckBox) findViewById(R.id.bogonNetworks);

			// show the interface elements
			typeSpinner.setVisibility(View.VISIBLE);
			macET.setVisibility(View.VISIBLE);
			mtuET.setVisibility(View.VISIBLE);
			descET.setVisibility(View.VISIBLE);
			mssET.setVisibility(View.VISIBLE);
			speedSpinner.setVisibility(View.VISIBLE);
			descLabel.setVisibility(View.VISIBLE);
			typeLabel.setVisibility(View.VISIBLE);
			macLabel.setVisibility(View.VISIBLE);
			mtuLabel.setVisibility(View.VISIBLE);
			mssLabel.setVisibility(View.VISIBLE);
			speedLabel.setVisibility(View.VISIBLE);
			privNet.setVisibility(View.VISIBLE);
			bogonNet.setVisibility(View.VISIBLE);

			// bind to the ArrayList
			ArrayAdapter<String> typeArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, typeStore);
			typeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			typeSpinner.setAdapter(typeArrayAdapter);
			typeSpinner.setSelection(selectedType);
			// bind to the ArrayList
			ArrayAdapter<String> speedArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, speedStore);
			speedArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			speedSpinner.setAdapter(speedArrayAdapter);
			speedSpinner.setSelection(selectedType);

			// fill the editText fields
			descET.setText(description);
			macET.setText(mac);
			mtuET.setText(mtu);
			mssET.setText(mss);

			// bind to the ArrayList (needs a toString method in the SectorList class)
			ArrayAdapter<String> spinnerSpeedArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, speedStore);
			spinnerSpeedArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			speedSpinner.setAdapter(spinnerSpeedArrayAdapter);
			speedSpinner.setSelection(selectedSpeed);

			// TODO
			// remove toast messages
			// fill out if else statement 

			// display nothing if "None" was selected
			//if(typeSpinner.getSelectedItem().toString().equals("None"))

			if(typeSpinner.getSelectedItem().toString().trim().equals("Static"))
			{
				Toast.makeText(getApplicationContext(), "Static", Toast.LENGTH_SHORT).show();

				TextView staticIpLabel = (TextView) findViewById(R.id.IpLabel);
				EditText staticIp = (EditText) findViewById(R.id.Ip);
				Spinner staticSubnet = (Spinner) findViewById(R.id.Subnet);

				staticIpLabel.setVisibility(View.VISIBLE);
				staticIp.setVisibility(View.VISIBLE);
				staticSubnet.setVisibility(View.VISIBLE);
			}
			else if(typeSpinner.getSelectedItem().toString().equals("DHCP"))
			{
				Toast.makeText(getApplicationContext(), "DHCP", Toast.LENGTH_SHORT).show();

				TextView dhcpIpLabel = (TextView) findViewById(R.id.IpLabel);
				EditText dhcpIp = (EditText) findViewById(R.id.Ip);
				Spinner dhcpSubnet = (Spinner) findViewById(R.id.Subnet);

				dhcpIpLabel.setText("Alias IP address");
				dhcpIpLabel.setVisibility(View.VISIBLE);
				dhcpIp.setVisibility(View.VISIBLE);
				dhcpSubnet.setVisibility(View.VISIBLE);
			}
			// TODO PPP
			else if(typeSpinner.getSelectedItem().toString().equals("PPP"))
				Toast.makeText(getApplicationContext(), "PPP", Toast.LENGTH_SHORT).show();
			else if(typeSpinner.getSelectedItem().toString().equals("PPPoE"))
			{
				Toast.makeText(getApplicationContext(), "PPPoE", Toast.LENGTH_SHORT).show();

				TextView pppoeUsernameLabel = (TextView) findViewById(R.id.usernameLabel);
				EditText pppoeUsername = (EditText) findViewById(R.id.username);
				TextView pppoePasswordLabel = (TextView) findViewById(R.id.passwordLabel);
				EditText pppoePassword = (EditText) findViewById(R.id.password);
				TextView pppoeServiceLabel = (TextView) findViewById(R.id.serviceLabel);
				EditText pppoeService = (EditText) findViewById(R.id.service);
				TextView pppoeDialLabel = (TextView) findViewById(R.id.dialLabel);
				CheckBox pppoeDial = (CheckBox) findViewById(R.id.dial2);
				TextView pppoeIdleLabel = (TextView) findViewById(R.id.idleLabel);
				EditText pppoeIdle = (EditText) findViewById(R.id.idle2);
				TextView pppoeResetLabel = (TextView) findViewById(R.id.resetLabel);
				Spinner pppoReset = (Spinner) findViewById(R.id.resetSpinner);
				TextView pppoAdvancedLabel = (TextView) findViewById(R.id.advancedLabel);
				Button pppoButton = (Button) findViewById(R.id.advancedBtn);
				/////////////////

				pppoeUsernameLabel.setVisibility(View.VISIBLE);
				pppoeUsername.setVisibility(View.VISIBLE);
				pppoePasswordLabel.setVisibility(View.VISIBLE);
				pppoePassword.setVisibility(View.VISIBLE);
				pppoeServiceLabel.setVisibility(View.VISIBLE);
				pppoeService.setVisibility(View.VISIBLE);
				pppoeDialLabel.setVisibility(View.VISIBLE);
				pppoeDial.setVisibility(View.VISIBLE);
				pppoeIdleLabel.setVisibility(View.VISIBLE);
				pppoeIdle.setVisibility(View.VISIBLE);
				pppoeResetLabel.setVisibility(View.VISIBLE);
				pppoReset.setVisibility(View.VISIBLE);
				pppoAdvancedLabel.setVisibility(View.VISIBLE);
				pppoButton.setVisibility(View.VISIBLE);

			}
			else if( (typeSpinner.getSelectedItem().toString().equals("PPTP")) || (typeSpinner.getSelectedItem().toString().equals("L2TP"))) 
			{
				Toast.makeText(getApplicationContext(), "PPTP or L2TP", Toast.LENGTH_SHORT).show();

				TextView pptpUsernameLabel = (TextView) findViewById(R.id.usernameLabel);
				EditText pptpUsername = (EditText) findViewById(R.id.username);
				TextView pptpPasswordLabel = (TextView) findViewById(R.id.passwordLabel);
				EditText pptpPassword = (EditText) findViewById(R.id.password);

				TextView pptpLocalIpLabel = (TextView) findViewById(R.id.localIpLabel);
				EditText pptpLocalIp = (EditText) findViewById(R.id.localIp);
				Spinner pptpLocalSubnet = (Spinner) findViewById(R.id.localSubnet);
				TextView pptpRemoteIpLabel = (TextView) findViewById(R.id.remoteIpLabel);
				EditText pptpRemoteIp = (EditText) findViewById(R.id.remoteIp);

				TextView pptpDialLabel = (TextView) findViewById(R.id.dialLabel);
				CheckBox pptpDial = (CheckBox) findViewById(R.id.dial2);

				TextView pptpidleLabel = (TextView) findViewById(R.id.idleLabel);
				EditText pptpidle = (EditText) findViewById(R.id.idle2);

				TextView pptpAdvancedLabel = (TextView) findViewById(R.id.advancedLabel);
				Button pptpButton = (Button) findViewById(R.id.advancedBtn);

				pptpUsernameLabel.setVisibility(View.VISIBLE);
				pptpUsername.setVisibility(View.VISIBLE);

				pptpPasswordLabel.setVisibility(View.VISIBLE);
				pptpPassword.setVisibility(View.VISIBLE);
				pptpLocalIpLabel.setVisibility(View.VISIBLE);
				pptpLocalIp.setVisibility(View.VISIBLE);
				pptpLocalSubnet.setVisibility(View.VISIBLE);
				pptpRemoteIpLabel.setVisibility(View.VISIBLE);
				pptpRemoteIp.setVisibility(View.VISIBLE);
				pptpDialLabel.setVisibility(View.VISIBLE);
				pptpDial.setVisibility(View.VISIBLE);

				pptpidleLabel.setVisibility(View.VISIBLE);
				pptpidle.setVisibility(View.VISIBLE);
				pptpAdvancedLabel.setVisibility(View.VISIBLE);
				pptpButton.setVisibility(View.VISIBLE);
			}


		}
		else
			interf.setChecked(false); // if interface is disabled untick the checkbox


	}


	/*
	 * OnClick methods for the Save button
	 */

	// TODO once page if feature complete I need to implement pushing changes to pfSense
	public void onClick(View view)
	{	
		switch (view.getId()) 
		{
		case R.id.save:
			Log.d(TAG, "Need to update pfsense");
			break;
		}
	}


	/*
	 * Subclass for ASYNC task
	 * 
	 * Gets the Interface page
	 */

	class PfFetch extends AsyncTask<String, Void, String>
	{

		ProgressDialog dialogT;

		@Override
		protected void onPreExecute()
		{
			try {
				dialogT = new ProgressDialog(InterfacesActivity.this);
				dialogT.setMessage("Retrieving page...");
				dialogT.setIndeterminate(true);
				dialogT.setCancelable(false);
				dialogT.show();
			} catch (Exception e) {
				Log.d(TAG, "ASYNC task exception, onPreExecute: " + e);
			}
		}

		@Override
		protected String doInBackground(String... args)
		{

			try {

				if (pf.getProtocol().equals("HTTP"))
				{
					HttpMethods methods = new HttpMethods(pf.getHttpCookieStore());
					String interfacePage = methods.getPfPage(new URL(pf.getPfURL() + args[0]));
					scrapeInterface(interfacePage);
				}
				else
				{
					HttpsMethods methods = new HttpsMethods(pf.getHttpsCookieStore());
					String interfacePage = methods.getPfPage(new URL(pf.getPfURL() + args[0]));
					scrapeInterface(interfacePage);
				}
				return "";

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			return "error";
		}


		@Override
		protected void onPostExecute(String result) {
			drawInterface();
			dialogT.dismiss();

		}
	}

	public void scrapeInterface(String page)
	{
		typeStore = new ArrayList<String>();
		speedStore = new ArrayList<String>();

		Document doc = Jsoup.parse(page, "ISO-8859-1");

		// get csrf
		Element csrf = doc.select("form input[name=__csrf_magic]").first();
		String csrfString = csrf.attr("value"); 

		// get the interface status
		// enabled = true
		// disabled = false
		Element enabled = doc.select("form[action=interfaces.php] input[name=enable]").first();
		interfaceStatus = enabled.hasAttr("checked"); 

		// get the interface description
		Element desc = doc.select("form[action=interfaces.php] input[name=descr]").first();
		description = desc.attr("value"); 

		// get the list of types
		Elements types = doc.select("form[action=interfaces.php] select[name=type] option");
		for(int i=0; i<types.size(); i++)
		{
			Element e = types.get(i);
			if(e.hasAttr("selected"))
				selectedType = i; // get the position of the currently selected type

			typeStore.add(e.text());
		}

		// get the mac address
		Element macAddress = doc.select("form[action=interfaces.php] input[name=spoofmac]").first();
		mac = macAddress.attr("value");

		// get the mtu
		Element MTU = doc.select("form[action=interfaces.php] input[name=mtu]").first();
		mtu = MTU.attr("value");

		// get the mss
		Element MSS = doc.select("form[action=interfaces.php] input[name=mss]").first();
		mss = MSS.attr("value");

		// get the list of speed and duplex
		Elements speed = doc.select("form[action=interfaces.php] select[name=mediaopt] option");
		for(int i=0; i<speed.size(); i++)
		{
			Element e = speed.get(i);
			if(e.hasAttr("selected"))
				selectedSpeed = i; // get the position of the currently selected speed

			speedStore.add(e.text());
		}
	}

}
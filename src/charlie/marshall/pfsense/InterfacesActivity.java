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
import android.widget.LinearLayout;
import android.widget.Spinner;

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

			// display gerneral conf & private networks interface options
			LinearLayout generalConfLayout = (LinearLayout) findViewById(R.id.generalConfLayout);
			LinearLayout privateNetworksLayout = (LinearLayout) findViewById(R.id.privateNetworksLayout);
			generalConfLayout.setVisibility(View.VISIBLE);
			privateNetworksLayout.setVisibility(View.VISIBLE);

			// get reference to the general config elements
			Spinner typeSpinner = (Spinner) findViewById(R.id.type);  			
			Spinner speedSpinner = (Spinner) findViewById(R.id.speed);  
			EditText descET = (EditText) findViewById(R.id.desc);
			EditText macET = (EditText) findViewById(R.id.macAddress);
			EditText mtuET = (EditText) findViewById(R.id.mtu);
			EditText mssET = (EditText) findViewById(R.id.mss);
			
			// fill the editText fields
			descET.setText(description);
			macET.setText(mac);
			mtuET.setText(mtu);
			mssET.setText(mss);

			// TODO currently not used but will be once we scrape for them
			CheckBox privNet = (CheckBox) findViewById(R.id.blockPrivateNetworks);
			CheckBox bogonNet = (CheckBox) findViewById(R.id.blockBogonNetworks);

			// bind to the ArrayList for the type spinner
			ArrayAdapter<String> typeArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, typeStore);
			typeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			typeSpinner.setAdapter(typeArrayAdapter);
			typeSpinner.setSelection(selectedType);
			
			// bind to the ArrayList for the speed spinner
			ArrayAdapter<String> speedArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, speedStore);
			speedArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			speedSpinner.setAdapter(speedArrayAdapter);
			speedSpinner.setSelection(selectedType);

			// bind to the ArrayList (needs a toString method in the SectorList class)
			ArrayAdapter<String> spinnerSpeedArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, speedStore);
			spinnerSpeedArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			speedSpinner.setAdapter(spinnerSpeedArrayAdapter);
			speedSpinner.setSelection(selectedSpeed);

			// If "None" is selected do nothing
			//if(typeSpinner.getSelectedItem().toString().equals("None"))

			if(typeSpinner.getSelectedItem().toString().trim().equals("Static"))
			{
				// display Static configuration interface options
				LinearLayout staticLayout = (LinearLayout) findViewById(R.id.staticLayout);
				staticLayout.setVisibility(View.VISIBLE);
				
				// TODO populate with scraped details
				EditText staticIp = (EditText) findViewById(R.id.staticIp);
				Spinner staticGateway = (Spinner) findViewById(R.id.staticGateway);
			}
			else if(typeSpinner.getSelectedItem().toString().equals("DHCP"))
			{
				// display DHCP configuration interface options
				LinearLayout dhcpConfLayout = (LinearLayout) findViewById(R.id.dhcpConfLayout);
				dhcpConfLayout.setVisibility(View.VISIBLE);
				
				// TODO populate with scraped details
				EditText dhcpHostname = (EditText) findViewById(R.id.dhcpHostName);
				EditText dhcpIp = (EditText) findViewById(R.id.dhcpIp);
				Spinner dhcpSubnet = (Spinner) findViewById(R.id.dhcpSubnet);
			}
			// TODO PPP
			else if(typeSpinner.getSelectedItem().toString().equals("PPP"))
			{
				// display PPP configuration interface options
				LinearLayout pppConfLayout = (LinearLayout) findViewById(R.id.pppConfLayout);
				pppConfLayout.setVisibility(View.VISIBLE);
				
				// TODO populate with scraped details
				Spinner pppServiceProvider = (Spinner) findViewById(R.id.pppServiceProviderSpinner);
				EditText pppUsername = (EditText) findViewById(R.id.pppUsername);
				EditText pppPassword = (EditText) findViewById(R.id.pppPassword);
				EditText pppPhoneNo = (EditText) findViewById(R.id.pppPhoneNo);
				CheckBox pppAccessPoint = (CheckBox) findViewById(R.id.pppAccessPoint);
				Spinner pppModemPortSpinner = (Spinner) findViewById(R.id.pppModemPortSpinner);
			}
			else if(typeSpinner.getSelectedItem().toString().equals("PPPoE"))
			{
				// display PPPoE configuration interface options
				LinearLayout pppoeConfLayout = (LinearLayout) findViewById(R.id.pppoeConfLayout);
				pppoeConfLayout.setVisibility(View.VISIBLE);

				// TODO populate with scraped details
				EditText pppoeUsername = (EditText) findViewById(R.id.usernamePppoe);
				EditText pppoePassword = (EditText) findViewById(R.id.passwordPppoe);
				EditText pppoeService = (EditText) findViewById(R.id.servicePppoe);
				CheckBox pppoeDial = (CheckBox) findViewById(R.id.dialPppoe);
				EditText pppoeIdle = (EditText) findViewById(R.id.idlePppoe);
				Spinner pppoReset = (Spinner) findViewById(R.id.resetPppoeSpinner);

			}
			else if( (typeSpinner.getSelectedItem().toString().equals("PPTP")) || (typeSpinner.getSelectedItem().toString().equals("L2TP"))) 
			{
				// display PPTP/L2TP configuration interface options
				LinearLayout pptpConfLayout = (LinearLayout) findViewById(R.id.pptpConfLayout);
				pptpConfLayout.setVisibility(View.VISIBLE);

				// TODO populate with scraped details
				EditText pptpUsername = (EditText) findViewById(R.id.pptpUsername);
				EditText pptpPassword = (EditText) findViewById(R.id.pptpPassword);
				EditText pptpLocalIp = (EditText) findViewById(R.id.pptpLocalIp);
				Spinner pptpLocalSubnet = (Spinner) findViewById(R.id.pptpLocalSubnet);
				EditText pptpRemoteIp = (EditText) findViewById(R.id.pptpRemoteIp);
				CheckBox pptpDial = (CheckBox) findViewById(R.id.pptpDial);
				EditText pptpidle = (EditText) findViewById(R.id.pptpIdle);
				Button pptpButton = (Button) findViewById(R.id.pptpAadvancedBtn);
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
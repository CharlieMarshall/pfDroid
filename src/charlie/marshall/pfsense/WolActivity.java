package charlie.marshall.pfsense;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

// TODO check MAC addresses are correctly formatted
// TODO check descriptions are not empty - we cant have an empty client

public class WolActivity extends CustomActivity
{
	private Pfsense pf;
	private SubDrop sd;
	private int menu, subDrop;
	private int wolPos;

	private ArrayList<Wol> wolStore;
	private ArrayList<Interfaces> interfaceStore;
	private String csrfString;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wol);

		// get data from intent
		Intent i = getIntent();
		pf = (Pfsense)i.getSerializableExtra("pf");
		menu = i.getIntExtra("menu", 0);
		subDrop = i.getIntExtra("subDrop", 0);

		sd = pf.getSubDrops(menu);

		// scrape the page for clients
		new PfWol().execute(sd.getURL(subDrop));
		
	}

	/* 
	 * Create context menu for long clicks
	 *
	 */
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		wolPos = info.position;

		switch (item.getItemId()) {
		case R.id.wake:
			Log.d(TAG, "wake");
			sendMagicPacket(wolPos);
			return true;
		case R.id.edit:
			Log.d(TAG, "Edit");
			editWolAlert(false);
			return true;
		case R.id.delete:
			Log.d(TAG, "Delete ");
			confirmDelete();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	public void sendMagicPacket(int position){
		new PfWol().execute(sd.getURL(subDrop) + "?mac=" + wolStore.get(position).getMac() + "&if=" + wolStore.get(position).getInterfaceName());
	}


	/*
	 * AlertDialog to confirm delete host 
	 * 
	 * Ok = run Async task to delete hots
	 * Cancel = do nothing
	 */

	public void confirmDelete() {

		AlertDialog dialog = new AlertDialog.Builder(this)
		//.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle("Confirm delete?")
		.setMessage("Do you really want to delete this entry?")
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();	
				new PfWol().execute(sd.getURL(subDrop) + "?act=del&id=" + wolPos);
			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.create();
		dialog.show();
	}

	/*
	 * Method to populate and draw the ListView
	 */

	public void drawList()
	{
		// Find the ListView resource.     
		ListView listView = (ListView) findViewById( R.id.mainListView ); 
		ArrayAdapter<Wol> listAdapter = new ArrayAdapter<Wol>(this, android.R.layout.simple_list_item_1, wolStore);

		listView.setTextFilterEnabled(true);
		registerForContextMenu(listView); // this line is needed for click listeners
		listView.setAdapter( listAdapter );    

		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				sendMagicPacket(position);
			}
		});
	}

	/*
	 * OnClick methods for the 3 activity buttons 
	 */

	public void onClick(View view)
	{	
		switch (view.getId()) 
		{
		case R.id.addWol:
			Log.d(TAG, "addWol clicked");
			editWolAlert(true);
			break;
		case R.id.wakeAll:
			Log.d(TAG, "wakeAll clicked");
			new PfWol().execute(sd.getURL(subDrop) + "?wakeall=true");
			break;
		case R.id.manualWake:
			Log.d(TAG, "manulWake clicked");
			manualWolAlert();
			break;
		}
	}

	/*
	 * Method for editing / adding a host
	 * 
	 * If newHost == true  - host is added
	 * If newHost == false - host is edited
	 * 
	 */

	public void editWolAlert(boolean newHost)
	{
		final boolean addHost = newHost;

		// ADDED THESE THREE LINES FOR CUSTOM VIEW
		View dialog_layout = getLayoutInflater().inflate(R.layout.add_wol_alert, null);
		final EditText description = (EditText) dialog_layout.findViewById(R.id.description);
		final EditText macAddress = (EditText) dialog_layout.findViewById(R.id.macAddress);
		final Spinner spinner = (Spinner) dialog_layout.findViewById(R.id.spinner);  

		// This is needed so we can work out the interface name from an alias. 
		// If removed, we adapter could use Interfaces instead of String, but then we only have access to Alias and can't work out the interface name!
		String[] inter = new String[interfaceStore.size()];
		for(int i =0; i<interfaceStore.size(); i++)
			inter[i]=interfaceStore.get(i).getAlias();

		// bind to the ArrayList (needs a toString method in the SectorList class)
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, inter);
		spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down vieww
		spinner.setAdapter(spinnerArrayAdapter);

		if (addHost != true) // 
		{
			Wol w = wolStore.get(wolPos);
			int spinnerPosition = spinnerArrayAdapter.getPosition(w.getInterfaceAlias());
			spinner.setSelection(spinnerPosition);
			macAddress.setText(w.getMac());
			description.setText(w.getDesc());
		}

		AlertDialog dialog = new AlertDialog.Builder(this)
		//.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle("Edit/Add a host")
		.setView(dialog_layout)
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				Wol w = null;
				if (addHost != true)
				{
					w = wolStore.get(wolPos);
					Interfaces in = interfaceStore.get(spinner.getSelectedItemPosition());

					w.setInterfaceAlias(in.getAlias());
					w.setInterfaceName(in.getName());
					w.setMac(macAddress.getText().toString());
					w.setDesc(description.getText().toString());
				}
				else 
				{
					Interfaces in = interfaceStore.get(spinner.getSelectedItemPosition());
					w = new Wol(in.getAlias(), in.getName());
					w.setMac(macAddress.getText().toString());
					w.setDesc(description.getText().toString());
					Log.d(TAG, "size of wol store: " + wolStore.size());
					Log.d(TAG, "adding new host at pos: " + wolStore.size());

					wolStore.add(w);
				}

				String query = "";
				String id = "";
				try {
					query = 
							"interface=" + URLEncoder.encode(w.getInterfaceName(),"ISO-8859-1") +
							"&mac=" + URLEncoder.encode(w.getMac().toString(),"ISO-8859-1") +
							"&descr=" + URLEncoder.encode(w.getDesc().toString(),"ISO-8859-1") +
							"&Submit=Save" ;

					if (addHost==false) // if editing
						id = Integer.toString(wolPos);

					Log.d(TAG, "query: " + query);
					Log.d(TAG, "id: " + id);

				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				new PfWolPost().execute(query, id);

				//Dismiss once everything is OK.
				dialog.dismiss();					
			}
		})
		.setNegativeButton("Close", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.create();
		dialog.show();
	}

	/*
	 * Method to enter a Mac address and send a magic packet 
	 * 
	 */

	public void manualWolAlert()
	{
		// ADDED THESE THREE LINES FOR CUSTOM VIEW
		View dialog_layout = getLayoutInflater().inflate(R.layout.manual_wol_alert, null);
		final EditText editText = (EditText) dialog_layout.findViewById(R.id.macAddress);
		final Spinner spinner = (Spinner) dialog_layout.findViewById(R.id.spinner);  

		// bind to the ArrayList (needs a toString method in the SectorList class)
		ArrayAdapter<Interfaces> spinnerArrayAdapter = new ArrayAdapter<Interfaces>(this, android.R.layout.simple_spinner_item, interfaceStore);
		spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down vieww
		spinner.setAdapter(spinnerArrayAdapter);

		AlertDialog dialog = new AlertDialog.Builder(this)
		//.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle("Enter host details to wake")
		.setView(dialog_layout)
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				editText.getText().toString();
				spinner.getSelectedItemPosition();

				Interfaces in = interfaceStore.get(spinner.getSelectedItemPosition());
				String query = "";
				try {
					query = 
							"__csrf_magic=" + URLEncoder.encode(csrfString,"ISO-8859-1") +
							"&interface=" + URLEncoder.encode(in.getName(),"ISO-8859-1") +
							"&mac=" + URLEncoder.encode(editText.getText().toString(),"ISO-8859-1") +
							"&Submit=Send" ;

					Log.d(TAG, "query: " + query);

				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				new PfWol().execute(sd.getURL(subDrop) + query);

				//Dismiss once everything is OK.
				dialog.dismiss();					
			}
		})
		.setNegativeButton("Close", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.create();
		dialog.show();
	}




	/*
	 * Subclass for ASYNC task
	 * 
	 * Gets the wol page for scraping
	 * OR
	 * Sends the magic packet via an HTTP get with a query string
	 * OR
	 * Sends magic packet to ALL clients
	 * OR
	 * Deletes existing clients
	 */

	class PfWol extends AsyncTask<String, Void, String> {

		ProgressDialog dialogT;

		@Override
		protected void onPreExecute() {
			try {
				dialogT = new ProgressDialog(WolActivity.this);
				dialogT.setMessage("Performing task...");
				dialogT.setIndeterminate(true);
				dialogT.setCancelable(false);
				dialogT.show();
			} catch (Exception e) {
				Log.d(TAG, "ASYNC task exception, onPreExecute: " + e);
			}
		}

		@Override
		protected String doInBackground(String... args) {

			try {

				String wolPage = "";
				
				if (pf.getProtocol().equals("HTTP"))
				{
					HttpMethods methods = new HttpMethods(pf.getHttpCookieStore());
					wolPage = methods.getPfPage(new URL(pf.getPfURL() + args[0]));
				}
				else
				{
					HttpsMethods methods = new HttpsMethods(pf.getHttpsCookieStore());
					wolPage = methods.getPfPage(new URL(pf.getPfURL() + args[0]));
				}

				scrapeWol(wolPage);
				// TODO make method void
				return "";

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			return "error";
		}


		@Override
		protected void onPostExecute(String result) {
			drawList();
			dialogT.dismiss();
		}


	} // end of PfWol subclass

	/*
	 * Subclass for ASYNC task
	 * 
	 * Create new / edit wol host to pfsense
	 */

	class PfWolPost extends AsyncTask<String, Void, String> {

		ProgressDialog dialogT;

		@Override
		protected void onPreExecute() {
			try {
				dialogT = new ProgressDialog(WolActivity.this);
				dialogT.setMessage("Updating...");
				dialogT.setIndeterminate(true);
				dialogT.setCancelable(false);
				dialogT.show();
			} catch (Exception e) {
				Log.d(TAG, "ASYNC task exception, onPreExecute: " + e);
			}
		}

		@Override
		protected String doInBackground(String... args) {

			try {

				String wolPage = "";

				if (pf.getProtocol().equals("HTTP"))
				{
					HttpMethods methods = new HttpMethods(pf.getHttpCookieStore());
					wolPage = methods.setWolClient(pf.getPfURL(), args[0], args[1]);
				}
				else
				{
					HttpsMethods methods = new HttpsMethods(pf.getHttpsCookieStore());
					wolPage = methods.setWolClient(pf.getPfURL(), args[0], args[1]);
				}

				scrapeWol(wolPage);

				// TODO make method void
				return "";

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			return "error";
		}


		@Override
		protected void onPostExecute(String result) {
			dialogT.dismiss();
			drawList();
		}
	} // end of PfWolPost subclass


	/*
	 * Method to scrape the wol page
	 * 
	 * Saves wol clients into an ArrayList wolStore
	 * Saves interfaces into an ArrayList interfaceStore
	 * 
	 * We save interfaces so we can find the interfaces names eg opt1 from an alias name eg wifi
	 * 
	 */


	public void scrapeWol(String page) throws IOException{
		Wol wol;
		Interfaces interf;

		Document doc = Jsoup.parse(page, "ISO-8859-1");

		// get csrf 
		// only required for manually (entering a mac address) waking a host
		Element csrf = doc.select("form input[name=__csrf_magic]").first();
		csrfString = csrf.attr("value"); 

		// get interfaces and alias
		Elements interfaces = doc.select("select option");

		interfaceStore = new ArrayList<Interfaces>();

		for (int i=0; i<interfaces.size(); i++)
		{
			Element e = interfaces.get(i);
			interf = new Interfaces(e.attr("value"), e.select("option").text());
			interfaceStore.add(i, interf);
		}
		// end of scraping interfaces

		// get listed clients
		Elements clients = doc.select("table td.listlr");

		wolStore = new ArrayList<Wol>();

		for (int i=0; i<clients.size(); i++)
		{
			Element e = clients.get(i);
			String alias = "";

			for(int y=0; y<interfaceStore.size(); y++)
			{

				Interfaces in = interfaceStore.get(y);

				// annoying spaces (&nbsp) are trailing after the alias, need to be removed for string comparison
				if(in.getAlias().equals(e.text().replace("\u00a0","")))
				{
					alias = in.getName();
					break; // no need to continue comparing when we found the match
				}
			}
			wol = new Wol(e.text().replace("\u00a0",""), alias);
			wolStore.add(i, wol);
		}

		Elements macs = doc.select("table td.listr");

		for (int i=0; i<macs.size(); i++)
		{
			Element e = macs.get(i);
			wolStore.get(i).setMac(e.text().replace("\u00a0",""));
		}

		Elements descs = doc.select("table td.listbg");

		for (int i=0; i<descs.size(); i++)
		{
			Element e = descs.get(i);
			wolStore.get(i).setDesc(e.text().replace("\u00a0",""));
		}
		// end of clients
	}

}
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

// TODO check MAC addresses are correctly formatted
// TODO check descriptions are not empty - we cant have an empty client

public class AliasActivity extends CustomActivity
{
	private Pfsense pf;
	private SubDrop sd;
	private int menu, subDrop;
	private int listPos;

	private ArrayList<Alias> aliasStore;
	private String csrfString;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alias);

		// get data from intent
		Intent i = getIntent();
		pf = (Pfsense)i.getSerializableExtra("pf");
		menu = i.getIntExtra("menu", 0);
		subDrop = i.getIntExtra("subDrop", 0);

		sd = pf.getSubDrops(menu);
		new PfPower().execute(sd.getURL(subDrop));
	}

	/* 
	 * Create context menu for long clicks
	 *
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_alias, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		listPos = info.position;

		switch (item.getItemId()) {
		case R.id.edit:
			Log.d(TAG, "Edit");
			editAlert(false);
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
		//new PfWol().execute(sd.getURL(subDrop) + "?mac=" + wolStore.get(position).getMac() + "&if=" + wolStore.get(position).getInterfaceName());
	}


	/*
	 * AlertDialog to confirm reboot/halt 
	 * 
	 * Ok = run Async task to reboot / halt
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
				new PfWol().execute(sd.getURL(subDrop) + "?act=del&id=" + listPos);
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
		ArrayAdapter<Alias> listAdapter = new ArrayAdapter<Alias>(this, android.R.layout.simple_list_item_1, aliasStore);

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
	 * OnClick methods for the add alias button
	 */

	public void onClick(View view)
	{	
		Log.d(TAG, "addWol clicked");
		editAlert(true);

	}

	/*
	 * Method for editing / adding a host
	 * 
	 * If newHost == true  - host is added
	 * If newHost == false - host is edited
	 * 
	 */

	public void editAlert(boolean newHost)
	{
		final boolean addHost = newHost;

		// ADDED THESE THREE LINES FOR CUSTOM VIEW
		View dialog_layout = getLayoutInflater().inflate(R.layout.edit_alias_alert, null);

		final EditText name = (EditText) dialog_layout.findViewById(R.id.name);
		final EditText value = (EditText) dialog_layout.findViewById(R.id.value);
		final EditText description = (EditText) dialog_layout.findViewById(R.id.description);

		if (addHost != true) // 
		{
			Alias a = aliasStore.get(listPos);
			name.setText(a.getName());
			value.setText(a.getValue());
			description.setText(a.getDesc());
		}

		AlertDialog dialog = new AlertDialog.Builder(this)
		.setTitle("Edit/Add an alias")
		.setView(dialog_layout)
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				Alias a = null;
				if (addHost != true)
				{
					a = aliasStore.get(listPos);
					a.setName(name.getText().toString());
					a.setValue(value.getText().toString());
					a.setDesc(description.getText().toString());
				}
				else 
				{
					a = new Alias(name.getText().toString());
					a.setValue(value.getText().toString());
					a.setDesc(description.getText().toString());
					Log.d(TAG, "size of wol store: " + aliasStore.size());
					Log.d(TAG, "adding new host at pos: " + aliasStore.size());

					aliasStore.add(a);
				}

				String query = "";
				String id = "";
				try {
					query = 
							"interface=" + URLEncoder.encode(a.getName(),"ISO-8859-1") +
							"&mac=" + URLEncoder.encode(a.getValue().toString(),"ISO-8859-1") +
							"&descr=" + URLEncoder.encode(a.getDesc().toString(),"ISO-8859-1") +
							"&Submit=Save" ;

					if (addHost==false) // if editing
						id = Integer.toString(listPos);

					Log.d(TAG, "query: " + query);
					Log.d(TAG, "id: " + id);

				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//	new PfWolPost().execute(query, id);

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
	 * Gets the WOL page
	 */

	class PfPower extends AsyncTask<String, Void, String> {

		ProgressDialog dialogT;

		@Override
		protected void onPreExecute() {
			try {
				dialogT = new ProgressDialog(AliasActivity.this);
				dialogT.setMessage("Scraping page...");
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

				if (pf.getProtocol().equals("HTTP"))
				{
					HttpMethods methods = new HttpMethods(pf.getHttpCookieStore());
					String page = methods.getPfPage(new URL(pf.getPfURL() + args[0]));
					scrapeWol(page);
				}
				else
				{
					HttpsMethods methods = new HttpsMethods(pf.getHttpsCookieStore());
					String page = methods.getPfPage(new URL(pf.getPfURL() + args[0]));
					scrapeWol(page);
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
			dialogT.dismiss();
			drawList();
		}


	} // end of PfPower subclass

	/*
	 * Subclass for ASYNC task
	 * 
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
				dialogT = new ProgressDialog(AliasActivity.this);
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

				if (pf.getProtocol().equals("HTTP"))
				{
					HttpMethods methods = new HttpMethods(pf.getHttpCookieStore());
					String wolPage = methods.getPfPage(new URL(pf.getPfURL() + args[0]));

					// need to apply changes:

					// URL:
					//https://192.168.100.1/firewall_aliases.php

					//query string:
					// __csrf_magic=sid%3Aa5c70a68a224cfba338f7b053d3c954b29133d35%2C1366553591
					//&apply=Apply+changes

					scrapeWol(wolPage);
				}
				else
				{
					HttpsMethods methods = new HttpsMethods(pf.getHttpsCookieStore());
					String wolPage = methods.getPfPage(new URL(pf.getPfURL() + args[0]));

					// need to apply changes:

					// URL:
					//https://192.168.100.1/firewall_aliases.php

					//query string:
					// __csrf_magic=sid%3Aa5c70a68a224cfba338f7b053d3c954b29133d35%2C1366553591
					//&apply=Apply+changes

					scrapeWol(wolPage);
				}

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


	} // end of PfPower subclass

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
				dialogT = new ProgressDialog(AliasActivity.this);
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

				if (pf.getProtocol().equals("HTTP"))
				{
					HttpMethods methods = new HttpMethods(pf.getHttpCookieStore());
					String wolPage = methods.setWolClient(pf.getPfURL(), args[0], args[1]);
					scrapeWol(wolPage);
				}
				else
				{
					HttpsMethods methods = new HttpsMethods(pf.getHttpsCookieStore());
					String wolPage = methods.setWolClient(pf.getPfURL(), args[0], args[1]);
					scrapeWol(wolPage);
				}
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
	} // end of PfPower subclass


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
		Alias alias;

		Document doc = Jsoup.parse(page, "ISO-8859-1");

		// get csrf 
		// only required for manually (entering a mac address) waking a host
		Element csrf = doc.select("form input[name=__csrf_magic]").first();
		csrfString = csrf.attr("value"); 
		//Log.d(TAG, "csrf: " +csrfString);


		// get listed clients
		Elements clients = doc.select("table td.listlr");

		//Log.d(TAG, "number of clients: " + clients.size());
		aliasStore = new ArrayList<Alias>();

		for (int i=0; i<clients.size(); i++)
		{
			Element e = clients.get(i);

			alias = new Alias(e.text());
			aliasStore.add(i, alias);
		}

		Elements values = doc.select("table td.listr");

		for (int i=0; i<values.size(); i++)
		{
			Element e = values.get(i);
			aliasStore.get(i).setValue(e.text());
		}

		Elements descs = doc.select("table td.listbg");

		for (int i=0; i<descs.size(); i++)
		{
			Element e = descs.get(i);
			aliasStore.get(i).setDesc(e.text());
		}
		// end of clients
	}

}
package charlie.marshall.pfsense;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ArpActivity extends ListActivity
{
	private Pfsense pf;
	private SubDrop sd;
	private int menu, subDrop;

	private ArrayList<Arp> arpStore;
	
	private String TAG = "pfsense_app";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_wol);

		// get data from intent
		Intent i = getIntent();
		pf = (Pfsense)i.getSerializableExtra("pf");
		menu = i.getIntExtra("menu", 0);
		subDrop = i.getIntExtra("subDrop", 0);

		sd = pf.getSubDrops(menu);

		// scrape the page for clients
		new PfArp().execute(sd.getURL(subDrop));		
	}


	/*
	 * Method to populate and draw the ListView
	 */

	public void drawList()
	{
		setListAdapter(new ArrayAdapter<Arp>(this, R.layout.activity_index, arpStore));
		 
		ListView listView = getListView();
		listView.setTextFilterEnabled(true);
 
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
			    
				Arp arp = arpStore.get(position);
				
				Intent i = new Intent(ArpActivity.this, ViewArpActivity.class);
				i.putExtra("arp", arp);
				startActivity(i);
			}
		});
	}

	/*
	 * Subclass for ASYNC task
	 * 
	 * Gets the Arp table for scraping
	 * 
	 */

	class PfArp extends AsyncTask<String, Void, String> {

		ProgressDialog dialogT;

		@Override
		protected void onPreExecute() {
			try {
				dialogT = new ProgressDialog(ArpActivity.this);
				dialogT.setMessage("Retrieving Arp Table...");
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

				String page = "";
				
				if (pf.getProtocol().equals("HTTP"))
				{
					HttpMethods methods = new HttpMethods(pf.getHttpCookieStore());
					page = methods.getPfPage(new URL(pf.getPfURL() + args[0]));
				}
				else
				{
					HttpsMethods methods = new HttpsMethods(pf.getHttpsCookieStore());
					page = methods.getPfPage(new URL(pf.getPfURL() + args[0]));
				}

				scrapeArp(page);
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

	} // end of PfArp subclass


	/*
	 * Method to scrape the returned Arp table page
	 * 
	 * Saves each table row into an Arp class
	 * Saves each ARP in an ArrayList arpStore
	 * 
	 */


	public void scrapeArp(String page) throws IOException{
		Arp arp;
		arpStore = new ArrayList<Arp>();

		Document doc = Jsoup.parse(page, "ISO-8859-1");

		// get interfaces and alias
		Elements table = doc.select("table[class=tabcont sortable]");
		
		Elements tableRow = table.select("tr");
		
		for(int i=1; i<tableRow.size(); i++)
		{
			Element e = tableRow.get(i).select("td.listlr").first();
			arp = new Arp(e.text()); // create an arp entry with the IP address
			
			Elements value = tableRow.get(i).select("td.listr");
			for (int y=0; y<value.size(); y++)
			{
				switch(y)
				{
				case 0:
					arp.setMac(value.get(y).text());
					break;
				case 1:
					arp.setHostName(value.get(y).text());
					break;
				case 2:
					arp.setInterface(value.get(y).text());
					arpStore.add(arp);
					break;
				}
			}	
		}
	} // end of scrapeArp

}
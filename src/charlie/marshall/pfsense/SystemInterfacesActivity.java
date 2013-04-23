package charlie.marshall.pfsense;

import java.io.IOException;
import java.net.URL;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SystemInterfacesActivity extends CustomActivity
{
	private Pfsense pf;
	SubDrop sd;
	int menu, subDrop;

	String[] inter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// get data from intent
		Intent i = getIntent();
		pf = (Pfsense)i.getSerializableExtra("pf");
		menu = i.getIntExtra("menu", 0);
		subDrop = i.getIntExtra("subDrop", 0);

		setContentView(R.layout.activity_status_interfaces);

		sd = pf.getSubDrops(menu);

		// scrape the page for the interfaces names and data
		new PfScrape().execute(sd.getURL(subDrop));

	}

	public void drawList()
	{
		// Find the ListView resource.     
		ListView listView = (ListView) findViewById( R.id.mainListView ); 
		ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, inter);

		listView.setTextFilterEnabled(true);
		registerForContextMenu(listView); // this line is needed for click listeners
		listView.setAdapter( listAdapter );    

		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// currently does nothing
			}
		});
	}

	public void setText(String text)
	{
		TextView textView = (TextView) findViewById(R.id.textView);
		textView.setText(text);
	}


	/*
	 * Subclass for ASYNC task
	 * 
	 * Gets the page
	 */

	class PfScrape extends AsyncTask<String, Void, String> {

		ProgressDialog dialogT;

		@Override
		protected void onPreExecute() {
			try {
				dialogT = new ProgressDialog(SystemInterfacesActivity.this);
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

				// TODO move inter so its not global
				inter = scrapeWol(page);

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


	public String[] scrapeWol(String page) throws IOException{

		Document doc = Jsoup.parse(page, "ISO-8859-1");

		// get interface titles
		Elements table = doc.select("table");

		Elements noRows = doc.select("td.listtopic");
		Log.d(TAG, "no of topics: " + noRows.size());

		String[] topics = new String[noRows.size()];
		int i =0;

		Elements e = table.select("td");
		for (Element y : e)
		{
			if(y.hasClass("listtopic"))
			{

				Log.d(TAG, "int i: " + i);
				topics[i] = y.text();
				Log.d(TAG, "TOPIC: " + y.text());
				i++;
			}
			else if(y.hasClass("vncellt"))
			{
				Log.d(TAG, "HEADING: " + y.text());
			}
			else if(y.hasClass("listr"))
			{
				// normally childNodeSize is 1, will be 3 if its down
				if(y.childNodeSize()>1)
				{
					if(y.text().replace("\u00a0","").equals("up"))
					{
						//  TODO set button text as Connect
						Log.d(TAG, "NODE IS UP");
					}
					else if(y.text().replace("\u00a0","").equals("down"))
					{
						//  TODO set button text as Disconnect
						Log.d(TAG, "NODE IS DOWN");
					}
					Log.d(TAG, "ACTION for button:" + y.select("a").attr("href").toString());
				}
				Log.d(TAG, "VALUE: " + y.text());
			}
		}
		return topics;
	}

}
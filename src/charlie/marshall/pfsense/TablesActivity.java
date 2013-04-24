package charlie.marshall.pfsense;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
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
import android.widget.Spinner;
import android.widget.TextView;


public class TablesActivity extends CustomActivity
{
	private Pfsense pf;
	private SubDrop sd;
	private int menu, subDrop;
	private int menuPosition;

	String csrfString;

	private ArrayList<String> tableStore;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tables);

		// get data from intent
		Intent i = getIntent();
		pf = (Pfsense)i.getSerializableExtra("pf");
		menu = i.getIntExtra("menu", 0);
		subDrop = i.getIntExtra("subDrop", 0);

		sd = pf.getSubDrops(menu);

		new postTablesAsync().execute(pf.getPfURL() + sd.getURL(subDrop));
	}

	public void drawActivity()
	{
		Spinner tableSpinner = (Spinner) findViewById(R.id.tableSpinner);    

		// bind to the ArrayList (needs a toString method in the SectorList class)
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tableStore);
		spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down vieww
		tableSpinner.setAdapter(spinnerArrayAdapter);
		tableSpinner.setSelection(menuPosition);
	}

	/*
	 * OnClick method for the ping button
	 */

	public void onClick(View view)
	{	
		Spinner tableSpinner = (Spinner) findViewById(R.id.tableSpinner);  
		String table = tableSpinner.getSelectedItem().toString();

		String query = "";

		try {

			query = "/?type=" + URLEncoder.encode(table, "ISO-8859-1");

			new postTablesAsync().execute(pf.getPfURL() + sd.getURL(subDrop) + query);

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateText(String result)
	{
		TextView tableResult = (TextView) findViewById(R.id.display);
		tableResult.setText(result);
	}


	/*
	 * Subclass for ASYNC task
	 * 
	 * args[0] = the url in String form
	 * 
	 * Fetches the requested page (HTTP GET) and then scrapes the page for the result and outputs the result
	 */

	class postTablesAsync extends AsyncTask<String, Void, String> {

		ProgressDialog dialogT;

		@Override
		protected void onPreExecute() {
			try {
				dialogT = new ProgressDialog(TablesActivity.this);
				dialogT.setMessage("Retrieving page...");
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
					page = methods.getPfPage(new URL(args[0]));
				}
				else
				{
					HttpsMethods methods = new HttpsMethods(pf.getHttpsCookieStore());
					page = methods.getPfPage(new URL(args[0]));
				}

				return page;

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "error";
		}


		@Override
		protected void onPostExecute(String result) {
			String tableResult = scrapePingResult(result); 
			drawActivity();
			updateText(tableResult);
			dialogT.dismiss();
		}
	} // end of postPingAsync subclass



	/*
	 * Method to scrape the page 
	 * 
	 * Scrapes the page for the csrf - this doesnt get used!!
	 * Scrapes the page for the list of tables - used for the spinner
	 * Scrapes the page for the resulting tables
	 */

	public String scrapePingResult(String page)
	{
		String returnString = "";
		Document doc = Jsoup.parse(page, "ISO-8859-1");

		// get csrf
		Element csrf = doc.select("form input[name=__csrf_magic]").first();
		csrfString = csrf.attr("value"); 

		Elements tables = doc.select("select option");

		tableStore = new ArrayList<String>();

		for (int i=0; i<tables.size(); i++)
		{
			Element e = tables.get(i);
			if(e.hasAttr("selected"))
				menuPosition = i;

			tableStore.add(i, e.text());
		}

		Elements tds = doc.select("table td");
		for (int i=0; i<tds.size(); i++)
		{
			if(!tds.get(i).text().equals(""))
				returnString += tds.get(i).text() + "\n"; 
		}

		return returnString;
	}


} // end of class

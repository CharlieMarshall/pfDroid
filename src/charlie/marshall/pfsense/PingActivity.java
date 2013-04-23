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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;


public class PingActivity extends CustomActivity
{
	private Pfsense pf;
	private SubDrop sd;
	private int menu, subDrop;

	String csrfString;

	private ArrayList<Interfaces> interfaceStore;
	private ArrayList<String> countStore; // use a string here it gets passed as a query string

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ping);

		// get data from intent
		Intent i = getIntent();
		pf = (Pfsense)i.getSerializableExtra("pf");
		menu = i.getIntExtra("menu", 0);
		subDrop = i.getIntExtra("subDrop", 0);

		sd = pf.getSubDrops(menu);

		new PingAsync().execute(sd.getURL(subDrop));
	}

	public void drawActivity()
	{
		Spinner interfaceSpinner = (Spinner) findViewById(R.id.interfaceSpinner);  
		Spinner countSpinner = (Spinner) findViewById(R.id.count);  

		// bind to the ArrayList (needs a toString method in the SectorList class)
		ArrayAdapter<Interfaces> spinnerArrayAdapter = new ArrayAdapter<Interfaces>(this, android.R.layout.simple_spinner_item, interfaceStore);
		spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down vieww
		interfaceSpinner.setAdapter(spinnerArrayAdapter);

		// bind to the ArrayList (needs a toString method in the SectorList class)
		ArrayAdapter<String> countArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, countStore);
		countArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down vieww
		countSpinner.setAdapter(countArrayAdapter);
		countSpinner.setSelection(2); // hard code selected = '3'
	}

	/*
	 * OnClick method for the ping button
	 */

	public void onClick(View view)
	{	
		EditText host = (EditText) findViewById(R.id.host);
		Spinner interfaceSpinner = (Spinner) findViewById(R.id.interfaceSpinner);  
		Spinner countSpinner = (Spinner) findViewById(R.id.count);  
		String count = countSpinner.getSelectedItem().toString();

		Interfaces in = interfaceStore.get(interfaceSpinner.getSelectedItemPosition());

		String query = "";

		try {

			query = 
					"__csrf_magic=" + URLEncoder.encode(csrfString, "ISO-8859-1") +
					"&host=" + URLEncoder.encode(host.getText().toString(), "ISO-8859-1") +
					"&interface=" + URLEncoder.encode(in.getName(), "ISO-8859-1") +
					"&count=" + URLEncoder.encode(count, "ISO-8859-1") +
					"&Submit=" + URLEncoder.encode("Ping", "ISO-8859-1") ;

			Log.d(TAG, "query: " + query);

			new postPingAsync().execute(query);

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateText(String result)
	{
		TextView pingResult = (TextView) findViewById(R.id.pingResult);
		pingResult.setText(result);
	}


	/*
	 * Subclass for ASYNC task
	 * 
	 * Gets the first instance of the ping page so we can get the csrf & the interfaces etc
	 * 
	 */

	class PingAsync extends AsyncTask<String, Void, String> {

		ProgressDialog dialogT;

		@Override
		protected void onPreExecute() {
			try {
				dialogT = new ProgressDialog(PingActivity.this);
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

				String pingPage = "";

				if (pf.getProtocol().equals("HTTP"))
				{
					HttpMethods methods = new HttpMethods(pf.getHttpCookieStore());
					pingPage = methods.getPfPage(new URL(pf.getPfURL() + sd.getURL(subDrop)));
				}
				else
				{
					HttpsMethods methods = new HttpsMethods(pf.getHttpsCookieStore());
					pingPage = methods.getPfPage(new URL(pf.getPfURL() + sd.getURL(subDrop)));
				}
				return pingPage;

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "error";
		}


		@Override
		protected void onPostExecute(String result) {
			scrapePingPage(result);
			drawActivity();
			dialogT.dismiss();
		}
	} // end of PingAsync subclass

	/*
	 * Subclass for ASYNC task
	 * 
	 * Sends the ping as a HTTP post and then scrapes the page for the result and outputs the result
	 */

	class postPingAsync extends AsyncTask<String, Void, String> {

		ProgressDialog dialogT;

		@Override
		protected void onPreExecute() {
			try {
				dialogT = new ProgressDialog(PingActivity.this);
				dialogT.setMessage("Pinging host...");
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
					page = methods.getPingResultsPage(new URL(pf.getPfURL() + sd.getURL(subDrop)), args[0]);
				}
				else
				{
					HttpsMethods methods = new HttpsMethods(pf.getHttpsCookieStore());
					page = methods.getPingResultsPage(new URL(pf.getPfURL() + sd.getURL(subDrop)), args[0]);
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
			String pingResult = scrapePingResult(result); 
			updateText(pingResult);
			dialogT.dismiss();
		}
	} // end of postPingAsync subclass


	/*
	 * Method to scrape the page that pfsense renders (pre any ping) 
	 * 
	 * Extract the csrf
	 * Extracts the list of interfaces and their aliases
	 * Extracts the number of counts eg 1-10
	 */

	public void scrapePingPage(String page)
	{
		Interfaces interf;

		Document doc = Jsoup.parse(page, "ISO-8859-1");

		// get the csrf
		Element csrf = doc.select("form input[name=__csrf_magic]").first();
		csrfString = csrf.attr("value"); 
		Log.d(TAG, "csrf: " +csrfString);

		// get interfaces and alias
		Elements interfaces = doc.select("select[name=interface] option");

		//Log.d(TAG, "number of interfaces: " + interfaces.size());
		interfaceStore = new ArrayList<Interfaces>();

		for (int i=0; i<interfaces.size(); i++)
		{
			Element e = interfaces.get(i);
			interf = new Interfaces(e.attr("value"), e.select("option").text());
			interfaceStore.add(i, interf);
		}
		// end of scraping interfaces

		// get count
		Elements pingCounts = doc.select("select[name=count] option");

		//Log.d(TAG, "number of interfaces: " + interfaces.size());
		countStore = new ArrayList<String>();

		for (int i=0; i<pingCounts.size(); i++)
		{
			Element e = pingCounts.get(i);
			countStore.add(i, e.select("option").text());
		}
	}

	/*
	 * Method to scrape the results of a ping 
	 * 
	 * Returns the result as a String
	 */

	public String scrapePingResult(String page)
	{
		Document doc = Jsoup.parse(page, "ISO-8859-1");

		// get csrf
		Element csrf = doc.select("form input[name=__csrf_magic]").first();
		csrfString = csrf.attr("value"); 
		Log.d(TAG, "csrf: " +csrfString);

		Element output = doc.select("pre").first();

		return output.text();
	}


} // end of class
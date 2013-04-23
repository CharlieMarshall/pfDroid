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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;


public class TracerouteActivity extends CustomActivity
{
	private Pfsense pf;
	private SubDrop sd;
	private int menu, subDrop;

	String csrfString;

	private ArrayList<String> hopStore; // use a string here it gets passed as a query string

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_traceroute);

		// get data from intent
		Intent i = getIntent();
		pf = (Pfsense)i.getSerializableExtra("pf");
		menu = i.getIntExtra("menu", 0);
		subDrop = i.getIntExtra("subDrop", 0);

		sd = pf.getSubDrops(menu);

		new TraceRouteAsync().execute(sd.getURL(subDrop));
	}

	public void drawActivity()
	{
		  
		Spinner hopsSpinner = (Spinner) findViewById(R.id.hopsSpinner);  

		// bind to the ArrayList (needs a toString method in the SectorList class)
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, hopStore);
		spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down vieww
		hopsSpinner.setAdapter(spinnerArrayAdapter);
		hopsSpinner.setSelection(17); // hard code selected = '3'
	}

	/*
	 * OnClick method for the ping button
	 */

	public void onClick(View view)
	{	
		EditText host = (EditText) findViewById(R.id.host);
		Spinner hopsSpinner = (Spinner) findViewById(R.id.hopsSpinner);  
		String hops = hopsSpinner.getSelectedItem().toString();
		CheckBox icmp = (CheckBox) findViewById(R.id.icmp);

		String query = "";

		try {

			query =	"__csrf_magic=" + URLEncoder.encode(csrfString, "ISO-8859-1") +
					"&host=" + URLEncoder.encode(host.getText().toString(), "ISO-8859-1") +
					"&ttl=" + URLEncoder.encode(hops, "ISO-8859-1") +
					"&Submit=" + URLEncoder.encode("Traceroute", "ISO-8859-1") ;
			
			// append icmp to query string if its selected
			if(icmp.isChecked())
				query += "&useicmp=on";
			
			Log.d(TAG, "query: " + query);

			new postTracerouteAsync().execute(query);

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateText(String result)
	{
		TextView traceResult = (TextView) findViewById(R.id.tracerouteResult);
		traceResult.setText(result);
	}


	/*
	 * Subclass for ASYNC task
	 * 
	 * Gets the first instance of the ping page so we can get the csrf & the interfaces etc
	 * 
	 */

	class TraceRouteAsync extends AsyncTask<String, Void, String> {

		ProgressDialog dialogT;

		@Override
		protected void onPreExecute() {
			try {
				dialogT = new ProgressDialog(TracerouteActivity.this);
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
			scrapeTracePage(result);
			drawActivity();
			dialogT.dismiss();
		}
	} // end of PingAsync subclass

	/*
	 * Subclass for ASYNC task
	 * 
	 * Sends the ping as a HTTP post and then scrapes the page for the result and outputs the result
	 */

	class postTracerouteAsync extends AsyncTask<String, Void, String> {

		ProgressDialog dialogT;

		@Override
		protected void onPreExecute() {
			try {
				dialogT = new ProgressDialog(TracerouteActivity.this);
				dialogT.setMessage("Tracing...");
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
			String pingResult = scrapeTraceResult(result); 
			updateText(pingResult);
			dialogT.dismiss();
		}
	} // end of postTracerouteAsync subclass


	/*
	 * Method to scrape the page that pfsense renders (pre any Traceroute) 
	 * 
	 * Extract the csrf
	 * Extracts the number of hops eg 1-64
	 */

	public void scrapeTracePage(String page)
	{
		Document doc = Jsoup.parse(page, "ISO-8859-1");

		// get the csrf
		Element csrf = doc.select("form input[name=__csrf_magic]").first();
		csrfString = csrf.attr("value"); 
		Log.d(TAG, "csrf: " +csrfString);

		// get hops
		Elements interfaces = doc.select("select[name=ttl] option");

		hopStore = new ArrayList<String>();

		for (int i=0; i<interfaces.size(); i++)
		{
			Element e = interfaces.get(i);
			Log.d(TAG, "hop: " + e.text());
			hopStore.add(i, e.text());
		}
		// end of scraping hops

	}

	/*
	 * Method to scrape the results of a Traceroute 
	 * 
	 * Returns the result as a String
	 */

	public String scrapeTraceResult(String page)
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
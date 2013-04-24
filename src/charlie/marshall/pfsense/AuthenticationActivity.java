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


public class AuthenticationActivity extends CustomActivity
{
	private Pfsense pf;
	private SubDrop sd;
	private int menu, subDrop;

	String csrfString;

	private ArrayList<String> serverStore;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_authentication);

		// get data from intent
		Intent i = getIntent();
		pf = (Pfsense)i.getSerializableExtra("pf");
		menu = i.getIntExtra("menu", 0);
		subDrop = i.getIntExtra("subDrop", 0);

		sd = pf.getSubDrops(menu);

		new AuthenticationAsync().execute(sd.getURL(subDrop));
	}

	public void drawActivity()
	{
		Spinner serverSpinner = (Spinner) findViewById(R.id.spinnerServer);  

		// bind to the ArrayList (needs a toString method in the SectorList class)
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, serverStore);
		spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down vieww
		serverSpinner.setAdapter(spinnerArrayAdapter);
	}

	/*
	 * OnClick method for the Test button
	 */

	public void onClick(View view)
	{	
		EditText username = (EditText) findViewById(R.id.username);
		EditText password = (EditText) findViewById(R.id.password);

		Spinner serverSpinner = (Spinner) findViewById(R.id.spinnerServer);  
		String server = serverSpinner.getSelectedItem().toString();

		String query = "";

		try {

			query = 
					"__csrf_magic=" + URLEncoder.encode(csrfString, "ISO-8859-1") +
					"&authmode=" + URLEncoder.encode(server, "ISO-8859-1") +
					"&username=" + URLEncoder.encode(username.getText().toString(), "ISO-8859-1") +
					"&password=" + URLEncoder.encode(password.getText().toString(), "ISO-8859-1") +
					"&Submit=" + URLEncoder.encode("Test", "ISO-8859-1") ;

			Log.d(TAG, "query: " + query);

			new postAuthenticationAsync().execute(query);

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateText(String result)
	{
		TextView authResult = (TextView) findViewById(R.id.result);
		authResult.setText(result);
	}


	/*
	 * Subclass for ASYNC task
	 * 
	 * Gets the first instance of the Authentication page so we can get the csrf & the list of servers etc
	 * 
	 */

	class AuthenticationAsync extends AsyncTask<String, Void, String> {

		ProgressDialog dialogT;

		@Override
		protected void onPreExecute() {
			try {
				dialogT = new ProgressDialog(AuthenticationActivity.this);
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
			scrapeAuthPage(result);
			drawActivity();
			dialogT.dismiss();
		}
	} // end of AuthentcationAsync subclass

	/*
	 * Subclass for ASYNC task
	 * 
	 * Sends the Authentication search as a HTTP post and then scrapes the page for the result and outputs the result
	 */

	class postAuthenticationAsync extends AsyncTask<String, Void, String> {

		ProgressDialog dialogT;

		@Override
		protected void onPreExecute() {
			try {
				dialogT = new ProgressDialog(AuthenticationActivity.this);
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
			String authResult = scrapeAuthResult(result); 
			updateText(authResult);
			dialogT.dismiss();
		}
	} // end of postAuthenticationAsync subclass


	/*
	 * Method to scrape the page that pfsense renders (pre any search) 
	 * 
	 * Extract the csrf
	 * Extracts the list of servers
	 */

	public void scrapeAuthPage(String page)
	{
		Document doc = Jsoup.parse(page, "ISO-8859-1");

		// get the csrf
		Element csrf = doc.select("form input[name=__csrf_magic]").first();
		csrfString = csrf.attr("value"); 
		Log.d(TAG, "csrf: " +csrfString);

		// get list of servers
		Elements servers = doc.select("select[name=authmode] option");

		serverStore = new ArrayList<String>();

		for (int i=0; i<servers.size(); i++)
		{
			Element e = servers.get(i);
			serverStore.add(i, e.text());
		}
		// end of scraping servers
	}

	/*
	 * Method to scrape the results of authentication 
	 * 
	 * Returns the result as a String
	 */

	public String scrapeAuthResult(String page)
	{
		Document doc = Jsoup.parse(page, "ISO-8859-1");

		// get csrf
		Element csrf = doc.select("form input[name=__csrf_magic]").first();
		csrfString = csrf.attr("value"); 
		Log.d(TAG, "csrf: " +csrfString);

		// get returned message
		Element output = doc.select("table.infobox").first();

		if(output == null) // if unsuccessful get error message
			output = doc.select("div#inputerrorsdiv").first();

		return output.text();
	}


} // end of class
package charlie.marshall.pfsense;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

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
import android.widget.EditText;
import android.widget.TextView;


public class DnsActivity extends CustomActivity
{
	private Pfsense pf;
	private SubDrop sd;
	private int menu, subDrop;

	String csrfString;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dns);

		// get data from intent
		Intent i = getIntent();
		pf = (Pfsense)i.getSerializableExtra("pf");
		menu = i.getIntExtra("menu", 0);
		subDrop = i.getIntExtra("subDrop", 0);

		sd = pf.getSubDrops(menu);
	}

	/*
	 * OnClick methods for dns lookup (only 1 button so coded in the method)
	 */

	public void onClick(View view)
	{	
		EditText hostname = (EditText) findViewById(R.id.hostname);
		String query = "";

		try {
			query = 
					"host=" + URLEncoder.encode(hostname.getText().toString(), "ISO-8859-1") +
					"&Submit=" + URLEncoder.encode("DNS Lookup", "ISO-8859-1") ;

			Log.d(TAG, "query: " + query);

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new DnsLookup().execute(query);
	}

	public void updateText(String hostText1, String hostText2, String one, String two)
	{
		TextView tx1 = (TextView) findViewById(R.id.col1);
		TextView tx2 = (TextView) findViewById(R.id.col2);
		TextView hostx1 = (TextView) findViewById(R.id.host1);
		TextView hostx2 = (TextView) findViewById(R.id.host2);
		hostx1.setText(hostText1);
		hostx2.setText(hostText2);
		tx1.setText(one);
		tx2.setText(two);
	}

	/*
	 * Subclass for ASYNC task
	 * 
	 * send DNS query via Http Post
	 */

	class DnsLookup extends AsyncTask<String, Void, String> {

		ProgressDialog dialogT;

		@Override
		protected void onPreExecute() {
			try {
				dialogT = new ProgressDialog(DnsActivity.this);
				dialogT.setMessage("Looking up DNS...");
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
				String dnsPage = "";
				
				if (pf.getProtocol().equals("HTTP"))
				{
					HttpMethods methods = new HttpMethods(pf.getHttpCookieStore());
					dnsPage = methods.getDNSPage(new URL(pf.getPfURL() + sd.getURL(subDrop)), args[0]);
				}
				else
				{
					HttpsMethods methods = new HttpsMethods(pf.getHttpsCookieStore());
					dnsPage = methods.getDNSPage(new URL(pf.getPfURL() + sd.getURL(subDrop)), args[0]);
				}
				
				// TODO make method void
				return dnsPage;

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			return "error";
		}


		@Override
		protected void onPostExecute(String result) {
			scrapeDns(result);
			dialogT.dismiss();
		}
	} // end of PfPower subclass


	public void scrapeDns(String page)
	{
		Document doc = Jsoup.parse(page, "ISO-8859-1");

		// TODO check this is being updated!

		// get csrf
		Element csrf = doc.select("form input[name=__csrf_magic]").first();
		String csrfString = csrf.attr("value"); 
		Log.d(TAG, "csrf: " +csrfString);

		//get ip/hostname we entered 
		Element host = doc.select("form input[name=host]").first();
		String host1 = host.attr("value");

		//get the result
		Element retrieved = doc.select("font").first();
		String host2 = retrieved.text();
		Log.d(TAG, "host: " + host1 + " - " + host2);

		Elements table = doc.select("table");
		Elements td = table.get(3).select("td");

		String hostText1 = host1;
		String hostText2 = host2;

		String one = "\n";
		String two = "\n"; 
		int i=0;
		for (Element e : td)
		{

			if (i % 2==0) 
				one += e.text()  + "\n";
			else
				two +=  e.text() + "\n";

			i++;
		}

		updateText(hostText1, hostText2, one, two);
	}


} // end of class
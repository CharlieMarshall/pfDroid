package charlie.marshall.pfsense;

import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

// TODO check MAC addresses are correctly formatted
// TODO check descriptions are not empty - we cant have an empty client

public class InterfacesActivity extends CustomActivity
{
	private Pfsense pf;
	private SubDrop sd;
	private int menu, subDrop;


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
		new PfPower().execute(sd.getURL(subDrop));
	}

	/*
	 * Subclass for ASYNC task
	 * 
	 * Gets the WOL page
	 */

	class PfPower extends AsyncTask<String, Void, String>
	{

		ProgressDialog dialogT;

		@Override
		protected void onPreExecute()
		{
			try {
				dialogT = new ProgressDialog(InterfacesActivity.this);
				dialogT.setMessage("Scraping page...");
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
			dialogT.dismiss();

		}
	}

	public void scrapeInterface(String page)
	{
		Document doc = Jsoup.parse(page, "ISO-8859-1");

		// get csrf
		Element csrf = doc.select("form input[name=__csrf_magic]").first();
		String csrfString = csrf.attr("value"); 

		//get interface status 

		//form action="interfaces.php"
		//input name="enable" type="checkbox" value="yes"

		Element enabled = doc.select("form[action=interfaces.php] input[name=enable]").first();
		String enableInterface = enabled.attr("value"); 

		Element description = doc.select("form[action=interfaces.php] input[name=descr]").first();
		String desc = description.attr("value"); 

		Element mac = doc.select("form[action=interfaces.php] input[name=spoofmac]").first();
		String macAddress = mac.attr("value"); 

		Element mtu = doc.select("form[action=interfaces.php] input[name=mtu]").first();
		String MTU = mtu.attr("value"); 

		Element mss = doc.select("form[action=interfaces.php] input[name=mss]").first();
		String MSS = mss.attr("value"); 
	}

}
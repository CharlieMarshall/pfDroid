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
import android.widget.ListView;
import android.widget.RelativeLayout;

public class SystemInterfacesActivity extends ListActivity
{
	private Pfsense pf;
	private SubDrop sd;
	private int menu, subDrop;

	private ArrayList<InterfaceStatusInfo> interfaces;
	private String TAG = "pfsense_app";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// get data from intent
		Intent i = getIntent();
		pf = (Pfsense)i.getSerializableExtra("pf");
		menu = i.getIntExtra("menu", 0);
		subDrop = i.getIntExtra("subDrop", 0);

		sd = pf.getSubDrops(menu);

		new PfScrape().execute(sd.getURL(subDrop));

	}

	/*
	 * Method to populate and draw the ListView
	 */

	public void drawList()
	{
		Log.d(TAG, "size of interfacse: " + interfaces.size());
		setListAdapter(new InterfaceStatusArrayAdapter(this, interfaces)) ;
		ListView listView = getListView();
		listView.setTextFilterEnabled(true);
	}

	/*
	 * OnClick methods for the disconnect/connect & info buttons 
	 */

	public void onClick(View view)
	{	
		final int position = getListView().getPositionForView((RelativeLayout)view.getParent());
		InterfaceStatusInfo t = interfaces.get(position);;

		Log.d(TAG, "position:" + position);

		switch (view.getId()) 
		{
		case R.id.interfaceInfoBtn:
			Log.d(TAG, "INFO");
			viewInfo(t);
			break;
		case R.id.interfaceStatusBtn:
			Log.d(TAG, "connect/diconnect");
			Log.d(TAG, "link: " + t.getLink());

			// TODO confirm working then uncomment
			//new PfGet().execute(sd.getURL(subDrop) + "/"  + t.getLink());
			break;
		}
	}

	public void viewInfo(InterfaceStatusInfo interfaceInfo)
	{
		Intent i = new Intent(SystemInterfacesActivity.this, ViewInterfaceStatusActivity.class);
		i.putExtra("interface" , interfaceInfo);
		startActivity(i);
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
					page = methods.getPfPage(new URL(pf.getPfURL() + args[0]));
				}
				else
				{
					HttpsMethods methods = new HttpsMethods(pf.getHttpsCookieStore());
					page = methods.getPfPage(new URL(pf.getPfURL() + args[0]));
				}

				scrapePage(page);

				// TODO make this method void
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


	} // end of PfScrape subclass


	/*
	 * Method to scrape the status interfaces page
	 * 
	 * TODO NEED TO DEFINE METHOD HERE
	 */

	public void scrapePage(String page) throws IOException{

		interfaces = new ArrayList<InterfaceStatusInfo>();
		InterfaceStatusInfo interfaceInfo = null;
		InterfaceStatus interStatus = new InterfaceStatus(); 

		Document doc = Jsoup.parse(page, "ISO-8859-1");

		Elements table = doc.select("table");

		int interfaceCount = 0;
		String value = "";
		boolean statusTag = false;
		boolean typeTag = false;

		Elements e = table.select("td");
		for (Element y : e)
		{

			if(y.hasClass("listtopic")) // interface name / signifies start of a interface
			{
				if(interfaceCount!=0) // add last interface scraped to the arrayList
					interfaces.add(interfaceInfo);

				interfaceInfo = new InterfaceStatusInfo(y.text()); // constructor with interface name as parameter
				interfaceCount++;
			}
			else if(y.hasClass("vncellt")) // this is a heading
			{
				statusTag = false;
				typeTag = false;
				
				if(y.text().equals("Status"))
					statusTag = true;
				else if( (y.text().equals("PPPoE")) || (y.text().equals("PPTP")) || (y.text().equals("L2TP")) || (y.text().equals("PPP")) )
				{
					typeTag = true;
					interfaceInfo.setInterfaceType(true, y.text());
				}
				interStatus = new InterfaceStatus(); 
				interStatus.setHeading(y.text());
			}
			else if(y.hasClass("listr")) // this is a value
			{

				if((statusTag == true))
				{
					if(y.text().equals("up"))
						interfaceInfo.setStatus(true);
					
					value = y.text();
					interfaceInfo.setStatusStr(y.text());
				}

				else if(typeTag == true)
				{
					interfaceInfo.setInterfaceTypeValue(y.text());

					if(y.childNodeSize()>1)
					{
						if(y.child(0).hasAttr("href"))
						{
							interfaceInfo.setLink(y.child(0).attr("href"));
							value = y.text().replace("\u00a0","");
							interfaceInfo.setInterfaceTypeValue(value);
						}
						else // handles ISP DNS servers (<br>) 
							value = y.text();
					}
				}
				else
					value = y.text();
				
				interStatus.setValue(value);
				interfaceInfo.addInterface(interStatus);

			}
		}
		interfaces.add(interfaceInfo);

		// DEBUGGING DELETE BELOW WHEN HAPPY

		/*
		for (int z=0; z<interfaces.size(); z++)
		{
			Log.d(TAG, "print : " + z);
			InterfaceStatusInfo w = interfaces.get(z);
			w.printTest();
		}

		Log.d(TAG, "----------------------");
		for (int z=0; z<interfaces.size(); z++)
		{
			InterfaceStatusInfo w = interfaces.get(z);

			Log.d(TAG, "INTERFACE: " + w.getHeader());

			if(w.getStatus()==true)
				Log.d(TAG, "Status: UP");
			else
				Log.d(TAG, "Status: DOWN");

			if(w.hasButton==true)
			{
				Log.d(TAG, "HAS A BUTTON");
				Log.d(TAG, "link: " + w.getLink());
			}
			if(w.hasType==true)
			{
				Log.d(TAG, "type: " + w.getType());
				Log.d(TAG, "type value: " + w.getTypeValue());
			}	
		} // end of debugging
		 */
	}

}

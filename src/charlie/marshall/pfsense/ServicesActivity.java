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

public class ServicesActivity extends ListActivity
{
	private Pfsense pf;
	private SubDrop sd;
	private int menu, subDrop;
	private String TAG = "pfsense_app";
	private ArrayList<Services> servicesStore;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// get data from intent
		Intent i = getIntent();
		pf = (Pfsense)i.getSerializableExtra("pf");
		menu = i.getIntExtra("menu", 0);
		sd = pf.getSubDrops(menu);
		
		subDrop = i.getIntExtra("subDrop", 0);
		new PfGet().execute(sd.getURL(subDrop));
	}

	/*
	 * Method to populate and draw the ListView
	 */

	public void drawList()
	{
		setListAdapter(new ServicesArrayAdapter(this, servicesStore)) ;
		ListView listView = getListView();
		listView.setTextFilterEnabled(true);
	}
	
	/*
	 * OnClick methods for the start, stop buttons 
	 */

	public void onClick(View view)
	{	
		final int position = getListView().getPositionForView((RelativeLayout)view.getParent());
		Services s = servicesStore.get(position);;
		
		switch (view.getId()) 
		{
		case R.id.startService:		
			new PfGet().execute(sd.getURL(subDrop) + "/" + s.getStart());
			break;
		case R.id.stopService:
			new PfGet().execute(sd.getURL(subDrop) + "/"  + s.getStop());
			break;
		}
	}

	/*
	 * Subclass for ASYNC task
	 * 
	 * Gets the services page
	 */

	class PfGet extends AsyncTask<String, Void, String>
	{

		ProgressDialog dialogT;

		@Override
		protected void onPreExecute()
		{
			try 
			{
				dialogT = new ProgressDialog(ServicesActivity.this);
				dialogT.setMessage("Performing task...");
				dialogT.setIndeterminate(true);
				dialogT.setCancelable(false);
				dialogT.show();
			}
			catch (Exception e)
			{
				Log.d(TAG, "ASYNC task exception, onPreExecute: " + e);
			}
		}

		@Override
		protected String doInBackground(String... args)
		{

			try
			{
				
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
				return "";

			} catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			return "error";
		}

		@Override
		protected void onPostExecute(String result)
		{
			drawList();
			dialogT.dismiss();
		}
		
	} // end of PfGet subclass


	/*
	 * Method to scrape the services page
	 * 
	 * Saves services into an ArrayList serviceStore
	 * 
	 */

	public void scrapePage(String page) throws IOException
	{
		Services service = null;
		
		Document doc = Jsoup.parse(page, "ISO-8859-1");
		
		servicesStore = new ArrayList<Services>();

		Elements tableRow = doc.select("table[class=tabcont sortable] tr");
		for (int i=1; i<tableRow.size(); i++) // i = 1 as the first tr contains the headings
		{
			Element e = tableRow.get(i);
			
			Elements services = e.select("td.listlr");
			for (int y=0; y<services.size(); y++)
			{
				Element s = services.get(y);
				service = new Services(s.text());
			}
			
			Elements text = e.select("td.listr");

			// if service is running listr will have 2 entries - size ==2
			// if service is not running - size == 1 & listbg == 1 
			// listbg - contains 'Stopped'
			// listr - contains the 'descriptions' as well as 'Running'

			if(text.size()==1)
			{
				service.setDesc(text.text());
				Element status = tableRow.select("td.listbg").first();
				service.setStatus(status.text());
				
				Elements startLink = e.select("td.list a");
				service.setStart(startLink.attr("href"));
				
				Log.d(TAG, "here is the link: " + startLink.attr("href"));
			}
			else
			{
				for (int y=0; y<text.size(); y++)
				{
					if(y==0)
						service.setDesc(text.get(y).text());
					else if(y==1)
						service.setStatus(text.get(y).text());
				}
				
				Elements links = e.select("td.list a");
				//Log.d(TAG, "number of links: " + links.size());
				
				Log.d(TAG, "here is the start link: " + links.get(0).attr("href"));
				Log.d(TAG, "here is the stop link: " + links.get(1).attr("href"));
				service.setStart(links.get(0).attr("href"));
				service.setStop(links.get(1).attr("href"));
			}
		servicesStore.add(service);
		}
	}

}

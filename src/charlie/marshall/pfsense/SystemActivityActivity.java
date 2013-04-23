package charlie.marshall.pfsense;

import java.net.URL;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class SystemActivityActivity extends CustomActivity
{
	private Pfsense pf;
	SubDrop sd;
	int menu, subDrop;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// get data from intent
		Intent i = getIntent();
		pf = (Pfsense)i.getSerializableExtra("pf");
		menu = i.getIntExtra("menu", 0);
		subDrop = i.getIntExtra("subDrop", 0);

		// get the title of the activity
		//setTitle(pf.getLinkHeader(menu));

		setContentView(R.layout.activity_sys_activity);

		sd = pf.getSubDrops(menu);



		new PfPower().execute(sd.getURL(subDrop));

	}

	public void setText(String text)
	{
		TextView textView = (TextView) findViewById(R.id.textView);
		textView.setText(text);
	}


	/*
	 * Subclass for ASYNC task
	 * 
	 * Attempts to login in to money extra and save the HTML in a txt file to the SD card
	 */

	class PfPower extends AsyncTask<String, Void, String> {

		ProgressDialog dialogT;

		@Override
		protected void onPreExecute() {
			try {
				dialogT = new ProgressDialog(SystemActivityActivity.this);
				dialogT.setMessage("Logging In...");
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
				String displayText = "";
				
				if (pf.getProtocol().equals("HTTP"))
				{
					HttpMethods methods = new HttpMethods(pf.getHttpCookieStore());
					displayText = methods.getSysAct(new URL(pf.getPfURL() + args[0]));
				}
				else
				{
					HttpsMethods methods = new HttpsMethods(pf.getHttpsCookieStore());
					displayText = methods.getSysAct(new URL(pf.getPfURL() + args[0]));
				}
				return displayText;

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			return "error";
		}


		@Override
		protected void onPostExecute(String result) {
			dialogT.dismiss();
			setText(result);
		}


	} // end of PfPower subclass

}
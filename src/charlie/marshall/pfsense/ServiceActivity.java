package charlie.marshall.pfsense;

import java.net.URL;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

// TODO check MAC addresses are correctly formatted
// TODO check descriptions are not empty - we cant have an empty client

public class ServiceActivity extends CustomActivity
{
	private Pfsense pf;
	private SubDrop sd;
	private int menu, subDrop;

	private Services s;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_service);

		// TODO change this activity to return a result ie serviceSActivity should start looking for a result
		// this would reduce the number of lines!!!
		// would require to handle back button presses

		// get data from intent 
		// most of this is only required to pass back to the services intent
		Intent i = getIntent();
		pf = (Pfsense)i.getSerializableExtra("pf");
		menu = i.getIntExtra("menu", 0);
		subDrop = i.getIntExtra("subDrop", 0);
		sd = pf.getSubDrops(menu);

		s = (Services)i.getSerializableExtra("s");
		draw(s);
	}

	public void draw(Services s)
	{
		TextView name = (TextView) findViewById(R.id.name);
		TextView desc = (TextView) findViewById(R.id.desc);
		TextView status = (TextView) findViewById(R.id.status);

		Button startBtn = (Button) findViewById(R.id.start);
		Button stopBtn = (Button) findViewById(R.id.stop);

		name.setText(s.getName());
		desc.setText(s.getDesc());

		String serviceStatus = s.getStatus();
		status.setText(serviceStatus);

		if(serviceStatus.equals("Stopped"))
		{
			startBtn.setText("Start Service");
			stopBtn.setVisibility(View.GONE);		
		}
	}


	/*
	 * OnClick methods for the activity buttons
	 */

	public void onClick(View view)
	{	
		switch (view.getId()) 
		{
		case R.id.start:
			new PfWol().execute(sd.getURL(subDrop) + "/" + s.getStart());
			break;
		case R.id.stop:
			new PfWol().execute(sd.getURL(subDrop) + "/"  + s.getStop());
			break;
		}

	}


	/*
	 * Subclass for ASYNC task
	 * 
	 * Does an HTTP get with query string which contains whether to start or stop a service
	 */

	class PfWol extends AsyncTask<String, Void, String> {

		ProgressDialog dialogT;

		@Override
		protected void onPreExecute() {
			try {
				dialogT = new ProgressDialog(ServiceActivity.this);
				dialogT.setMessage("Changing service status...");
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
				if (pf.getProtocol().equals("HTTP"))
				{
					HttpMethods methods = new HttpMethods(pf.getHttpCookieStore());
					methods.getPfPage(new URL(pf.getPfURL() + args[0]));
				}
				else
				{
					HttpsMethods methods = new HttpsMethods(pf.getHttpsCookieStore());
					methods.getPfPage(new URL(pf.getPfURL() + args[0]));
				}

				// TODO make method void
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
			openServicesActivity();
		}


	} // end of PfPower subclass

	/*
	 * Method to return to the list of services where they will be scraped to get they're new status
	 */

	public void openServicesActivity()
	{
		Intent i = new Intent(this, ServicesActivity.class);
		i.putExtra("subDrop", subDrop);
		i.putExtra("menu", menu);
		i.putExtra("pf", pf);
		startActivity(i);

		finish();
	}

}
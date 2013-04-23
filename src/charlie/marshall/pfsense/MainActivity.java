package charlie.marshall.pfsense;

import java.net.URL;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends CustomActivity {

	private HttpCookieStore httpCookie;
	private HttpsCookieStore httpsCookie;
	private Pfsense pf = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}


	public void onClick(View view) {	

		switch (view.getId()) {

		case R.id.login:
			if (getUserSettings() == true)
				new PfLogin().execute();
			else
				Toast.makeText(getApplicationContext(), "You must every input all settings on the settings page", Toast.LENGTH_SHORT).show();

			break;
		case R.id.settings:
			startActivity(new Intent(this, MyPreferenceActivity.class));			
		}
	}

	public boolean getUserSettings()
	{
		// get the Preference file
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

		String username = prefs.getString("username", "").trim();
		String password = prefs.getString("password", "").trim();
		String domain = prefs.getString("domain", "").trim();
		String protocol = prefs.getString("protocol", "").trim();
		String port = prefs.getString("port", "").trim();

		if (!username.equals("") && !password.equals("") && !domain.equals("") && !port.equals("") )
		{
			pf = new Pfsense(username, password, domain, protocol, port);
			return true;
		}
		else
			return false;
	}

	/*
	 * Advance to Index activity
	 */

	public void openIndex(){		
		Intent i = new Intent(this, IndexActivity.class);	
		i.putExtra("pf", pf);
		startActivity(i);
	}

	/*
	 * Subclass for ASYNC task
	 * 
	 * Attempts to login in to money extra and save the HTML in a txt file to the SD card
	 */

	class PfLogin extends AsyncTask<String, Void, Integer> {

		ProgressDialog dialogT;

		@Override
		protected void onPreExecute() {
			try {
				dialogT = new ProgressDialog(MainActivity.this);
				dialogT.setMessage("Logging In...");
				dialogT.setIndeterminate(true);
				dialogT.setCancelable(false);
				dialogT.show();
			} catch (Exception e) {
				Log.d(TAG, "ASYNC task exception, onPreExecute: " + e);
			}
		}

		@Override
		protected Integer doInBackground(String... args) {
			int login = -1;

			try {				
				URL url = pf.getPfURL();

				Log.d(TAG, "URL from preferences: " + url);


				if (pf.getProtocol().equals("HTTP"))
				{
					Log.d(TAG, "HTTP WEB CONFIGURATOR");

					httpCookie = new HttpCookieStore();
					HttpMethods methods = new HttpMethods(httpCookie);				
					login = methods.login(pf.getUser(), pf.getPassword(), url);
					
					if (login == 0)
						pf.setLinks(methods.extractLinks());
				}
				else
				{
					Log.d(TAG, "SECURE HTTP WEB CONFIGURATOR");

					httpsCookie = new HttpsCookieStore();
					HttpsMethods methods = new HttpsMethods(httpsCookie);
					login = methods.login(pf.getUser(), pf.getPassword(), url);

					if (login == 0)
						pf.setLinks(methods.extractLinks());
				}

				return login;

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			return -1;
		}


		@Override
		protected void onPostExecute(Integer result) {

			dialogT.dismiss();

			Log.d(TAG, "GOT RETURN ITEM: " + result);
			switch(result)
			{
			case 0:
				// save the cookies
				pf.setHttpCookieStore(httpCookie);
				pf.setHttpsCookieStore(httpsCookie);
				openIndex();			
				break;
			case 1:
				Toast.makeText(getApplicationContext(), "Host not found", Toast.LENGTH_SHORT).show();
				break;
			case 2:
				Toast.makeText(getApplicationContext(), "IO Exception, have you trusted your certificate?", Toast.LENGTH_SHORT).show();
				break;
			case 3:
				Toast.makeText(getApplicationContext(), "ParserConfiguration Exception", Toast.LENGTH_SHORT).show();
				break;
			case 4:
				Toast.makeText(getApplicationContext(), "SAX Exception", Toast.LENGTH_SHORT).show();
				break;
			default:
				Toast.makeText(getApplicationContext(), "Unsuccessful", Toast.LENGTH_SHORT).show();
				break;
			} // end switch
		} //e end onPostExecute
	} // end of PfLogin subclass
}
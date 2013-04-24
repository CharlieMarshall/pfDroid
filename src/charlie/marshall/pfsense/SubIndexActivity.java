package charlie.marshall.pfsense;

import java.net.URL;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class SubIndexActivity extends ListActivity {
	private Pfsense pf;
	SubDrop sd;
	int menu;
	String TAG = "pfsense_app";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// get data from intent
		Intent i = getIntent();
		pf = (Pfsense)i.getSerializableExtra("pf");
		menu = i.getIntExtra("display", 0);

		// set the title of the activity
		setTitle(pf.getLinkHeader(menu));

		sd = pf.getSubDrops(menu);

		setListAdapter(new ArrayAdapter<String>(this, R.layout.activity_index, sd.getSubDropNames()));

		ListView listView = getListView();
		listView.setTextFilterEnabled(true);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Intent i = null; // or should this be new Intent() ??

				Log.d(TAG, "TITLE:" + sd.getHeader());
				Log.d(TAG, "SD:" + sd.getTitle(position));

				// BIG if/else if statement 
				// determines which activity to open on a click
				// outer if/else if for the main menu titles
				// inner is for the subpage titles
				// Toast msg if no activity found for page eg not yet supported

				if(sd.getHeader().equals("System"))
				{
					if (sd.getTitle(position).equals("Logout"))
					{
						new PfPower().execute("/?logout"); // logout of webGui session
						
						// logout of app
						Intent returnIntent = new Intent();
						setResult(RESULT_OK,returnIntent);     
						finish();
						return;
					}
					else
					{
						Toast.makeText(getApplicationContext(), "TO DO", Toast.LENGTH_SHORT).show();
						return;
					}
				}
				else if(sd.getHeader().equals("Interfaces"))
				{
					if (!sd.getTitle(position).equals("(assign)"))
					{
						Log.d(TAG, "clicked an interface");
						i = new Intent(SubIndexActivity.this, InterfacesActivity.class);
					}
					else
					{
						Toast.makeText(getApplicationContext(), "TO DO", Toast.LENGTH_SHORT).show();
						return;
					}
				}
				else if(sd.getHeader().equals("Firewall"))
				{
					if (sd.getTitle(position).equals("Aliases"))
						i = new Intent(SubIndexActivity.this, AliasActivity.class);
					else
					{
						Toast.makeText(getApplicationContext(), "TO DO", Toast.LENGTH_SHORT).show();
						return;
					}
				}
				else if(sd.getHeader().equals("Services"))
				{
					if (sd.getTitle(position).equals("Wake on LAN"))
						i = new Intent(SubIndexActivity.this, WolActivity.class);
					else
					{
						Toast.makeText(getApplicationContext(), "TO DO", Toast.LENGTH_SHORT).show();
						return;
					}
				}
				else if(sd.getHeader().equals("VPN"))
				{
					Toast.makeText(getApplicationContext(), "TO DO", Toast.LENGTH_SHORT).show();
					return;
				}
				else if(sd.getHeader().equals("Status"))
				{
					if (sd.getTitle(position).equals("Interfaces"))
						i = new Intent(SubIndexActivity.this, SystemInterfacesActivity.class);
					else if (sd.getTitle(position).equals("Services"))
						i = new Intent(SubIndexActivity.this, ServicesActivity.class);
					else
					{
						Toast.makeText(getApplicationContext(), "TO DO", Toast.LENGTH_SHORT).show();
						return;
					}
				}
				else if(sd.getHeader().equals("Diagnostics"))
				{ 
					if (sd.getTitle(position).equals("ARP Table"))
						i = new Intent(SubIndexActivity.this, ArpActivity.class);
					else if (sd.getTitle(position).equals("Authentication"))
						i = new Intent(SubIndexActivity.this, AuthenticationActivity.class);
					else if (sd.getTitle(position).equals("DNS Lookup"))
						i = new Intent(SubIndexActivity.this, DnsActivity.class);
					else if (sd.getTitle(position).equals("Factory Defaults"))
					{
						// TODO reinstate when app is stable
						// TODO perhaps have a double warning

						//confirmDelete("Are you sure you want to reset to factory defaults?", sd.getURL(position));
						return;
					}
					else if (sd.getTitle(position).equals("Halt System"))
					{
						confirmDelete("Are you sure you want to halt the system?", sd.getURL(position));
						return;
					}
					else if (sd.getTitle(position).equals("Limiter Info"))
						i = new Intent(SubIndexActivity.this, LimiterInfoActivity.class);
					else if (sd.getTitle(position).equals("pfInfo"))
						i = new Intent(SubIndexActivity.this, pfInfoActivity.class);
					else if (sd.getTitle(position).equals("pfTop"))
						i = new Intent(SubIndexActivity.this, pfTopActivity.class);
					else if (sd.getTitle(position).equals("Ping"))
						i = new Intent(SubIndexActivity.this, PingActivity.class);
					else if (sd.getTitle(position).equals("Reboot"))
					{
						confirmDelete("Are you sure you want to reboot the system?", sd.getURL(position));
						return;
					}
					else if (sd.getTitle(position).equals("System Activity"))
						i = new Intent(SubIndexActivity.this, SystemActivityActivity.class);
					else if (sd.getTitle(position).equals("Traceroute"))
						i = new Intent(SubIndexActivity.this, TracerouteActivity.class);
					else
					{
						Toast.makeText(getApplicationContext(), "TO DO", Toast.LENGTH_SHORT).show();
						return;
					}
				}

				else if(sd.getHeader().equals("Help"))
				{
					if(sd.getTitle(position).equals("About this Page"))
					{
						Toast.makeText(getApplicationContext(), "Sorry this link is unsupported in the app", Toast.LENGTH_SHORT).show();
						return;
					}
					else
					{
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(sd.getURL(position))));
						return;
					}
				}				

				//We should only get here if we are opening a new activity

				// Now add the parameters to the Intent
				i.putExtra("menu", menu);
				i.putExtra("subDrop", position);
				i.putExtra("pf", pf);
				startActivity(i);
			}
		});
	}

	/*
	 * AlertDialog to confirm whether to carry out reboot/halt/factory
	 * 
	 * Ok = run Async task to reboot/halt/factory 
	 * Cancel = do nothing
	 */

	public void confirmDelete(String msg, final String url) {
		AlertDialog dialog = new AlertDialog.Builder(this)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle("Confirmation prompt")
		.setMessage(msg)
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				dialog.dismiss();	
				new PfPower().execute(url);
			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.create();
		dialog.show();
	}


	/*
	 * Subclass for ASYNC task
	 * 
	 * This thread is run to send a halt / reboot / reset to factory - command to pfsense
	 */

	class PfPower extends AsyncTask<String, Void, Integer> {

		ProgressDialog dialogT;

		@Override
		protected void onPreExecute() {
			try {
				dialogT = new ProgressDialog(SubIndexActivity.this);
				dialogT.setMessage("Sending command...");
				dialogT.setIndeterminate(true);
				dialogT.setCancelable(false);
				dialogT.show();
			} catch (Exception e) {
				Log.d(TAG, "ASYNC task exception, onPreExecute: " + e);
			}
		}

		@Override
		protected Integer doInBackground(String... args) {
			int login = -1; // TODO currently we are just returning this !!!!!!

			try {				

				if (pf.getProtocol().equals("HTTP"))
				{
					HttpMethods methods = new HttpMethods(pf.getHttpCookieStore());				
					methods.getPower(new URL(pf.getPfURL() + args[0]));
				}
				else
				{
					HttpsMethods methods = new HttpsMethods(pf.getHttpsCookieStore());
					methods.getPower(new URL(pf.getPfURL() + args[0]));
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

			// TODO handle result code

			dialogT.dismiss();
			finish();
		}

	} // end of PfPower subclass

}
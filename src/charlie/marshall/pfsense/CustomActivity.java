package charlie.marshall.pfsense;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


public class CustomActivity extends Activity {

	static final String TAG = "pfsense_app";

	/*
	 * onCreateOptionsMenu()
	 * 
	 * Method to bring up the options menu (menu button)
	 * 
	 */


	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);

		return true;
	}

	/*
	 * onOptionsItemSelected()
	 *
	 * Method to handle the options menu selection (clicks/touches)
	 *
	 */	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			startActivity(new Intent(this, MyPreferenceActivity.class));			
			break;
		case R.id.about:
			aboutDialog();
			break;
		case R.id.rate:
			rateApp();
			break;
		}
		return false;
	} // end of onOptionsItemSected()

	private void rateApp() {
		Uri uri = Uri.parse("market://details?id=" + this.getPackageName());
		Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
		try {
			startActivity(goToMarket);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, "Couldn't launch the market", Toast.LENGTH_LONG).show();
		}
	}

	public void aboutDialog() {
		String version = "";
		try{
			version = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			version = "Error fetching version number";
		}
		
		String message = "Version: " + version +"\nAuthor: Charlie Marshall\n\n\nThis is an alpha release for testing only. Please report any issues to the pfsense forum http://forum.pfsense.org/index.php/topic,61416.0.html";
		
		// Linkify the message
	    final SpannableString s = new SpannableString(message);
	    Linkify.addLinks(s, Linkify.ALL);

	    
		AlertDialog dialog = new AlertDialog.Builder(this)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle("pfSense androidGUI app")
		.setMessage(s)
		.setPositiveButton("Close",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,
					int which) {
				dialog.dismiss();
			}
		}).create();
		dialog.show();
		
		// Make the textview clickable. Must be called after show()
	    ((TextView)dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
	}
	

} // end of Home class
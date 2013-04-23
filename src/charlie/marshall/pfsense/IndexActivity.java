package charlie.marshall.pfsense;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class IndexActivity extends ListActivity {
	private Pfsense pf;
	String TAG = "pfsense_app";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent i = getIntent();
		pf = (Pfsense)i.getSerializableExtra("pf");
		
		setListAdapter(new ArrayAdapter<String>(this, R.layout.activity_index, pf.getLinks()));
		 
		ListView listView = getListView();
		listView.setTextFilterEnabled(true);
 
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
			    
			    openSubIndex(position);
			}
		});
	}
	
	/*
	 * Advance to SubIndexActivity
	 */
	
	public void openSubIndex(int display){		
		Intent i = new Intent(this, SubIndexActivity.class);
		i.putExtra("display", display);
		i.putExtra("pf", pf);
		startActivityForResult(i, 1);
	}
	
	/*
	 * Method to handle logout command from SubIndexActivity
	 */
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		  if (requestCode == 1) {

		     if(resultCode == RESULT_OK){      
		         finish();          
		     }
		     if (resultCode == RESULT_CANCELED) {    
		         // Do nothing
		     }
		  }
		}//onActivityResult

}
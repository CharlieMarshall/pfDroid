package charlie.marshall.pfsense;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

@SuppressWarnings("serial") //with this annotation we are going to hide compiler warning
public class Navigation implements Serializable 
{
	private List<SubDrop> store;
	static final String TAG = "pfsense_app";  // LogCat tag

	/*
	 * Constructor
	 */

	public Navigation()
	{	
		store = new ArrayList<SubDrop>();
	}

	public void addToStore(int location, SubDrop subdrop)
	{
		store.add(subdrop);
	}

	public void printLinks()
	{
		Log.d(TAG, "PRINT ALL CONTENTS:");
		for (int i=0; i<store.size(); i++)
		{
			store.get(i).printSubDrop();
		}
	}

	public void printHeaderList()
	{
		Log.d(TAG, "PRINT HEADER LIST:");

		for (int i=0; i<store.size(); i++)
		{
			Log.d(TAG, store.get(i).getHeader());
		}
	}

	public String getHeader(int i)
	{
		return store.get(i).getHeader();
		
	}
	public String[] getHeaderList()
	{
		String[] tmp = new String[store.size()];
		for (int i=0; i<store.size(); i++ )
		{
			tmp[i] = store.get(i).getHeader();
		}
		return tmp;
	}
	
	public SubDrop getSubDrops(int i)
	{
		return store.get(i);
	}

}

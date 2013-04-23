package charlie.marshall.pfsense;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

@SuppressWarnings("serial") //with this annotation we are going to hide compiler warning
public class SubDrop implements Serializable
{

	static final String TAG = "pfsense_app";
	private List<Links> subdrop;
	private String header = "";

	
	public SubDrop(String header)
	{		
		this.header = header;
		subdrop = new ArrayList<Links>();
	}
	
	
	
	public void addSubDrop(String title, String url)
	{
		subdrop.add(new Links(title, url));
	}
	
	public void printSubDrop()
	{
		Log.d(TAG, "Menu TITLE: " + header);

		for (int i=0; i<subdrop.size(); i++)
		{
			Log.d(TAG, "Item name: " + subdrop.get(i).getTitle());
			Log.d(TAG, "Item link: " + subdrop.get(i).getUrl());
		}
	}
	
	public String getHeader()
	{
		return header;
	}

	public String getTitle(int i)
	{
		return subdrop.get(i).getTitle();
	}
	
	public String getURL(int i)
	{
		return subdrop.get(i).getUrl();
	}

	public String[] getSubDropNames()
	{
		String[] tmp = new String[subdrop.size()];
		for (int i=0; i<subdrop.size(); i++ )
		{
			tmp[i] = subdrop.get(i).getTitle();
		}
		return tmp;
	}
	
	
	public int getSize()
	{
		return subdrop.size();
	}
}
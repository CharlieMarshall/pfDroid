package charlie.marshall.pfsense;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.ParserConfigurationException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import android.util.Log;

public class HttpsMethods
{

	static final String TAG = "pfsense_app";

	HttpsURLConnection httpsconn;
	URL loginurl;
	Scanner inStream;
	HttpsCookieStore cookieStore = null;


	/*
	 * Constructor method
	 * 
	 * Sets the passed argument cookieStore to the global variable cm
	 * 
	 */
	
	public HttpsMethods(HttpsCookieStore cookieStore) throws MalformedURLException
	{
		this.cookieStore = cookieStore;
	}


	/*
	 * 
	 * Does the initial login with username and password
	 * 
	 */

	public int login(String username, String password, URL url)
	{
		Log.d(TAG, "start login()");

		loginurl = url;

		// Not sure why maybe cookie related we HAVE to call getPage() before we can successfully POST (login)
		try
		{
			getPage();


			String query = "usernamefld=" + username + "&passwordfld=" + URLEncoder.encode(password,"ISO-8859-1") + "&login=Login";


			if ( postForm(url, query) == 200 )
			{
				Log.d(TAG, "HTTP POST of login was received successfully");
				getPage();
				return 0;
			}

		}		
		catch (UnknownHostException e)
		{
			Log.d(TAG, "CAUGHT Exception!: " + e.getMessage());
			return 1;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 2;
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 3;
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 4;
		}
		return 5;
	}



	public Scanner getPage() throws IOException, ParserConfigurationException, SAXException
	{
		int responseCode = 0;

		// TODO remove loop is it needed? surely only for redirects
		while (responseCode != 200 )
		{

			switch(responseCode)
			{
			case 302:
				Log.d(TAG, "302 response");
				loginurl = new URL (httpsconn.getHeaderField("Location"));
			case 0:
				// For when I have reset it to 0

				httpsconn=(HttpsURLConnection)loginurl.openConnection();
				cookieStore.setCookiesHTTPS(httpsconn);
				httpsconn.setRequestMethod("GET");
				inStream = new Scanner(httpsconn.getInputStream(), "ISO-8859-1");
				responseCode = httpsconn.getResponseCode();
				cookieStore.storeCookiesHTTPS(httpsconn);

				break;
			case 403:
				Log.d(TAG, "403 response");
				return inStream;
			default:
				//TODO
				Log.d(TAG, "NEED TO HANDLE");
				break;
			}

		} // end of while loop

		return inStream;
	}


	/*
	 * 
	 * Method to HTTPs post a form
	 * 
	 * Arguments: URL to post to and the query string ( already encoded )
	 * 
	 * Returns: The response code
	 * 
	 */

	public int postForm(URL url, String parameters) throws IOException
	{

		try{
			Log.d(TAG, "postForm()");
			loginurl = url;

			Log.d(TAG, "URL: " + loginurl.toString());

			httpsconn=(HttpsURLConnection)loginurl.openConnection();
			cookieStore.setCookiesHTTPS(httpsconn);

			httpsconn.setDoOutput(true);
			httpsconn.setRequestMethod("POST");
			httpsconn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			// send the POST out
			PrintWriter out = new PrintWriter(httpsconn.getOutputStream());
			out.print(parameters);
			out.close();

			// get the cookie from the connection
			cookieStore.storeCookiesHTTPS(httpsconn);

			inStream = new Scanner(httpsconn.getInputStream(),"ISO-8859-1");

		}
		catch(Exception e){
			Log.d(TAG, "Exception: " + e.getMessage().toString());

			// TODO what should I return here? Use a finally and return the real response code?
			return 404;
		}

		return httpsconn.getResponseCode();
	}	

	/*
	 * 
	 * Method to extract and return the CSRF ( Cross-site request forgery ) 
	 * 
	 */

	public Navigation extractLinks() throws IOException
	{
		Document doc = Jsoup.parse(httpsconn.getInputStream(), "ISO-8859-1", loginurl.toString() );

		Elements menus = doc.select("#navigation li");

		int title =0;
		Navigation check = new Navigation();
		SubDrop sd = null;
		for (Element test : menus)
		{			
			for (Element div : test.select("div"))
			{

				if (sd != null) // stops adding a blank item at the beginning
					check.addToStore(title, sd);

				sd = new SubDrop (div.text());
				title ++;
			}

			for (Element link : test.select(".subdrop a"))
				sd.addSubDrop(link.text(), link.attr("href"));

		}
		check.addToStore(title, sd); // adds the last one!

		return check;
	}


	/*
	 * 
	 * Method to print the inputStream to LogCat
	 * 
	 */

	public void printStream(Scanner stream) throws IOException
	{
		while(stream.hasNextLine()){
			Log.d(TAG, stream.nextLine());
		}
	}

	public String getStream(Scanner stream) throws IOException
	{
		StringBuilder sb = new StringBuilder();

		while(stream.hasNextLine()){
			sb.append(stream.nextLine());
			sb.append("\n");
		}

		return sb.toString();
	}

	public void getDNS(URL url) 
	{
		Log.d(TAG, "getDNS");
		loginurl = url;
		Log.d(TAG, "url: " + url);
		try
		{
			getPage();
		}
		catch(Exception e){
			// TODO Auto-generated method stub
		}	

	}

	/*
	 * 
	 * Method to reboot/halt the system
	 * 
	 */

	public void getPower(URL url) 
	{
		Log.d(TAG, "POWER");
		loginurl = url;
		Log.d(TAG, "url: " + url);
		try
		{
			getPage(); // we need to get this page to scrape the csrf
			String csrf = getCSRF();

			String query = 
					"__csrf_magic=" + URLEncoder.encode(csrf,"ISO-8859-1")
					+ "&Submit=" + URLEncoder.encode(" Yes ","ISO-8859-1"); 

			postForm(url, query);
		}
		catch(Exception e){
			// TODO Auto-generated method stub
		}	
	}
	/*
	 * 
	 * Method to extract the page Title
	 * 
	 */

	public String getPgTitle() throws IOException
	{
		Document doc = Jsoup.parse(httpsconn.getInputStream(), "ISO-8859-1", loginurl.toString() );

		//<span class="pgtitle"><a href="/diag_dns.php">Diagnostics: DNS Lookup</a></span>
		Element link = doc.select("span.pgtitle").first();

		return link.text();
	}

	/*
	 * 
	 * Method to extract and return the CSRF ( Cross-site request forgery ) 
	 * 
	 * When there is a form tag
	 * 
	 */

	public String getCSRF() throws IOException
	{
		Document doc = Jsoup.parse(httpsconn.getInputStream(), "ISO-8859-1", loginurl.toString() );

		Element csrf = doc.select("form input[name=__csrf_magic]").first();

		return csrf.attr("value");
	}

	/*
	 * 
	 * Method to extract and return the CSRF ( Cross-site request forgery ) 
	 * 
	 * From script when csrf is not in a form tag
	 * 
	 */

	public String extractCSRFscript() throws IOException{
		Document doc = Jsoup.parse(httpsconn.getInputStream(), "ISO-8859-1", loginurl.toString() );

		Elements scripts = doc.select("script");

		String csrfValue = "";

		Log.d(TAG, "number of elements: " + scripts.size());
		for (int i =0; i<scripts.size(); i++)
		{
			if (scripts.get(i).html().startsWith("var csrfMagicToken ="))
			{
				Log.d(TAG, "full csrf line: " + scripts.get(i).html());
				String[] temp;
				String delimiter = "\"";
				// given string will be split by the argument delimiter provided. 
				temp = scripts.get(i).html().split(delimiter);

				csrfValue = temp[1];
			}
		}
		return csrfValue;
	}


	public String getSysAct(URL url) {
		Log.d(TAG, "SYSACT");
		String returnMsg = "";
		loginurl = url;
		Log.d(TAG, "url: " + url);
		try
		{
			getPage(); // we need to get this page to scrape the csrf
			String csrf = extractCSRFscript();

			String query = 
					"__csrf_magic=" + URLEncoder.encode(csrf,"ISO-8859-1")
					+ "&getactivity=" + URLEncoder.encode("yes","ISO-8859-1"); 

			postForm(url, query);
			//printStream(inStream);
			returnMsg = getStream(inStream);
		}
		catch(Exception e){
			// TODO Auto-generated method stub
		}	

		return returnMsg;
	}

	////////////////////////////////////////WOL BELOW
	
	/*
	 * Method just does an HTTP get
	 * Returns the page as a String
	 * 
	 * This method actually sends magicpackets and deletes hosts (encoded in the url)
	 */
	

	public String getPfPage(URL url) throws IOException {
		Log.d(TAG, "getWolPage");
		loginurl = url;
		Log.d(TAG, "url: " + url);
		try
		{
			getPage(); // we need to get this page to scrape the csrf		
		}
		catch(Exception e){
			// TODO Auto-generated method stub
		}	

		return getStream(inStream);
	}

	/*
	 * Method to update or add a new client to the list of wol clients
	 * Returns the page as a String
	 */
	
	public String setWolClient(URL pfsenseURL, String parameters, String id) throws IOException, ParserConfigurationException, SAXException
	{

		if(!id.equals(""))  // this is an edit append id to url and to query string for posting
			loginurl = new URL(pfsenseURL + "/services_wol_edit.php?id=" + id);
		else // this is a new host
			loginurl = new URL(pfsenseURL + "/services_wol_edit.php");
			
		getPage(); // get the wol_edit page so we can get the CSRF
		
		// add id and CSRF to query string
		parameters += "&__csrf_magic=" + URLEncoder.encode(getCSRF(),"ISO-8859-1") + 
					  "&id=" + id;  
		
		try
		{
			if ( postForm(loginurl, parameters) == 200 )
			{
				Log.d(TAG, "HTTP POST of login was received successfully ie 200");
				
				// get the services_wol page so we can rescrape and update the listivew
				loginurl = new URL(pfsenseURL + "/services_wol.php");
				getPage();
				
				return getStream(inStream); // return page for scraping
			}
		}		
		catch (UnknownHostException e)
		{
			Log.d(TAG, "CAUGHT Exception!: " + e.getMessage());
			return "error";
		}
		return "error";
	}

	//////////////////////////////////////////WOL ABOVE
	
	/////////////////////////////////////////DNS below
	
	/*
	 * Method to post a DNS lookup
	 * Returns the page as a String
	 * 
	 */
	

	public String getDNSPage(URL url, String query) throws IOException {
		Log.d(TAG, "getDnsPage");
		loginurl = url;
		Log.d(TAG, "url: " + url);
		try
		{
			getPage(); // we need to get this page to scrape the csrf
			String csrf = extractCSRFscript();

			// append csrf as we are GETting and scraping the page pre this method
			query += "&__csrf_magic=" + URLEncoder.encode(csrf,"ISO-8859-1");
			
			postForm(url, query);		
		}
		catch(Exception e){
			// TODO Auto-generated method stub
		}	

		return getStream(inStream);
	}
	
	public String getPingResultsPage(URL url, String query) throws IOException {
		Log.d(TAG, "getPingResultsPage");
		loginurl = url;
		Log.d(TAG, "url: " + url);
		try
		{		
			postForm(url, query);		
		}
		catch(Exception e){
			// TODO Auto-generated method stub
		}	

		return getStream(inStream);
	}
	
}
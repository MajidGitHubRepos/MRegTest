package com.mxgraph.examples.web;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class GetVariablesServlet extends HttpServlet
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4442397463551836919L;


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");

		OutputStream out = response.getOutputStream();
		String encoding = request.getHeader("Accept-Encoding");

		// Supports GZIP content encoding
		if (encoding != null && encoding.indexOf("gzip") >= 0)
		{
			response.setHeader("Content-Encoding", "gzip");
			out = new GZIPOutputStream(out);
		}

		PrintWriter writer = new PrintWriter(out);

		try
		{
			if (request.getContentLength() < Constants.MAX_REQUEST_SIZE)
			{

				String inMsg = "";
				Iterator iterator = ModelJsonServer.vatriablesHashMap.entrySet().iterator();
				int counter = 0;
				while(iterator.hasNext())
				{
					counter++;
					Map.Entry mentry = (Map.Entry) iterator.next(); 
					inMsg = inMsg + mentry.getKey().toString()+","+mentry.getValue().toString()+"*";
				}
				inMsg = inMsg.substring(0, inMsg.length() - 1);
				writer.println(inMsg);

			}
			else
			{
				error(writer, "drawingTooLarge");
			}
		}
		catch (Exception e)
		{
			error(writer, "invalidOrMissingFile");
		}

		//writer.println("</script>");
		//writer.println("</body>");
		//writer.println("</html>");

		writer.flush();
		writer.close();
	}

	public static void error(PrintWriter w, String key)
	{
		w.println("<b><p>ERROR in MY CODE </p></b>");
	}
	



}

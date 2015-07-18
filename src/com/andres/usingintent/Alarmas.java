package com.andres.usingintent;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class Alarmas extends Activity{
	
	//-----------------	
	//Activity GestorAlarmas
	//-----------------	
	
	private ImageButton ImgBtnGAinftemp,ImgBtnGAsuptemp,ImgBtnGAinfhum,ImgBtnGAsuphum;
	private TextView TxtVwGAinftemp,TxtVwGAsuptemp,TxtVwGAinfhum,TxtVwGAsuphum;
	private EditText EdtTxtGAinftemp,EdtTxtGAsuptemp,EdtTxtGAinfhum,EdtTxtGAsuphum;	
	private String lim;//Limite a fijar

	public void onCreate(Bundle savedInstanceState){
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarmas);
		
		EdtTxtGAinftemp = (EditText)findViewById(R.id.edit_GAinftemp);
		EdtTxtGAsuptemp = (EditText)findViewById(R.id.edit_GAsuptemp);	
		EdtTxtGAinfhum = (EditText)findViewById(R.id.edit_GAinfhum);
		EdtTxtGAsuphum = (EditText)findViewById(R.id.edit_GAsuphum);			
				
		TxtVwGAinftemp = (TextView)findViewById(R.id.GAinftemp);
		TxtVwGAsuptemp = (TextView)findViewById(R.id.GAsuptemp);     	
		TxtVwGAinfhum = (TextView)findViewById(R.id.GAinfhum); 
		TxtVwGAsuphum = (TextView)findViewById(R.id.GAsuphum); 		
    	
		ImgBtnGAinftemp = (ImageButton)findViewById(R.id.saveGAinftemp);		
		ImgBtnGAsuptemp = (ImageButton)findViewById(R.id.saveGAsuptemp);	
		ImgBtnGAinfhum = (ImageButton)findViewById(R.id.saveGAinfhum);
		ImgBtnGAsuphum = (ImageButton)findViewById(R.id.saveGAsuphum);   	
		
		//Obtener limites de rango actuales
		TareaStateGA tareaStateGA = new TareaStateGA();
		tareaStateGA.execute();	
		
		//Notificacion de alarmas
		TareaAlarms tareaAlarms = new TareaAlarms();
		tareaAlarms.execute();		
		
		//TEMPERATURA - Rango Inferior: t0alarma
		ImgBtnGAinftemp.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {	
				TareaSaveGA tareaSaveGA = new TareaSaveGA();
				lim="t0a";
				tareaSaveGA.execute("/"+lim+"/"+EdtTxtGAinftemp.getText().toString());
			}
		});		
		
		//TEMPERATURA - Rango Superior: t1alarma
		ImgBtnGAsuptemp.setOnClickListener(new OnClickListener() {				
			@Override
			public void onClick(View v) {	
				TareaSaveGA tareaSaveGA = new TareaSaveGA();
				lim="t1a";
				tareaSaveGA.execute("/"+lim+"/"+EdtTxtGAsuptemp.getText().toString());
			}
		});	
		
		//HUMEDAD - Rango Inferior: h0alarma
		ImgBtnGAinfhum.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {	
				TareaSaveGA tareaSaveGA = new TareaSaveGA();
				lim="h0a";
				tareaSaveGA.execute("/"+lim+"/"+EdtTxtGAinfhum.getText().toString());
			}
		});	
		
		//HUMEDAD - Rango Superior: h1alarma
		ImgBtnGAsuphum.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {	
				TareaSaveGA tareaSaveGA = new TareaSaveGA();
				lim="h1a";
				tareaSaveGA.execute("/"+lim+"/"+EdtTxtGAsuphum.getText().toString());
			}
		});			
	}//Fin onCreate
	
	//Tarea ASINCRONA para consulta en segundo plano
	//Estado de rangos Gestor Alarmas 
	private class TareaStateGA extends AsyncTask<String,Integer,Boolean> {
	
		private String _T0,_T1,_H0,_H1;		
		 
	    protected Boolean doInBackground(String... params) {
	    	
	    	boolean resul = true;	    	
	    	HttpClient httpClient = new DefaultHttpClient();//Cliente HTTP
	        			
			HttpGet del =new HttpGet(getString(R.string.Server_IP)+getString(R.string.Server_Port)+"/alarmas" );
			del.setHeader("content-type", "application/json");
			
			try
	        {	//Solicitud GET		
	        	HttpResponse resp = httpClient.execute(del);
	        	String respStr = EntityUtils.toString(resp.getEntity());//String respuesta
	        	
	        	JSONObject respJSON = new JSONObject(respStr);//JSON respuesta
	        	
	        	//Extraccion de parametros JSON
	        	_T0=respJSON.getString("t0a");
	        	_T1=respJSON.getString("t1a");
	        	_H0=respJSON.getString("h0a");
	        	_H1=respJSON.getString("h1a");	     	      	
	        }
	        catch(Exception ex)
	        {
	        	Log.e("ServicioRest","Error!", ex);
	        	resul = false;
	        }	 
	        return resul;
	    }
	    
	    protected void onPostExecute(Boolean result) {
	    	
	    	if (result)
	    	{	//Refresh TextView 
	    		TxtVwGAinftemp.setText(_T0);
	    		TxtVwGAsuptemp.setText(_T1);		    		
	    		TxtVwGAinfhum.setText(_H0);
	    		TxtVwGAsuphum.setText(_H1);
	    	}
	    }   	      
	}//Fin TareaStateGA	
	
	//Tarea ASINCRONA para consulta en segundo plano
	//Fijar rango Gestor Alarmas
	private class TareaSaveGA extends AsyncTask<String,Integer,Boolean> { 
	
		private String val;//Variable auxiliar			
		 
	    protected Boolean doInBackground(String... params) {
	    	
	    	boolean resul = true;		    	
	    	HttpClient httpClient = new DefaultHttpClient();//Cliente HTTP
	        
			String id = params[0];//Parametro pasado en la invocacion
			
			HttpGet del =new HttpGet(getString(R.string.Server_IP)+getString(R.string.Server_Port) + id);
			del.setHeader("content-type", "application/json");
			
			try
	        {	//Solicitud GET		
	        	HttpResponse resp = httpClient.execute(del);
	        	String respStr = EntityUtils.toString(resp.getEntity());//String respuesta
	        	
	        	JSONObject respJSON = new JSONObject(respStr);//JSON respuesta

	        	val=respJSON.getString(lim);//Extraccion de parametro JSON	        	
	        	
	        }
	        catch(Exception ex)
	        {
	        	Log.e("ServicioRest","Error!", ex);
	        	resul = false;
	        }
	 
	        return resul;
	    }
	    
	    protected void onPostExecute(Boolean result) {
	    	
	    	if (result)
	    	{	//Refresh TextView 
	    		if(lim.equals("t0a"))
	    			TxtVwGAinftemp.setText(val);
	    		if(lim.equals("t1a"))
	    			TxtVwGAsuptemp.setText(val);	    		
	    		if(lim.equals("h0a"))
	    			TxtVwGAinfhum.setText(val);
	    		if(lim.equals("h1a"))
	    			TxtVwGAsuphum.setText(val);
	    	}
	    }     	    	    
	}//Fin TareaSaveGA
	
	//Tarea ASINCRONA para consulta en segundo plano
	//Obtener estado de alarmas
	private class TareaAlarms extends AsyncTask<String,Integer,Boolean> {
		
		private String atemp,ahum,agas;			
		
	    protected Boolean doInBackground(String... params) {    	
	    	boolean resul = true;	    	
	    	HttpClient httpClient = new DefaultHttpClient();//Cliente HTTP
	        			
			HttpGet del =new HttpGet(getString(R.string.Server_IP)+getString(R.string.Server_Port)+"/panel/");
			del.setHeader("content-type", "application/json");
			
			try
	        {	//Solicitud GET		
	        	HttpResponse resp = httpClient.execute(del);
	        	String respStr = EntityUtils.toString(resp.getEntity());//String respuesta	        	
	        	JSONObject respJSON = new JSONObject(respStr);//JSON respuesta
	        	//Extraccion de parametros JSON
	        	atemp=respJSON.getString("Atemp");
	        	ahum=respJSON.getString("Ahum");
	        	agas=respJSON.getString("Aair");  	        	
	        }
	        catch(Exception ex)
	        {
	        	Log.e("ServicioRest","Error!", ex);
	        	resul = false;
	        }	 
	        return resul;
	    }
	    
	    protected void onPostExecute(Boolean result) {
	    	
	    	if (result)
	    	{	//Alarma TEMPERATURA  	
	    		if(atemp.equals("1")&&!PanelAlarmas.temperatura){
					displayNotification(1);
					PanelAlarmas.temperatura=true;//Evitar redisparo
	    		}
	    		//Alarma HUMEDAD
	    		if(ahum.equals("1")&&!PanelAlarmas.humedad){
					displayNotification(2);	
					PanelAlarmas.humedad=true;				
	    		}
	    		//Alarma GAS
	    		if(agas.equals("1")&&!PanelAlarmas.aire){
					displayNotification(3);	
					PanelAlarmas.aire=true;	
	    		}
	    	}
	    }//Fin OnPostExecute       	    	    
	}//Fin TareaRefresh
		
	//Gestion de notificaciones de alarmas
	protected void displayNotification(int _notificationID){
		
		Notification notif;
		CharSequence from,message;
		
		Intent i=new Intent(this,NotificationView.class);
		i.putExtra("notificationID",_notificationID);
		
		PendingIntent pendingIntent=
				PendingIntent.getActivity(this,_notificationID,i,0);
		
		NotificationManager nm=(NotificationManager)
				getSystemService(NOTIFICATION_SERVICE);
		
		switch(_notificationID){
		
			case 1://Alarma TEMPERATURA
				notif=new Notification(R.drawable.alarma_temp,"TEMPERATURA",System.currentTimeMillis());				
				from="Alarma de Temperatura";
				message="Temperatura crítica en el sistema";
				break;
				
			case 2://Alarma HUMEDAD
				notif=new Notification(R.drawable.alarma_hum,"HUMEDAD",System.currentTimeMillis());
				
				from="Alarma de Humedad";
				message="Humedad crítica en el sistema";		
				break;
				
			case 3://Alarma GAS
				notif=new Notification(R.drawable.alarma_gas,"GAS",System.currentTimeMillis());				
				from="Alarma de Gas";
				message="Calidad de aire crítica en el sistema";		
				break;				
				
			default:
				notif=new Notification(R.drawable.ic_launcher,"Error",System.currentTimeMillis());			
				from="Error de notificación";
				message="Error en ID de notificacion";			
		}
		
		notif.setLatestEventInfo(this, from, message, pendingIntent);
		
		notif.vibrate = new long[] {100,250,100,500};
		nm.notify(_notificationID, notif);
	}//Fin displayNotification				
	
}//Fin Alarmas

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

public class Credits extends Activity{
	
	//-----------------	
	//Activity Credits
	//-----------------		

	public void onCreate(Bundle savedInstanceState){
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.credits);
		
		TareaAlarms tareaAlarms = new TareaAlarms();
		tareaAlarms.execute();				
	}	
	
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
}

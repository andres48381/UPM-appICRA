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
import android.widget.Button;
import android.widget.TextView;

public class Ventilacion extends Activity{

	//-----------------	
	//Activity Ventilacion
	//-----------------	
	
	private Button BtnVel0,BtnVel1,BtnVel2,BtnVel3,BtnVel4;
	private TextView TxtVwEstVent;

	
	public void onCreate(Bundle savedInstanceState){
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ventilador);
		
		BtnVel0 = (Button)findViewById(R.id.vel0);	//Boton Select Velocidad	
		BtnVel1 = (Button)findViewById(R.id.vel1);	
		BtnVel2 = (Button)findViewById(R.id.vel2);			
		BtnVel3 = (Button)findViewById(R.id.vel3);			
		BtnVel4 = (Button)findViewById(R.id.vel4);	
		
		TxtVwEstVent = (TextView)findViewById(R.id.EstVent); 	
    	
		TareaSetSpeed tareaSetSpeed = new TareaSetSpeed();
		tareaSetSpeed.execute("I"); //Obtener Velocidad actual	
		
		 //NEW
		TareaAlarms tareaAlarms = new TareaAlarms();
		tareaAlarms.execute();
		
		BtnVel0.setOnClickListener(new OnClickListener() {	//VENTILADOR - Velocidad: 0
			@Override
			public void onClick(View v) {	
				TareaSetSpeed tareaSetSpeed = new TareaSetSpeed();
				tareaSetSpeed.execute("0");
			}
		});		
		BtnVel1.setOnClickListener(new OnClickListener() {	//VENTILADOR - Velocidad: 1
			@Override
			public void onClick(View v) {	
				TareaSetSpeed tareaSetSpeed = new TareaSetSpeed();
				tareaSetSpeed.execute("1");
			}
		});				
		BtnVel2.setOnClickListener(new OnClickListener() {	//VENTILADOR - Velocidad: 2
			@Override
			public void onClick(View v) {	
				TareaSetSpeed tareaSetSpeed = new TareaSetSpeed();
				tareaSetSpeed.execute("2");
			}
		});			
		BtnVel3.setOnClickListener(new OnClickListener() {	//VENTILADOR - Velocidad: 3
			@Override
			public void onClick(View v) {	
				TareaSetSpeed tareaSetSpeed = new TareaSetSpeed();
				tareaSetSpeed.execute("3");
			}
		});		
		BtnVel4.setOnClickListener(new OnClickListener() {	//VENTILADOR - Velocidad: 4
			@Override
			public void onClick(View v) {	
				TareaSetSpeed tareaSetSpeed = new TareaSetSpeed();
				tareaSetSpeed.execute("4");
			}
		});			
	}//Fin onCreate	
	
	//Tarea Asincrona para consulta en segundo plano
	//Establecer velocidad ventilacion
	private class TareaSetSpeed extends AsyncTask<String,Integer,Boolean> {
	
		private String vent;//Estado de ventilacion	
	 
	    protected Boolean doInBackground(String... params) {
	    	
	    	boolean resul = true;	    	
	    	HttpClient httpClient = new DefaultHttpClient();//Cliente HTTP
			HttpGet del;
			
			String id = params[0];//Parametro pasado en la invocacion
			
			if(id.equals("I")){//Obtener valor de ventilacion
				del=new HttpGet(getString(R.string.Server_IP)+getString(R.string.Server_Port)+"/monitor/");//Solicitud GET
				del.setHeader("content-type", "application/json");				
			}
			else{//Establecer valor de ventilacion
				del=new HttpGet(getString(R.string.Server_IP)+getString(R.string.Server_Port)+"/vent/" + id);//Solicitud GET
				del.setHeader("content-type", "application/json");
			}
			try
	        {			
	        	HttpResponse resp = httpClient.execute(del);
	        	String respStr = EntityUtils.toString(resp.getEntity());//String respuesta
	        	
	        	JSONObject respJSON = new JSONObject(respStr);//JSON respuesta

	        	vent=respJSON.getString("vent"); //Extraccion de parametro JSON	    	       	
	        }
	        catch(Exception ex)
	        {
	        	Log.e("Vent","Error!", ex);
	        	resul = false;
	        }	 
	        return resul;
	    }
	    
	    protected void onPostExecute(Boolean result) {
	    	
	    	if (result)	    	
	    		TxtVwEstVent.setText(vent);    	
	    }   	     
	}//Fin TareaSetSpeed	
	
	//Tarea Asincrona para consulta en segundo plano
	//Obtener estado de alarmas
	private class TareaAlarms extends AsyncTask<String,Integer,Boolean> {
		
		private String atemp,ahum,agas;			
		
	    protected Boolean doInBackground(String... params) {    	
	    	boolean resul = true;	    	
	    	HttpClient httpClient = new DefaultHttpClient();//Cliente HTTP
	        			
			HttpGet del =new HttpGet(getString(R.string.Server_IP)+getString(R.string.Server_Port)+"/panel/");//Solicitud GET
			del.setHeader("content-type", "application/json");
			
			try
	        {			
	        	HttpResponse resp = httpClient.execute(del);
	        	String respStr = EntityUtils.toString(resp.getEntity());//String respuesta
	        	
	        	JSONObject respJSON = new JSONObject(respStr);//JSON respuesta

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
	    	{	  //NEW   	
	    		if(atemp.equals("1")&&!PanelAlarmas.temperatura){//Alarma TEMPERATURA
					displayNotification(1);
					PanelAlarmas.temperatura=true;
	    		}
	    		
	    		if(ahum.equals("1")&&!PanelAlarmas.humedad){//Alarma HUMEDAD
					displayNotification(2);	
					PanelAlarmas.humedad=true;				
	    		}

	    		if(agas.equals("1")&&!PanelAlarmas.aire){//Alarma GAS
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

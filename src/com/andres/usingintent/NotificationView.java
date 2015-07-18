package com.andres.usingintent;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import android.app.Activity;
import android.app.NotificationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class NotificationView extends Activity
{
	//-----------------		
	//Activity NotificationView	
	//-----------------		
	private int notificationID;
	private TextView TxtVw1,TxtVw2,TxtVw3;
	private String param,infAlarm,supAlarm;//NEW
	private TareaNotif tareaNotif;
	@Override
	public void onCreate(Bundle savedInstanceState){
		
		super.onCreate(savedInstanceState);
		//Obtener ID de notificacion
		notificationID=getIntent().getExtras().getInt("notificationID");
		
		switch(notificationID){
		
		case 1://Notificacion Temperatura
			setContentView(R.layout.notif_temp);
			
			TxtVw1 = (TextView)findViewById(R.id.notifTemp); 
			TxtVw2 = (TextView)findViewById(R.id.notifGAinftemp);
			TxtVw3 = (TextView)findViewById(R.id.notifGAsuptemp); 			
			
			param="temp";
			infAlarm="t0a";
			supAlarm="t1a";
			
			tareaNotif = new TareaNotif();//Obtener situacion alarma temperatura
			tareaNotif.execute();				
			break;
			
		case 2://Notificacion Humedad
			setContentView(R.layout.notif_hum);
			
			TxtVw1 = (TextView)findViewById(R.id.notifHum); 
			TxtVw2 = (TextView)findViewById(R.id.notifGAinfhum);
			TxtVw3 = (TextView)findViewById(R.id.notifGAsuphum); 			
			
			param="hum";
			infAlarm="h0a";
			supAlarm="h1a";
			
			tareaNotif = new TareaNotif();//Obtener situacion alarma temperatura
			tareaNotif.execute();				
			break;

		case 3://Notificacion Gas
			setContentView(R.layout.notif_gas);
    		PanelAlarmas.aire=false;
			break;
			
		default:
			break;	
		}
		
		//buscar el servicio de gestor de notificaciones
		NotificationManager nm=(NotificationManager)
				getSystemService(NOTIFICATION_SERVICE);
		//cancelar la notificacion que se ha iniciado
		//nm.cancel(getIntent().getExtras().getInt("notificationID"));
		nm.cancel(notificationID);

	}
	
	//Tarea Asincrona para consulta en segundo plano
	private class TareaNotif extends AsyncTask<String,Integer,Boolean> {//Estado de limites Gestor Alarmas 
	
		private String x,x0,x1;//Contenedores auxiliares		
		 
	    protected Boolean doInBackground(String... params) {
	    	
	    	boolean resul = true;	    	
	    	HttpClient httpClient = new DefaultHttpClient();//Cliente HTTP
	        			
			HttpGet del =new HttpGet(getString(R.string.Server_IP)+getString(R.string.Server_Port)+"/alarmas" );//Solicitud GET
			del.setHeader("content-type", "application/json");
			
			try
	        {			
	        	HttpResponse resp = httpClient.execute(del);
	        	String respStr = EntityUtils.toString(resp.getEntity());//String respuesta
	        	
	        	JSONObject respJSON = new JSONObject(respStr);//JSON respuesta

	        	x=respJSON.getString(param);//Extraccion de parametros JSON        	
	        	x0=respJSON.getString(infAlarm);
	        	x1=respJSON.getString(supAlarm);     	      	
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
	    	{
	    		TxtVw1.setText(x);
	    		TxtVw2.setText(x0);
	    		TxtVw3.setText(x1);
	    		if(param.equals("temp"))PanelAlarmas.temperatura=false;
	    		if(param.equals("hum"))PanelAlarmas.humedad=false;
	    	}
	    }   	      
	}//Fin TareaNotifTemp		
	

}

package com.andres.usingintent;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class Rangos extends Activity{
	
	//-----------------	
	//Activity RangoActuadores
	//-----------------		
	
	private ImageButton ImgBtnRAinftemp,ImgBtnRAsuptemp,ImgBtnRAinfhum,ImgBtnRAsuphum;
	private TextView TxtVwRAinftemp,TxtVwRAsuptemp,TxtVwRAinfhum,TxtVwRAsuphum;
	private EditText edit_RAinftemp,edit_RAsuptemp,edit_RAinfhum,edit_RAsuphum;	
	private String lim;//Limite de rango editado

	public void onCreate(Bundle savedInstanceState){
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rangos);
		
		//Edit rango
		edit_RAinftemp = (EditText)findViewById(R.id.edit_RAinftemp);
		edit_RAsuptemp = (EditText)findViewById(R.id.edit_RAsuptemp);	
		edit_RAinfhum = (EditText)findViewById(R.id.edit_RAinfhum);
		edit_RAsuphum = (EditText)findViewById(R.id.edit_RAsuphum);			
		
		//Texto View rango 
		TxtVwRAinftemp = (TextView)findViewById(R.id.RAinftemp);
		TxtVwRAsuptemp = (TextView)findViewById(R.id.RAsuptemp);     	
		TxtVwRAinfhum = (TextView)findViewById(R.id.RAinfhum); 
		TxtVwRAsuphum = (TextView)findViewById(R.id.RAsuphum); 	
		
		//Boton Save limite rango
		ImgBtnRAinftemp = (ImageButton)findViewById(R.id.saveRAinftemp);			
		ImgBtnRAsuptemp = (ImageButton)findViewById(R.id.saveRAsuptemp);	
		ImgBtnRAinfhum = (ImageButton)findViewById(R.id.saveRAinfhum);
		ImgBtnRAsuphum = (ImageButton)findViewById(R.id.saveRAsuphum);   
		
		//Obtener limites de rango actuales
		TareaStateRA tareaStateRA = new TareaStateRA();
		tareaStateRA.execute();	
		
		//Notificacion de alarmas
		TareaAlarms tareaAlarms = new TareaAlarms();
		tareaAlarms.execute();
		
		//TEMPERATURA - Rango Inferior: t0	
		ImgBtnRAinftemp.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {	
				TareaSaveRA tareaSaveRA = new TareaSaveRA();
				lim="t0";
				tareaSaveRA.execute("/"+lim+"/"+edit_RAinftemp.getText().toString());
			}
		});		
		//TEMPERATURA - Rango Superior: t1
		ImgBtnRAsuptemp.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {	
				TareaSaveRA tareaSaveRA = new TareaSaveRA();
				lim="t1";
				tareaSaveRA.execute("/"+lim+"/"+edit_RAsuptemp.getText().toString());
			}
		});			
		//HUMEDAD - Rango Inferior: h0	
		ImgBtnRAinfhum.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {	
				TareaSaveRA tareaSaveRA = new TareaSaveRA();
				lim="h0";
				tareaSaveRA.execute("/"+lim+"/"+edit_RAinfhum.getText().toString());
			}
		});		
		//HUMEDAD - Rango Superior: h1	
		ImgBtnRAsuphum.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {	
				TareaSaveRA tareaSaveRA = new TareaSaveRA();
				lim="h1";
				tareaSaveRA.execute("/"+lim+"/"+edit_RAsuphum.getText().toString());
			}
		});		
	}//Fin onCreate
	
	//Tarea ASINCRONA para consulta en segundo plano
	//Estado de limites Rango Actuadores 
	private class TareaStateRA extends AsyncTask<String,Integer,Boolean> {
	
		private String _t0,_t1,_h0,_h1;		
		 
	    protected Boolean doInBackground(String... params) {
	    	
	    	boolean resul = true;	    	
	    	HttpClient httpClient = new DefaultHttpClient();//Cliente HTTP
	        			
			HttpGet del =new HttpGet(getString(R.string.Server_IP)+getString(R.string.Server_Port)+"/rangos" );
			del.setHeader("content-type", "application/json");
			
			try
	        {	//Solicitud GET		
	        	HttpResponse resp = httpClient.execute(del);
	        	String respStr = EntityUtils.toString(resp.getEntity());//String respuesta	        	
	        	JSONObject respJSON = new JSONObject(respStr);//JSON respuesta
	        	//Extraccion de parametros JSON
	        	_t0=respJSON.getString("t0");
	        	_t1=respJSON.getString("t1");
	        	_h0=respJSON.getString("h0");
	        	_h1=respJSON.getString("h1");	        	
	        	
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
	    		TxtVwRAinftemp.setText(_t0);
	    		TxtVwRAsuptemp.setText(_t1); 		    		
	    		TxtVwRAinfhum.setText(_h0);	
	    		TxtVwRAsuphum.setText(_h1);
	    	}
	    }    	    	    
	}//Fin TareaStateRA	
	
	//Tarea ASINCRONA para consulta en segundo plano
	//Salvar limite Rango Actuadores
	private class TareaSaveRA extends AsyncTask<String,Integer,Boolean> {
	
		private String val;				
		 
	    protected Boolean doInBackground(String... params) {
	    	
	    	boolean resul = true;		    	
	    	HttpClient httpClient = new DefaultHttpClient();//Cliente HTTP
	        
			String id = params[0];//Parametro pasado en la invocacion
			
			HttpGet del = new HttpGet(getString(R.string.Server_IP)+getString(R.string.Server_Port) + id);
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
	    		if(lim.equals("t0"))
	    			TxtVwRAinftemp.setText(val);
	    		if(lim.equals("t1"))
	    			TxtVwRAsuptemp.setText(val);		    		
	    		if(lim.equals("h0"))
	    			TxtVwRAinfhum.setText(val);
	    		if(lim.equals("h1"))
	    			TxtVwRAsuphum.setText(val);
	    	}
	    }      	    		    
	}//Fin TareaSaveRA	
				
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
				
}//Fin Rangos

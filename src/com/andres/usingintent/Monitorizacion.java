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
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Monitorizacion extends Activity{

	//-----------------		
	//Activity Monitorizacion	
	//-----------------		
	
	private ImageButton BtnRefresh;	
	private ProgressBar PrgBar;
	private int progressStatus = 0;		
	private TextView TxtVwtemp,TxtVwhum,TxtVwcalef,TxtVwhumidi,TxtVwvent;
	private TextView TxtVwatemp,TxtVwahum,TxtVwagas;
	int notificationID=1;
	
	public void onCreate(Bundle savedInstanceState){
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.monitorizar);
		
		BtnRefresh = (ImageButton)findViewById(R.id.BtnRefresh);		 
		PrgBar = (ProgressBar) findViewById(R.id.AirIndicator);		     
		TxtVwtemp = (TextView)findViewById(R.id.temp); 
		TxtVwhum = (TextView)findViewById(R.id.hum);    	
		TxtVwcalef = (TextView)findViewById(R.id.calef);
		TxtVwhumidi = (TextView)findViewById(R.id.humidi);   
		TxtVwvent = (TextView)findViewById(R.id.vent);
		TxtVwatemp = (TextView)findViewById(R.id.atemp);
		TxtVwahum = (TextView)findViewById(R.id.ahum);
		TxtVwagas = (TextView)findViewById(R.id.agas);
    	
		TareaRefresh tareaRefreshCreate = new TareaRefresh();
		tareaRefreshCreate.execute();
		
		BtnRefresh.setOnClickListener(new OnClickListener() {//Refresh valores			
			@Override
			public void onClick(View v) {	
				
				TareaRefresh tareaRefreshClick = new TareaRefresh();
				tareaRefreshClick.execute();
			}
		});	
	}//Fin onCreate
	
	//Tarea Asincrona para consulta en segundo plano
	//Obtener valores monitorizacion
	private class TareaRefresh extends AsyncTask<String,Integer,Boolean> {
	
		private String temp,hum,air;//Contenedores auxiliares
		private String calef,humidi,vent;
		private String atemp,ahum,agas;		
		 
	    protected Boolean doInBackground(String... params) {
	    	
	    	
	    	boolean resul = true;	    	
	    	HttpClient httpClient = new DefaultHttpClient();//Cliente HTTP
	        			
			HttpGet del =new HttpGet(getString(R.string.Server_IP)+getString(R.string.Server_Port)+"/monitor/");//Solicitud GET
			del.setHeader("content-type", "application/json");
			
			try
	        {			
	        	HttpResponse resp = httpClient.execute(del);
	        	String respStr = EntityUtils.toString(resp.getEntity());//String respuesta
	        	
	        	JSONObject respJSON = new JSONObject(respStr);//JSON respuesta

	        	temp=respJSON.getString("temp");//Extraccion de parametros JSON
	        	hum=respJSON.getString("hum");
	        	air=respJSON.getString("air");
	        	calef=respJSON.getString("calef");
	        	humidi=respJSON.getString("humidi");
	        	vent=respJSON.getString("vent");
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
	    	{
	    		TxtVwtemp.setText(temp);//Refresh TextView     		
	    		TxtVwhum.setText(hum);   
	    		
	    		Log.d("air",air);
	    		progressStatus=Integer.parseInt(air);
	    		
	    		PrgBar.setProgress(progressStatus);	   		
	    		
	    		if(calef.equals("1")){
	    			TxtVwcalef.setText("ON");
	    			TxtVwcalef.setTextColor(Color.parseColor("#00FF00"));
	    		}
	    		else{
	    			TxtVwcalef.setText("OFF");
	    			TxtVwcalef.setTextColor(Color.parseColor("#FF0000"));	    			
	    		}
	    		
	    		if(humidi.equals("1")){
	    			TxtVwhumidi.setText("ON");
	    			TxtVwhumidi.setTextColor(Color.parseColor("#00FF00"));	    			
	    		}
	    		else{
	    			TxtVwhumidi.setText("OFF");
	    			TxtVwhumidi.setTextColor(Color.parseColor("#FF0000"));		    			
	    		}
	    		
	    		TxtVwvent.setText(vent);
	    		
	    		if(atemp.equals("1")){//Alarma TEMPERATURA
	    			TxtVwatemp.setText("ON");
	    			TxtVwatemp.setTextColor(Color.parseColor("#00FF00"));	 	    			
	    			if(!PanelAlarmas.temperatura){
						displayNotification(1);
						PanelAlarmas.temperatura=true;
	    			}
	    		}
	    		else{
	    			TxtVwatemp.setText("OFF");	
	    			TxtVwatemp.setTextColor(Color.parseColor("#FF0000"));		    			
	    		}
	    		
	    		if(ahum.equals("1")){//Alarma HUMEDAD
	    			TxtVwahum.setText("ON");
	    			TxtVwahum.setTextColor(Color.parseColor("#00FF00"));	
	    			if(!PanelAlarmas.humedad){
						displayNotification(2);	
						PanelAlarmas.humedad=true;	
	    			}
	    		}
	    		else{
	    			TxtVwahum.setText("OFF");
	    			TxtVwahum.setTextColor(Color.parseColor("#FF0000"));	
	    		}
	    		
	    		if(agas.equals("1")){//Alarma GAS
	    			TxtVwagas.setText("ON");
	    			TxtVwagas.setTextColor(Color.parseColor("#00FF00"));	    			
	    			if(!PanelAlarmas.aire){
						displayNotification(3);	
						PanelAlarmas.aire=true;
	    			}
	    		}
	    		else{
	    			TxtVwagas.setText("OFF");  
	    			TxtVwagas.setTextColor(Color.parseColor("#FF0000"));	
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

}//Fin Monitorizacion


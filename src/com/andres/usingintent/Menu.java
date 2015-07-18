package com.andres.usingintent;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;

public class Menu extends Activity {
	//-----------------	
	//Activity Menu
	//-----------------	
	private TextView Txtmodo;//Cabecera
	private ImageView ImgVwServer;	
	private String registro;//Identificador de fichero registro de plantacion
	private String bit;//Bit de conexion
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		String modo=getIntent().getStringExtra("modo");//Nivel de privilegio	
		String user=getIntent().getStringExtra("user");//Usuario identificado
		
		//Carga de Layout
		if(modo.equals("ADMINISTRADOR")){
			setContentView(R.layout.menu_moderador);
			ImgVwServer=(ImageView)findViewById(R.id.conexionMenuModerador);  
		}
		
		else {
			setContentView(R.layout.menu_registrado);
			ImgVwServer=(ImageView)findViewById(R.id.conexionMenuRegistrado);  
		}
		
		//Cabecera pantalla
		Txtmodo=(TextView)findViewById(R.id.modo);	
		Txtmodo.setText(modo+" "+user);
		
		//Gestion de conexion servidor
		TareaBitLife tareaBitLifeMenu = new TareaBitLife();//FALLO
		tareaBitLifeMenu.execute();			
		
		//Obtencion del Identificador de fichero registro de plantacion
		TareaGetRegister tareaGetRegister=new TareaGetRegister();
		tareaGetRegister.execute();	
		
		//Notificacion de alarmas
		TareaAlarms tareaAlarms = new TareaAlarms();
		tareaAlarms.execute();	
		
	}//Fin OnCreate	
	
	protected void onRestart(){
		super.onRestart();
		//Estado de conexion
		TareaBitLife tareaBitLifeMenu = new TareaBitLife();
		tareaBitLifeMenu.execute();		
	}//Fin onRestart

	//Tarea Asincrona para consulta en segundo plano
	//Estado de conexion con servidor
	private class TareaBitLife extends AsyncTask<String,Integer,Boolean> {
		 
	    protected Boolean doInBackground(String... params) {
	    	
	    	boolean resul = true;			
			
	    	HttpClient httpClient = new DefaultHttpClient();//Cliente HTTP        			
			HttpGet del =new HttpGet(getString(R.string.Server_IP)+getString(R.string.Server_Port)+"/life");//Solicitud GET
			del.setHeader("content-type", "application/json");
			
			try
	        {			
	        	HttpResponse resp = httpClient.execute(del);
	        	String respStr = EntityUtils.toString(resp.getEntity());//String respuesta
	        	
	        	JSONObject respJSON = new JSONObject(respStr);//JSON respuesta

	        	bit=respJSON.getString("bit");//Extraccion de parametros JSON    	        	
	        }
	        catch(Exception ex)
	        {
	        	Log.e("Login","Error!", ex);
	        	resul = false;
	        }	 
	        return resul;
	    }
	    
	    protected void onPostExecute(Boolean result) {
	    	
	    	if (result)
	    	{
	    		if(bit.equals("101"))//101 bit de confirmacion Server RUN
	    			ImgVwServer.setImageResource(R.drawable.check_ok);
	    		else
	    			ImgVwServer.setImageResource(R.drawable.check_fail);	    			    		    		
	    	}
	    	else
	    		ImgVwServer.setImageResource(R.drawable.check_fail);	   		
	    }//Fin OnPostExecute       	    	    
	}//Fin TareaBitLife		
	
	//Tarea Asincrona para consulta en segundo plano
	//Obtencion URL fichero registro plantacion
	private class TareaGetRegister extends AsyncTask<String,Integer,Boolean> {
		 
	    protected Boolean doInBackground(String... params) {
	    	
	    	boolean resul = true;		    	
	    	HttpClient httpClient = new DefaultHttpClient();//Cliente HTTP
			
			HttpGet del =new HttpGet(getString(R.string.Server_IP)+getString(R.string.Server_Port)+"/registro" );//Solicitud GET
			del.setHeader("content-type", "application/json");
			
			try
	        {			
	        	HttpResponse resp = httpClient.execute(del);
	        	String respStr = EntityUtils.toString(resp.getEntity());//String respuesta
	        	
	        	JSONObject respJSON = new JSONObject(respStr);//JSON respuesta
	        	
	        	registro=respJSON.getString("reg");//Extraccion de parametros JSON      		        	
	        }
	        catch(Exception ex)
	        {
	        	Log.e("ServicioRest","Error!", ex);
	        	resul = false;
	        }	 
	        return resul;
	    }	     	       
	}//Fin TareaGetRegister	
		
	public void OnClickRegister (View view){		
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(getString(R.string.Server_IP)+"/serverICRA/"+registro));//URL registro
		startActivity(intent);		
	}//Fin OnClickRegister
	
	public void OnClickMonitor (View view){startActivity(new Intent("com.andres.usingintent.Monitorizacion"));}
	public void OnClickRangos (View view){startActivity(new Intent("com.andres.usingintent.Rangos"));}
	public void OnClickAlarmas (View view){startActivity(new Intent("com.andres.usingintent.Alarmas"));}
	public void OnClickVentilador (View view){startActivity(new Intent("com.andres.usingintent.Ventilacion"));}
	public void OnClickPower (View view){startActivity(new Intent("com.andres.usingintent.Power"));}
	public void OnClickCredits (View view){startActivity(new Intent("com.andres.usingintent.Credits"));}
	public void OnClickGrafica (View view){startActivity(new Intent("com.andres.usingintent.Grafica"));}
	
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
		
}//Fin MainActivity

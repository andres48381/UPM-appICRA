package com.andres.usingintent;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class Grafica extends Activity{

	//-----------------		
	//Activity Grafica	
	//-----------------		

	private EditText EdtTxtdate0,EdtTxttime0,EdtTxtdate1,EdtTxttime1;
	private TextView TxtVwState;
	private Button BtnTrazar;
	private Spinner cmbOpciones1,cmbOpciones2;
	private JSONArray jArray1,jArray2;//Vectores JSON
	private String respStr;//String JSON
	private String param1,param2;//Variables a trazar
	private String funcion1,funcion2;//Nombre de funciones
	private String unidad;
	float step;//Division de escala
	int indice;
	
	public void onCreate(Bundle savedInstanceState){
		
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.grafica);
    	
    	EdtTxtdate0 = (EditText)findViewById(R.id.EdtTxtDate0);
    	EdtTxttime0 = (EditText)findViewById(R.id.EdtTxtTime0); 
    	EdtTxtdate1 = (EditText)findViewById(R.id.EdtTxtDate1); 
    	EdtTxttime1 = (EditText)findViewById(R.id.EdtTxtTime1);      	
    	TxtVwState=(TextView)findViewById(R.id.TxtVwTrazar);
    	BtnTrazar=(Button)findViewById(R.id.BtnTrazar); 

    	final String[] lista =new String[]{"Temperatura","Temperatura Nv Inferior","Temperatura Nv Superior",
    			"Temperatura Alarma Nv Inferior","Temperatura Alarma Nv Superior","Humedad","Humedad Nv Inferior",
    			"Humedad Nv Superior","Humedad Alarma Nv Inferior","Humedad Alarma Nv Superior","Calidad aire",
    			"Ventilación","Calefacción","Humidificador","Alarma Temperatura","Alarma Humedad","Alarma Aire",
    			"Parada Sistema"};
    	
       	final String[] paramGET=new String[]{"temperatura","temp_min","temp_max","Atemp_min","Atemp_max","humedad","hum_min",
    			"hum_max","Ahum_min","Ahum_max","aire","ventilador","calefaccion","humidificador","alarmaT","alarmaH",
    			"alarmaG","pause"}; 
       	
    	final String[] leyenda =new String[]{"Temp","Temp.Inf","Temp.Sup","Temp.Alarm.Inf","Temp.Alarm.Sup","Hum","Hum.Inf",
    			"Hum.Sup","Hum.Alarm.Inf","Hum.Alarm.Sup","Calidad.Air","Vent","Calef","Humidi","Alarm.Temp","Alarm.Hum",
    			"Alarm.Air","Emergency"};  
    	
    	final String[] unidades =new String[]{"[ºC]","[ºC]","[ºC]","[ºC]","[ºC]","[%]","[%]","[%]","[%]","[%]","[n/a]","[ON/OFF]",
    			"[ON/OFF]","[ON/OFF]","[ON/OFF]","[ON/OFF]","[ON/OFF]","[ON/OFF]"};      	
    	
    	final String[] temp =new String[]{"N/A","Temperatura","Temperatura Nv Inferior","Temperatura Nv Superior",
    			"Temperatura Alarma Nv Inferior","Temperatura Alarma Nv Superior"};
    	
    	final String[] hum =new String[]{"N/A","Humedad","Humedad Nv Inferior","Humedad Nv Superior","Humedad Alarma Nv Inferior",
    			"Humedad Alarma Nv Superior"};
    	
    	final String[] empty =new String[]{"N/A"};    	
    	
    	final String[] perif =new String[]{"N/A","Ventilación","Calefacción","Humidificador","Alarma Temperatura","Alarma Humedad",
    			"Alarma Aire","Parada Sistema"};	

		TareaAlarms tareaAlarms = new TareaAlarms();
		tareaAlarms.execute();	
    	
    	ArrayAdapter<String> adaptador1 =new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, lista);
    	ArrayAdapter<String> adaptador2 =new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, temp);
    	final ArrayAdapter<String> tempAdapter =new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, temp); 
    	final ArrayAdapter<String> humAdapter =new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, hum);   	
    	final ArrayAdapter<String> emptyAdapter =new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, empty);
    	final ArrayAdapter<String> perifAdapter =new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, perif);
    	
    	cmbOpciones1 = (Spinner)findViewById(R.id.CmbOpciones);
    	cmbOpciones2 = (Spinner)findViewById(R.id.CmbOpciones2);    	
    	adaptador1.setDropDownViewResource(
    	android.R.layout.simple_spinner_dropdown_item);
    	adaptador2.setDropDownViewResource(
    	android.R.layout.simple_spinner_dropdown_item);    	
    	cmbOpciones1.setAdapter(adaptador1);   
    	cmbOpciones2.setAdapter(adaptador2);    	
    	//cmbOpciones2.setAdapter(emptyAdapter);  
    	
    	cmbOpciones1.setOnItemSelectedListener(
    			new AdapterView.OnItemSelectedListener() {
    			public void onItemSelected(AdapterView<?> parent,
    			android.view.View v, int position, long id) {
    				param1=paramGET[position];
    				funcion1=leyenda[position];
    				unidad=unidades[position];
    				
    				if(unidad.equals("[ºC]")){
    					cmbOpciones2.setAdapter(tempAdapter);   
    					indice=-1;
    				}
    					
    				if(unidad.equals("[%]")){
    					cmbOpciones2.setAdapter(humAdapter);   
    					indice=4;
    				}
    				
    				if(unidad.equals("[n/a]"))
    					cmbOpciones2.setAdapter(emptyAdapter);  
 
    				
    				if(unidad.equals("[ON/OFF]")){
    					cmbOpciones2.setAdapter(perifAdapter);  
    					indice=10;
    				}
    			}    			
    			
				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					// TODO Auto-generated method stub
					
				}
    			});    
    	
    	cmbOpciones2.setOnItemSelectedListener(
    			new AdapterView.OnItemSelectedListener() {
    			public void onItemSelected(AdapterView<?> parent,
    			android.view.View v, int position, long id) {
    				
    				if(position==0)
    					funcion2=param2="empty";
    				else{
	    				funcion2=leyenda[position+indice];
	    				param2=paramGET[position+indice];
    				}
    			}    			
    			
				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					// TODO Auto-generated method stub
					
				}
    			});
    	
    	
    	BtnTrazar.setOnClickListener(new OnClickListener() {//Trazar grafica			
			@Override
			public void onClick(View v) {	
								
				String date0=EdtTxtdate0.getText().toString();
				String date1=EdtTxtdate1.getText().toString();
				String time0=EdtTxttime0.getText().toString();
				String time1=EdtTxttime1.getText().toString();
				boolean b=date0.length()==8;
				b=b&&date1.length()==8;
				b=b&&time0.length()==6;
				b=b&&time1.length()==6;
				//Campos completados
				if(b){
					TareaTrazar tareaTrazar = new TareaTrazar();
					tareaTrazar.execute(date0,time0,date1,time1);
				}
			}
		});   	

	}//Fin onCreate
	
	protected void onStop(){
		super.onStop();
		TxtVwState.setText("");				
	}//Fin onStop	
	
	//Tarea Asincrona para consulta en segundo plano
	//Peticion vector de datos al servidor	
	private class TareaTrazar extends AsyncTask<String,Integer,Boolean> {
			 
	    protected Boolean doInBackground(String... params) {
	    	
	    	boolean resul = true;	    	
	    	HttpClient httpClient = new DefaultHttpClient();//Cliente HTTP
	    	
	    	String _date0=params[0];//Fecha inicio
	    	String _time0=params[1];//Hora inicio
	    	String _date1=params[2];//Fecha final
	    	String _time1=params[3];//Hora final
	    	        			
			HttpGet del =new HttpGet(getString(R.string.Server_IP)+getString(R.string.Server_Port)+"/grafica/"+param1+"/"+param2+"/"+_date0+"/"+_time0+"/"+_date1+"/"+_time1);//Solicitud GET
			del.setHeader("content-type", "application/json");
			
			try
	        {			
	        	HttpResponse resp = httpClient.execute(del);
	        	respStr = EntityUtils.toString(resp.getEntity());//String respuesta
	        	if(respStr.equals("empty"))resul=false;       	
	        }
	        catch(Exception ex)
	        {
	        	Log.e("ServicioRest","Error!", ex);
	        	resul = false;
	        }	 
	        return resul;
	    }
	    
	    protected void onPostExecute(Boolean result) {
	    	
	    	if(result){
	    		TxtVwState.setText("Trazando...");
	    		
	    		Intent i=new Intent("com.andres.usingintent.Trazado");	
	    		
	    		//String con JSONArray
	    		i.putExtra("respStr", respStr);
	    		//Parametros a trazar
	    		i.putExtra("param1", param1);
	    		i.putExtra("param2", param2);
	    		//Nombre de funcion
	    		i.putExtra("funcion1", funcion1);
	    		i.putExtra("funcion2", funcion2);
	    		//Unidades
	    		i.putExtra("unidad", unidad);    		
	    		//Time de inicio
	    		i.putExtra("time", EdtTxttime0.getText().toString());
	    		
	    		startActivityForResult(i,1);		    		
	    	}
	    }//Fin OnPostExecute       	    	    
	}//Fin TareaRefresh		
	
	//Boton Fecha y Hora actual
	public void OnClickTimeNow (View view){
    	Time today = new Time(Time.getCurrentTimezone()); 
    	today.setToNow();
    	
    	String year=String.valueOf(today.year);
    	
    	String month;//Ajuste de formato
    	if((today.month+1)<10)month="0"+String.valueOf(today.month+1);  
    	else month=String.valueOf(today.month);
    	
    	String day;//Ajuste de formato
    	if(today.monthDay<10)day="0"+String.valueOf(today.monthDay);
    	else day=String.valueOf(today.monthDay);
    	
    	EdtTxtdate1.setText(year+month+day);
    	EdtTxttime1.setText(today.format("%k%M%S"));		
		}//Fin OnClickTimeNow	
	
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
	
	
	
}//Fin Grafica
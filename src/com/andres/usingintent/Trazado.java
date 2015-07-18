package com.andres.usingintent;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;


import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class Trazado extends Activity {
	
	//-----------------	
	//Activity Trazado
	//-----------------			

	private XYPlot plot;//Objeto para plotear
	private String param1,param2;//Variables a trazar
	private String funcion1,funcion2;//Leyenda de variable
	private String unidad;//Unidades de variables
	private String timeStr;//Time inicial
	private Number[] NmbSeries1,NmbSeries2;//Series de datos
	private float step;//Division de escala
	float min=0;//Minimo absoluto 
	float max=0;//Maximo absoluto 
	boolean duo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Receive from Activity preview
		String respStr=getIntent().getStringExtra("respStr");
		param1=getIntent().getStringExtra("param1");	
		param2=getIntent().getStringExtra("param2");
		//1 or 2 graphics
		if(!param2.equals("empty"))duo=true;
		else duo=false;
		
		funcion1=getIntent().getStringExtra("funcion1");	
		funcion2=getIntent().getStringExtra("funcion2");
		unidad=getIntent().getStringExtra("unidad");			
		timeStr=getIntent().getStringExtra("time");
    	
		TareaAlarms tareaAlarms = new TareaAlarms();
		tareaAlarms.execute();	
		
		//String "hhmmss" to int h,m,s
		int h=Integer.parseInt(timeStr.substring(0,2));
		int m=Integer.parseInt(timeStr.substring(2,4));
		int s=Integer.parseInt(timeStr.substring(4,6));			
		
		try{
			//Obtencion JSONObject
	    	JSONObject json = new JSONObject(respStr);
	    	//Obtencion JSONArray "grafica"
	    	JSONArray jArray = json.getJSONArray("grafica");	    		    		
    		int size=jArray.length();    		
    		
	    	NmbSeries1=new Number[size*2];//Array {time,valor}	
	    	String resul=jArray.getJSONObject(0).getString(param1);		
	    	float val1=Float.parseFloat(resul);
	    	min=max=val1;//min MAX ejeY inicial
	    	String resul2;
	    	float val2;
		    	
	    	if(duo){//2 graphics
				resul2=jArray.getJSONObject(0).getString(param2);	    	
		    	NmbSeries2=new Number[size*2];//Array {time,valor}	
		    	val2=Float.parseFloat(resul2);	    	
		    	//min MAX ejeY inicial
		    	min=(val1<=val2?val1:val2);
		    	max=(val1>=val2?val1:val2);	
	    	}
	    	
	    	float aux_param,aux_param2;
	    	String time,time2;
	    	int aux_time,aux_time2;
	    	int j=0;//Indice JsonArray
	    	
	    	//Creacion de NumbSeries	
	    	
	    	for(int i=0; i<size; i++){		    		
	    		j=2*i;//Indice NmbSeries[]
	    		
	    	  //Extraccion de parametros
			  time = jArray.getJSONObject(i).getString("time");		
			  resul = jArray.getJSONObject(i).getString(param1);				  
			  //to Float
			  aux_param=Float.parseFloat(resul);
			  //to Int			  
			  aux_time=Integer.parseInt(time)*1000;//ms			  
			  //[x,]Time			  
			  NmbSeries1[j]=aux_time;			  
			  //[,y]Valor
			  NmbSeries1[j+1]=aux_param; 
			  
			  if(duo){//2 graphics
				  resul2 = jArray.getJSONObject(i).getString(param2);	
				  aux_param2=Float.parseFloat(resul2);
				  NmbSeries2[j]=aux_time;
				  NmbSeries2[j+1]=aux_param2;				  
				  //min y MAX ejeY
				  val1=(aux_param<=aux_param2?aux_param:aux_param2);			    	  
				  val2=(aux_param>=aux_param2?aux_param:aux_param2);
				  if(val1<min)min=val1;
				  if(val2>max)max=val2;	
			  }
			  else{//1 graphic
				  //min y MAX ejeY
				  if(aux_param<min)min=aux_param;
				  if(aux_param>max)max=aux_param;
			  }			    	  
	    	}		 
		}
		catch(Exception ex){
			Log.e("JSONArray","Error!", ex);			
		}
    	
		//Set layout
		setContentView(R.layout.plot);
		plot = (XYPlot) findViewById(R.id.xyPlot);		
		
        //XYSeries
        XYSeries series1 = new SimpleXYSeries(Arrays.asList(NmbSeries1),         
                SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED,funcion1); 
        
        //Formato de XYSeries
		LineAndPointFormatter s1Format = new LineAndPointFormatter();
		s1Format.setPointLabelFormatter(new PointLabelFormatter());
		s1Format.configure(getApplicationContext(),
				R.xml.lpf1);
		
		//Add to Ploteado
		plot.addSeries(series1, s1Format);
		
		String title,rangeLabel;
		
		if(duo){//2 graphics	
	        XYSeries series2 = new SimpleXYSeries(Arrays.asList(NmbSeries2),          
	                SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED,funcion2); 	
			
			LineAndPointFormatter s2Format = new LineAndPointFormatter();
			s2Format.setPointLabelFormatter(new PointLabelFormatter());
			s2Format.configure(getApplicationContext(),
					R.xml.lpf2);
			
			plot.addSeries(series2, s2Format);		
			
			title=funcion1+" - "+funcion2+" frente al Tiempo";
			rangeLabel=funcion1+unidad+" - "+funcion2+unidad;
		}
		else{
			title=funcion1+" frente al Tiempo";
			rangeLabel=funcion1+unidad;			
		}

		//Set Title
		plot.setTitle(title);
		//Set Range Eje Y
		plot.setRangeLabel(rangeLabel);		
		
		//Division de escala
		float dif=max-min;
		
		if (dif>20)step=20.0f;
		else if (dif>10)step=1.0f;
		else if (dif>4)step=0.5f;
		//else if(dif>1.)step=0.2f;
		else step=0.1f;		
		
		//Ajustes de representacion
		plot.setRangeBoundaries(min-step,max+step , BoundaryMode.FIXED);//Limite eje Y
		plot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, step);//Division de escala	    
		plot.setTicksPerRangeLabel(1);
		plot.getGraphWidget().setDomainLabelOrientation(-45);
		TimeZone tz = TimeZone.getTimeZone("UTC");
	    SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");//Time Format
	    df.setTimeZone(tz);	
	    plot.setDomainValueFormat(df);
	}//Fin onCreate

	
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
			
	
}//Fin Trazado
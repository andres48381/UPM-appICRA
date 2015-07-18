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
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class Login extends Activity{
	//-----------------	
	//Activity Login
	//-----------------		

	private Button BtnLogin,BtnConectar;
	private EditText EdtTxtuser,EdtTxtpass;
	private TextView TxtVwLblMensaje;
	private ImageView ImgVwimageServer;	
	private String user,permiso;//Usuario y privilegios
	private String bit;//Bit de conexion

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.control_login);
	
		BtnLogin=(Button)findViewById(R.id.BtnLogin);
		BtnConectar=(Button)findViewById(R.id.BtnConectar);
		EdtTxtuser=(EditText)findViewById(R.id.TxtUsuario);
		EdtTxtpass=(EditText)findViewById(R.id.TxtPassword);		
		TxtVwLblMensaje=(TextView)findViewById(R.id.LblMensaje);
		ImgVwimageServer=(ImageView)findViewById(R.id.conexionServer);  
		
		//Gestion de conexion servidor
		TareaBitLife tareaBitLife = new TareaBitLife();
		tareaBitLife.execute();	
		
		//Solicitar identificacion
		BtnLogin.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {	
				
				String aux_user=EdtTxtuser.getText().toString();
				String aux_pass=EdtTxtpass.getText().toString();
				
				if(aux_user.equals("")||aux_pass.equals(""))
					TxtVwLblMensaje.setText("Rellene los campos");

				else{
					TareaLogin tareaLogin = new TareaLogin();
					tareaLogin.execute(aux_user,aux_pass);
				}
			}//Fin onClick
		});	
		
		//Establecer conexion servidor
		BtnConectar.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {	
				
				TareaBitLife tareaBtnBitLife = new TareaBitLife();
				tareaBtnBitLife.execute();	
				
			}//Fin onClick
		});				
	}//Fin onCreate
	
	protected void onStop(){
		super.onStop();
		//Clean mensaje
		TxtVwLblMensaje.setText("");				
	}//Fin onStop	
		
	protected void onRestart(){
		super.onRestart();
		//Estado de conexion
		TareaBitLife tareaBitLife = new TareaBitLife();
		tareaBitLife.execute();				
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
	    		if(bit.equals("101"))
	    			//Server RUN
	    			ImgVwimageServer.setImageResource(R.drawable.check_ok);
	    		else
	    			//Serer STOP
	    			ImgVwimageServer.setImageResource(R.drawable.check_fail);	    			    		    		
	    	}
	    	else
	    		ImgVwimageServer.setImageResource(R.drawable.check_fail);	    		
	    }//Fin OnPostExecute       	    	    
	}//Fin TareaBitLife	
	
	
	//Tarea ASINCRONA para consulta en segundo plano
	//Identificacion de usuario y contraseña
	private class TareaLogin extends AsyncTask<String,Integer,Boolean> {//Actualizar valores
		 
	    protected Boolean doInBackground(String... params) {
	    	
	    	boolean resul = true;	
	    	
			user = params[0];//Parametro pasado en la invocacion
			String _pass = params[1];			
			
	    	HttpClient httpClient = new DefaultHttpClient();//Cliente HTTP        			
			HttpGet del =new HttpGet(getString(R.string.Server_IP)+getString(R.string.Server_Port)+"/login/"+user+"/"+_pass);
			del.setHeader("content-type", "application/json");
			
			try
	        {	//Solicitud GET			
	        	HttpResponse resp = httpClient.execute(del);
	        	String respStr = EntityUtils.toString(resp.getEntity());//String respuesta
	        	
	        	JSONObject respJSON = new JSONObject(respStr);//JSON respuesta
	        	
	        	permiso=respJSON.getString("level");//Extraccion de parametros JSON    	        	
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
	    		
	    		if(permiso.equals("empty")==false){//Permiso adquirido	    			
		    		Intent i=new Intent("com.andres.usingintent.Menu");	    		
		    		i.putExtra("modo", permiso);
		    		i.putExtra("user", user);
		    		TxtVwLblMensaje.setText("Correcto");

		    		startActivityForResult(i,1);
	    		}
	    		else//Permiso no adquirido    
	    			TxtVwLblMensaje.setText("Incorrecto");	
	    		
	    	}
	    	else{//Fallo de conexion
	    		if(EdtTxtuser.getText().toString().equals("admin")){//Modo forzado
		    		Intent i=new Intent("com.andres.usingintent.Menu");	    		
		    		i.putExtra("modo", "ADMINISTRADOR");
		    		i.putExtra("user", "admin");
		    		TxtVwLblMensaje.setText("Modo forzado");

		    		startActivityForResult(i,1);    				
    			}
	    		else
	    			TxtVwLblMensaje.setText("Sin conexión");
	    	}
	    	
    		//Limpiar EditText
    		EdtTxtuser.setText("");
    		EdtTxtpass.setText("");	

	    }//Fin OnPostExecute       	    	    
	}//Fin TareaRefresh		
}//Fin Login

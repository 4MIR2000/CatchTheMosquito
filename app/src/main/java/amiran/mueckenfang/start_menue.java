package amiran.mueckenfang;

import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.http.AndroidHttpClient;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;



public class start_menue extends ActionBarActivity implements View.OnClickListener {
    Button button_start;
    private Animation animationEinblenden;
    private Animation animationWackeln;
    private Handler handler = new Handler();
    private Runnable wackelnRunnable= new WackleButton();
    private Typeface ttf;

    private RelativeLayout namenseingabe;
    private Button speichern;
    private EditText spielername;
    private static final String HIGHSCORE_SERVER_BASE_URL ="https://myserver-2000.appspot.com/hello";
    private static final String HIGHSCORESERVER_GAME_ID ="mueckenfang";
    String highscoresHtml;
    TextView highscores_listMenue;
    private Spinner schwierigkeitsgrad;
    private ArrayAdapter<String> schwierigskeitsgradAdapter;

    String highscores;
    private ListView list;
    ToplistAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_menue);

        namenseingabe = (RelativeLayout) findViewById(R.id.namenseingabe);
        speichern = (Button)findViewById(R.id.speichern);
        speichern.setOnClickListener(this);
        spielername = (EditText)findViewById(R.id.spielername);

        button_start = (Button) findViewById(R.id.start);
        button_start.setOnClickListener(this);
        spielername.setOnClickListener(this);
        ttf = Typeface.createFromAsset(getAssets(), "JandaManateeBubble.ttf");

        button_start.setTypeface(ttf);
        animationEinblenden = AnimationUtils.loadAnimation(this, R.anim.einblenden);
        animationWackeln = AnimationUtils.loadAnimation(this, R.anim.wackeln);

        ((TextView)findViewById(R.id.highscore_text)).setTypeface(ttf);
        ((TextView)findViewById(R.id.willkommen)).setTypeface(ttf);
        namenseingabe.setVisibility(View.INVISIBLE);

        highscores_listMenue = (TextView) findViewById(R.id.highscores);
        highscores_listMenue.setOnClickListener(this);

        highscores_listMenue= (TextView) findViewById(R.id.highscores);
        schwierigkeitsgrad = (Spinner) findViewById(R.id.schwierigkeitsgrad);



        schwierigskeitsgradAdapter = new ArrayAdapter<String>(this,android.R.layout.
                simple_spinner_dropdown_item,new String[]
                {"leicht","mittel","schwer"});

        schwierigkeitsgrad.setAdapter(schwierigskeitsgradAdapter);







    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.start) {
            int s = schwierigkeitsgrad.getSelectedItemPosition();
            Intent i = new Intent(this,game.class);
            i.putExtra("schwierigkeitsgrad",s);
            startActivityForResult(i,1);
        }
        if(view.getId() == R.id.spielername){
            spielername.setText("");
        }

        if(view.getId() == R.id.speichern){
            schreibeHighscoreName();
            highscoreAnzeigen();
            namenseingabe.setVisibility(View.INVISIBLE);

            Thread t = new Thread(new SendeHighscore());
            t.start();

        }

        if(view.getId() == R.id.highscores ){
            setContentView(R.layout.toplist);
            Thread t = new Thread(new HoleHighscores(100));
            t.start();
        }
    }

    private void schreibeHighscoreName() {
        String name = spielername.getText().toString().trim();
        SharedPreferences sp = getSharedPreferences("GAME",0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("SpielerName",name);
        editor.commit();
    }
    private String leseHighscoreName(){
        SharedPreferences sp = getSharedPreferences("GAME", 0);
        return sp.getString("SpielerName", "");
    }

  @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == 1){
            if(resultCode> leseHighscore()){
                schreibeHighscore(resultCode);
                namenseingabe.setVisibility(View.VISIBLE);
            }
        }
    }




    private void internethighscores(String nickname,int points, int max){
        try {
            highscores = "";
            AndroidHttpClient client = AndroidHttpClient.newInstance("Mueckenfang"); // er redet mit dem Servlet
            HttpUriRequest request;

                 request = new HttpGet(HIGHSCORE_SERVER_BASE_URL + "?game=" + HIGHSCORESERVER_GAME_ID +
                        "&name=" + URLEncoder.encode(nickname) //wandelt den namen in URL Sprache um (erzetzt z.b leerzeichen durch +)
                        + "&points=" + Integer.toString(points)
                        + "&max=" + Integer.toString(max)
                );





                                                                                //Anfrage und zuschicken des Namens und des Highscores bzw. des max(stings der Liste)
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            InputStreamReader reader = new InputStreamReader(entity.getContent(),"UTF8");
            int c = reader.read();
            while(c >0){
                highscores+= (char)c;
                c=reader.read();
            }
            highscores = highscores.replace(",",":");

            List<String> highscoreList = new ArrayList<String>();
            TextUtils.SimpleStringSplitter sss = new TextUtils.SimpleStringSplitter('\n');
            sss.setString(highscores);

            while(sss.hasNext()){
                highscoreList.add(sss.next());
            }

           highscoresHtml ="";
            for(String s :highscoreList){
                highscoresHtml += "<b>"
                        +s.replace("," , "</b> <font color='red' ")
                        + "</font></br>";
            }




            runOnUiThread(new ZeigeHighscores());

            //topliste(100) Layout Siehe toplistAdapter
            if(findViewById(R.id.list) != null){
                list = (ListView) findViewById(R.id.list);
                adapter = new ToplistAdapter(this,0,highscoreList);
                runOnUiThread(new ZeigeTopliste());
            }

            client.close();

        }catch(IOException e){
            highscores = "Fehler. Überprüfe deine Internetverbindung";
        }
    }

        private class HoleHighscores implements Runnable{
            int max;
            public HoleHighscores(int m){
                m=max;
            }

            @Override
            public void run(){
                internethighscores("",0,max);
            }
        }



       private class ZeigeHighscores implements Runnable{
           @Override
           public void run(){

               //tv.setText(highscores);
               highscores_listMenue.setText(Html.fromHtml(highscoresHtml));
           }

       }

    //HIGHSCORE WIRD GESENDET
    private class SendeHighscore implements Runnable{

        @Override
        public void run(){
            internethighscores(leseHighscoreName(), leseHighscore(),0);
        }
    }


    private void schreibeHighscore(int highscore){
        SharedPreferences sp = getSharedPreferences("Game", 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("Highscore",highscore);
        editor.commit();

    }

    private int leseHighscore() {
        SharedPreferences sp = start_menue.this.getSharedPreferences("Game",0);
        return sp.getInt("Highscore",0);

    }

    private void highscoreAnzeigen() {
        TextView highscore = (TextView) findViewById(R.id.highscore);
        highscore.setTypeface(ttf);
        if (leseHighscore() > 0) {
            highscore.setText(Integer.toString(leseHighscore())+" von "+leseHighscoreName());

        }else{
            highscore.setText("-");
        }
    }

    public class ZeigeTopliste implements Runnable{

        @Override
        public void run(){
            list.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
     public void onResume() {
         super.onResume();
        View v = findViewById(R.id.wurzel);
         v.startAnimation(animationEinblenden);
         handler.postDelayed(new WackleButton(), 1000 * 5);
         highscoreAnzeigen();
         Thread t = new Thread(new HoleHighscores(10));
         t.start(); //kehrt wieder zurück sodass der HaubtThread (UIThread weiter läuft


    }

    private class WackleButton implements Runnable {
        @Override
        public void run() {
            button_start.startAnimation(animationWackeln);
            handler.postDelayed(new WackleButton(), 1000 * 5);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        handler.removeCallbacks(new WackleButton());
    }

}


package amiran.mueckenfang;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.Random;



public class game extends ActionBarActivity implements View.OnClickListener{
    boolean spielLaeuft;
    int zeit;
    int muecken;
    int gefangeneMuecken;
    int runde;
    int punkte;
    private float masstab;
    private double wahrscheinlichkeit;
    private Random zufallsgenerator;
    ViewGroup spielbereich;
    private Handler handler = new Handler();
    private int mueckeAngezeigt;
    boolean mueckegezeichnet;
    private MediaPlayer mp;
    private Animation animationEinblenden;
    private static final long hoechstAlter = 2000;
    ImageView muecke;
    private View mueckeGedrückt;
    ImageView mueckeArray[];
    private static final int INTERVALL_MS =50;
    int frame = 0;
    private static final String HIMMELSRICHTUNGEN[][]={
            {"nw","w","sw"},
            {"n","","s"},
            {"no","o","so"}

    };
    Button playAgain;


    private Typeface ttf;
    private int schwierigkeitsgrad;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        zufallsgenerator = new Random();
        spielbereich = (ViewGroup) findViewById(R.id.spielfeld);

        mp = MediaPlayer.create(this, R.raw.summen);
        animationEinblenden = AnimationUtils.loadAnimation(this, R.anim.einblenden);
        ttf = Typeface.createFromAsset(getAssets(), "JandaManateeSolid.ttf");
        ((TextView)findViewById(R.id.punkte)).setTypeface(ttf);
        ((TextView)findViewById(R.id.runde)).setTypeface(ttf);
        ((TextView)findViewById(R.id.zeit)).setTypeface(ttf);
        ((TextView)findViewById(R.id.treffer)).setTypeface(ttf);

        ((TextView)findViewById(R.id.gameOver)).setVisibility(View.GONE);
        ((Button)findViewById(R.id.play_again)).setVisibility(View.GONE);
        schwierigkeitsgrad = getIntent().getIntExtra("schwierigkeitsgrad",0);
        spielStarten();



    }


    public void zeitherunterZaelen() {


        zeit = zeit - 1;
        float zufallszahl = zufallsgenerator.nextFloat();
        double wahrscheinlichkeit = muecken * 1.5 / 60;
        if (wahrscheinlichkeit > 1) {
            eineMueckeAnzeigen();
            if (zufallszahl < wahrscheinlichkeit - 1) {
                eineMueckeAnzeigen();
            }
        } else {
            if (zufallszahl < wahrscheinlichkeit) {
                eineMueckeAnzeigen();
            }
        }

        if(mueckegezeichnet == true) {
            mueckeVerschwinden();

        }
        update();

    }

    //neues Spiel wird gestartet
    private void spielStarten() {
        spielLaeuft = true;
        runde = 0;
        punkte = 0;
        starteRunde();
    }

    //neue Runde wird gestartet
    private void starteRunde() {
        runde++;
        gefangeneMuecken = 0;
        muecken = runde * (12+schwierigkeitsgrad*10);
        zeit = 60;
        update();
        mueckeAngezeigt=0;
        mueckeArray = new ImageView[muecken*2];
        frame=0;
        handler.postDelayed(runnable, INTERVALL_MS);

    }

    private void mueckenbewegen() {
        int nummer2 = 0;

            while (nummer2 < mueckeAngezeigt) {

                if(mueckeArray[nummer2] != null) {
                int vx = (Integer) mueckeArray[nummer2].getTag(R.id.vx);
                int vy = (Integer) mueckeArray[nummer2].getTag(R.id.vy);

                FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) mueckeArray[nummer2].getLayoutParams();
                params.leftMargin += vx * runde;
                params.topMargin += vy * runde;
                mueckeArray[nummer2].setLayoutParams(params);
                nummer2++;

            }

        }


      }



    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(mueckegezeichnet == true) {
                mueckenbewegen();
            }
           frame++;
            if(frame >= 1000/INTERVALL_MS) {
                zeitherunterZaelen();
                frame = 0;
            }
                if (!pruefeSpielende()) {
                    if (!pruefeRundenende()) {
                        handler.postDelayed(runnable, INTERVALL_MS);
                    }
                }

            }


    };




    private void update() {
        TextView tvpunkte = (TextView) findViewById(R.id.punkte);
        tvpunkte.setText(Integer.toString(punkte));

        TextView tvrunde = (TextView) findViewById(R.id.runde);
        tvrunde.setText(Integer.toString(runde));

        TextView tvtreffer = (TextView) findViewById(R.id.treffer);
        tvtreffer.setText(Integer.toString(gefangeneMuecken));

        TextView tvzeit = (TextView) findViewById(R.id.zeit);
        tvzeit.setText(Integer.toString(zeit));

        FrameLayout fltreffer = (FrameLayout) findViewById(R.id.bar_treffer);
        FrameLayout flzeit = (FrameLayout) findViewById(R.id.bar_zeit);

        masstab = getResources().getDisplayMetrics().density;
        ViewGroup.LayoutParams lptreffer = fltreffer.getLayoutParams();
        lptreffer.width = Math.round(masstab * 300 / muecken *
                Math.min(gefangeneMuecken, muecken));
        ViewGroup.LayoutParams lpzeit = flzeit.getLayoutParams();
        lpzeit.width = Math.round(masstab * (300 / 60) * zeit);


    }


    private boolean pruefeSpielende() {
        if (gefangeneMuecken < muecken && zeit <= 0) {

            gameOver();

            return true;

        }
        return false;
    }

    private boolean pruefeRundenende() {
        if (muecken <= gefangeneMuecken) {
            starteRunde();
            return true;

        }
        return false;
    }

    private void setzeBild(ImageView muecke, int vx, int vy){
        muecke.setImageResource(getResources().getIdentifier
                ("muecke_"+HIMMELSRICHTUNGEN[vx+1][vy+1],"drawable",this.getPackageName()));
    }

    private void eineMueckeAnzeigen() {
        muecke = new ImageView(this);
        mueckegezeichnet = true;


        int breite = spielbereich.getWidth();
        int hoehe = spielbereich.getHeight();
        int muecke_breite = (int) Math.round(masstab * 50);
        int muecke_hoehe = (int) Math.round(masstab * 42);
        int links = zufallsgenerator.nextInt(breite - muecke_breite);
        int oben = zufallsgenerator.nextInt(hoehe - muecke_hoehe);

        int vx;
        int vy;
        double faktor = 1;


        do{
            vx=zufallsgenerator.nextInt(3) -1;
            vy=zufallsgenerator.nextInt(3) -1;
        }while(vx == 0&& vy==0);
        if(vx!=0 && vy!=0){
            faktor = 0.70710678;
        }

        setzeBild(muecke,vx,vy);

        vx=(int)Math.round(masstab*vx*faktor);
        vy=(int)Math.round(masstab*vy*faktor);
        //muecke erzeugen

        muecke.setScaleType(ImageView.ScaleType.CENTER);

        muecke.setOnClickListener(this);
        muecke.setTag(R.id.geburtsdatum, new Date());
        //Geschwindigkeit
        muecke.setTag(R.id.vx, new Integer(vx));
        muecke.setTag(R.id.vy, new Integer(vy));

        //muecke zeichnen
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(muecke_breite, muecke_hoehe);
        params.leftMargin = links;
        params.topMargin = oben;
        params.gravity = Gravity.TOP + Gravity.LEFT;

        spielbereich.addView(muecke, params);

        try {
                if (mp.isPlaying()) {
                    mp.pause();

                }
                mp.seekTo(0);
                mp.start();

        } catch (Exception e) {
        }

       mueckenunterscheiden(muecke);
    }



    public void mueckenunterscheiden(ImageView m){
        mueckeArray[mueckeAngezeigt] = m;
        mueckeAngezeigt++;

    }







    public void mueckeVerschwinden() {

        int nummer2 = 0;
        while (nummer2 < mueckeAngezeigt) {

            Date geburtsdatum = (Date) mueckeArray[nummer2].getTag(R.id.geburtsdatum);
            long alter = (new Date()).getTime() - geburtsdatum.getTime();
            if (alter > hoechstAlter) {
                spielbereich.removeView(mueckeArray[nummer2]);

            }
            nummer2++;
            }
        }



    private void gameOver() {

        ((TextView)findViewById(R.id.gameOver)).setVisibility(View.VISIBLE);
        ((Button)findViewById(R.id.play_again)).setVisibility(View.VISIBLE);
        findViewById(R.id.play_again).setOnClickListener(this);
        ((TextView)findViewById(R.id.gameOver)).setTypeface(ttf);
        ((Button)findViewById(R.id.play_again)).setTypeface(ttf);

        spielLaeuft = false;
        mp.stop();







    }



    @Override
    public void onClick(View v) {
            if(v.getId() != R.id.play_again) {
                mueckeGedrückt = v;
                gefangeneMuecken++;
                punkte += 100+ (schwierigkeitsgrad*100);
                update();
                mp.pause();
                Animation animationTreffer = AnimationUtils.loadAnimation(this, R.anim.treffer);
                animationTreffer.setAnimationListener(new MueckeAnimationListener());
                mueckeGedrückt.startAnimation(animationTreffer);
            }else{
                if(v.getId() == R.id.play_again) {
                    //startActivity(new Intent(this,start_menue.class))
                    //Activity muss geschloßen werden(sowie back Button)
                    setResult(punkte);
                    finish();




                }
            }
        }


    private class MueckeAnimationListener implements Animation.AnimationListener {
        private View muecke;

        @Override
        public void onAnimationEnd(Animation animation) {
            handler.post(new Runnable(){
                @Override
                public void run() {
                    spielbereich.removeView(mueckeGedrückt);
                }

            });

        }
        @Override
        public void onAnimationRepeat(Animation animation){
        }
        @Override
        public void onAnimationStart(Animation animation){
        }

    }

    @Override
    public void onDestroy() {
        mp.release();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        View v = findViewById(R.id.game);
        v.startAnimation(animationEinblenden);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);

    }


}

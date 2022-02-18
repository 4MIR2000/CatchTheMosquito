/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Servlet Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloWorld
*/

package com.example.Amir.myapplication.backend;








import java.io.IOException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;


import java.util.List;

import javax.servlet.http.*;
import javax.xml.crypto.Data;


public class MyServlet extends HttpServlet {

    int points;
    int max;
    String name;
    String game;

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        game = req.getParameter("game");
        name = req.getParameter("name");


        String pointsStr = req.getParameter("points");
         points = 0;
        if(pointsStr != null) {
            points = Integer.parseInt(pointsStr);
        }

        String maxStr = req.getParameter("max");
         max = 10;
        if(maxStr != null ) {
            max = Integer.parseInt(maxStr);
        }
            if(points>0 && name != null){
               addHighscore(game,name,points);

            }

        returnHighscores(resp,game,max);
        }


    private void addHighscore(String game,String name, int points){
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key gameKey = KeyFactory.createKey("game", game);
        Entity highscore = new Entity("highscore", gameKey);
        highscore.setProperty("name", name);
        highscore.setProperty("points", points);
        datastore.put(highscore);
    }

    private void returnHighscores(HttpServletResponse resp,String game, int max) throws IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key gameKey = KeyFactory.createKey("game", game);
        Query query = new Query("highscore", gameKey);
        query.addSort("points", Query.SortDirection.DESCENDING);
        List<Entity> highscores = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(max));

        for(Entity e:highscores){
            resp.getWriter().println(e.getProperty("name")+"," +
                  e.getProperty("points"));
        }

    }




    }



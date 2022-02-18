package amiran.mueckenfang;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by Amir on 25.09.2015.
 */
public class ToplistAdapter extends ArrayAdapter<String> {
    Context context;
    List<String> toplist;
    public ToplistAdapter(Context c, int textViewResourceId, List<String> list ) {
        super(c,textViewResourceId,list);
        context = c;
        toplist = list;

    }

    @Override
    public View getView(int position,View convertView, ViewGroup parent){

        View element = convertView;

        if(element==null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            element = inflater.inflate(R.layout.toplist_element, null);
        }
        TextView platz = (TextView) element.findViewById(R.id.platz);
        TextView name = (TextView) element.findViewById(R.id.name);
        TextView punkte = (TextView) element.findViewById(R.id.punkte);

        platz.setText(Integer.toString(position + 1) + ".");




        TextUtils.SimpleStringSplitter sss = new TextUtils.SimpleStringSplitter(':');
        sss.setString(toplist.get(position));
        name.setText(sss.next());
        punkte.setText(sss.next());

        return element;



    }

    @Override
    public int getCount(){
        return toplist.size();
    }
}

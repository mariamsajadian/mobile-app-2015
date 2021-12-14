package com.sajadian.ubiquitous;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sajadian.ubiquitous.R;

public class PollutionAdapter extends ArrayAdapter<Pollution> {
    private final ArrayList<Pollution> values;
    private Context context;
    
    public PollutionAdapter(Context context, ArrayList<Pollution> values) {
        super(context, android.R.layout.simple_list_item_1, values);
        this.values = values;
        this.context=context;
    }

    @Override
    public Pollution getItem(int position) {
        return values.get(position);
    }

    @Override
    public long getItemId(int position) {
    	return values.get(position).getID();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if(v==null){
        	LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.list_item, parent,false);
		}
        
        Pollution p=values.get(position);
        TextView status = (TextView) v.findViewById(R.id.txtStatus);
        TextView hrate = (TextView) v.findViewById(R.id.txtHeartRate);
        TextView blood = (TextView) v.findViewById(R.id.txtBlood);
        TextView signs = (TextView) v.findViewById(R.id.txtSigns);
        TextView date=(TextView)v.findViewById(R.id.txtDate);
        ImageButton btn=(ImageButton)v.findViewById(R.id.btnLocation);
        
        hrate.setText(p.getRate()==0?"-":String.valueOf(p.getRate()));
        blood.setText(p.getBlood());
        signs.setText(p.getSigns());
        date.setText(formatDate(p.getDate()));
        btn.setTag(p.getLocation());
        int pv=p.getPollution();
        
        int c = Color.WHITE;
		String t = "Unknown";

		if (pv > 0 && pv <= 50) {
			c = Color.GREEN;
			t = "Good";
		} else if (pv > 50 && pv <= 100) {
			c = Color.YELLOW;
			t = "Moderate";
		} else if (pv > 100 && pv <= 150) {
			c = Color.rgb(255, 50, 10);
			t = "Unhealthy";
		} else if (pv > 150) {
			c = Color.RED;
			t = "Hazardous";
		}
		status.setTextColor(c);
		status.setText(t);
        return v;
    }
    
    private String formatDate(String date){
		 SimpleDateFormat df = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy", Locale.ENGLISH);
		 try {
			Date dt=df.parse(date);
			return DateUtils.getRelativeDateTimeString(context,
					dt.getTime(),DateUtils.SECOND_IN_MILLIS, 
					DateUtils.WEEK_IN_MILLIS, 0).toString();
			
		} catch (ParseException e) {
			Log.e("Date", "Error in date covertion!");
		}
		 return date;
    }
}
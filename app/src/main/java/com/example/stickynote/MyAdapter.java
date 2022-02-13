package com.example.stickynote;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class MyAdapter extends ArrayAdapter<Note>
{
    private int resourceId;

    public MyAdapter(Context context , int textViewResourceId , List<Note> objects)
    {
        super(context , textViewResourceId , objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position,  View convertView,  ViewGroup parent)
    {
        Note note = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
        TextView title = view.findViewById(R.id.title);
        title.setText(note.getTitle());
        return view;
    }
}

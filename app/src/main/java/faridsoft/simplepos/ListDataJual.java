package faridsoft.simplepos;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListDataJual extends BaseAdapter {
    // Declare Variables
    DataHelper dbHelper;
    private int count;
    private int stepNumber;
    private int startCount;
    private int layoutResourceId;
    Context mContext;
    LayoutInflater inflater;
    private List<itemsesuai> datatemenlist = null;
    private ArrayList<itemsesuai> arraylist;
    private int posisi;
    public ListDataJual(Context context, int layoutResourceId, List<itemsesuai> itemsesuai, int startCount, int stepNumber) {
        this.mContext = context;
        this.startCount = Math.min(startCount, itemsesuai.size()); //don't try to show more views than we have
        this.count = this.startCount;
        this.stepNumber = stepNumber;
        this.layoutResourceId = layoutResourceId;
        this.datatemenlist = itemsesuai;
        inflater = LayoutInflater.from(mContext);
        this.arraylist = new ArrayList<itemsesuai>();
        this.arraylist.addAll(datatemenlist);
    }
    public List<itemsesuai> getdaftar() {
        return this.datatemenlist;
    }
    public class ViewHolder {
        TextView kodebrg;ImageView imageView;
        TextView namabrg,jum,tgl,ids;

    }

    @Override
    public int getCount() {
        return datatemenlist.size();
    }

    @Override
    public itemsesuai getItem(int position) {
        return datatemenlist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        final ListView lis;
        this.posisi=position;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(layoutResourceId, null);
            // Locate the TextViews in listview_item.xml
            holder.ids = (TextView) view.findViewById(R.id.txtid);
            holder.kodebrg = (TextView) view.findViewById(R.id.txtkodebrg);
            holder.namabrg = (TextView) view.findViewById(R.id.txtnamabrg);
            holder.tgl = (TextView) view.findViewById(R.id.txttgl);
            holder.jum = (TextView) view.findViewById(R.id.txtjum);
            holder.imageView=(ImageView) view.findViewById(R.id.image_view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        // Set the results into TextViews
        holder.kodebrg.setText(datatemenlist.get(position).getKodebrg());
        holder.namabrg.setText(datatemenlist.get(position).getNamabrg());
        holder.jum.setText(datatemenlist.get(position).getJum());
        holder.tgl.setText(datatemenlist.get(position).getTgl());
        holder.ids.setText(String.valueOf(datatemenlist.get(position).getid()));

        String firstLetter = String.valueOf(position+1);
        ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
        // generate random color
        int color = generator.getColor(datatemenlist.get(position));
        //int color = generator.getRandomColor();

        TextDrawable drawable = TextDrawable.builder()
                .buildRound(firstLetter, color); // radius in px
        holder.imageView.setImageDrawable(drawable);
        ImageView imageClick = (ImageView) view
                .findViewById(R.id.image_view2);

        try {


            imageClick.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {



                    switch (v.getId()) {
                        case R.id.image_view2:

                            PopupMenu popup = new PopupMenu(mContext, v);
                            popup.getMenuInflater().inflate(R.menu.menu_datajual,
                                    popup.getMenu());
                            popup.show();
                            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    final String kodekat = datatemenlist.get(position).getKodebrg();
                                    switch (item.getItemId()) {
                                        case R.id.detil:

                                            ((DataPenjualan)mContext).detilbarang(kodekat);
                                            break;
                                        case R.id.hapus:
                                            new AlertDialog.Builder(mContext)
                                                    .setTitle(R.string.hapus)
                                                    .setMessage(R.string.yakin)
                                                    .setNegativeButton(android.R.string.no, null)
                                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                                        public void onClick(DialogInterface arg0, int arg1) {
                                                            dbHelper = new DataHelper(mContext);
                                                            SQLiteDatabase db = dbHelper.getWritableDatabase();

                                                            db.execSQL("delete from t_penjualan where c_idpenjualan = '"+kodekat+"'");
                                                            Toast.makeText(mContext, R.string.sukses, Toast.LENGTH_LONG).show();
                                                            if(mContext instanceof DataPenjualan){
                                                                ((DataPenjualan)mContext).datasupplier("");
                                                            }
                                                            EditText txtcari = (EditText) ((DataPenjualan)mContext).findViewById(R.id.search);
                                                            txtcari.setText("");

                                                        }
                                                    }).create().show();


                                            break;

                                        default:
                                            break;
                                    }

                                    return true;
                                }
                            });

                            break;

                        default:
                            break;
                    }



                }
            });

        } catch (Exception e) {

            e.printStackTrace();
        }






        return view;
    }

    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        datatemenlist.clear();
        if (charText.length() == 0) {
            datatemenlist.addAll(arraylist);
        }
        else
        {
            for (itemsesuai wp : arraylist)
            {
                if (wp.getNamabrg().toLowerCase(Locale.getDefault()).contains(charText))
                {
                    datatemenlist.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }

    public boolean showMore(){
        if(count == datatemenlist.size()) {
            return true;
        }else{
            count = Math.min(count + stepNumber, datatemenlist.size()); //don't go past the end
            notifyDataSetChanged(); //the count size has changed, so notify the super of the change
            return endReached();
        }
    }

    /**
     * @return true if then entire data set is being displayed, false otherwise
     */
    public boolean endReached(){
        return count == datatemenlist.size();
    }

    /**
     * Sets the ListView back to its initial count number
     */
    public void reset(){
        count = startCount;
        notifyDataSetChanged();
    }

}
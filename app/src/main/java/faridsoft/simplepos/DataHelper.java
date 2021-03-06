package faridsoft.simplepos;

/**
 * Created by faridya on 9/27/2017.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DataHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "simpelpos.db";
    private static final int DATABASE_VERSION = 2;
    public DataHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS t_pengguna");

        String sql = "create table t_pengguna(c_user varchar primary key, nama varchar not null, c_password varchar not null);";
        db.execSQL(sql);
        sql = "INSERT INTO t_pengguna  VALUES ('admin', 'admin','"+ md5("admin") +"');";
        db.execSQL(sql);

        db.execSQL("DROP TABLE IF EXISTS t_kategori");
        db.execSQL("create table t_kategori(c_kode varchar(50) primary key, c_namakategori varchar(100) not null);");

        db.execSQL("DROP TABLE IF EXISTS t_satuan");
        db.execSQL("create table t_satuan(c_kodesatuan varchar(50) primary key, c_satuan varchar(100) not null);");
        sql = "INSERT INTO t_satuan  VALUES ('s1', 'pcs');";
        db.execSQL(sql);

        db.execSQL("DROP TABLE IF EXISTS t_supplier");
        db.execSQL("create table t_supplier(c_idsupplier varchar(50) primary key, c_supplier varchar(100) not null,c_alamat varchar not null,c_telp varchar(50) not null,c_utang double(10,2) NOT NULL DEFAULT '0.00');");

        db.execSQL("DROP TABLE IF EXISTS t_pelanggan");
        db.execSQL("create table t_pelanggan(c_idpelanggan varchar(50) primary key, c_pelanggan varchar(100) not null,c_alamat varchar not null,c_telp varchar(50) not null,c_piutang double(10,2) NOT NULL DEFAULT '0.00');");

        db.execSQL("DROP TABLE IF EXISTS t_barang");
        db.execSQL("create table t_barang(c_kodebrg varchar(50) primary key, c_deskripsi varchar(150) not null,c_kodekategori varchar(50),c_kodesatuan varchar(50),c_hargabeli double(10,2) DEFAULT '0.00'," +
                "c_hargajual1 double(10,2) DEFAULT '0.00',c_hargajual2 double(10,2) DEFAULT '0.00',c_stok double(10,2) NOT NULL DEFAULT '0.00',`c_stokmin` double(10,2) NOT NULL DEFAULT '0.00',`c_idsupplier` varchar(50) DEFAULT NULL,`c_gambar` text);");

        db.execSQL("DROP TABLE IF EXISTS t_sesuai");
        db.execSQL("create table t_sesuai(`id` INTEGER PRIMARY KEY  AUTOINCREMENT, c_tanggal date not null,c_kodebrg varchar(50) not null,`c_jumlah` double(10,2) NOT NULL,c_alasan);");

        db.execSQL("DROP TABLE IF EXISTS t_penjualan");
        db.execSQL("create table t_penjualan(c_idpenjualan varchar(50) primary key, c_tanggal date not null,c_idpelanggan varchar(50) ,`c_jumlah` double(10,2) NOT NULL DEFAULT '0.00',`c_ppn` double(10,2) NOT NULL DEFAULT '0.00',`c_diskon` double(10,2) NOT NULL DEFAULT '0.00',`c_total` double(10,2) NOT NULL DEFAULT '0.00',`c_cash` double(10,2) NOT NULL DEFAULT '0.00',c_hari smallint,c_user varchar(50))");

        db.execSQL("DROP TABLE IF EXISTS t_penjualandetil");
        db.execSQL("create table t_penjualandetil(c_idpenjualan varchar(50), c_kodebrg varchar(50),`c_jumlahbrg` double(6,2) NOT NULL DEFAULT '0.00',`c_hargabeli` double(10,2) NOT NULL DEFAULT '0.00',`c_hargajual` double(10,2) NOT NULL DEFAULT '0.00',primary key(c_idpenjualan,c_kodebrg))");


        db.execSQL("DROP TABLE IF EXISTS t_piutang");
        db.execSQL("create table t_piutang(c_idpenjualan varchar(50), c_idpelanggan varchar(50),c_tanggal date not null,`c_jumlahpiutang` double(10,2) NOT NULL DEFAULT '0.00',`c_jumlahbayar` double(10,2) NOT NULL DEFAULT '0.00',c_jatuhtempo date not null,primary key(c_idpenjualan,c_idpelanggan))");


        db.execSQL("CREATE TRIGGER if not exists tsesuai_ai   \n" +
                "   AFTER INSERT  \n" +
                " ON[t_sesuai]  \n" +
                "   for each row  \n" +
                "     BEGIN  \n" +
                "        update t_barang set c_stok=c_stok+new.c_jumlah where c_kodebrg=new.c_kodebrg;  \n" +
                "     END;  ;");
        db.execSQL("CREATE TRIGGER if not exists tsesuai_ad   \n" +
                "   AFTER DELETE  \n" +
                " ON[t_sesuai]  \n" +
                "   for each row  \n" +
                "     BEGIN  \n" +
                "        update t_barang set c_stok=c_stok-(old.c_jumlah) where c_kodebrg=old.c_kodebrg;  \n" +
                "     END;  ;");
        db.execSQL("CREATE TRIGGER if not exists tsesuai_au   \n" +
                "   AFTER Update  \n" +
                " ON[t_sesuai]  \n" +
                "   for each row  \n" +
                "     BEGIN  \n" +
                "        update t_barang set c_stok=c_stok-(old.c_jumlah)+new.c_jumlah where c_kodebrg=old.c_kodebrg;  \n" +
                "     END;  ;");

        db.execSQL("CREATE TRIGGER if not exists tpenjualan_ai   \n" +
                "   AFTER INSERT  \n" +
                " ON[t_penjualan]  \n" +
                "   for each row  \n" +
                "        WHEN new.c_cash<new.c_total " +
                "     BEGIN  \n" +
                "        insert into t_piutang(c_idpenjualan,c_idpelanggan,c_tanggal,c_jumlahpiutang,c_jatuhtempo) values(new.c_idpenjualan,new.c_idpelanggan,new.c_tanggal,(new.c_total-new.c_cash),date(new.c_tanggal,'+' || new.c_hari || ' day'));  \n" +
                "     END;  ;");

        db.execSQL("CREATE TRIGGER if not exists tpenjualan_ad   \n" +
                "   AFTER DELETE  \n" +
                " ON[t_penjualan]  \n" +
                "   for each row  \n" +
                "     BEGIN  \n" +
                "        delete from t_piutang where c_idpenjualan=old.c_idpenjualan;  \n" +
                "        delete from t_penjualandetil where c_idpenjualan=old.c_idpenjualan;  \n" +
                "     END;  ;");

        db.execSQL("CREATE TRIGGER if not exists tpenjualandetil_ai   \n" +
                "   AFTER INSERT  \n" +
                " ON[t_penjualandetil]  \n" +
                "   for each row  \n" +
                "     BEGIN  \n" +
                "        update t_barang set c_stok=c_stok-new.c_jumlahbrg where c_kodebrg=new.c_kodebrg;  \n" +
                "     END;  ;");

        db.execSQL("CREATE TRIGGER if not exists tpenjualandetil_ad   \n" +
                "   AFTER Delete  \n" +
                " ON[t_penjualandetil]  \n" +
                "   for each row  \n" +
                "     BEGIN  \n" +
                "        update t_barang set c_stok=c_stok+old.c_jumlahbrg where c_kodebrg=old.c_kodebrg;  \n" +
                "     END;  ;");

    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub

    }

    public void buatabel(){

    }

    public static final String md5(final String password) {
        try {

            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(password.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

}
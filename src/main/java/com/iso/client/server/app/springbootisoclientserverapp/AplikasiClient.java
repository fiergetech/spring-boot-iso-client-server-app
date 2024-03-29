package com.iso.client.server.app.springbootisoclientserverapp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;

import com.iso.Iso8583Helper;


public class AplikasiClient {
    public static void main(String[] args) {
        //date format tergantung spek, jika 24h format maka H kapital
        //request
        DateFormat formatterBit7 = new SimpleDateFormat("MMddHHmmss");

        Map<Integer, String> logonRequest = new LinkedHashMap<Integer, String>();
        logonRequest.put(7, formatterBit7.format(new Date()));
        logonRequest.put(11, "834624");
        logonRequest.put(70, "001");

        AplikasiClient client = new AplikasiClient();
        BigInteger bitmapRequest = Iso8583Helper.hitungBitmap(logonRequest);

        String strBitmap = bitmapRequest.toString(2);
        System.out.println("Bitmap Binary : ["+strBitmap+"]");

        String strBitmapHex = bitmapRequest.toString(16);
        System.out.println("Bitmap Hexa : ["+strBitmapHex+"]");

        String strLogonRequest = Iso8583Helper.messageString("0800", logonRequest);
        System.out.println("Logon Request : ["+strLogonRequest+"]");

        short messageLength = (short) (strLogonRequest.length() + 2);
        System.out.println("Message Length : "+ messageLength);

        byte[] baLength = new byte[2];
        baLength[0] = (byte) ((messageLength >> 8) & 0xff);
        baLength[1] = (byte) (messageLength  & 0xff);
        System.out.println("Message Length Byte Order : "+new String(baLength));



        Map<Integer, String> logonResponse = new LinkedHashMap<Integer, String>();
        logonResponse.put(7, formatterBit7.format(new Date()));
        logonResponse.put(11, "834624");
        logonResponse.put(39, "00");
        logonResponse.put(70, "001");

        BigInteger bitmapResponse = Iso8583Helper.hitungBitmap(logonResponse);
        System.out.println("Bitmap Binary Logon Response ["+bitmapResponse.toString(2));
        System.out.println("Bitmap Hex Logon Response ["+bitmapResponse.toString(16));

        client.kirim(strLogonRequest);
    }

    public BigInteger hitungBitmap(Map<Integer, String> message){
        //untuk bitmap 128
        BigInteger bitmap = BigInteger.ZERO.setBit(128 - 1);

        for(Integer de : message.keySet()){
            if (de > 64){
                bitmap = bitmap.setBit(128 - 1);
            }
            bitmap = bitmap.setBit(128 - de);
        }
        return bitmap;
    }

    public String messageString(String mti, Map<Integer, String> message){
        StringBuilder hasil = new StringBuilder();
        hasil.append(mti);
        hasil.append(hitungBitmap(message).toString(16));
        for (Integer de : message.keySet()){
            hasil.append(message.get(de));
        }
        return hasil.toString();
    }

    public void kirim(String message){
        short messageLength = (short) (message.length() + 2);
        System.out.println("Panjang message : "+ messageLength);

        try {
            // mengirim data
            Socket koneksi = new Socket("localhost", 12345);
            DataOutputStream out = new DataOutputStream(koneksi.getOutputStream());
            out.writeShort(messageLength);
            out.writeBytes(message);
            out.flush();
            System.out.println("Data terkirim");

            // menerima response
            DataInputStream in = new DataInputStream(koneksi.getInputStream());
            short respLength = in.readShort();
            System.out.println("Panjang response = "+respLength);
            byte[] responseData = new byte[respLength - 2];
            in.readFully(responseData);
            System.out.println("Response = ["+new String(responseData)+"]");

            koneksi.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


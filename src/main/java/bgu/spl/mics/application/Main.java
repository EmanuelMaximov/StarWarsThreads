package bgu.spl.mics.application;

import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.BombDestroyerEvent;
import bgu.spl.mics.application.messages.DeactivationEvent;
import bgu.spl.mics.application.passiveObjects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.*;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

/**
 * This is the Main class of the application. You should parse the input file,
 * create the different components of the application, and run the system.
 * In the end, you should output a JSON.
 */
public class Main {
    public static void main(String[] args) {
        Ewoks ewoks = Ewoks.getInstance();
        Diary d = Diary.getInstance();
        MessageBusImpl ms = MessageBusImpl.getInstance();
        CountDownLatch countDownLatch = new CountDownLatch(4);//starting Leia after all MS have started

        JsonParser jsonParser = new JsonParser();  //will contain the JsonFile
        JsonObject jsonObject;

        try {
            jsonObject = (JsonObject) jsonParser.parse(new FileReader(args[0])); //Take the JsonFile from args[0]
            JsonArray array = jsonObject.get("attacks").getAsJsonArray();
            int R2D2Duration = jsonObject.get("R2D2").getAsInt();
            int LandoDuration = jsonObject.get("Lando").getAsInt();
            int numOfEwoks = jsonObject.get("Ewoks").getAsInt();
            Attack[] attacks = new Attack[array.size()];

            for (int i = 0; i < array.size(); i++) { //create a new object from type - Attacks
                int Attackduration = (array.get(i)).getAsJsonObject().get("duration").getAsInt();
                JsonArray serials = array.get(i).getAsJsonObject().get("serials").getAsJsonArray();
                List<Integer> serialNumbers = new ArrayList<Integer>();
                for (int j = 0; j < serials.size(); j++) {
                    serialNumbers.add((serials.get(j).getAsInt()) - 1);
                }
                attacks[i] = new Attack(serialNumbers, Attackduration); //insert the new attack
            }

            ewoks.init(numOfEwoks);
            LeiaMicroservice Leia = new LeiaMicroservice(attacks);
            HanSoloMicroservice HanSolo = new HanSoloMicroservice(countDownLatch);
            C3POMicroservice C3PO = new C3POMicroservice(countDownLatch);
            LandoMicroservice Lando = new LandoMicroservice(countDownLatch, LandoDuration);
            R2D2Microservice R2D2 = new R2D2Microservice(countDownLatch, R2D2Duration);
            ms.subscribeEvent(AttackEvent.class, C3PO);
            ms.subscribeEvent(AttackEvent.class, HanSolo);
            ms.subscribeEvent(DeactivationEvent.class, R2D2);
            ms.subscribeEvent(BombDestroyerEvent.class, Lando);
            d.setExpectedAttacks(array.size());
            Thread d1 = new Thread(Leia);
            Thread d2 = new Thread(C3PO);
            Thread d3 = new Thread(HanSolo);
            Thread d4 = new Thread(R2D2);
            Thread d5 = new Thread(Lando);
            d2.start();
            d3.start();
            d4.start();
            d5.start();
            countDownLatch.await();
            d1.start();
            d1.join();
            d2.join();
            d3.join();
            d4.join();
            d5.join();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(args[1]);
            JsonObject newDiary = gson.toJsonTree(d).getAsJsonObject();
            newDiary.remove("ExpectedAttacks");
            gson.toJson(newDiary, writer);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}

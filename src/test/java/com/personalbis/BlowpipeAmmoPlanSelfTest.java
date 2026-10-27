package com.personalbis;

import java.util.*;
import com.personalbis.BlowpipeAmmoPlan.Pipe;
import com.personalbis.BlowpipeAmmoPlan.Dart;

public final class BlowpipeAmmoPlanSelfTest
{
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    public static void main(String[] args)
    {
        check(BlowpipeAmmoPlan.scannedPipes(Collections.emptyList()).isEmpty(),"hidden before bank scan");
        List<BankItem> bank=Arrays.asList(new BankItem(12926,1,"Toxic blowpipe"),new BankItem(12924,1,"Toxic blowpipe (empty)"),
            new BankItem(31583,1,"Rosewood blowpipe"),new BankItem(31579,0,"Ironwood blowpipe"),new BankItem(11230,99,"Dragon dart"));
        check(BlowpipeAmmoPlan.scannedPipes(bank).equals(EnumSet.of(Pipe.TOXIC,Pipe.ROSEWOOD)),"positive bank ownership, grouped variants");
        check(BlowpipeAmmoPlan.scannedPipes(Collections.singletonList(new BankItem(11230,99,"Dragon dart"))).isEmpty(),"loose darts do not reveal controls");
        check(Pipe.CAMPHOR.accepts(Dart.MITHRIL)&&!Pipe.CAMPHOR.accepts(Dart.ADAMANT),"camphor cap");
        check(Pipe.IRONWOOD.accepts(Dart.ADAMANT)&&!Pipe.IRONWOOD.accepts(Dart.RUNE),"ironwood cap");
        check(Pipe.ROSEWOOD.accepts(Dart.RUNE)&&!Pipe.ROSEWOOD.accepts(Dart.AMETHYST)&&!Pipe.ROSEWOOD.accepts(Dart.DRAGON),"rosewood cap");
        check(Pipe.TOXIC.accepts(Dart.DRAGON)&&Pipe.BLAZING.accepts(Dart.AMETHYST),"toxic and cosmetic variant");
        for(Pipe pipe:Pipe.values())check(BlowpipeAmmoPlan.options(pipe).get(0)==Dart.UNKNOWN,"unknown default");
        Map<Pipe,Dart> saved=BlowpipeAmmoPlan.decode("TOXIC:DRAGON,ROSEWOOD:RUNE");
        check(BlowpipeAmmoPlan.decode(BlowpipeAmmoPlan.encode(saved)).equals(saved),"saved round trip");
        check(BlowpipeAmmoPlan.decode("ROSEWOOD:DRAGON,BOGUS:RUNE,TOXIC:BAD").isEmpty(),"invalid/incompatible values discarded");
        check(BlowpipeAmmoPlan.decode(null).isEmpty(),"new profile defaults unknown");
        System.out.println("PASS blowpipe bank visibility, variants, compatible choices, unknown and saved settings");
    }
}
